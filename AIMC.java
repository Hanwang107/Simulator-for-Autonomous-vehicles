import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;

public class AIMC extends JPanel {
	// Animation and drawing
    Thread currentThread;  
    boolean isPaused = false;
    int sleepTime = 50;

    int roadWidth = 60;
    int roadLength = 200;
    int offset = 60;
    
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
    	int direction = e.direction;
        int fromLane = e.fromLane;
        // TO DO BASED ON fromLane and direction
        allowTraffic();

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

    void scheduleArrival() {
            int fromLane = RandTool.uniform(0,4);
        	double nextArrivalTime = clock + randomInterarrivalTime(fromLane);
            int direction = RandTool.unform(1,4);
        	eventList.add(new Event(nextArrivalTime, Event.ARRIVAL, fromLane, direction));
            carList[fromLane].add(new Car(nextArrivalTime));
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
    // GUI and drawing
    public void paintComponent (Graphics g)
    {
        super.paintComponent (g);
        Graphics2D g2 = (Graphics2D) g;

        // Clear.
        Dimension D = this.getSize();
        g.setColor (Color.white);
        g.fillRect (0,0, D.width,D.height);

        g2.setStroke(new BasicStroke(2f));  

        // Draw left road.
        g.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth); // lane 0
        g.setColor (Color.yellow);
        g2.drawLine (offset, D.height / 2, offset + roadLength, D.height / 2); // lane 1
        g.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth);

        // Draw top road.
        g.setColor (Color.black);
        g2.drawLine (offset + roadLength, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth - roadLength); // lane 2
        g.setColor (Color.yellow);
        g2.drawLine (offset + roadLength + roadWidth, D.height / 2 - roadWidth, offset + roadLength + roadWidth, D.height / 2 - roadWidth - roadLength); // lane 3
        g.setColor (Color.black);
        g2.drawLine (offset + roadLength + 2 * roadWidth , D.height / 2 - roadWidth, offset + roadLength + 2 * roadWidth, D.height / 2 - roadWidth - roadLength);

        // Draw right road.
        offset += roadLength + 2 * roadWidth;
        g.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth); // lane 4
        g.setColor (Color.yellow);
        g2.drawLine (offset, D.height / 2, offset + roadLength, D.height / 2); // lane 5
        g.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth);

        // Draw bottom road.
        offset -= roadLength + 2 * roadWidth;
        g.setColor (Color.black);
        g2.drawLine (offset + roadLength, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth + roadLength); // lane 6
        g.setColor (Color.yellow);
        g2.drawLine (offset + roadLength + roadWidth, D.height / 2 + roadWidth, offset + roadLength + roadWidth, D.height / 2 + roadWidth + roadLength); // lane 7
        g.setColor (Color.black);
        g2.drawLine (offset + roadLength + 2 * roadWidth , D.height / 2 + roadWidth, offset + roadLength + 2 * roadWidth, D.height / 2 + roadWidth + roadLength);

        // Draw intersection box
        final float dash1[] = { 10.0f };
  		final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
  		g2.setStroke(dashed);
  		g.setColor (Color.red);
    	g2.drawRect(offset + roadLength, D.height / 2 - roadWidth, 2 * roadWidth, 2 * roadWidth);

	}    

    JPanel makeBottomPanel ()
    {
        JPanel panel = new JPanel ();
        
		JButton resetB = new JButton ("Reset");
		resetB.addActionListener (
			new ActionListener () {
			   public void actionPerformed (ActionEvent a)
			   {
			       //reset ();
			   }
			});
		panel.add (resetB);

        panel.add (new JLabel ("          "));
		JButton nextB = new JButton ("Next");
		nextB.addActionListener (
			new ActionListener () {
			   public void actionPerformed (ActionEvent a)
			   {
			       //nextStep ();
			   }
			});
		panel.add (nextB);


        panel.add (new JLabel ("          "));
		JButton goB = new JButton ("Go");
		goB.addActionListener (
			new ActionListener () {
			   public void actionPerformed (ActionEvent a)
			   {
			       //go ();
			   }
			});
		panel.add (goB);

        panel.add (new JLabel ("  "));
		JButton pauseB = new JButton ("Pause");
		pauseB.addActionListener (
			new ActionListener () {
			   public void actionPerformed (ActionEvent a)
			   {
			       //pause ();
			   }
			});
		panel.add (pauseB);

        panel.add (new JLabel ("           "));
		JButton quitB = new JButton ("Quit");
		quitB.addActionListener (
			new ActionListener () {
			   public void actionPerformed (ActionEvent a)
			   {
			       System.exit(0);
			   }
           });
		panel.add (quitB);
        
        return panel;
    }   

    void makeFrame ()
    {
        JFrame frame = new JFrame ();
        frame.setSize (700, 700);
        frame.setTitle ("Autonomous Intersection Management and Control");
        Container cPane = frame.getContentPane();
        cPane.add (makeBottomPanel(), BorderLayout.SOUTH);
        cPane.add (this, BorderLayout.CENTER);
        frame.setVisible (true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ///////////////////////////////////////////////////////////////////////
    // main
    public static void main(String[] argv) {
        AIMC aimc = new AIMC();
    	aimc.makeFrame();
    } 
}

class Car {
    double arrivalTime;
    public Car (double arrivalTime)
    {
        this.arrivalTime = arrivalTime;
    }
}

class Event implements Comparable {
    public static int ARRIVAL = 1;
    public static int DEPARTURE = 2;

    public static int LEFT = 2;
    public static int STRAIGHT = 1;
    public static int RIGHT = 0;

    int type = -1;                     // Arrival or departure.
    double eventTime;                  // When it occurs.
    int fromLane = -1;
    int direction = -1;

    public Event(double eventTime, int type, int fromLane, int direction)
    {
    	this.eventTime = eventTime;
    	this.type = type;
        this.fromLane = fromLane;
        this.direction = direction;
    }

    public int compareTo(Object obj)
    {
        Event e = (Event) obj;
        if (eventTime < e.eventTime) {
            return -1;
        }
        else if (eventTime > e.eventTime) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public boolean equals(Object obj)
    {
        return (compareTo(obj) == 0);
    }

}