/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

/**
 * An interface containing constants for the simulation.
 * @author Ocampo
 */
public interface SimulationConstants {
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
}
