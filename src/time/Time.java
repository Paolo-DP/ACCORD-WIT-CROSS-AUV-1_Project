/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package time;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Time keeping class.
 * @see java.time.LocalTime
 * @author Ocampo
 */
public class Time {
	/**
	 * Returns the current time from the system clock. The {@code LocalTime} class 
	 * returns current time on the precision of the nano-seconds scale. Precision of 
	 * this returned time is in full seconds, any fractional sceonds are not counted.
	 * @return Current system time.
	 * @see java.time.LocalTime#now()
	 */
	public static LocalTime now() {
		return LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
	}
}
