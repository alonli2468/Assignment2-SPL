package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);
    
    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        for (int i=0; i<numThreads; i++){
            workers[i] = new TiredThread(i, 0.5 + Math.random());
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }
    }

    public void submit(Runnable task) {
        // TODO
        TiredThread worker;
        Runnable new_task;
        try {
            synchronized(idleMinHeap){
                do{
                    while (idleMinHeap.isEmpty()) {
                        idleMinHeap.wait();
                    }
                    worker = idleMinHeap.poll();
                } while (worker.isBusy());
                final TiredThread finalWorker = worker; 

                new_task = () -> {
                    try {
                        task.run();
                    } finally {
                        synchronized(idleMinHeap){
                            idleMinHeap.add(finalWorker);
                            inFlight.decrementAndGet();
                            idleMinHeap.notifyAll();
                        }
                    }
                };

                inFlight.incrementAndGet();
            }
            
            worker.newTask(new_task);
        } catch(InterruptedException e){
            TiredThread.currentThread().interrupt();
        }
    }
        
    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        Iterator<Runnable> itr = tasks.iterator();
        while (itr.hasNext()) {
            submit(itr.next());
        }
        synchronized (idleMinHeap){
            try {
                while (inFlight.get() > 0) {
                    idleMinHeap.wait();
                }
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        
    }

    public void shutdown() throws InterruptedException {
        // TODO
        for (int i=0; i<workers.length; i++){
            workers[i].shutdown();
        }
        for (int i=0; i<workers.length; i++){
            workers[i].join();
        }
    }
    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        String status = "Workers Report:\n";
        for (int i = 0; i < workers.length; i++) {
            status = status + "Worker #" + workers[i].getWorkerId() + 
                 " [Fatigue: " + workers[i].getFatigue() + 
                 ", Time Used: " + workers[i].getTimeUsed() + 
                 ", Time Idle: " + workers[i].getTimeIdle() + "]\n";
        }
        return status;
    }
}
