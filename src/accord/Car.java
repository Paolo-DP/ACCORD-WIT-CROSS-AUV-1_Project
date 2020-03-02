/*
 * The MIT License
 *
 * Copyright 2020 Paolo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package accord;

import simulator.SimulationConstants;
import vehicle.VehicleProperty;

/**
 *
 * @author Paolo
 */
public interface Car extends SimulationConstants {

    int DEFAULT_XDim = 278;
    int DEFAULT_YDim = 112;
    
    int DEFAULT_THROTTLE_CEILING = 127;
    int DEFAULT_THROTTLE_FLOOR = 0;
    /**
     * Adds a new Route maneuver to the Cars route Queue.
     * It Follows the Car simulator constants LEFT_TURN, STRAIGHT, RIGHT_TURN
     * @param dir
     * @return 
     */
    boolean addRouteDirection(int dir);
    /**
     * 
     * @param steer
     * @return 
     */
    boolean adjustSteering(int steer);

    boolean adjustThrottle(int throttle);

    void advanceRoute();

    boolean alignXAxis();

    CarDetails getFullDetails();

    int getID();

    double getLastTimeStamp();

    double getMaintainOrient();

    int getNextRouteDirection();

    double getOrientation();

    double getSpeed();

    double getSpeedEquivalent(int throttle);

    int getSteeringPower();

    double getTempOrient();

    int getThrottlePower();

    VehicleProperty getVehicleProperty();

    int getXDimension();

    int getXLocation();

    int getYDimension();

    int getYLocation();

    boolean isUpdated();
    
    boolean isOutOfBounds();
    /**
     * The car will turn its steering in the direction of the desired orientation
     * It will return to straight once it is facing the correct orientation
     * @param orient - the desired orientation (degrees)
     * @param overwrite - used to cancel and overwrite the timed maintain
     * @return 
     */
    boolean maintainOrientation(double orient, boolean overwrite);
    /**
     * The car will turn its steering to the direction of the desired
     * orientation and maintain that orientation for a specified amount of time.
     * After which, it will turn to go back to the old orientation.
     * 
     * @param orient - desired orientation (degrees)
     * @param time - time to maintain that orientation (ms)
     * @return 
     */
    boolean maintainOrientationTimed(double orient, double time);
    /**
     * Prints critical car attributes to the console for debugging.
     * Includes but not limited to:
     * ID
     * isUpdated
     * Last time stamp
     * X
     * Y
     * Orientation
     * Out of bounds
     * throttle
     * maintain orientation
     * temp orientation (timed)
     */
    void printCarAttributes();

    /**
     * Manually set all car attributes. Disregards pozyx data.
     * @param id Car ID
     * @param x x location
     * @param y y location
     * @param orien orientation in degrees
     * @param xdim x dimension, typically length of car
     * @param ydim y dimension, typically width of car
     * @param speed speed in mm/s
     */
    void setAttributesManual(int id, int x, int y, double orien, int xdim, int ydim, double speed);
    /**
     * specifies the output path (folder directory) where the Car object will
     * output the generated CSV files.
     * @param path - file directory
     * @return if the file directory or the file writers were successfully made
     */
    boolean setCSVOutput(String path);
    
    void setVerbose(boolean verbose);

    void setDataHistory(int[] xHist, int[] yHist, double[] orientHist, double[] timeHist);
    
    void setOutOfBounds(boolean out);
    /**
     * manually sets the location of the car
     * Also adds a time stamp and adds it to the car location history
     * @param x - x location
     * @param y - y location
     * @param time - time stamp (ms)
     */
    void setLocation(int x, int y, double time);

    boolean steeringDecrement();

    boolean steeringIncrement();

    boolean throttleDecrement();

    boolean throttleIncrement();

    double timeSinceLastUpdate();
    
    /**
     * updates and orientation of the Car based on its current throttle and steering.
     * The magnitude of the update is also proportional to the amount of time
     * elapsed from the last time this function is called
     * it also updates all other car attributes such as location history
     * If this method is successful, it sets the internal variable isUpdated
     * to true.
     * @return if the updating of attributes is successful
     */
    boolean updateLocation();
    
}
