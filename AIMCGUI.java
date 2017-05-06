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
*  Description: GUI and drawing
*/
public class AIMCGUI extends JPanel implements Observer {

	private int roadWidth = 60;
    private int roadLength = 200;
    private int offset = 60;

    private int carLength = 40;
    private int carWidth = 30;
    private int lGap = 5;
    private int wGap = 15; //(roadWidth - carWidth) / 2;

    // Animation and drawing.
    boolean isDeparture = false;
    Thread currentThread;  
    boolean isPaused = false;
    int sleepTime = 500;
    //DecimalFormat df = new DecimalFormat ("##.####");

    PriorityQueue<Car>[] carList;

	private AIMC aimc;
    private Event currentEvent;
    private int departingLane;
    private int arrowDirection;

    // Car direction
    double left = Car.LEFT;
    double right = Car.RIGHT;
    double straight = Car.STRAIGHT;

	public AIMCGUI() {
        aimc = new AIMC();
        aimc.addObserver(this);
        carList = aimc.getCarList();
		makeFrame();
	}


	private void makeFrame ()
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

    public void paintComponent (Graphics g)
    {
        super.paintComponent (g);
        Graphics2D g2 = (Graphics2D) g;

        // Clear.
        Dimension D = this.getSize();
        g2.setColor (Color.white);
        g2.fillRect (0,0, D.width,D.height);

        g2.setStroke(new BasicStroke(2f));  

        // Draw left road.
        g2.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth); // lane 0
        g2.setColor (Color.yellow);
        g2.drawLine (offset, D.height / 2, offset + roadLength, D.height / 2); // lane 1
        g2.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth);

        // Draw top road.
        g2.setColor (Color.black);
        g2.drawLine (offset + roadLength, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth - roadLength); // lane 2
        g2.setColor (Color.yellow);
        g2.drawLine (offset + roadLength + roadWidth, D.height / 2 - roadWidth, offset + roadLength + roadWidth, D.height / 2 - roadWidth - roadLength); // lane 3
        g2.setColor (Color.black);
        g2.drawLine (offset + roadLength + 2 * roadWidth , D.height / 2 - roadWidth, offset + roadLength + 2 * roadWidth, D.height / 2 - roadWidth - roadLength);

        // Draw right road.
        offset += roadLength + 2 * roadWidth;
        g2.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 - roadWidth, offset + roadLength, D.height / 2 - roadWidth); // lane 4
        g2.setColor (Color.yellow);
        g2.drawLine (offset, D.height / 2, offset + roadLength, D.height / 2); // lane 5
        g2.setColor (Color.black);
        g2.drawLine (offset, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth);

        // Draw bottom road.
        offset -= roadLength + 2 * roadWidth;
        g2.setColor (Color.black);
        g2.drawLine (offset + roadLength, D.height / 2 + roadWidth, offset + roadLength, D.height / 2 + roadWidth + roadLength); // lane 6
        g2.setColor (Color.yellow);
        g2.drawLine (offset + roadLength + roadWidth, D.height / 2 + roadWidth, offset + roadLength + roadWidth, D.height / 2 + roadWidth + roadLength); // lane 7
        g2.setColor (Color.black);
        g2.drawLine (offset + roadLength + 2 * roadWidth , D.height / 2 + roadWidth, offset + roadLength + 2 * roadWidth, D.height / 2 + roadWidth + roadLength);

        // Draw intersection box
        final float dash1[] = { 10.0f };
  		final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
  		g2.setStroke(dashed);
  		g2.setColor (Color.red);
    	g2.drawRect(offset + roadLength, D.height / 2 - roadWidth, 2 * roadWidth, 2 * roadWidth);


        // Draw Cars
        if (carList[0].size() != 0 || carList[0] != null) {
            Object[] cars = carList[0].toArray();
            // Draw left cars (fromLane: 0)
            for (int i = 0; i < cars.length; i++) {
                // Car
                g2.setColor(Color.green);
                int x = offset + roadLength - (i + 1) * (lGap + carLength);
                int y = D.height / 2 + wGap;
                g2.fillRect(x, y, carLength, carWidth);

                // Id text
                g2.setColor(Color.black);
                Car car = (Car) cars[i];
                String carId = "" + car.carID;
                g2.drawString(carId, x, y + carWidth / 2);
            }

            // Direction of the departure is simulated by drawing rectangles
            if (isDeparture && departingLane == 0 && aimc.departFlag[0]) {
                g2.setColor(Color.green);
                g2.setStroke(new BasicStroke(2f));

                if (arrowDirection == right) {
                    g2.fillRect(offset + roadLength, D.height / 2, roadWidth, roadWidth);
                } else if (arrowDirection == straight) {
                    g2.fillRect(offset + roadLength, D.height / 2, 2 * roadWidth, roadWidth);
                } else {
                    g2.fillRect(offset + roadLength, D.height / 2, 2 * roadWidth, roadWidth);
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2 - roadWidth, roadWidth, roadWidth);
                }
                aimc.departFlag[0] = false;
            }
        }

        if (carList[1].size() != 0 || carList[1] != null) {
            Object[] cars = carList[1].toArray();
            // Draw top cars (fromLane: 1)
            for (int i = 0; i < cars.length; i++) {
                // Car
                g2.setColor(Color.magenta);
                int x = offset + roadLength +wGap;
                int y = D.height / 2 - roadWidth - (i + 1) * (lGap + carLength);
                g2.fillRect(x, y, carWidth, carLength);   

                // Id text
                g2.setColor(Color.black);
                Car car = (Car) cars[i];
                String carId = "" + car.carID;
                g2.drawString(carId, x, y + carLength / 2);     
            }

            // Direction of the departure is simulated by drawing rectangles
            if (isDeparture && departingLane == 1 && aimc.departFlag[1]) {
                g2.setColor(Color.magenta);
                g2.setStroke(new BasicStroke(2f));
                
                if (arrowDirection == right) {
                    g2.fillRect(offset + roadLength, D.height / 2 - roadWidth, roadWidth, roadWidth);
                } else if (arrowDirection == straight) {
                    g2.fillRect(offset + roadLength, D.height / 2 - roadWidth, roadWidth, 2 * roadWidth);
                } else {
                    g2.fillRect(offset + roadLength, D.height / 2 - roadWidth, roadWidth, 2 * roadWidth);
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2, roadWidth, roadWidth);
                }
                aimc.departFlag[1] = false;
            }
        }

        if (carList[2].size() != 0 || carList[2] != null) {
            Object[] cars = carList[2].toArray();
            // Draw right cars (fromLane: 2)
            for (int i = 0; i < cars.length; i++) {
                // Car
                g2.setColor(Color.orange); 
                int x = offset + roadLength + 2 * roadWidth + (i + 1) * lGap +  i * carLength;
                int y = D.height / 2 - carWidth - wGap;
                g2.fillRect(x, y, carLength, carWidth);    

                // Id text
                g2.setColor(Color.black);
                Car car = (Car) cars[i];
                String carId = "" + car.carID;
                g2.drawString(carId, x, y + carWidth / 2);       
            }

            // Direction of the departure is simulated by drawing rectangles
            if (isDeparture && departingLane == 2 && aimc.departFlag[2]) {
                g2.setColor(Color.orange);
                g2.setStroke(new BasicStroke(2f));

                if (arrowDirection == right) {
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2 - roadWidth, roadWidth, roadWidth);
                } else if (arrowDirection == straight) {
                    g2.fillRect(offset + roadLength, D.height / 2 - roadWidth, 2 * roadWidth, roadWidth);
                } else {
                    g2.fillRect(offset + roadLength, D.height / 2  - roadWidth, 2 * roadWidth, roadWidth);
                    g2.fillRect(offset + roadLength, D.height / 2, roadWidth, roadWidth);
                }
                aimc.departFlag[2] = false;
            }
        }

        if (carList[3].size() != 0 || carList[3] != null) {
            Object[] cars = carList[3].toArray();
             // Draw bottom cars (fromLane: 3)
            for (int i = 0; i < cars.length; i++) {
                g2.setColor(Color.cyan);
                int x = offset + roadLength + roadWidth + wGap;
                int y = D.height / 2 + roadWidth + (i + 1) * lGap + i * carLength;
                g2.fillRect(x, y, carWidth, carLength); 

                g2.setColor(Color.black);
                Car car = (Car) cars[i];
                String carId = "" + car.carID;
                g2.drawString(carId, x, y + carLength / 2);           
            }

            // Direction of the departure is simulated by drawing rectangles
            if (isDeparture && departingLane == 3 && aimc.departFlag[3]) {
                g2.setColor(Color.cyan);
                g2.setStroke(new BasicStroke(2f));

                if (arrowDirection == right) {
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2, roadWidth, roadWidth);
                } else if (arrowDirection == straight) {
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2 - roadWidth, roadWidth, 2 * roadWidth);
                } else {
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2 - roadWidth, roadWidth, 2 * roadWidth);
                    g2.fillRect(offset + roadLength + roadWidth, D.height / 2, roadWidth, roadWidth);
                }
                aimc.departFlag[3] = false;
            }
        }
	}    

    JPanel makeBottomPanel ()
    {
        JPanel panel = new JPanel ();
		JButton resetB = new JButton ("Reset");

        panel.add (new JLabel ("          "));
        JButton nextB = new JButton ("Next");

        panel.add (new JLabel ("          "));
        JButton goB = new JButton ("Go");

        panel.add (new JLabel ("  "));
        JButton pauseB = new JButton ("Pause");
        pauseB.setEnabled(false);

        panel.add (new JLabel ("           "));
        JButton quitB = new JButton ("Quit");

		resetB.addActionListener(
			new ActionListener() {
			   public void actionPerformed(ActionEvent a)
			   {
                    reset();
                    pauseB.setEnabled(false);
                    nextB.setEnabled(true);
                    goB.setEnabled(true);
			   }
			});
		panel.add (resetB);

		nextB.addActionListener(
			new ActionListener() {
			   public void actionPerformed(ActionEvent a)
			   {
			       nextStep();
			   }
			});
		panel.add (nextB);

		goB.addActionListener(
			new ActionListener() {
			   public void actionPerformed(ActionEvent a)
			   {
                    goB.setEnabled(false);
                    nextB.setEnabled(false);
                    pauseB.setEnabled(true);
                    go();
			   }
			});
		panel.add (goB);

		pauseB.addActionListener(
			new ActionListener() {
			   public void actionPerformed(ActionEvent a)
			   {
                    pauseB.setEnabled(false);
                    nextB.setEnabled(true);
                    goB.setEnabled(true);
                    pause();
			   }
			});
		panel.add (pauseB);

		quitB.addActionListener(
			new ActionListener() {
			   public void actionPerformed (ActionEvent a)
			   {
			       System.exit(0);
			   }
           });
		panel.add (quitB);
        
        return panel;
    } 


    ///////////////////////////////////////////////////////////////////////
    // Animation

    void go()
    {
        // Fire off a thread so that Swing's thread isn't used.
        // if (isPaused) {
        //     isPaused = false;
        //     return;
        // }
        if (currentThread != null) {
            currentThread.interrupt ();
            currentThread = null;
        }
        
        currentThread = new Thread () {
                public void run () 
                {   
                    //doAnimation = true;
                    simulate ();
                }
                
        };
        currentThread.start();
    }

    void pause() 
    {
        isPaused = true;
    }    

    void simulate()
    {
        while (true) {

            if (! isPaused) {
                nextStep();
            }
            
            repaint();

            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e){
                break;
            }
        } 

        repaint();
    }

    void nextStep() {
        aimc.nextStep();
        repaint();
    }

    void reset() {
        pause();
        aimc.reset();
        repaint();
    }

    // Override update() method of the Observer class
    public void update(Observable aimcgui, Object aimc) {
        // Draw the directional arrows when the departure occurs
        isDeparture = true;
        drawDeparture();
    }

    // Draw the directional arrows when the departure occurs
    private void drawDeparture() {
        currentEvent = aimc.getCurrentEvent();
        departingLane = currentEvent.fromLane;
        arrowDirection = currentEvent.direction;

        // Debugging
        System.out.println("-------------> Departure occuring <-------------------- ");
        System.out.println("GUI msg: car = " + currentEvent.carID + ", lane = " + departingLane + ", direction = " + arrowDirection);
        System.out.println("departFlag[" + departingLane + "] = " + aimc.departFlag[departingLane]);
        repaint();
    }

	public static void main(String[] argv) {
        AIMCGUI gui = new AIMCGUI();
    }	
}