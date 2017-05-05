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
*  Description: Vehicles class
*/

class Car implements Comparable {
    public static int LEFT = 2;
    public static int STRAIGHT = 1;
    public static int RIGHT = 0;

    //private AIMC aimc;

    double arrivalTime;
    int direction = -1;
    int fromLane = -1;
    int carID = -1;
    public Car (double arrivalTime, int direction, int fromLane, int carID) {
        this.arrivalTime = arrivalTime;
        this.direction = direction;
        this.fromLane = fromLane;
        this.carID = carID;
    }

    public int compareTo(Object obj)
    {
        Car car = (Car) obj;
        if (arrivalTime < car.arrivalTime) {
            return -1;
        }
        else if (arrivalTime > car.arrivalTime) {
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