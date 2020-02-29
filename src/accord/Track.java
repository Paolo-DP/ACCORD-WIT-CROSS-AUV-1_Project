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
    private int[][] trackbounds = new int[2][2];
    public void addTrackSegment(TrackSegment seg, boolean autoConnect){
        //if(!segments.isEmpty())
            if(autoConnect)
                segments.get(segments.size()-1).connectNextSegment(seg);
        segments.add(seg);
    }
    public void addIntersection(IntersectionSegment intersect, TrackSegment[] intersectionSegs){
        
    }
    public boolean complete(){
        if(segments.isEmpty())
            return false;
        segments.get(0).connectPrevSegment(segments.get(segments.size()-1));
        return true;
    }
    public TrackSegment getNextSegment(Car c){
        return carSegList.get(carList.indexOf(c)).getNextSeg(c);
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
        /*
        if(getCurrentSegment(ct)==null)
            ct.isOutOfBounds=true;
        else{
            ct.isOutOfBounds=false;
            ct.distanceFromDrivingLine = ct.currentSeg.distFromCenterLine(c);
            updateCarTrackerAngles(ct);
        }
        */
        ct.x = c.getXLocation();
        ct.y = c.getYLocation();
        ct.currentSeg = searchCurrentSegment(c);
        if(ct.currentSeg != null){
            updateCarTrackerAngles(ct);
            ct.nextSeg = ct.currentSeg.getNextSeg(c);
        }
        else{
            ct.angleDeviation = 0;
            ct.distanceFromDrivingLine = 0;
            ct.idealAngle = c.getOrientation();
            ct.hasReservation = false;
        }
        if(ct.nextSeg != null && ct.nextSeg.isIntersection())
            ct.hasReservation = ((IntersectionSegment)ct.nextSeg).isReserved(c);
        else if(ct.currentSeg != null && ct.currentSeg.isIntersection())
            ct.hasReservation = ((IntersectionSegment)ct.currentSeg).isReserved(c);
        else
            ct.hasReservation = false;
        ct.isOutOfBounds = (ct.currentSeg == null);
        ct.car.setOutOfBounds(ct.isOutOfBounds);
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
    public CarTracker nullTracker(CarDetails deets){
        CarTracker ct = new CarTracker(null);
        ct.currentSeg = searchCurrentSegment(deets.xloc, deets.yloc);
        ct.isOutOfBounds = (ct.currentSeg == null);
        if(ct.currentSeg != null){
            ct.distanceFromDrivingLine = ct.currentSeg.distFromCenterLine(deets.xloc, deets.yloc);
            updateCarTrackerAngles(ct, deets.xloc, deets.yloc, deets.orient);
            //ct.idealAngle = ct.currentSeg.idealDirection(deets.xloc, deets.yloc);
            
            if(!ct.currentSeg.isIntersection())
                ct.nextSeg = ct.currentSeg.getNextSeg(null);
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
                ct.nextSeg = ct.currentSeg.getNextSeg(ct.car);
                return ct.currentSeg;
            }
            else{
                ct.currentSeg = searchCurrentSegment(ct.car);
                if(ct.currentSeg == null)
                    return null;
                ct.nextSeg = ct.currentSeg.getNextSeg(ct.car);
                return ct.currentSeg;
            }
        }
        else if(ct.nextSeg != null){
            if(ct.nextSeg.isWithinBounds(ct.car)){
                ct.currentSeg = ct.nextSeg;
                ct.nextSeg = ct.currentSeg.getNextSeg(ct.car);
                return ct.currentSeg;
            }
            else{
                ct.currentSeg = searchCurrentSegment(ct.car);
                if(ct.currentSeg == null)
                    return null;
                ct.nextSeg = ct.currentSeg.getNextSeg(ct.car);
                return ct.currentSeg;
            }
        }
        else{
            ct.currentSeg = searchCurrentSegment(ct.car);
            if(ct.currentSeg == null)
                return null;
            ct.nextSeg = ct.currentSeg.getNextSeg(ct.car);
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
    private TrackSegment searchCurrentSegment(int x, int y){
        TrackSegment current = null;
        for(int i=0; i<segments.size(); i++){
            if(segments.get(i).isWithinBounds(x, y)){
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
    public boolean updateCarTrackerAngles(CarTracker ct, int x, int y, double orient){
        if(ct.currentSeg==null)
            return false;
        ct.idealAngle = ct.currentSeg.idealDirection(x, y);
        ct.angleDeviation = Math.abs(orient - ct.idealAngle);
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
    
    public void setTrackBounds(int x1, int y1, int x2, int y2){
        trackbounds[0][0] = x1;
        trackbounds[0][1] = y1;
        trackbounds[1][0] = x2;
        trackbounds[1][1] = y2;
    }
    public boolean isWithinTrack(int x, int y){
        return (x >= trackbounds[0][0] && x <= trackbounds[1][0] &&
                y >= trackbounds[0][1] && y <= trackbounds[1][1]);
    }
    
    public Track getRouteTrack(Car c, TrackSegment start){
        
        TrackSegment current = start;
        TrackSegment next = null;
        TrackSegment prev = null;
        Track routeTrack = new Track();
        if(c==null || start == null)
            return routeTrack;
        //routeTrack.addTrackSegment(current, false);
        do{
            routeTrack.addTrackSegment(current, false);
            if(current.isIntersection())
                next = ((IntersectionSegment)current).getExitTo(prev, c.getNextRouteDirection());
            else
                next = current.getNextSeg(c);
            prev = current;
            current = next;
        }while(current != null);
        
        return routeTrack;
    }
    
    public void printAllSegments(){
        for(int i=0; i<segments.size(); i++){
            TrackSegment s = segments.get(i);
            System.out.println("Segment ID: " + s.getSegmentID()
                + "\tXLoc: " + s.getXLocation()
                + "\tYLoc: " + s.getYLocation());
        }
    }
    
    
}
