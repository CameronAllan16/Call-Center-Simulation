/*
    You can import any additional package here.
 */
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class CallCenter {

    /*
       N is the total number of customers that each agent will serve in
       this simulation.
       (Note that an agent can only serve one customer at a time.)
     */
    public static final Queue<Integer> waitingQueue = new LinkedList<>();

    public static final Queue<Integer> dispatchQueue = new LinkedList<>();

    public static int admittedNewCustomer = -1;

    private static final ReentrantLock newCustomerLock = new ReentrantLock();

    private static final Condition free = newCustomerLock.newCondition();

    private static final Condition busy = newCustomerLock.newCondition();

    private static final int CUSTOMERS_PER_AGENT = 5;

    /*
       NUMBER_OF_AGENTS specifies the total number of agents.
     */
    private static final int NUMBER_OF_AGENTS = 3;

    /*
       NUMBER_OF_CUSTOMERS specifies the total number of customers to create
       for this simulation.
     */
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;

    /*
      NUMBER_OF_THREADS specifies the number of threads to use for this simulation.
      (The number of threads should be greater than the number of agents and greeter combined
      to allow simulating multiple concurrent customers.)
     */
    private static final int NUMBER_OF_THREADS = 10;


    /*
       The Agent class.
     */
    public static class Agent implements Runnable {

        //The ID of the agent.
        private final int ID;

        //Feel free to modify the constructor
        public Agent(int i) {
            ID = i;
        }
        /*
        Your Agent implementation must call the method below
        to serve each customer.
        Do not modify this method.
         */
        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                /*
                   Simulate busy serving a customer by sleeping for a random amount of time.
                */
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            int customersServed = 0;
            while (!(customersServed >= CUSTOMERS_PER_AGENT)){
                try {
                    sleep(0);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!(dispatchQueue.isEmpty())){
                    serve(dispatchQueue.remove() + 1);
                    customersServed++;
                }

            }
        }
    }


    /*
        The greeter class.
     */
    public static class Greeter implements Runnable {

        public void greet(int i, int t) {
            System.out.println("Greeting customer " + i + ": your place in queue is " + t);
        }

        public void run() {
            int customerServed = 0;
            while (!(customerServed >= NUMBER_OF_CUSTOMERS)) {
                try {
                    sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!(admittedNewCustomer == -1)) {
                    int id = admittedNewCustomer;
                    admittedNewCustomer = -1;
                    waitingQueue.remove(id);
                    greet(id, dispatchQueue.size() + 1);
                    dispatchQueue.add(customerServed);
                    customerServed++;
                }
            }
        }
    }


    /*
        The customer class.
     */
    public static class Customer implements Runnable {

        //The ID of the customer.
        private final int ID;


        //Feel free to modify the constructor
        public Customer (int i){
            ID = i;
        }

        public void run() {
            newCustomerLock.lock();

            try{
                while (admittedNewCustomer != -1){
                    free.await();
                }
                admittedNewCustomer = ID;
                waitingQueue.add(admittedNewCustomer);
                busy.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                newCustomerLock.unlock();
            }
        }
    }

    /*
        Create the greeter and agents threads first, and then create the customer threads.
     */
    public static void main(String[] args){

	//Insert a random sleep between 0 and 150 miliseconds after submitting every customer task,
     // to simulate a random interval between customer arrivals.
        ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        es.submit(new Greeter());

        for (int i = 1; i <= NUMBER_OF_AGENTS; i++){
            es.submit(new Agent(i));
        }

        try{
            sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++){
            es.submit(new Customer(i));

            try{
                sleep(ThreadLocalRandom.current().nextInt(0, 150));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
    }

}
