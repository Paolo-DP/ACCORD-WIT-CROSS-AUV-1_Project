/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tiles;

import geometry.IntegerPoint;
import java.util.Objects;

/**
 * A tile in the intersection. Tiles are indexed by zero-based coordinates (x, y). 
 * The x-coordinate refers to the column, and y-coordinate with the row. It is analogous 
 * to cartesian plane coordinates.
 * @author Ocampo
 */
public class Tile {
	
	/**
	 * Edge tile type.
	 */
	public static final int EDGE_TILE = -2;
	/**
	 * Internal tile type.
	 */
	public static final int INTERNAL_TILE = -3;
	/**
	 * Unreserved status of the tile.
	 */
	public static final int UNRESERVED_STATUS = -1;
	
	private IntegerPoint id;  // Tile index / ID
	private int type;
	private int status;

	/**
	 * Construct a tile object.
	 * @param id ID or index of the tile. Tiles are indexed by zero based coordinates.
	 * @param type Type of tile: {@code EDGE_TILE} or {@code INTERNAL_TILE}.
	 * @param status Tile reservation status. Use {@code UNRESERVED_STATUS} 
	 *				 if the tile is not reserved.
	 */
	public Tile(IntegerPoint id, int type, int status) {
		this.id = id;
		this.type = type;
		this.status = status;
	}

	/**
	 * Construct a tile object with unreserved status.
	 * @param id ID or index of the tile. Tiles are indexed by zero based coordinates.
	 * @param type Type of tile: {@code EDGE_TILE} or {@code INTERNAL_TILE}.
	 */
	public Tile(IntegerPoint id, int type) {
		this(id, type, UNRESERVED_STATUS);
	}

	/**
	 * Returns the ID of the tile. Tiles are indexed by zero-based coordinates.
	 * @return Tile ID, in zero-based coordinates.
	 */
	public IntegerPoint getId() {
		return id;
	}

	/**
	 * Returns  the type of the tile. It either returns {@code EDGE_TILE} or {@code INTERNAL_TILE}.
	 * @return Tile type: {@code EDGE_TILE} or {@code INTERNAL_TILE}
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the reservation status of the tile. It returns -1 ({@code UNRESERVED_STATUS}) 
	 * when the tile is not reserved.
	 * @return Tile reservation status.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the reservation status of the tile. Set to -1 ({@code UNRESERVED_STATUS}) 
	 * if the tile is not reserved.
	 * @param status Tile reservation status.
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Returns the tile hash code based on its ID.
	 * @return Tile hash code.
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.id);
		return hash;
	}

	/**
	 * Checks if two tiles are equal to each other. Tiles are equal if they had the 
	 * same ID, regardless of the status and types they are in.
	 * @param obj An object to compare to.
	 * @return True if the tiles are equal to each other.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Tile other = (Tile) obj;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public String toString() {
		String s = "UnknownTile";
		if (type == INTERNAL_TILE) s = "I-Tile";
		else if (type == EDGE_TILE) s = "E-Tile";
		s += id.toString() + " status " + String.valueOf(status);
		return s;
	}
	
	
	
}
