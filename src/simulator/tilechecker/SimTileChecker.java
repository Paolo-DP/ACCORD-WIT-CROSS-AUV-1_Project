
package simulator.tilechecker;

import java.time.LocalTime;
import java.util.Map;
import reservation.tiles.TileTimes;
import reservation.util.ReservationsMap;
import tiles.TileSet;

/**
 * Tile checker for the simulator. It takes in a tile set and checks if any 
 * of the tiles are reserved by another vehicle. The reservations of the tiles 
 * are in the reservation map.
 * @author Ocampo
 */
public class SimTileChecker {
	
	/**
	 * This method takes in a specific tileset-times mapping and checks against 
	 * the reservations if there is a tile is already reserved by another vehicle. 
	 * It returns the ID of the vehicle that reserved at least one tile in the 
	 * given tile-times, or -1 if no vehicle has reserved such tiles.
	 * @param tileTimes The tile-times map to be checked.
	 * @param reservations Reservations made by vehicles.
	 * @return ID of the vehicle on which a part of the tileset has been reserved 
	 *		   or -1 if no vehicle has reserved it.
	 */
	public static int checkForReservation(TileTimes tileTimes, ReservationsMap reservations) {
		// Check parameters.
		if (tileTimes == null || reservations == null)
			throw new NullPointerException();
		// For every reservation
		for (Map.Entry<Integer, TileTimes> reservationEntry : reservations.entrySet()) {
			TileTimes reservedTileTimes = reservationEntry.getValue();  // Get the tile time reserved for the particular vehicle.
			// For every given tileset-time map
			for (Map.Entry<LocalTime, TileSet> tileTimesEntry : tileTimes.entrySet()) {
				// Get the tile set at a particular time
				LocalTime time = tileTimesEntry.getKey();
				// Search for the time in the reserved tile-times.
				if (reservedTileTimes.containsKey(time)) {
					// Find the reserved tiles.
					TileSet reservedTiles = new TileSet(tileTimesEntry.getValue());  // Initially set the tiles to reserved tiles.
					reservedTiles.retainAll(reservedTileTimes.get(time));  // Retain the tiles denoted by the current tile-times.
					if (!reservedTiles.isEmpty()) return reservationEntry.getKey();  // If the set is not empty, some tiles are reserved.
				}
			}
		}
		// Return an integer denoting no reservation.
		return -1;
	}
}
