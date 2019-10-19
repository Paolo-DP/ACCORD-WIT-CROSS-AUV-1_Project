/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reservation.tiles;

import geometry.GridLine;
import geometry.GridPoint;
import geometry.IntegerPoint;
import geometry.util.GridPointSet;
import java.util.LinkedHashMap;
import java.util.Map;
import math.ExtendedMath;
import tiles.Intersection;
import tiles.TileSet;
import vehicle.Vehicle;
import vehicle.VehicleBounds;
import tiles.Tile;

/**
 * A tile retrieval system.
 * @author Ocampo
 */
public class TileRetriever {
	
	private final Intersection intersection;

	/**
	 * Constructs a tile retrieval system.
	 * @param intersection 
	 */
	public TileRetriever(Intersection intersection) {
		this.intersection = intersection;
	}
	
	/**
	 * Gets the occupied tiles of the vehicle. Since a computer cannot perform infinite 
	 * sampling, the boundaries at sampled at finite intervals. The smaller sample 
	 * interval, the better retrieval results.
	 * @param vehicle Vehicle.
	 * @param sampleInterval Sample interval to divide up the vehicle boundaries.
	 * @return Tiles occupied by the vehicle as a set.
	 */
	public TileSet getOccupiedTiles(Vehicle vehicle, double sampleInterval) {
		TileSet occupiedTiles = new TileSet();
		VehicleBounds bounds = vehicle.getBounds();
		// Initiate a map that separates tiles by rows.
		Map<Integer, TileSet> tileMap = new LinkedHashMap<>();
		// Map boundary occupied tiles into rows.
		for (GridLine line : bounds.getBounds()) {
			// Sample the line into points and get occupied tiles of each point.
			GridPointSet pointSet = new GridPointSet(line, sampleInterval);
//			System.out.println("Point set: " + pointSet);  // DEBUG
			for (GridPoint point : pointSet) {
				if (!inBounds(point)) continue;  // Check if a point is in the intersection.
				// Avoid the points that lie between tiles.
				double remX = ExtendedMath.rem(point.x, intersection.getTileLength());
				double remY = ExtendedMath.rem(point.y, intersection.getTileLength());
				if (remX == 0 || remY == 0) continue;  // Avoids the points that lie exactly on the boundary.
				// Retrieve the tile occupied by the point.
				int tileX = (int) ExtendedMath.div(point.x, intersection.getTileLength());
				int tileY = (int) ExtendedMath.div(point.y, intersection.getTileLength());
				// IntegerPoint tileId = new IntegerPoint(tileX, tileY);
				IntegerPoint tileId = new IntegerPoint();
				tileId.x = tileX;
				tileId.y = tileY;
				Tile tile = new Tile(tileId, getTileType(tileId));
				// Put the tiles in the map. Tiles are mapped by x-index.
				if (tileMap.containsKey(tileX)) {
					// If the map contains the row index, add tile to existing tile set.
					TileSet set = tileMap.get(tileX);
					set.add(tile);
					tileMap.replace(tileX, set);
				} else {
					// Add a new tile set
					TileSet set = new TileSet();
					set.add(tile);
					tileMap.put(tileX, set);
				}
			}
		}
		// Add the internal tiles
		for (Integer key : tileMap.keySet()) {
			// Get the tiles, minimum and maximum.
			TileSet set = tileMap.get(key);
			Tile minTile = getMinTile(set);
			Tile maxTile = getMaxTile(set);
			// Construct a new set containing the internal tiles.
			TileSet newSet = new TileSet();
//			System.out.println(minTile);  // DEBUG
//			System.out.println(maxTile);  // DEBUG
			for (int i = minTile.getId().y; i <= maxTile.getId().y; i++) {
				IntegerPoint id = new IntegerPoint(key, i);
				newSet.add(new Tile(id, getTileType(id)));
//				System.out.println(newSet);  // DEBUG
			}
			// Replace the original set with the new one
			tileMap.replace(key, newSet);
		}
		// Construct the final tile set.
		for (TileSet set : tileMap.values()) {
			occupiedTiles.addAll(set);
		}
		
		return occupiedTiles;
	}
	
	// <editor-fold desc=" Private Methods " defaultstate="collapsed">
	private boolean inBounds(GridPoint pt) {
		return pt.x >= 0 && pt.x <= intersection.getLength() && pt.y >= 0 && pt.y <= intersection.getLength();
	}
	
	private int getTileType(IntegerPoint tileId) {
		return (
				tileId.x == 0 || 
				tileId.x == intersection.getResolution()-1 || 
				tileId.y == 0 ||
				tileId.y == intersection.getResolution()-1
		) ? Tile.EDGE_TILE : Tile.INTERNAL_TILE;
	}
	
	private Tile getMinTile(TileSet set) {
		Tile minTile = null;
		if (set != null) {
			for (Tile tile : set) {
				// Check if the tile is not null.
				if (tile != null) {
					// Check if the current minimum tile is not null and is lesser.
					if (minTile != null) {
						if (tile.getId().y >= minTile.getId().y) continue;
					}
					minTile = tile;
				}
			}
		}
		return minTile;
	}
	
	private Tile getMaxTile(TileSet set) {
		Tile maxTile = null;
		if (set != null) {
			for (Tile tile : set) {
				// Check if the tile is not null.
				if (tile != null) {
					// Check if the current maximum tile is not null and is greater.
					if (maxTile != null) {
						if (tile.getId().y <= maxTile.getId().y) continue;
					}
					maxTile = tile;
				}
			}
		}
		return maxTile;
	}
	// </editor-fold>
	
	
}
