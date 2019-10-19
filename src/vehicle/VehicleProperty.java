/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicle;

/**
 * A class containing the properties of the vehicle. These properties include length, 
 * width, wheelbase--the distance between the front and rear axles, maximum speed, 
 * maximum acceleration, and maximum steering angle.
 * @author Ocampo
 */
public class VehicleProperty {
	private final double length;  // scaled cm
	private final double width;  // scaled cm
	private final double wheelbase;  // scaled cm
	private final double maxSpeed;  // in scaled cm/s
	private final double maxAcceleration;  // in scaled cm/s
	private final double maxSteeringAngle;  // Radians

	/**
	 * Construct vehicle properties.
	 * @param length Vehicle length in scaled cm
	 * @param width Vehicle width in scaled cm.
	 * @param wheelbase Vehicle wheelbase in scaled cm. It is the distance between 
	 *					the axles.
	 * @param maxSpeed Maximum speed in scaled cm/s.
	 * @param maxAcceleration Maximum acceleration in scaled cm/s/s.
	 * @param maxSteeringAngle Maximum steering angle in radians.
	 */
	public VehicleProperty(double length, double width, double wheelbase, double maxSpeed, double maxAcceleration, double maxSteeringAngle) {
		this.length = length;
		this.width = width;
		this.wheelbase = wheelbase;
		this.maxSpeed = maxSpeed;
		this.maxAcceleration = maxAcceleration;
		this.maxSteeringAngle = maxSteeringAngle;
	}

	/**
	 * Returns the vehicle length.
	 * @return Vehicle length.
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the vehicle width.
	 * @return Vehicle width.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Returns the vehicle's wheelbase. The wheelbase is the distance between the 
	 * axles.
	 * @return Vehicle's wheelbase.
	 */
	public double getWheelbase() {
		return wheelbase;
	}

	/**
	 * Returns the maximum speed of the vehicle.
	 * @return Vehicle's maximum speed.
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Returns the maximum acceleration of the vehicle.
	 * @return Vehicle's maximum acceleration.
	 */
	public double getMaxAcceleration() {
		return maxAcceleration;
	}

	/**
	 * Returns the maximum steering angle of the vehicle. The steering angle is 
	 * measured as the angle from the vehicle's lateral axis to the wheel's lateral 
	 * axis.
	 * @return Vehicle's maximum steering angle.
	 */
	public double getMaxSteeringAngle() {
		return maxSteeringAngle;
	}
	
	/**
	 * Returns the turn radius of the vehicle when the wheels are set to the maximum 
	 * steering angle. The turn radius is minimum at that angle. It returns {@code NaN} 
	 * when steering angle is 0.
	 * @return Turn radius of the vehicle.
	 */
	public double getMinimumTurnRadius() {
		if (maxSteeringAngle == 0) return Double.NaN;
		return Math.sqrt(Math.pow(1.0 / Math.sin(maxSteeringAngle), 2) - 1.0/4.0) * wheelbase;
	}
	
}
