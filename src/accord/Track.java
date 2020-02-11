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
    ArrayList<CarTracker> trackers = new ArrayList<CarTracker>();
    
    double trackAngleOffset = 0;
    
    public void addTrackSegment(TrackSegment seg){
        if(!segments.isEmpty())
            segments.get(segments.size()-1).connectNextSegment(seg);
        segments.add(seg);
    }
    public void addIntersection(TrackSegment[] intersectionSegs){
        
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
    
    public CarTracker updateCarTracker(Car c){
        CarTracker ct = null;
        for(int i=0; i<trackers.size(); i++){
            if(trackers.get(i).car == c){
                ct = trackers.get(i);
                break;
            }
        }
        if(ct == null){
            ct = new CarTracker(c);
            trackers.add(ct);
        }
        if(getCurrentSegment(ct)==null)
            ct.isOutOfBounds=true;
        else{
            ct.isOutOfBounds=false;
            ct.distanceFromDrivingLine = ct.currentSeg.distFromCenterLine(c);
            updateCarTrackerAngles(ct);
        }
        ct.car.outOfBounds = ct.isOutOfBounds;
        return ct;
    }
    public CarTracker getCarTracker(Car c){
        CarTracker ct = null;
        for(int i=0; i<trackers.size(); i++){
            if(trackers.get(i).car == c){
                ct = trackers.get(i);
                break;
            }
        }
        return ct;
    }
    public TrackSegment getCurrentSegment(int xLoc, int yLoc){
        for(int i=0; i<segments.size(); i++){
            if(segments.get(i).isWithinBounds(xLoc, yLoc))
                return segments.get(i);
        }
        return null;
    }
    public TrackSegment getCurrentSegment(CarTracker ct){
        /*if(ct.currentSeg==null && ct.nextSeg==null){
            ct.currentSeg = searchCurrentSegment(ct.car);
            if(ct.currentSeg == null)
                return null;
            ct.nextSeg = ct.currentSeg.getNextSeg();
            return ct.currentSeg;
        }
        else if(ct.currentSeg.isWithinBounds(ct.car))
            return ct.currentSeg;
        else {
            if(ct.nextSeg.isWithinBounds(ct.car)){
                ct.currentSeg = ct.nextSeg;
                ct.nextSeg = ct.currentSeg.getNextSeg();
                return ct.currentSeg;
            }
        }
        else{
            ct.currentSeg = searchCurrentSegment(ct.car);
            ct.nextSeg = ct.currentSeg.getNextSeg();
            return ct.currentSeg;
        }*/
        if(ct.currentSeg != null){
            if(ct.currentSeg.isWithinBounds(ct.car)){
                ct.nextSeg = ct.currentSeg.getNextSeg();
                return ct.currentSeg;
            }
            else{
                ct.currentSeg = searchCurrentSegment(ct.car);
                if(ct.currentSeg == null)
                    return null;
                ct.nextSeg = ct.currentSeg.getNextSeg();
                return ct.currentSeg;
            }
        }
        else if(ct.nextSeg != null){
            if(ct.nextSeg.isWithinBounds(ct.car)){
                ct.currentSeg = ct.nextSeg;
                ct.nextSeg = ct.currentSeg.getNextSeg();
                return ct.currentSeg;
            }
            else{
                ct.currentSeg = searchCurrentSegment(ct.car);
                if(ct.currentSeg == null)
                    return null;
                ct.nextSeg = ct.currentSeg.getNextSeg();
                return ct.currentSeg;
            }
        }
        else{
            ct.currentSeg = searchCurrentSegment(ct.car);
            if(ct.currentSeg == null)
                return null;
            ct.nextSeg = ct.currentSeg.getNextSeg();
            return ct.currentSeg;
        }
    }
    private TrackSegment searchCurrentSegment(Car c){
        TrackSegment current = null;
        for(int i=0; i<segments.size(); i++){
            if(segments.get(i).isWithinBounds(c)){
                current = segments.get(i);
                break;
            }
        }
        return current;
    }
    
    public int distfromCenterLine(Car c){
        int xLoc = c.getXLocation();
        int yLoc = c.getYLocation();
        TrackSegment seg = getCurrentSegment(xLoc, yLoc);
        if(seg!= null)
            return seg.distFromCenterLine(xLoc, yLoc);
        else
            return Integer.MAX_VALUE;
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
        TrackSegment currSeg = getCurrentSegment(xLoc, yLoc);
        if(currSeg == null)
            return 0;
        double segDir = currSeg.idealDirection(xLoc, yLoc);
        double deviat = Math.abs(carDir-segDir);
        if(deviat>180)
            deviat = -(360-deviat);
        return deviat;
    }
    public double directionDeviation(CarTracker ct){
        double deviat = Math.abs(ct.car.getOrientation()-ct.currentSeg.idealDirection(ct.car));
        if(deviat>180)
            deviat = -(360-deviat);
        return deviat;
    }
    public boolean updateCarTrackerAngles(CarTracker ct){
        if(ct.currentSeg==null)
            return false;
        ct.idealAngle = ct.currentSeg.idealDirection(ct.car);
        ct.angleDeviation = Math.abs(ct.car.getOrientation() - ct.idealAngle);
        if(ct.angleDeviation>180)
            ct.angleDeviation = ct.angleDeviation - 360;
        return true;
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
    
    public TrackSegment getSegment(int index){
        if(index < segments.size())
            return segments.get(index);
        else
            return null;
    }
    public int getNSegments(){
        return segments.size();
    }
    public int[][] getTrackBounds(){
        int [][] bounds = {{0,0},{0,0}};
        int x,y;
        TrackSegment seg;
        for(int i=0; i<segments.size(); i++){
            seg = segments.get(i);
            x = seg.getXLocation();
            y = seg.getYLocation();
            if(x < bounds[0][0])
                bounds[0][0] = x;
            if(y < bounds[0][1])
                bounds[0][1] = y;
            if(x > bounds[1][0])
                bounds[1][0] = x;
            if(y > bounds[1][1])
                bounds[1][1] = y;
            
            x = seg.getExitXLocation();
            y = seg.getExitYLocation();
            if(x < bounds[0][0])
                bounds[0][0] = x;
            if(y < bounds[0][1])
                bounds[0][1] = y;
            if(x > bounds[1][0])
                bounds[1][0] = x;
            if(y > bounds[1][1])
                bounds[1][1] = y;
            
        }
        return bounds;
    }
}
