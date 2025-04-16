import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import static java.lang.Thread.sleep;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class CallCenter {
    private static final int CUSTOMERS_PER_AGENT = 5;
    private static final int NUMBER_OF_AGENTS = 3;
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;
    private static final int NUMBER_OF_THREADS = 10;

    private static final Queue<Customer> waitQueue = new LinkedList<>();
    private static final Queue<Customer> serveQueue = new LinkedList<>();
    //2 queues, one lock snd signal for each
    private final static ReentrantLock waitQueueLock = new ReentrantLock();
    private final static ReentrantLock serveQueueLock = new ReentrantLock();

    private final static Condition waitQueueNotEmpty = waitQueueLock.newCondition();
    private final static Condition serveQueueNotEmpty = serveQueueLock.newCondition();

    public static class Agent implements Runnable {
        private final int ID;

        public Agent(int i) {
            ID = i;
        }

        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        public void run() {
            for(int i = 0; i < CUSTOMERS_PER_AGENT; i++){
                serveQueueLock.lock();
                try {
                    while (serveQueue.isEmpty()) {
                        serveQueueNotEmpty.await();
                    }
                    Customer customer = serveQueue.remove();
                    serve(customer.getId());
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                } finally {
                    serveQueueLock.unlock();
                }
            }
        }
    }

    public static class Greeter implements Runnable{

        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
                try {
                    sleep(ThreadLocalRandom.current().nextInt(10, 1000));
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
            }
        }
        public void run() {
            for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++){
                waitQueueLock.lock();
                try {
                    while (waitQueue.isEmpty()) {
                        waitQueueNotEmpty.await();
                    }
                    Customer customer = waitQueue.remove();
                    greet(customer.getId());

                    serveQueueLock.lock();
                    try {
                        serveQueue.add(customer);
                        serveQueueNotEmpty.signal();
                        System.out.println("Customer " + customer.getId() + " is number " + serveQueue.size() + " in the serve queue");
                    } finally {
                        serveQueueLock.unlock();
                    }
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                } finally {
                    waitQueueLock.unlock();
                }
            }
        }
    }


    public static class Customer implements Runnable {
        private final int ID;
        private static int counter = 1;

        public Customer (){
            this.ID = counter++;
        }

        public int getId() {
            return this.ID;
        }

        public void run() {
            waitQueueLock.lock();
            try {
                waitQueue.add(this);
                waitQueueNotEmpty.signal();
            } finally {
                waitQueueLock.unlock();
            }
        }
    }

    public static void main(String[] args){
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        es.submit(new Greeter());

        for (int i = 1; i <= NUMBER_OF_AGENTS; i++) {
            es.submit(new Agent(i));
        }

        for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++) {
            es.submit(new Customer());
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        es.shutdown();
    }

}
