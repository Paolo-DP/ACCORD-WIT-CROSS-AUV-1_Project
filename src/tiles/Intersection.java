/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tiles;

import geometry.GridPoint;

/**
 * A model for the intersection. The intersection is divided into an n X n grid
 * of tiles.
 *
 * @author Ocampo
 */
public class Intersection {

	private final double length;
	private final int resolution;
	private final double tileLength;

	/**
	 * Construct an intersection. The intersection is divided into n X n tiles,
	 * where n is the resolution.
	 *
	 * @param length Intersection side length.
	 * @param resolution Resolution of the intersection (n).
	 */
	public Intersection(double length, int resolution) {
		this.length = length;
		this.resolution = resolution;
		tileLength = length / resolution;
	}

	/**
	 * Returns the intersection side length.
	 *
	 * @return Intersection side length.
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the intersection resolution, that is, the number of tiles on one
	 * side.
	 *
	 * @return Intersection resolution.
	 */
	public int getResolution() {
		return resolution;
	}

	/**
	 * Returns the intersection's tile side length.
	 *
	 * @return Intersection's tile side length.
	 */
	public double getTileLength() {
		return tileLength;
	}
	
	/**
	 * Checks if the specific point is inside the intersection. The intersection 
	 * spans from (0,0) to ({@code length},{@code length}).
	 * 
	 * @param point Point.
	 * @return True if the point is inside the intersection.
	 */
	public boolean isInside(GridPoint point) {
		return point.x >= 0 && point.x <= length && point.y >= 0 && point.y <= length;
	}

}
