/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reservation.tiles;

import java.util.LinkedHashMap;
import java.util.Map;
import tiles.TileSet;
import java.time.*;

/**
 * An association of a time and the tile set associated with the specific time.
 * @author Ocampo
 */
public class TileTimes extends LinkedHashMap<LocalTime, TileSet> {

	/**
     * Constructs an empty insertion-ordered <tt>TileTimes</tt> instance
     * with the specified initial capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
	public TileTimes(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
     * Constructs an empty insertion-ordered <tt>TileTimes</tt> instance
     * with the specified initial capacity and a default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
	public TileTimes(int initialCapacity) {
		super(initialCapacity);
	}

	/**
     * Constructs an empty insertion-ordered <tt>TileTimes</tt> instance
     * with the default initial capacity (16) and load factor (0.75).
     */
	public TileTimes() {
	}

	/**
     * Constructs an insertion-ordered <tt>TileTimes</tt> instance with
     * the same mappings as the specified map.  The <tt>TileTimes</tt>
     * instance is created with a default load factor (0.75) and an initial
     * capacity sufficient to hold the mappings in the specified map.
     *
     * @param  m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
	public TileTimes(Map<? extends LocalTime, ? extends TileSet> m) {
		super(m);
	}

	/**
     * Constructs an empty <tt>TileTimes</tt> instance with the
     * specified initial capacity, load factor and ordering mode.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @param  accessOrder     the ordering mode - <tt>true</tt> for
     *         access-order, <tt>false</tt> for insertion-order
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
	public TileTimes(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}

	
	
}
