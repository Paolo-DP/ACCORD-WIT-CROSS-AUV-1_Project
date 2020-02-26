/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.tileretrieval;

import geometry.GridPoint;
import java.time.LocalTime;
import math.ExtendedMath;
import reservation.tiles.TileRetriever;
import reservation.tiles.TileTimes;
import tiles.Intersection;
import tiles.TileSet;
import vehicle.Heading;
import vehicle.Vehicle;
import vehicle.VehicleProperty;

/**
 * Tile retrieval system on the simulator. It retrieves the tiles occupied by the 
 * vehicle on a specific time. It does this by mapping a time stamp to a specific 
 * set of tiles.
 * @author Ocampo
 */
public class SimTileRetriever {
	
	private LocalTime arrivalTime;  // Arrival time
	private double speed;  // Speed in scaled cm/s.
	private double acceleration;  // Acceleration in scaled cm/s/s
	private VehicleProperty vehicleProperty;  // Properties of the vehicle in simulation
	private Intersection intersection;  // Intersection.
	private int direction;  // Direction of travel: Straight, Left, Right
	private int heading;  // Heading: North, South, East, or West.
	// Other values
	private GridPoint[] startPoints;
	private double[] vehicleHeadings;
	private int vehicleId;
	private int timeBaseNs;
	
	// Headings
	/**
	 * Northward heading.
	 */
	public static final int NORTH = 0;
	/**
	 * Eastward heading.
	 */
	public static final int EAST = 1;
	/**
	 * Westward heading.
	 */
	public static final int WEST = 2;
	/**
	 * Southward heading.
	 */
	public static final int SOUTH = 3;
	
	// Directions
	/**
	 * Straight through direction.
	 */
	public static final int STRAIGHT = -1;
	/**
	 * Right turn direction.
	 */
	public static final int RIGHT_TURN = -2;
	/**
	 * Left turn direction.
	 */
	public static final int LEFT_TURN = -3;

	/**
	 * Constructs a tile retrieval system for the simulator.
	 * @param arrivalTime Arrival time of the vehicle at the intersection.
	 * @param speed Initial speed of the vehicle in the intersection.
	 * @param acceleration Acceleration of the vehicle in the intersection.
	 * @param vehicleProperty Properties of the vehicle.
	 * @param intersection The intersection to simulate.
	 * @param direction Direction of the vehicle. It can either be {@code STRAIGHT}, {@code LEFT_TURN}, or {@code RIGHT_TURN}.
	 * @param heading Heading of the vehicle. It can be either {@code NORTH}, {@code SOUTH}, {@code EAST}, or {@code WEST}.
	 * @param vehicleId Vehicle ID
	 * @param timeBaseNs Time base in nanoseconds. This is the time interval between each sample of occupied tiles.
	 * @throws IllegalArgumentException if the heading or direction is not supported.
	 */
	public SimTileRetriever(LocalTime arrivalTime, double speed, double acceleration, VehicleProperty vehicleProperty, Intersection intersection, int direction, int heading, int vehicleId, int timeBaseNs) {
		checkDirection(direction);
		checkHeading(heading);
		this.arrivalTime = arrivalTime;
		this.speed = speed;
		this.acceleration = acceleration;
		this.vehicleProperty = vehicleProperty;
		this.direction = direction;
		this.heading = heading;
		this.vehicleId = vehicleId;
		this.timeBaseNs = timeBaseNs;
		initValues(intersection);
	}

	/**
	 * Constructs a tile retrieval system for the simulator.
	 * @param arrivalTime Arrival time of the vehicle at the intersection.
	 * @param speed Initial speed of the vehicle in the intersection.
	 * @param acceleration Acceleration of the vehicle in the intersection.
	 * @param vehicleProperty Properties of the vehicle.
	 * @param intersection The intersection to simulate.
	 * @param vehicleId Vehicle ID
	 * @param timeBaseNs Time base in nanoseconds. This is the time interval between each sample of occupied tiles.
	 * @throws IllegalArgumentException if the heading or direction is not supported.
	 */
	public SimTileRetriever(LocalTime arrivalTime, double speed, double acceleration, VehicleProperty vehicleProperty, Intersection intersection, int vehicleId, int timeBaseNs) {
		this(arrivalTime, speed, acceleration, vehicleProperty, intersection, STRAIGHT, NORTH, vehicleId, timeBaseNs);
	}
	
	// <editor-fold defaultstate="collapsed" desc=" Property getters and setters ">

	/**
	 * Returns the arrival time of the vehicle at the intersection.
	 * @return Arrival time of the vehicle.
	 */
	public LocalTime getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Returns the speed of the vehicle in the intersection. If the vehicle is 
	 * accelerating, it is the initial speed.
	 * @return Speed of the vehicle in the intersection.
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Returns the acceleration of the vehicle in the intersection.
	 * @return Acceleration of the vehicle in the intersection
	 */
	public double getAcceleration() {
		return acceleration;
	}

	/**
	 * Returns the properties of the vehicle to be simulated.
	 * @return Properties of the vehicle to be simulated.
	 */
	public VehicleProperty getVehicleProperty() {
		return vehicleProperty;
	}

	/**
	 * Returns the intersection to be simulated.
	 * @return The intersection to be simulated.
	 */
	public Intersection getIntersection() {
		return intersection;
	}

	/**
	 * Returns the direction of the vehicle to travel. It can either be {@code STRAIGHT}, {@code LEFT_TURN}, or {@code RIGHT_TURN}.
	 * @return Direction of the vehicle to travel.
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Returns the heading of the vehicle. It can be either {@code NORTH}, {@code SOUTH}, {@code EAST}, or {@code WEST}.
	 * @return Heading of the vehicle.
	 */
	public int getHeading() {
		return heading;
	}

	/**
	 * Returns the ID of the vehicle to be simulated.
	 * @return ID of the vehicle to be simulated.
	 */
	public int getVehicleId() {
		return vehicleId;
	}

	/**
	 * Returns the time base of the simulator. This is the time interval between 
	 * each sample of occupied tiles. The time is in nanoseconds.
	 * @return The Simulator time base in nanoseconds.
	 */
	public int getTimeBaseNs() {
		return timeBaseNs;
	}

	/**
	 * Sets the vehicle arrival time in the intersection.
	 * @param arrivalTime Vehicle arrival time in the intersection.
	 */
	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Sets the speed of the vehicle in the intersection. If the vehicle is 
	 * accelerating, it is the initial velocity.
	 * @param speed Speed of the vehicle in the intersection.
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Set the acceleration of the vehicle in the intersection.
	 * @param acceleration Acceleration of the vehicle in the intersection.
	 */
	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * Sets the properties of the vehicle to be simulated.
	 * @param vehicleProperty Properties of the vehicle to be simulated.
	 */
	public void setVehicleProperty(VehicleProperty vehicleProperty) {
		this.vehicleProperty = vehicleProperty;
	}

	/**
	 * Sets the direction of the vehicle on the intersection. It can either be 
	 * {@code STRAIGHT}, {@code LEFT_TURN}, or {@code RIGHT_TURN}.
	 * @param direction Direction of the vehicle on the intersection
	 */
	public void setDirection(int direction) {
		checkDirection(direction);
		this.direction = direction;
	}

	public void setHeading(int heading) {
		checkHeading(heading);
		this.heading = heading;
	}

	/**
	 * Sets the ID of the vehicle to be simulated.
	 * @param vehicleId ID of the vehicle to be simulated.
	 */
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	/**
	 * Sets the time base of the simulator. This is the time interval between 
	 * each sample of occupied tiles. The time is in nanoseconds.
	 * @param timeBaseNs Time base of the simulator in nanoseconds.
	 */
	public void setTimeBaseNs(int timeBaseNs) {
		this.timeBaseNs = timeBaseNs;
	}

	/**
	 * Sets the intersection to be simulated.
	 * @param intersection Intersection to be simulated.
	 */
	public void setIntersection(Intersection intersection) {
		initValues(intersection);
	}
	
	// </editor-fold>
	
	/**
	 * Simulates the path of the vehicles and returns the set of occupied tiles 
	 * on the appropriate time.
	 * @return A set of tiles for each time instants.
	 */
	public TileTimes simulate() {
		if (direction == LEFT_TURN) return leftTurnSimulate();
		if (direction == RIGHT_TURN) return rightTurnSimulate();
		return straightSimulate();
	}
	
	// <editor-fold defaultstate="collapsed" desc=" Private Methods ">
	
	private void initValues(Intersection intersection) {
		this.intersection = intersection;
		double length = intersection.getLength();
		startPoints = new GridPoint[]{
			new GridPoint(3*length/4, length),
			new GridPoint(0, 3*length/4),
			new GridPoint(length, length/4),
			new GridPoint(length/4, 0)
		};
		vehicleHeadings = new double[]{
			Math.toRadians(270), 
			Math.toRadians(0), 
			Math.toRadians(180), 
			Math.toRadians(90)
		};
	}
	
	private TileTimes straightSimulate() {
		// Initialize vehicle
		Vehicle vehicle = new Vehicle(vehicleProperty, vehicleId);
		vehicle.setHeading(vehicleHeadings[heading]);  // Set heading according to direction.
		vehicle.setPosition(startPoints[heading]);  // Set starting point according to direction.
		vehicle.setSpeed(speed);  // Set vehicle speed
		vehicle.setAcceleration(acceleration);  // Set vehicle acceleration.
		double dt = (double) timeBaseNs / Math.pow(10, 9);  // Time base in seconds
		// Initialize the tile retrieval and mapping system
		TileRetriever tileRetriever = new TileRetriever(intersection);
		TileTimes tileTimes = new TileTimes();
		LocalTime currentTime = arrivalTime.withSecond(arrivalTime.getSecond());
		// Some values
		double targetSpeed = vehicle.getProperty().getMaxSpeed();
		// Movement loop
		while (intersection.isInside(vehicle.getPosition())) {
			TileSet occupiedTiles = tileRetriever.getOccupiedTiles(vehicle, intersection.getTileLength() / 64);
			tileTimes.put(currentTime, occupiedTiles);
			currentTime = currentTime.plusNanos(timeBaseNs);
//			System.out.println(currentTime);  // Debug code.
			// Vehicle movement
			// Acceleration monitoring.
			if (vehicle.getSpeed() >= targetSpeed) {
				vehicle.setSpeed(targetSpeed);
				vehicle.setAcceleration(0);
			}
			vehicle.move(dt);
		}
		return tileTimes;
	}
	
	private TileTimes rightTurnSimulate() {
		// Initialize vehicle
		Vehicle vehicle = new Vehicle(vehicleProperty, vehicleId);
		vehicle.setHeading(vehicleHeadings[heading]);  // Set heading according to direction.
		vehicle.setPosition(startPoints[heading]);  // Set starting point according to direction.
		vehicle.setSteeringAngle(vehicle.getProperty().getMaxSteeringAngle());
		vehicle.setSpeed(speed);  // Set vehicle speed
		vehicle.setAcceleration(acceleration);  // Set vehicle acceleration.
		double dt = (double) timeBaseNs / Math.pow(10, 9);  // Time base in seconds
		// Initialize the tile retrieval and mapping system
		TileRetriever tileRetriever = new TileRetriever(intersection);
		TileTimes tileTimes = new TileTimes();
		LocalTime currentTime = arrivalTime.withSecond(arrivalTime.getSecond());
		// Some values
		double targetSpeed = vehicle.getProperty().getMaxSpeed();
		// Parameters for turning the vehicle
		double initialHeading = vehicle.getHeading();
		double finalHeading = ExtendedMath.mod(initialHeading + Math.toRadians(90), Math.toRadians(360));
		// Movement loop
		while (intersection.isInside(vehicle.getPosition())) {
			TileSet occupiedTiles = tileRetriever.getOccupiedTiles(vehicle, intersection.getTileLength() / 64);
			tileTimes.put(currentTime, occupiedTiles);
			currentTime = currentTime.plusNanos(timeBaseNs);
			// Vehicle movement
			// Acceleration monitoring.
			if (vehicle.getSpeed() >= targetSpeed) {
				vehicle.setSpeed(targetSpeed);
				vehicle.setAcceleration(0);
			}
			// Turn monitoring
			double relativeHeading = ExtendedMath.mod(vehicle.getHeading() - initialHeading, Math.toRadians(360));  // (heading - initial) mod 2*pi
			if (relativeHeading >= Math.toRadians(90) && vehicle.getSteeringAngle() != 0) {
				vehicle.setSteeringAngle(0);
				vehicle.setHeading(finalHeading);
			}
			vehicle.move(dt);
		}
		return tileTimes;
	}
	
	private TileTimes leftTurnSimulate() {
		// Initialize vehicle
		Vehicle vehicle = new Vehicle(vehicleProperty, vehicleId);
		vehicle.setHeading(vehicleHeadings[heading]);  // Set heading according to direction.
		vehicle.setPosition(startPoints[heading]);  // Set starting point according to direction.
		vehicle.setSpeed(speed);  // Set vehicle speed
		vehicle.setAcceleration(acceleration);  // Set vehicle acceleration.
		double dt = (double) timeBaseNs / Math.pow(10, 9);  // Time base in seconds
		// Initialize the tile retrieval and mapping system
		TileRetriever tileRetriever = new TileRetriever(intersection);
		TileTimes tileTimes = new TileTimes();
		LocalTime currentTime = arrivalTime.withSecond(arrivalTime.getSecond());
		// Some values
		double targetSpeed = vehicle.getProperty().getMaxSpeed();  // Target speed
		GridPoint startPoint = new GridPoint(vehicle.getPosition().x, vehicle.getPosition().y);  // Start point.
		// Parameters for turning the vehicle
		double initialHeading = Heading.reverse(vehicle.getHeading());
		double finalHeading = ExtendedMath.mod(Heading.reverse(initialHeading) + Math.toRadians(90), Math.toRadians(360));
		double steeringAngle = 0;  // Steering angle to set.
		// Movement loop
		while (intersection.isInside(vehicle.getPosition())) {
			TileSet occupiedTiles = tileRetriever.getOccupiedTiles(vehicle, intersection.getTileLength() / 64);
			tileTimes.put(currentTime, occupiedTiles);
			currentTime = currentTime.plusNanos(timeBaseNs);
			// Vehicle movement
			// Acceleration monitoring.
			if (vehicle.getSpeed() >= targetSpeed) {
				vehicle.setSpeed(targetSpeed);
				vehicle.setAcceleration(0);
			}
			// Distance monitoring for the left turn
			GridPoint position = vehicle.getPosition();
			// Check if the vehicle is halfway the intersection.
			if (position.distance(startPoint) >= intersection.getLength() / 2) {
				steeringAngle = -vehicle.getProperty().getMaxSteeringAngle(); // Set steering angle to max left.
			}
			// Check if vehicle has made a left turn (90 degrees).
			double relativeHeading = ExtendedMath.mod(Heading.reverse(vehicle.getHeading()) - initialHeading, Math.toRadians(360));
			if (relativeHeading >= Math.toRadians(90)) {
				steeringAngle = 0;
				vehicle.setHeading(finalHeading);
			}
			// Set vehicle steering angle
			vehicle.setSteeringAngle(steeringAngle);
			// Move vehicle.
			vehicle.move(dt);
		}
		return tileTimes;
	}
	
	private void checkDirection(int direction) {
		if (!(direction == STRAIGHT || direction == RIGHT_TURN || direction == LEFT_TURN))
			throw new IllegalArgumentException("Invalid direction.");
	}
	
	private void checkHeading(int heading) {
		if (!(heading == NORTH || heading == SOUTH || heading == WEST || heading == EAST))
			throw new IllegalArgumentException("Invalid direction.");
	}
	
	// </editor-fold>
	
}
