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
    private PriorityQueue<Car> carList;
    private double clock;

    // Statistics.
    int numArrivals = 0;                    // How many arrived?
    int numDepartures;                      // How many left?
    double totalWaitTime, avgWaitTime;      // For time spent in queue
    double totalSystemTime, avgSystemTime;  // For time spent in system

    void reset() {
    	eventList = new PriorityQueue<Event>();
        carList = new PriorityQueue<Car>();
    	queues = new LinkedList [k];
        for (int i = 0; i < k; i++) {
            queues[i] = new LinkedList<Car>();
        }

	   // Initialize stats variables.
        numArrivals = 0;
    	numDepartures = 0;
    	avgWaitTime = totalWaitTime = 0;
    	avgSystemTime = totalSystemTime = 0;

    	// Need to have at least one event in event list.
        clock = 0;
        scheduleArrival();
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
    	numArrivals[e.fromLane]++;

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
    	

        // TO DO BASED ON fromLane and direction
        allowTraffic(Event e);

    	//Car c = queues[direction].removeFirst();
    	totalSystemTime += clock - c.entryTime;




    	if (queues[direction].size() > 0) {
    	    // There's a waiting customer => schedule departure.
    	    Car waitingCar = queues[direction].get(0);
    	    // Note where we are collecting stats for waiting time.
    	    totalWaitTime += clock - waitingCar.entryTime;
    	    scheduleDeparture(direction);
    	}
    }

    void allowTraffic(Event e) {
    	int direction = e.direction;
        int fromLane = e.fromLane;

        PriorityQueue<Car> allowedCars = new PriorityQueue<Car>();
        for (int i = 0; i < k; i++) {
        	if (i != fromLane) {
        		allowedCars.add(carList[i].peek());
        	}
        }

        switch (fromLane) {
        	case 0:
        		if (direction == Event.RIGHT) {
        			for (int i = 1; i < k; i++) {
        				int d = carList[i].peek().direction;

        				if (d == Event.RIGHT) {

        					// 1st check, all can go RIGHT
        					allow(i);
        				} else if (d == Event.STRAIGHT) {
        					if (i == 2 || i == 3) {
        						allow(i);
        					}
        				} else {
        					// LEFT
        					if (i == 3) {
        						allow(i);
        					}
        				}
        			}
        		}

        	case 1:	

        	case 2:	

        	case 3: 

        	default: 
        }
    }

    boolean isSameDirection(int i, int j) {
    	return (i == j);
    }

    void scheduleArrival() {
            int fromLane = RandTool.uniform(0,4);
        	double nextArrivalTime = clock + randomInterarrivalTime(fromLane);
            int direction = RandTool.unform(1,4);
        	eventList.add(new Event(nextArrivalTime, Event.ARRIVAL, fromLane, direction));
            carList[fromLane].add(new Car(nextArrivalTime, direction));
    }    

    void scheduleDeparture(int i) {
        double nextDepartureTime = clock + randomServiceTime();
        eventList.add(new Event(nextDepartureTime, Event.DEPARTURE, i));
    }

    double randomInterarrivalTime(int i) {
        return exponential(arrivalRate[i]);
    }

    // TO DO: change to fixed based on direction
    double randomServiceTime() {
        // Assignment 4 requires the serviceRates to be the same
        return exponential(serviceRate);
    }

    double exponential(double lambda) {
        return (1.0 / lambda) * (-Math.log(1.0 - RandTool.uniform()));
    }

    void stats() {
        if (numDepartures == 0) {
            return;
        }
        avgWaitTime = totalWaitTime / numDepartures;
        avgSystemTime = totalSystemTime / numDepartures;
    }


    ///////////////////////////////////////////////////////////////////////
    // main
    // public static void main(String[] argv) {
    //     AIMC aimc = new AIMC();
    // 	   aimc.makeFrame();
    // } 
}





