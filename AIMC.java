import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;

/** 
*  CSCI 6341/ Final Project: Automatic Crossroads Control and Management
*  Author: Ismayil Hasanov, Yubo Tsai, Han Wang
*  Date : Apr 25 2017
*  Description: Queue controller and algorithms for AIMC
*/

@SuppressWarnings("unchecked")
public class AIMC {
	// Animation and drawing
    Thread currentThread;  
    boolean isPaused = false;
    int sleepTime = 50;
    
    LinkedList<Car>[] queues;

    // Number of lanes
    int k = 4;

    // Avg time between arrivals = 1.0 (lambda). Can be changed via setter
    double arrivalRate = 1.0;

    // Assignment 4: Assume the service time is the same at all the servers,
    // and that each service time is exponentially distributed with mean 1.0
    private double serviceRate = 1.0;

    private PriorityQueue<Event> eventList;
    private PriorityQueue<Car>[] carList = new PriorityQueue[k];
    private double clock;


    // Statistics.
    int numArrivals;                    // How many arrived?
    int numDepartures;                      // How many left?
    double totalWaitTime, avgWaitTime;      // For time spent in queue
    double totalSystemTime, avgSystemTime;  // For time spent in system


    //Event direction
    double left = Event.LEFT;
    double right = Event.RIGHT;
    double straight = Event.STRAIGHT;

    int carID;

    public AIMC() {
        reset();
    }

    void reset() {
    	eventList = new PriorityQueue<Event>();
        //carList = new PriorityQueue<Car>();
        for (int i = 0; i < k; i++) {
            carList[i] = new PriorityQueue<Car>();
        }

	   // Initialize stats variables.
        carID = 0;
        numArrivals = 0;
    	numDepartures = 0;
    	avgWaitTime = totalWaitTime = 0;
    	avgSystemTime = totalSystemTime = 0;

    	// Need to have at least one event in event list.
        clock = 0;
    }

    void nextStep() {

    	// Event list empty?
    	if (eventList.isEmpty()) {
    	    System.out.println ("ERROR: nextStep(): EventList empty");
    	    return;
    	}

    	// Extract the next event and set the time to that event.
    	Event e = eventList.poll();
    	clock = e.eventTime;

    	// Handle each type separately.
    	if (e.type == Event.ARRIVAL) {
    	    handleArrival(e);
    	}
    	else if (e.type == Event.DEPARTURE) {
    	    handleDeparture(e);
    	}

    	// Do stats after event is processed.
    	stats();

    	// if (numDepartures % 1000 == 0) {
    	//     System.out.println ("After " + numDepartures + " departures: avgWait=" + avgWaitTime
        //            + "  avgSystemTime=" + avgSystemTime);
    	// }
    }

    // STILL IN PROGRESS
    void handleArrival(Event e) {
    	// For an arrival, we need to put the car in a queue, and
    	// schedule a departure for that queue if there isn't one scheduled.
    	// Lastly, we need to schedule the next arrival.
        //Debugging
        System.out.println("**********************************************************");
        System.out.println("This is Car" + e.carID + " which is arriving from Lane"+e.fromLane + " and turn to " +e.direction);
        System.out.println("The size of this lane: " + carList[e.fromLane].size());


    	numArrivals++;
      
        scheduleDeparture(e);  


        // TO check if any car can be allowed simultaneous
        //allowTraffic(e);

        // Choosing queue policy with the passed boolean variable:
        // true=random, false=shortest
        
    	//queues[e.direction].add(new Car (clock));

    	// if (queues[].size() == 1) {
    	//     // This is the only customer => schedule a departure.
    	//     scheduleDeparture(k);
    	// }

    	scheduleArrival();
    }

    void handleDeparture(Event e) {
    	// For a departure from a queue, remove the customer from 
    	// that particular queue, then schedule the next departure
    	// if that queue has waiting customers.
    	numDepartures++;

        totalSystemTime += clock - e.eventTime;
    	
        //remove it from carlist
        Car car = carList[e.fromLane].poll();


        //Debugging
        //System.out.println("****************");
        System.out.println("This is Car" + e.carID + " which departures from "+e.fromLane + " to " +e.direction);


    	//Car c = queues[direction].removeFirst();



    	// if (queues[direction].size() > 0) {
    	//     // There's a waiting customer => schedule departure.
    	//     Car waitingCar = queues[direction].get(0);
    	//     // Note where we are collecting stats for waiting time.
    	//     totalWaitTime += clock - waitingCar.entryTime;
    	//     scheduleDeparture(direction);
    	// }


    }


    //Simultaneous going strategy
    void allowTraffic(Event e) {
    	int direction = e.direction;
        int fromLane = e.fromLane;

        // allow queues for car
        PriorityQueue<Car> allowedCars = new PriorityQueue<Car>();
        //Get the first three cars from other lanes.
        for (int i = 0; i < k; i++) {
            if (carList[i].size() == 0) {
                continue;
            }

        	if (i != fromLane) {
        		allowedCars.add(carList[i].peek());
        	}
        }

        if (allowedCars.size() == 0) {
            return;
        }


        Iterator it = allowedCars.iterator();
        while (it.hasNext()) {
            Car car = (Car) it.next();
            //debugging
            //System.out.println(" CarID: " + car.carID+", " +car.fromLane + ", "+car.direction);

            // Left car
            if (car.fromLane == (fromLane + 1) % 4 && car.direction == right) {
                scheduleDeparture(e, car);
                //System.out.println(" CarID: " + car.carID+ " is gone at " + e.eventTime);
                //Remove this car from carlist and eventlist
                Car carToBeRemoved = carList[fromLane].poll();
                allowedCars.remove(carToBeRemoved);
                //eventList.remove(new Event(carToBeRemoved.arrivalTime, 2, carToBeRemoved.fromLane, carToBeRemoved.carID, car.direction));
                continue;                   
            } 

            // opposite car
            if (Math.abs(car.fromLane - fromLane) == 2) {
                if (fromLane != left && car.direction != left) {
                    scheduleDeparture(e, car);
                    //System.out.println(" CarID: " + car.carID+ " is gone at " + e.eventTime);
                    //Remove this car from carlist and eventlist
                    Car carToBeRemoved= carList[fromLane].poll();
                    allowedCars.remove(carToBeRemoved);
                    //eventList.remove(new Event(carToBeRemoved.arrivalTime, 2, carToBeRemoved.fromLane, carToBeRemoved.carID, car.direction));
                    continue; 
                }
            }

            // right car        
            if (fromLane == (car.fromLane + 1) % 4) {
                if (direction == right && car.direction == right) {
                    scheduleDeparture(e, car);
                    //System.out.println(" CarID: " + car.carID+ " is gone at " + e.eventTime);                    
                    //Remove this car from carlist and eventlist
                    Car carToBeRemoved= carList[fromLane].poll();
                    allowedCars.remove(carToBeRemoved);
                    //eventList.remove(new Event(carToBeRemoved.arrivalTime, 2, carToBeRemoved.fromLane, carToBeRemoved.carID, car.direction));
                    continue;                     
                }
            }
        }
    }

    boolean isSameDirection(int i, int j) {
    	return (i == j);
    }

    void scheduleArrival() {
        int fromLane = RandTool.uniform(0,3);
    	double nextArrivalTime = clock + randomInterarrivalTime(fromLane);
        int direction = RandTool.uniform(0,3);
    	eventList.add(new Event(nextArrivalTime, Event.ARRIVAL, fromLane, carID, direction));
        carList[fromLane].add(new Car(nextArrivalTime, direction, fromLane, carID));

        System.out.println("Car "+carID+" will arrival at " + nextArrivalTime);
        carID ++;
    }    

    // Schdule departure for the main car
    void scheduleDeparture(Event e) {
        double nextDepartureTime = clock + randomServiceTime();
        Event eventDeparture = new Event(nextDepartureTime, Event.DEPARTURE, e.fromLane, e.carID, e.direction);
        eventList.add(eventDeparture);
        //TO check if any car can be scheduled/go simultaneously
        allowTraffic(eventDeparture);

        System.out.println("Car "+e.carID+" will departure at " + nextDepartureTime);
    }

    //Schedule departure for cars from other lanes simultaneously
    private void scheduleDeparture(Event e, Car car) {
        double nextDepartureTime = e.eventTime;
        eventList.add(new Event(nextDepartureTime, Event.DEPARTURE, car.fromLane, car.carID, car.direction));

        //debugging     
        System.out.println("================AllowTraffic is scheduling: ==========");
        System.out.println("Event's CarID, fromLane, time: " + e.carID+", " +e.fromLane + ", "+e.direction+", "+e.eventTime);
        System.out.println("Other Cars "+car.carID+" will departure at " + nextDepartureTime);
    }

    double randomInterarrivalTime(int i) {
        return exponential(arrivalRate);
    }

    // TO DO: change to fixed based on direction
    double randomServiceTime() {
        // Assignment 4 requires the serviceRates to be the same
        return exponential(serviceRate);
    }

    double exponential(double lambda) {
        return (1.0 / lambda) * (- Math.log(1.0 - RandTool.uniform()));
    }

    void stats() {
        if (numDepartures == 0) {
            return;
        }
        avgWaitTime = totalWaitTime / numDepartures;
        avgSystemTime = totalSystemTime / numDepartures;
    }

    //getter for carList
    public PriorityQueue<Car>[] getCarList() {
        return carList;
    }


    /////////////////////////////////////////////////////////////////////
    // main
    public static void main(String[] argv) {
        AIMC aimc = new AIMC();
        while(true) {
            aimc.nextStep();
        }
    } 
}





