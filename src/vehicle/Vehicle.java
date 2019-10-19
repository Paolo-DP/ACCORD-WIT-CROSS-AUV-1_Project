
package vehicle;

import geometry.GridLine;
import geometry.GridPoint;
import java.util.Random;
import math.ExtendedMath;

/**
 * A simulated vehicle. The vehicle was modeled with the center of mass (COM) in 
 * the center of the vehicle. However, this could be inaccurate as most vehicles 
 * do not have their COMs in their centers.
 * <br><br>
 * The vehicle supports buffers to allow room for errors in the sensor readings 
 * of the vehicle. The heading of the vehicle is using clockwise standard angle 
 * units, in line with the inverted xy-coordinate system of most Java graphics 
 * API, such as swing.
 * @author Ocampo
 */
public class Vehicle {
	
	private VehicleProperty property;  // Properties of the vehicle.
	// States
	private double speed;  // Speed in scaled cm/s
	private double acceleration;  // Acceleration in scaled cm/s/s
	private GridPoint position;  // Vehicle position in x,y
	private double steeringAngle;  // in radians
	private double heading;  // clockwise-standard. 0 means east. Radian units.
	// Buffers
	private double frontStaticBuffer;
	private double rearStaticBuffer;
	private double leftStaticBuffer;
	private double rightStaticBuffer;
	private int id;

	/**
	 * Constructs a vehicle.
	 * @param property Vehicle properties.
	 */
	public Vehicle(VehicleProperty property) {
		this(property, Math.abs(new Random().nextInt()));
	}

	/**
	 * Constructs a vehicle.
	 * @param property Vehicle properties.
	 * @param id ID of the vehicle.
	 * @throws IllegalArgumentException if {@code id < 0}.
	 */
	public Vehicle(VehicleProperty property, int id) {
		checkId(id);
		this.property = property;
		this.id = id;
		speed = 0;
		acceleration = 0;
		position = new GridPoint();
		steeringAngle = 0;
		heading = 0;
		// Buffer initialization
		frontStaticBuffer = 0;
		rearStaticBuffer = 0;
		leftStaticBuffer = 0;
		rightStaticBuffer = 0;
	}
	
	
	
	// <editor-fold desc="Property and state getters and setters." defaultstate="collapsed">

	/**
	 * Returns the ID of the vehicle.
	 * @return Vehicle ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the properties of the vehicle.
	 * @return Vehicle properties.
	 */
	public VehicleProperty getProperty() {
		return property;
	}

	/**
	 * Returns the speed of the vehicle.
	 * @return Speed of the vehicle.
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Returns the acceleration of the vehicle.
	 * @return Acceleration of the vehicle.
	 */
	public double getAcceleration() {
		return acceleration;
	}

	/**
	 * Returns the position of the vehicle.
	 * @return Position of the vehicle.
	 */
	public GridPoint getPosition() {
		return position;
	}

	/**
	 * Returns the steering angle of the vehicle.
	 * @return Steering angle of the vehicle.
	 */
	public double getSteeringAngle() {
		return steeringAngle;
	}

	/**
	 * Returns the heading of the vehicle. The heading uses the clockwise standard 
	 * angle units. The angle unit is in radians. 0 means heading east.
	 * @return Heading of the vehicle in clockwise standard radians.
	 */
	public double getHeading() {
		return heading;
	}

	/**
	 * Returns the front static buffer of the vehicle.
	 * @return Front static buffer.
	 */
	public double getFrontStaticBuffer() {
		return frontStaticBuffer;
	}

	/**
	 * Returns the rear static buffer of the vehicle.
	 * @return Rear static buffer.
	 */
	public double getRearStaticBuffer() {
		return rearStaticBuffer;
	}

	/**
	 * Returns the left static buffer of the vehicle.
	 * @return Left static buffer.
	 */
	public double getLeftStaticBuffer() {
		return leftStaticBuffer;
	}

	/**
	 * Returns the right static buffer of the vehicle.
	 * @return Right static buffer.
	 */
	public double getRightStaticBuffer() {
		return rightStaticBuffer;
	}

	/**
	 * Sets the vehicle's speed.
	 * @param speed Vehicle's speed.
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Sets the vehicle's acceleration.
	 * @param acceleration Vehicle's acceleration.
	 */
	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * Sets the vehicle position.
	 * @param position Vehicle position.
	 */
	public void setPosition(GridPoint position) {
		this.position = position;
	}

	/**
	 * Sets the vehicle's steering angle. The steering angle is the angle of the 
	 * wheels with respect to the vehicle's lateral axis.
	 * @param steeringAngle Vehicle's steering angle.
	 */
	public void setSteeringAngle(double steeringAngle) {
		this.steeringAngle = steeringAngle;
	}

	/**
	 * Sets the heading of the vehicle. The heading uses the clockwise standard 
	 * angle units. The angle unit is in radians. 0 means heading east.
	 * @param heading Heading of the vehicle in clockwise standard radians.
	 */
	public void setHeading(double heading) {
		this.heading = heading;
	}

	/**
	 * Sets the front static buffer of the vehicle.
	 * @param frontStaticBuffer Vehicle's front static buffer.
	 */
	public void setFrontStaticBuffer(double frontStaticBuffer) {
		this.frontStaticBuffer = frontStaticBuffer;
	}

	/**
	 * Sets the rear static buffer of the vehicle.
	 * @param rearStaticBuffer Vehicle's rear static buffer.
	 */
	public void setRearStaticBuffer(double rearStaticBuffer) {
		this.rearStaticBuffer = rearStaticBuffer;
	}

	/**
	 * Sets the left static buffer of the vehicle.
	 * @param leftStaticBuffer Vehicle's left static buffer.
	 */
	public void setLeftStaticBuffer(double leftStaticBuffer) {
		this.leftStaticBuffer = leftStaticBuffer;
	}

	/**
	 * Sets the right static buffer of the vehicle.
	 * @param rightStaticBuffer Vehicle's right static buffer.
	 */
	public void setRightStaticBuffer(double rightStaticBuffer) {
		this.rightStaticBuffer = rightStaticBuffer;
	}
	
	/**
	 * Returns the theoretical turn radius of the vehicle, using the center as 
	 * the center of mass model. The turn radius depends on the vehicle's steering 
	 * angle. It returns {@code NaN} when the steering angle is 0.
	 * @return Theoretical turn radius of the vehicle.
	 */
	public double getTurnRadius() {
		return computeTurnRadius();
	}
	
	// </editor-fold>
	
	// <editor-fold desc="Private methods" defaultstate="collapsed">
	private double computeTurnRadius() {
		if (steeringAngle == 0) return Double.NaN;
		return Math.sqrt(Math.pow(1.0 / Math.sin(steeringAngle), 2) - 1.0/4.0) * property.getWheelbase();
	}
	
	private double computeRotationalVelocity() {
		double turnRadius = computeTurnRadius();
		if (turnRadius == Double.NaN) return Double.NaN;
		return speed / turnRadius;
	}
	
	private double computeRotationalAcceleration() {
		double turnRadius = computeTurnRadius();
		if (turnRadius == Double.NaN) return Double.NaN;
		return acceleration / turnRadius;
	}
	
	private GridPoint[] getCorners() {
		double[] xCorners = new double[]{
			property.getLength()/2 + frontStaticBuffer,
			-property.getLength()/2 - rearStaticBuffer,
			-property.getLength()/2 - rearStaticBuffer,
			property.getLength()/2 + frontStaticBuffer
		};
		double[] yCorners = new double[]{
			property.getWidth()/2 + leftStaticBuffer,
			property.getWidth()/2 + leftStaticBuffer,
			-property.getWidth()/2 - rightStaticBuffer,
			-property.getWidth()/2 - rightStaticBuffer
		};
		// Construct and initialize grid points
		GridPoint[] corners = new GridPoint[xCorners.length];  // X and y Corners must have the same size.
		for (int i = 0; i < xCorners.length; i++) {
			GridPoint pt = new GridPoint(xCorners[i], yCorners[i]);
			pt.rotate(heading);
			pt.translate(position.x, position.y);
			corners[i] = pt;
		}
		return corners;
	}
	
	private GridLine[] getBoundaries() {
		GridPoint[] corners = getCorners();
		GridLine[] boundaries = new GridLine[corners.length];
		for (int i = 0; i < corners.length; i++) {
			int iNext = (i + 1) % corners.length;
			boundaries[i] = new GridLine(corners[i], corners[iNext]);
		}
		return boundaries;
	}
	
	private void checkId(int id) {
		if (id < 0) throw new IllegalArgumentException("Invalid vehicle ID " + id);
	}
	// </editor-fold>
	
	/**
	 * Returns the bounds of the vehicle.
	 * @return Vehicle bounds.
	 */
	public VehicleBounds getBounds() {
		return new VehicleBounds(getBoundaries(), getCorners());
	}
	
	/**
	 * Moves the vehicle at a time interval. Vehicle states, such as speed, position, 
	 * and heading are updated.
	 * @param dt Time interval of the vehicle movement.
	 */
	public void move(double dt) {
		double vx = speed * Math.cos(heading);
		double vy = speed * Math.sin(heading);
		double dx = vx * dt;
		double dy = vy * dt;
		double dv = acceleration * dt;
		double dHeading = Math.signum(steeringAngle) * computeRotationalVelocity() * dt;
		// Update values
		position.translate(dx, dy);
		speed += dv;
		if (steeringAngle != 0) heading = ExtendedMath.mod(heading + dHeading, 2*Math.PI);
	}
	
}
