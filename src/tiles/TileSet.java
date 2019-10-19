/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tiles;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Set of tiles.
 * @author Ocampo
 */
public class TileSet extends LinkedHashSet<Tile> {
	/**
     * Constructs a new, empty tile set with the specified initial
     * capacity and load factor.
     *
     * @param      initialCapacity the initial capacity of the linked hash set
     * @param      loadFactor      the load factor of the linked hash set
     * @throws     IllegalArgumentException  if the initial capacity is less
     *               than zero, or if the load factor is nonpositive
     */
	public TileSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
     * Constructs a new, empty tile set with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param   initialCapacity   the initial capacity of the LinkedHashSet
     * @throws  IllegalArgumentException if the initial capacity is less
     *              than zero
     */
	public TileSet(int initialCapacity) {
		super(initialCapacity);
	}

	/**
     * Constructs a new, empty tile set with the default initial
     * capacity (16) and load factor (0.75).
     */
	public TileSet() {
	}

	/**
     * Constructs a new tile set with the same elements as the
     * specified collection.  The linked hash set is created with an initial
     * capacity sufficient to hold the elements in the specified collection
     * and the default load factor (0.75).
     *
     * @param c  the collection whose elements are to be placed into
     *           this set
     * @throws NullPointerException if the specified collection is null
     */
	public TileSet(Collection<? extends Tile> c) {
		super(c);
	}
}
