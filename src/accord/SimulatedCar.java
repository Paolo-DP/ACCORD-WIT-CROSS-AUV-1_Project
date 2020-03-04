
package accord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vehicle.VehicleProperty;

/**
 *
 * @author Ocampo
 */
public class SimulatedCar implements Car {

	public SimulatedCar() {
		this(System.out);
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
		initCarDetails(10, 10, 10, 10, 16);
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
		return true;
	}

	@Override
	public CarDetails getFullDetails() {
		return carDetails;
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
		return throttleToSpeedTable.getOrDefault(throttle, -1.0);
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
		return true;
	}

	@Override
	public boolean maintainOrientationTimed(double orient, double time) {
		tempOrient = orient;
		return true;
	}

	@Override
	public void printCarAttributes() {
		console.println(
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

	@Override
	public boolean setCSVOutput(String path) {
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

	@Override
	public boolean updateLocation() {
		double dt = timeSinceLastUpdate();
		double dx = speed * Math.cos(carDetails.orient) * dt;
		double dy = speed * Math.sin(carDetails.orient) * dt;
		carDetails.xloc += dx;
		carDetails.yloc += dy;
		double dheading = speed / getTurnRadius() * dt;
		carDetails.orient += dheading;
		updated = true;  // Set updated variable
		// Add to history
		addOrientHistory(carDetails.orient);
		addXLocationHistory(carDetails.xloc);
		addYLocationHistory(carDetails.yloc);
		try {
			writeCarState("Valid coor update");
		} catch (IOException ex) {
			
		}
		return updated;
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
		return property.getMinimumTurnRadius();
	}
	
	private void addOrientHistory(double orientation) {
		if (orientHistoryQueue.size() >= carDetails.orientHistory.length) orientHistoryQueue.pollLast();
		orientHistoryQueue.offerFirst(orientation);
		carDetails.orientHistory = toDoubleArray(orientHistoryQueue);
	}
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Init methods">
	private void initCarDetails(int timeHistorySize, int xLocationHistorySize, int yLocationHistorySize, int orientHistorySize, int routeDirectionsSize) {
		carDetails = new CarDetails();
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
	
	private CarDetails carDetails;
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
	private boolean verbose;
	private Map<Integer, Double> throttleToSpeedTable;
	private Deque<Integer> xLocationHistoryQueue;
	private Deque<Integer> yLocationHistoryQueue;
	private Deque<Double> orientHistoryQueue;
	private Deque<Double> timeHistoryQueue;
	private Deque<Integer> routeQueue;
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
	public static final int SPEED_FLOOR = 30;
	/**
	 * Throttle step.
	 */
	public static final int THROTTLE_STEP = (SPEED_LIMIT - SPEED_FLOOR) / 5;
	/**
	 * Steering step.
	 */
	public static final int STEERING_STEP = 10;

}
