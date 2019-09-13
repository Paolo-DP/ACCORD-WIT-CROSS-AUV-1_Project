/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.util.ArrayList;

/**
 *
 * @author Paolo
 */
public class Track {
    ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>();
    ArrayList<Car> carSegList = new ArrayList<Car>();
    
    double trackAngleOffset = 0;
    
    public void addTrackSegment(TrackSegment seg){
        if(!segments.isEmpty())
            segments.get(segments.size()-1).connectNextSegment(seg);
        segments.add(seg);
    }
    public TrackSegment getNextSegment(Car c){
        return null;
    }
    public void removeTrackSegment(TrackSegment seg){
        segments.remove(seg);
    }
    public TrackSegment getCurrentSegment(int xLoc, int yLoc){
        return null;
    }
    public int distfromCenterLine(Car c){
        return 0;
    }
    public int directionDeviation(Car c){
        return 0;
    }
    
    public void setTrackAngleOffset(double offset){ trackAngleOffset = offset;}
    
    private TrackSegment findCurrentSegment(Car c){
        return null;
    }
}
