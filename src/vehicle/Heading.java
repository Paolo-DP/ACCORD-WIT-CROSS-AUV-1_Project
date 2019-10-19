/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicle;

import math.ExtendedMath;

/**
 * Operations and utilities on headings.
 * @author Ocampo
 */
public class Heading {
	
	/**
	 * Returns the reverse angle. A reverse angle is standard angle units going in the 
	 * negative, or clockwise direction.
	 * @param angle Angle in radians.
	 * @return Reverse angle, in radians.
	 */
	public static double reverse(double angle) {
		return ExtendedMath.mod(2*Math.PI - angle, 2*Math.PI);
	}
	
}
