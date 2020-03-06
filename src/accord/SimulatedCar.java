
package accord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import vehicle.VehicleProperty;

/**
 *
 * @author Ocampo
 */
public class SimulatedCar implements Car {

	public SimulatedCar() {
		this(System.out);
	}
        
        public SimulatedCar(int id){
            steeringPower = 0;
            throttlePower = 0;
            speed = 0;
            maintainOrient = 0;
            outOfBounds = false;
            verbose = false;
            updated = false;
            this.console = console;
            routeQueue = new LinkedList<>();
            xLocationHistoryQueue = new LinkedList();
            yLocationHistoryQueue = new LinkedList<>();
            timeHistoryQueue = new LinkedList<>();
            orientHistoryQueue = new LinkedList<>();
            initThrottleToSpeedTable();
            initCarDetails(id, 10, 10, 10, 10, 16);
        }
	
	public SimulatedCar(PrintStream console) {
		steeringPower = 0;
		throttlePower = 0;
		speed = 0;
		maintainOrient = 0;
		outOfBounds = false;
		verbose = false;
		updated = false;
		this.console = console;
		routeQueue = new LinkedList<>();
		xLocationHistoryQueue = new LinkedList();
		yLocationHistoryQueue = new LinkedList<>();
		timeHistoryQueue = new LinkedList<>();
		orientHistoryQueue = new LinkedList<>();
		initThrottleToSpeedTable();
		initCarDetails(0, 10, 10, 10, 10, 16);
	}
	
	

	
	@Override
	public boolean addRouteDirection(int dir) {
		if (routeQueue.size() >= carDetails.routeDirections.length) return false;  // Route queue full
		boolean queueAdd = routeQueue.offer(dir);
		carDetails.routeCount = routeQueue.size();
		// Put into carDetails
		int i = 0;
		for (Integer route : routeQueue) {
			if (i >= carDetails.routeDirections.length) break;
			carDetails.routeDirections[i] = route;
			i++;
		}
		return queueAdd;
	}

	@Override
	public boolean adjustSteering(int steer) {
		steeringPower = steer;
		return true;
	}

	@Override
	public boolean adjustThrottle(int throttle) {
		if (throttle > SPEED_LIMIT) {
			throttlePower = SPEED_LIMIT;
		} else if (throttle > 0 && throttle <= SPEED_FLOOR) {
			throttlePower = SPEED_FLOOR;
		} else {
			throttlePower = Math.max(0, throttle);
		}
                speed = getSpeedEquivalent(throttlePower);
                if(commSched != null)
                    commSched.cmdSetThrottle(carDetails.carID, throttlePower);
		return true;
	}

	@Override
	public void advanceRoute() {
		routeQueue.poll();
		carDetails.routeCount = routeQueue.size();
		// Put into carDetails
		int i = 0;
		for (Integer xLocation : routeQueue) {
			if (i >= carDetails.routeDirections.length) break;
			carDetails.routeDirections[i] = xLocation;
			i++;
		}
	}

	@Override
	public boolean alignXAxis() {
		updateLocation();
                xAxisCalib = carDetails.orient;
		return true;
	}
        
        @Override
        public void alignXAxis(double orient){
            xAxisCalib = orient;
        }

	@Override
	public CarDetails getFullDetails() {
            CarDetails deets = new CarDetails();
            deets.carID = carDetails.carID;
            deets.isValidData = carDetails.isValidData;
            deets.isValidated = deets.isValidated;
            deets.orient = carDetails.orient;
            deets.orientHistory = Arrays.copyOf(carDetails.orientHistory, carDetails.orientHistory.length);
            deets.routeCount = carDetails.routeCount;
            deets.routeDirections = Arrays.copyOf(carDetails.routeDirections, carDetails.routeDirections.length);
            deets.speed = carDetails.speed;
            deets.timeStampHist = Arrays.copyOf(carDetails.timeStampHist, carDetails.timeStampHist.length);
            deets.xLocHistory = Arrays.copyOf(carDetails.xLocHistory, carDetails.xLocHistory.length);
            deets.xdimen = carDetails.xdimen;
            deets.xloc = carDetails.xloc;
            deets.yLocHistory = Arrays.copyOf(carDetails.yLocHistory, carDetails.yLocHistory.length);
            deets.ydimen = carDetails.ydimen;
            deets.yloc = carDetails.yloc;
		return deets;
	}

	@Override
	public int getID() {
		return carDetails.carID;
	}

	@Override
	public double getLastTimeStamp() {
		return carDetails.timeStampHist[0];
	}

	@Override
	public double getMaintainOrient() {
		return maintainOrient;
	}

	@Override
	public int getNextRouteDirection() {
		if (routeQueue.isEmpty()) return STRAIGHT;
		Integer route = routeQueue.peek();
		return route;
	}

	@Override
	public double getOrientation() {
		return carDetails.orient;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public double getSpeedEquivalent(int throttle) {
            if (throttleToSpeedTable.containsKey(throttle)) return throttleToSpeedTable.get(throttle);
            List<Map.Entry<Integer, Double>> entries = new ArrayList<>(throttleToSpeedTable.entrySet());
            double equivalentSpeed = 0;
            for (int i = 0; i < entries.size()-1; i++) {
                Map.Entry<Integer, Double> entry0 = entries.get(i);
                Map.Entry<Integer, Double> entry1 = entries.get(i+1);
                if (throttle >= entry0.getKey() && throttle <= entry1.getKey()) {
                    equivalentSpeed = entry0.getValue() + (entry1.getValue() - entry0.getValue()) / (entry1.getKey() - entry0.getKey()) * (throttle - entry0.getKey());
                    break;
                }
            }
		return equivalentSpeed;
	}
        
        private double accelerate(){
            double velocityFinal = getSpeedEquivalent(throttlePower);
            
            return 0;
        }
        

	@Override
	public int getSteeringPower() {
		return steeringPower;
	}

	@Override
	public double getTempOrient() {
		return tempOrient;
	}

	@Override
	public int getThrottlePower() {
		return throttlePower;
	}

	@Override
	public VehicleProperty getVehicleProperty() {
		return property;
	}

	@Override
	public int getXDimension() {
		return carDetails.xdimen;
	}

	@Override
	public int getXLocation() {
		return carDetails.xloc;
	}

	@Override
	public int getYDimension() {
		return carDetails.ydimen;
	}

	@Override
	public int getYLocation() {
		return carDetails.yloc;
	}

	@Override
	public boolean isUpdated() {
		return updated;
	}

	@Override
	public boolean isOutOfBounds() {
		return outOfBounds;
	}

	@Override
	public boolean maintainOrientation(double orient, boolean overwrite) {
		maintainOrient = orient;
		if (overwrite) tempOrient = orient;
                
                if(commSched != null)
                    commSched.cmdSetOrientation(carDetails.carID, xAxisCalib, orient, overwrite);
                
                //outputCarStateCSV("Maintain Orientation");
                
		return true;
	}

	@Override
	public boolean maintainOrientationTimed(double orient, double time) {
		tempOrient = orient;
                
                if(commSched != null)
                commSched.cmdSetOrientationTimed(carDetails.carID, xAxisCalib, orient, time);
                //outputCarStateCSV("Timed Orientation");
		return true;
	}

	@Override
	public void printCarAttributes() {
		System.out.println(
				"ID: " + Integer.toHexString(carDetails.carID)
				+ "\tUpdated: " + updated
				+ "\tTimestamp: " + getLastTimeStamp()
				+ "\tX = " + getXLocation() 
				+ "\tY = " + getYLocation()
				+ "\tOrient: " + getOrientation()
				+ "\tOut of bounds: " + isOutOfBounds()
				+ "\tThrottle: " + getThrottlePower()
				+ "\tMaintain: " + getMaintainOrient()
				+ "\tTemp: " + getTempOrient()
		);
	}

	@Override
	public void setAttributesManual(int id, int x, int y, double orien, int xdim, int ydim, double speed) {
		carDetails.carID = id;
		carDetails.xloc = x;
		carDetails.yloc = y;
		carDetails.orient = orien;
		carDetails.xdimen = xdim;
		carDetails.ydimen = ydim;
		this.speed = speed;
	}
        
    FileWriter fwCarState = null;
    String carStatePath = "";
        
	@Override
	public boolean setCSVOutput(String path) {
            
            File filePath = new File(path);
        if(filePath.isDirectory()){
            try{
                carStatePath = path + "\\CarState";
                File carPath = new File(carStatePath);
                carPath.mkdirs();
                
                fwCarState = new FileWriter(carStatePath + "\\0x" + Integer.toHexString(carDetails.carID) + "_CarState.csv");
                initCSVCarStateHeaders();
                return true;
            }catch(Exception e){
                return false;
            }
        }
        else{
            if(verbose)
                System.out.println("Car: ERROR! File path for csv files does not exist");
            return false;
        }
            /*
		if (path == null && console != null && verbose) {
			console.print("Car: ERROR! File path for csv files does not exist");
			return false;
		}
		File csvDir = new File(path);
		if (csvDir.exists() && csvDir.isDirectory()) {
			File coordinatesDirectory = new File(csvDir, "CoordinatesUpdate");
			File carStateDirectory = new File(csvDir, "CarState");
			String id = "0x" + Integer.toHexString(carDetails.carID);
			File coordinatesFile = new File(coordinatesDirectory, id + "_coordinates.csv");
			File carStateFile = new File(carStateDirectory, id + "_carstate.csv");
			try {
				coordinatesWriter = new FileWriter(coordinatesFile);
				writeCoordinateHeader();
				carStateWriter = new FileWriter(carStateFile);
				writeCarStateHeader();
				return true;
			} catch (IOException ex) {
				
			}
			return false;
		}
		
		if (verbose && console != null) console.print("Car: ERROR! File path for csv files does not exist");;
		return false;
            */
	}
        
    private void initCSVCarStateHeaders(){
        try{
        fwCarState.append("Local Time,");
        fwCarState.append("Car ID,");
        fwCarState.append("Source of change,");
        fwCarState.append("Updated,");
        fwCarState.append("Time Stamp,");
        fwCarState.append("X,");
        fwCarState.append("Y,");
        fwCarState.append("Orientation,");
        fwCarState.append("Out of Bounds,");
        fwCarState.append("Throttle Power,");
        fwCarState.append("Speed,");
        fwCarState.append("Maintain Orientation,");
        fwCarState.append("Temp Orientation\n");
        
        fwCarState.flush();
        }catch(Exception e){if(verbose)System.out.println("Car: ERROR! Car State file writer NULL");};
    }

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void setDataHistory(int[] xHist, int[] yHist, double[] orientHist, double[] timeHist) {
		carDetails.xLocHistory = xHist;
		carDetails.yLocHistory = yHist;
		carDetails.orientHistory = orientHist;
		carDetails.timeStampHist = timeHist;
		if (verbose) {
			printHistory();
		}
	}

	@Override
	public void setOutOfBounds(boolean out) {
		outOfBounds = out;
	}

	@Override
	public void setLocation(int x, int y, double time) {
		carDetails.xloc = x;
		carDetails.yloc = y;
	}

	@Override
	public boolean steeringDecrement() {
		return adjustSteering(steeringPower - STEERING_STEP);
	}

	@Override
	public boolean steeringIncrement() {
		return adjustSteering(steeringPower + STEERING_STEP);
	}

	@Override
	public boolean throttleDecrement() {
		if (throttlePower <= SPEED_FLOOR) return adjustThrottle(0);
		return adjustThrottle(throttlePower - THROTTLE_STEP);
	}

	@Override
	public boolean throttleIncrement() {
		if (throttlePower <= 0) return adjustThrottle(SPEED_FLOOR);
		return adjustThrottle(throttlePower + THROTTLE_STEP);
	}

	@Override
	public double timeSinceLastUpdate() {
		return 500;
	}

        private Instant instantSinceUpdate = null;
        private long lastTime = 0;
        
	@Override
	public boolean updateLocation() {
            if(lastTime <= 0){
                lastTime = System.nanoTime();
                return false;
            }
            speed = getSpeedEquivalent(throttlePower);
            //double dt = ((double)(LocalTime.now().minusNanos(lastUpdateTime.toNanoOfDay()).toNanoOfDay())); System.out.println("dt = " + dt);
            //double dt = ((double)(Instant.now().minusMillis(instantSinceUpdate.toEpochMilli()).toEpochMilli())) * 1000; System.out.println("dt = " + dt);
            double dt = (double)(System.nanoTime() - lastTime) / 1000000000;  //System.out.println("dt = " + dt);
            lastTime = System.nanoTime();
            double dx = speed * Math.cos(Math.toRadians(carDetails.orient)) * dt; //System.out.println("dx = " + dx);
            double dy = speed * Math.sin(Math.toRadians(carDetails.orient)) * dt; //System.out.println("dy = " + dy);
            carDetails.xloc += dx;
            carDetails.yloc += dy;
            double dheading = speed / getTurnRadius() * dt; //System.out.println("Turning Radius = " + getTurnRadius());
            double orientDev = maintainOrient - carDetails.orient; //System.out.println("orientDev = " + orientDev);
            if(Math.abs(orientDev) > 180)
                orientDev = 360 - orientDev;

            if(Math.abs(orientDev) > STEERING_WINDOW){
                if(orientDev<0)
                    dheading = -dheading;
            }
            else
                dheading = 0;
            carDetails.orient += Math.toDegrees(dheading); 
            updated = true;  // Set updated variable

            // Add to history
            addOrientHistory(carDetails.orient);
            addXLocationHistory(carDetails.xloc);
            addYLocationHistory(carDetails.yloc);
            try {
                    writeCarState("Valid coor update");
            } catch (IOException ex) {

            }

            outputCarStateCSV("Location Update");
            return updated;
	}
        
        private void outputCarStateCSV(String source){
        try{
        fwCarState.append((LocalTime.now(Clock.systemDefaultZone())).toString() + ",");
        fwCarState.append(Integer.toHexString(getID())+",");
        fwCarState.append(source + ",");
        fwCarState.append(Boolean.toString(isUpdated()) + ",");
        fwCarState.append(Integer.toString((int)getLastTimeStamp()) + ",");
        fwCarState.append(Integer.toString(getXLocation()) + ",");
        fwCarState.append(Integer.toString(getYLocation()) + ",");
        fwCarState.append(Integer.toString((int)getOrientation()) + ",");
        fwCarState.append(Boolean.toString(outOfBounds) + ",");
        fwCarState.append(Integer.toString(getThrottlePower()) + ",");
        fwCarState.append(Double.toString(getSpeed()) + ",");
        fwCarState.append(Integer.toString((int)getMaintainOrient()) + ",");
        fwCarState.append(Integer.toString((int)getTempOrient()) + "\n");
        
        fwCarState.flush();
        
        }catch(Exception e){if(verbose)System.out.println("Car: ERROR! Car State file writer NULL");};
    }
	
	public void outputCSV(String source) {
		if (writer == null) return;
		try {
			String row = Integer.toHexString(getID()) + ","
				+ source + ","
				+ isUpdated() + ","
				+ getXLocation() + ","
				+ getYLocation() + ","
				+ getOrientation() + ","
				+ outOfBounds + ","
				+ getThrottlePower() + ","
				+ getMaintainOrient() + ","
				+ getTempOrient() + "\n";
			writer.append(row);
			writer.flush();
		} catch (Exception e) {
			if (console != null && verbose) console.println("Car ERROR: ID: " + getID() +" Failed to print csv");
		}
	}
	
	public void setFileWriter(FileWriter writer) {
		if (writer == null) {
			if (console != null) console.println("Null file writer");
			return;
		}
		
		this.writer = writer;
		String header = "Car ID,Source of change,Updated,Time Stamp,X,Y,Orientation,Out of Bounds,Throttle Power,Maintain Orientation,Temp Orientation\n";
		try {
			this.writer.append(header);
		} catch (Exception ex) {
			if (console != null && verbose) console.println("File error");
		}
	}
	
	
	//<editor-fold defaultstate="collapsed" desc="Private methods">
	private void writeCoordinateHeader() throws IOException {
		if (coordinatesWriter == null) return;
		String header = "Car ID,Time Stamp,X,Y\n";
		coordinatesWriter.append(header);
		coordinatesWriter.flush();
	}
	
	private void writeCoordinates() throws IOException {
		if (coordinatesWriter == null) return;
		String row = Integer.toHexString(carDetails.carID) + ","
				+ LocalTime.now() + ","
				+ carDetails.xloc + ","
				+ carDetails.yloc;
		coordinatesWriter.append(row);
		coordinatesWriter.flush();
	}
	
	private void writeCarStateHeader() throws IOException {
		if (carStateWriter == null) return;
		String header = "Local Time,Car ID,Source of change,Updated,Time Stamp,X,Y,Orientation,Out of Bounds,Throttle Power,Speed,Maintain Orientation,Temp Orientation\n";
		carStateWriter.append(header);
		carStateWriter.flush();
	}
	
	private void writeCarState(String source) throws IOException {
		if (carStateWriter == null) return;
		String row = LocalTime.now(Clock.systemDefaultZone()) + "," 
				+ Integer.toHexString(getID()) + "," 
				+ source + ","
				+ isUpdated() + ","
				+ getLastTimeStamp() + ","
				+ getXLocation() + ","
				+ getYLocation() + ","
				+ getOrientation() + ","
				+ outOfBounds + ","
				+ getThrottlePower() + ","
				+ getSpeed() + ","
				+ getMaintainOrient() + ","
				+ getTempOrient() + "\n";
		carStateWriter.append(row);
		carStateWriter.flush();
	}
	
	private int[] toIntArray(Collection<Integer> c) {
		int[] arr = new int[c.size()];
		int i = 0;
		for (Integer x : c) {
			arr[i] = (x != null) ? x : 0;
			i++;
		}
		return arr;
	}
	
	private double[] toDoubleArray(Collection<Double> c) {
		double[] arr = new double[c.size()];
		int i = 0;
		for (Double x : c) {
			arr[i] = x;
		}
		return arr;
	}
	
	private void addXLocationHistory(int xloc) {
		if (xLocationHistoryQueue.size() >= carDetails.xLocHistory.length) xLocationHistoryQueue.pollLast();
		xLocationHistoryQueue.offerFirst(xloc);
		carDetails.xLocHistory = toIntArray(xLocationHistoryQueue);
	}
	
	private void addYLocationHistory(int yloc) {
		if (yLocationHistoryQueue.size() >= carDetails.yLocHistory.length) yLocationHistoryQueue.pollLast();
		yLocationHistoryQueue.offerFirst(yloc);
		carDetails.yLocHistory = toIntArray(yLocationHistoryQueue);
	}
	
	private double getTurnRadius() {
		//return property.getMinimumTurnRadius();
                return 320;
	}
	
	private void addOrientHistory(double orientation) {
		if (orientHistoryQueue.size() >= carDetails.orientHistory.length) orientHistoryQueue.pollLast();
		orientHistoryQueue.offerFirst(orientation);
		carDetails.orientHistory = toDoubleArray(orientHistoryQueue);
	}
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Init methods">
	private void initCarDetails(int id, int timeHistorySize, int xLocationHistorySize, int yLocationHistorySize, int orientHistorySize, int routeDirectionsSize) {
		carDetails = new CarDetails();
                carDetails.carID = id;
		carDetails.orientHistory = new double[orientHistorySize];
		carDetails.timeStampHist = new double[timeHistorySize];
		carDetails.xLocHistory = new int[xLocationHistorySize];
		carDetails.yLocHistory = new int[yLocationHistorySize];
		carDetails.routeDirections = new int[routeDirectionsSize];
		carDetails.xdimen = DEFAULT_XDim;
		carDetails.ydimen = DEFAULT_YDim;
		property = new VehicleProperty(carDetails.xdimen, carDetails.ydimen, carDetails.xdimen * 0.9, 100, 10, Math.toRadians(15));
	}
	
	private void initThrottleToSpeedTable() {
		int[] throttleColumn = new int[] {
			0, 32, 64, 96, 127
		};
		double[] speedColumn = new double[] {
			0, 0, 830, 1480, 1900
		};
		throttleToSpeedTable = new LinkedHashMap<>(throttleColumn.length);
		for (int i = 0; i < throttleColumn.length; i++) {
			throttleToSpeedTable.put(throttleColumn[i], speedColumn[i]);
		}
	}
        public void initThrottleToSpeedTable(int[] throttleColumn, double[] speedColumn){
            
            throttleToSpeedTable = new LinkedHashMap<>(throttleColumn.length);
		for (int i = 0; i < throttleColumn.length; i++) {
			throttleToSpeedTable.put(throttleColumn[i], speedColumn[i]);
		}
        }
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Print / Debug methods">
	private void printHistory() {
		{
			console.println("x History: " + Arrays.toString(carDetails.xLocHistory));
			console.println("y History: " + Arrays.toString(carDetails.yLocHistory));
			console.println("Orientation history: " + Arrays.toString(carDetails.orientHistory));
			console.println("Time history: " + Arrays.toString(carDetails.timeStampHist));
		}
	}
	//</editor-fold>
	
	private CarDetails carDetails = new CarDetails();
	private VehicleProperty property;
	private PrintStream console;
	// Car states
	private int steeringPower;
	private int throttlePower;
	private double speed;
	private double maintainOrient;
	private double tempOrient;
	private boolean updated;
	private boolean outOfBounds;
        private double xAxisCalib = 0;
	private boolean verbose;
	private Map<Integer, Double> throttleToSpeedTable;
	private Deque<Integer> xLocationHistoryQueue;
	private Deque<Integer> yLocationHistoryQueue;
	private Deque<Double> orientHistoryQueue;
	private Deque<Double> timeHistoryQueue;
	private Deque<Integer> routeQueue;
        private LocalTime lastUpdateTime = null;
	// File outputs
	private FileWriter coordinatesWriter;
	private FileWriter carStateWriter;
	private FileWriter writer;
	
	
	/**
	 * Speed limit.
	 */
	public static final int SPEED_LIMIT = 80;
	/**
	 * Speed floor. Lower speed limit.
	 */
	public static final int SPEED_FLOOR = 50;
	/**
	 * Throttle step.
	 */
	public static final int THROTTLE_STEP = (SPEED_LIMIT - SPEED_FLOOR) / 5;
	/**
	 * Steering step.
	 */
	public static final int STEERING_STEP = 10;
        
        public static final int STEERING_WINDOW = 2;
        
        private CommMessageScheduler commSched = null;
        public void setCommMessageScheduler(CommMessageScheduler commSched){
            this.commSched = commSched;
        }
        public CommMessageScheduler getCommMessageScheduler(){
            return commSched;
        }

}
