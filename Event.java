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
*  Description: Event class
*/

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