import java.util.*;
/** 
*  CSCI 6341/ Final Project: Automatic Crossroads Control and Management
*  Author: Ismayil Hasanov, Yubo Tsai, Han Wang
*  Date : Apr 25 2017
*  Description: Queue controller and algorithms for AIMC
*/
@SuppressWarnings("unchecked")
public class AIMC extends Observable {
	// Animation and drawing
    Thread currentThread;  
    boolean isPaused = false;
    int sleepTime = 50;   

    // Number of lanes
    int k = 4;

    // Flag used in the GUI departure drawing
    public boolean[] departFlag = new boolean[k];

    // Avg time between arrivals = 1.0 (lambda)
    double arrivalRate = 2.0;
    double serviceRate = 0.75;

    private PriorityQueue<Event> eventList;
    private Event currentEvent = null;
    private PriorityQueue<Car>[] carList = new PriorityQueue[k];
    private double clock;


    // Statistics.
    int numArrivals;                            // Number of arrivals
    int numDepartures;                          // Number of departures
    int[] numDeparturesFromLane = new int[k];   // Departures per lane
    double[] sysTime = new double[k];           // Time spent per lane
    double[] avgSysTime = new double[k];        // Average time spent per lane

    double totalWaitTime, avgWaitTime;      // For time spent in queue
    double totalSystemTime, avgSystemTime;  // For time spent in system

    //Event direction
    double left = Event.LEFT;
    double right = Event.RIGHT;
    double straight = Event.STRAIGHT;

    int carID = 1;

    public AIMC() {
        reset();
    }

    void reset() {
    	eventList = new PriorityQueue<Event>();
        Event currentEvent = null;
        for (int i = 0; i < k; i++) {
            carList[i] = new PriorityQueue<Car>();
            departFlag[i] = false;
            // Stats reset
            sysTime[i] = 0;
            avgSysTime[i] = 0;
            numDeparturesFromLane[i] = 0;     
        }

	   // Stats reset
        carID = 1;
        numArrivals = 0;
    	numDepartures = 0;
    	avgWaitTime = totalWaitTime = 0;
    	avgSystemTime = totalSystemTime = 0;

    	// Need to have at least one event in event list
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
    	currentEvent = eventList.poll();
    	clock = currentEvent.eventTime;

    	// Handle each type separately.
    	if (currentEvent.type == Event.ARRIVAL) {
    	    handleArrival(currentEvent);
    	}
    	else if (currentEvent.type == Event.DEPARTURE) {
            handleDeparture(currentEvent);
            // Let GUI know the departure occured
            setChanged();
            notifyObservers();    	    
    	}

    	// Do stats after event is processed.
    	stats(currentEvent);
    }

    private void handleArrival(Event e) {
        //Debugging
        //System.out.println("**********************************************************");
        //System.out.println("Car " + e.carID + " arrives at lane " + e.fromLane + " and turns to " + e.direction);
        //System.out.println("The size of this lane: " + carList[e.fromLane].size());


    	numArrivals++;

        carList[e.fromLane].add(new Car(e.eventTime, e.direction, e.fromLane, e.carID));
      
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

    private void handleDeparture(Event e) {
    	numDepartures++;
        numDeparturesFromLane[e.fromLane]++;
    	
        //remove it from carList, set departFlag for that lane
        Car car = carList[e.fromLane].poll();
        departFlag[e.fromLane] = true;

        
        sysTime[e.fromLane] += clock - car.arrivalTime;

        //Debugging
        //System.out.println("****************");
        // System.out.println("Car " + e.carID + " departs from lane " + e.fromLane + " to " + e.direction);
        // System.out.println("Departure #: " + numDepartures);
        // System.out.println("Waiting cars: 0=" + carList[0].size() + ", 1=" + carList[1].size() + ", 2=" + carList[2].size() + ", 3=" + carList[3].size());


    	//Car c = queues[direction].removeFirst();

    	// if (queues[direction].size() > 0) {
    	//     // There's a waiting customer => schedule departure.
    	//     Car waitingCar = queues[direction].get(0);
    	//     // Note where we are collecting stats for waiting time.
    	//     totalWaitTime += clock - waitingCar.entryTime;
    	//     scheduleDeparture(direction);
    	// }
    }

    private void scheduleArrival() {
        int fromLane = RandTool.uniform(0,3);
    	double nextArrivalTime = clock + randomInterarrivalTime(fromLane);
        int direction = RandTool.uniform(0,2);
    	eventList.add(new Event(nextArrivalTime, Event.ARRIVAL, fromLane, carID, direction));

        // Debugging
        // System.out.println("Car " + carID + " will arrive at " + nextArrivalTime);
        carID ++;
    }    

    // Schdule departure for the main car
    private void scheduleDeparture(Event e) {
        double nextDepartureTime = clock + randomServiceTime();
        Event eventDeparture = new Event(nextDepartureTime, Event.DEPARTURE, e.fromLane, e.carID, e.direction);
        eventList.add(eventDeparture);

        // Debugging
        System.out.println("Main departure scheduled ~> " + eventDeparture);
        // System.out.println("Car " + e.carID + " will depart at " + nextDepartureTime);
        //TO check if any car can be scheduled/go simultaneously
        allowTraffic(eventDeparture);
    }

    //Schedule departure for cars from other lanes simultaneously
    private void scheduleDeparture(Event e, Car car) {
        double nextDepartureTime = e.eventTime;
        Event eventDeparture = new Event(nextDepartureTime, Event.DEPARTURE, car.fromLane, car.carID, car.direction);
        eventList.add(eventDeparture);

        // Debugging
        System.out.println("Simultaneous departure scheduled -> " + eventDeparture);     
        // System.out.println("================ allowTraffic is scheduling: ==========");
        // System.out.println("Event's CarID, fromLane, time: " + e.carID+", " +e.fromLane + ", "+e.direction+", "+e.eventTime);
        // System.out.println("Other Cars "+car.carID+" will depart at " + nextDepartureTime);
    }

    // Simultaneous going strategy
    private void allowTraffic(Event e) {
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
            // Debugging
            // System.out.println(" CarID: " + car.carID+", " +car.fromLane + ", "+car.direction);

            // Left car
            if (car.fromLane == (fromLane + 1) % 4 && car.direction == right) {
                System.out.println("Checking left...");
                if (removeDuplicateEvent(car.carID, Event.DEPARTURE, e.eventTime)) {
                    scheduleDeparture(e, car);
                }

                //Remove this car from carlist and eventlist
                // Car carToBeRemoved = carList[fromLane].peek();
                // allowedCars.remove(carToBeRemoved);

                it.remove();
                continue;                   
            } 

            // opposite car
            if (Math.abs(car.fromLane - fromLane) == 2) {
                if (fromLane != left && car.direction != left) {
                    System.out.println("Checking opposite...");
                    if (removeDuplicateEvent(car.carID, Event.DEPARTURE, e.eventTime)) {
                        scheduleDeparture(e, car);
                    }
                    //System.out.println(" CarID: " + car.carID+ " is gone at " + e.eventTime);
                    //Remove this car from carlist and eventlist
                    // Car carToBeRemoved = carList[fromLane].peek();
                    // allowedCars.remove(carToBeRemoved);

                    it.remove();
                    continue; 
                }
            }

            // right car        
            if (fromLane == (car.fromLane + 1) % 4) {
                if (direction == right && car.direction == right) {
                    System.out.println("Checking right...");
                    if (removeDuplicateEvent(car.carID, Event.DEPARTURE, e.eventTime)) {
                        scheduleDeparture(e, car);
                    }
                    //System.out.println(" CarID: " + car.carID+ " is gone at " + e.eventTime);                    
                    //Remove this car from carlist and eventlist
                    // Car carToBeRemoved = carList[fromLane].peek();
                    // allowedCars.remove(carToBeRemoved);

                    it.remove();
                    continue;                     
                }
            }
        }
    }    

    private boolean removeDuplicateEvent(int carID, int type, double departureTime) {
        Event event;
        Iterator it = eventList.iterator();
        while (it.hasNext()) {
            event = (Event) it.next();
            if (event.carID == carID && event.type == type && departureTime < event.eventTime) {
                it.remove();
                // Debugging
                System.out.println("Duplicate removed => " + event);
                return true;
            }
        }
        return false;
    }

    double randomInterarrivalTime(int i) {
        return exponential(arrivalRate);
    }

    double randomServiceTime() {
        return exponential(serviceRate);
    }

    double exponential(double lambda) {
        return (1.0 / lambda) * (- Math.log(1.0 - RandTool.uniform()));
    }

    void stats(Event e) {
        if (numDepartures == 0) {
            return;
        }
        //avgWaitTime = totalWaitTime / numDepartures[e.fromLane];
        avgSysTime[e.fromLane] = sysTime[e.fromLane] / numDeparturesFromLane[e.fromLane];
    }

    // getter for carList
    public PriorityQueue<Car>[] getCarList() {
        return carList;
    }

    // getter for currentEvent
    public Event getCurrentEvent() {
        return currentEvent;
    }

    ///////////////////////////////////////////////////////////////////
    //main
    public static void main(String[] argv) {
        AIMC aimc = new AIMC();
        int maxDepartures = 300;
        while (aimc.numDepartures < maxDepartures) {
            aimc.nextStep();
        }

        // System.out.println("Avg wait time: " + aimc.totalWaitTime);
        for (int i = 0; i < aimc.k; i++) {
            System.out.println("Average system time for lane " + i + ": " + aimc.avgSysTime[i]);
        }
    } 
}





