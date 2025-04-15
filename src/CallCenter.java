import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import static java.lang.Thread.sleep;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;


public class CallCenter {
    private static final int CUSTOMERS_PER_AGENT = 5;
    private static final int NUMBER_OF_AGENTS = 3;
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;
    private static final int NUMBER_OF_THREADS = 10;

    private static Queue<Customer> waitQueue = new LinkedList<>();
    private static Queue<Customer> serveQueue = new LinkedList<>();

    public static class Agent implements Runnable {
    //TODO: complete the agent class
        private final int ID;

        public Agent(int i) {
            ID = i;
        }
        /*
        Your implementation must call the method below to serve each customer.
        Do not modify this method.
         */
        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            for(int i = 0; i < CUSTOMERS_PER_AGENT; i++){
                Customer customer = serveQueue.poll();
                serve(customer.getId());
            }
        }
    }

    public static class Greeter implements Runnable{
    //TODO: complete the Greeter class

        /*
        Your implementation must call the method below to serve each customer.
        Do not modify this method.
         */
        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
                try {
                    sleep(ThreadLocalRandom.current().nextInt(10, 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
        public void run() {
            for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++){
                Customer customer = waitQueue.poll();
                greet(customer.getId());
                serveQueue.add(customer);
                System.out.println("Customer " + customer.getId() + " is number " + serveQueue.size() + " in the serve queue");
            }
        }
    }

    /*
        The customer class.
     */
    public static class Customer implements Runnable {
    //TODO: complete the Customer class
        private int ID;
        private static int counter = 0;

        public Customer (){
            this.ID = counter++;
        }

        public int getId() {
            return this.ID;
        }

        public void run() {
            waitQueue.add(this);
        }
    }

    /*
        Create the greeter and agents tasks first, and then create the customer tasks.
        to simulate a random interval between customer calls, sleep for a random period after creating each customer task.
     */
    public static void main(String[] args){
    //TODO: complete the main method
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        es.submit(new Greeter());
        for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
            es.submit(new Agent(i));
        }
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            es.submit(new Customer());
            try {
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
