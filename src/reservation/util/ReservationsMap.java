/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reservation.util;

import java.util.LinkedHashMap;
import java.util.Map;
import reservation.tiles.TileTimes;

/**
 * A map that associates some integer identification number to a specific tiles at 
 * various times.
 * @author Ocampo
 */
public class ReservationsMap extends LinkedHashMap<Integer, TileTimes> {

	/**
     * Constructs an empty insertion-ordered <tt>ReservationsMap</tt> instance
     * with the specified initial capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
	public ReservationsMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
     * Constructs an empty insertion-ordered <tt>ReservationsMap</tt> instance
     * with the specified initial capacity and a default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
	public ReservationsMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
     * Constructs an empty insertion-ordered <tt>ReservationsMap</tt> instance
     * with the default initial capacity (16) and load factor (0.75).
     */
	public ReservationsMap() {
	}

	/**
     * Constructs an insertion-ordered <tt>ReservationsMap</tt> instance with
     * the same mappings as the specified map.  The <tt>ReservationsMap</tt>
     * instance is created with a default load factor (0.75) and an initial
     * capacity sufficient to hold the mappings in the specified map.
     *
     * @param  m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
	public ReservationsMap(Map<? extends Integer, ? extends TileTimes> m) {
		super(m);
	}

	/**
     * Constructs an empty <tt>ReservationsMap</tt> instance with the
     * specified initial capacity, load factor and ordering mode.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @param  accessOrder     the ordering mode - <tt>true</tt> for
     *         access-order, <tt>false</tt> for insertion-order
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
	public ReservationsMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}
	
	
}
