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
    ArrayList<Car> carList = new ArrayList<Car>();
    ArrayList<TrackSegment> carSegList = new ArrayList<TrackSegment>();
    
    double trackAngleOffset = 0;
    
    public void addTrackSegment(TrackSegment seg){
        if(!segments.isEmpty())
            segments.get(segments.size()-1).connectNextSegment(seg);
        segments.add(seg);
    }
    public boolean complete(){
        if(segments.isEmpty())
            return false;
        segments.get(0).connectPrevSegment(segments.get(segments.size()-1));
        return true;
    }
    public TrackSegment getNextSegment(Car c){
        return carSegList.get(carList.indexOf(c)).getNextSeg();
    }
    public void removeTrackSegment(TrackSegment seg){
        
        segments.remove(seg);
    }
    public TrackSegment getCurrentSegment(int xLoc, int yLoc){
        for(int i=0; i<segments.size(); i++){
            if(segments.get(i).isWithinBounds(xLoc, yLoc))
                return segments.get(i);
        }
        return null;
    }
    public TrackSegment getCurrentSegment(Car c){        
        int carIndex = carList.indexOf(c);
        int xLoc = c.getXLocation();
        int yLoc = c.getYLocation();
        /*
        if(carSegList.get(carIndex).isWithinBounds(xLoc, yLoc))
            return carSegList.get(carIndex);
        else{
            int count = 0;
            TrackSegment seg2chk = carSegList.get(carIndex).getNextSeg();
            while(count < segments.size()-1){
                if(seg2chk.isWithinBounds(xLoc, yLoc))
                    return carSegList.get(carIndex);
                seg2chk = seg2chk.getNextSeg();
            }
            return null;
        }
        */
        return getCurrentSegment(xLoc, yLoc);
    }
    public int distfromCenterLine(Car c){
        int xLoc = c.getXLocation();
        int yLoc = c.getYLocation();
        
        return this.getCurrentSegment(xLoc, yLoc).distFromCenterLine(xLoc, yLoc);
    }
    public int distfromCenterLine(int xLoc, int yLoc){
        TrackSegment seg = getCurrentSegment(xLoc, yLoc);
        if(seg==null)
            return Integer.MAX_VALUE;
        else
            return seg.distFromCenterLine(xLoc, yLoc);
    }
    public double directionDeviation(Car c){
        int xLoc = c.getXLocation();
        int yLoc = c.getYLocation();
        double carDir = c.getOrientation();
        return this.getCurrentSegment(xLoc, yLoc).idealDirection(xLoc, yLoc) - carDir;
    }
    public double idealDirection(int xLoc, int yLoc){
        TrackSegment seg = getCurrentSegment(xLoc, yLoc);
        if(seg==null)
            return Integer.MAX_VALUE;
        else
            return seg.idealDirection(xLoc, yLoc);
    }
    
    public void setTrackAngleOffset(double offset){ trackAngleOffset = offset;}
    public void setTrackMargin(int margin){
        for(int i=0; i<segments.size(); i++){
            segments.get(i).setBoundaryMargin(margin);
        }
    }
}
