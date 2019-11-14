
package reservation.manager;

import java.time.LocalTime;
import java.util.Map;
import reservation.tiles.TileTimes;
import reservation.util.ReservationsMap;
import simulator.tilechecker.SimTileChecker;
import simulator.tileretrieval.SimTileRetriever;
import tiles.Intersection;
import vehicle.VehicleProperty;

/**
 * Manages reservations of the intersection. When a vehicle makes a reservation, 
 * it performs a simulation as if that vehicle were crossing that road.
 * @author Ocampo
 */
public class ReservationManager {
	
	private ReservationsMap reservations;

	/**
	 * Constructs a reservation manager from a map of reservation.
	 * @param reservations Vehicle reservations.
	 * @throws NullPointerException if reservations is null.
	 */
	public ReservationManager(Map<Integer, TileTimes> reservations) {
		if (reservations == null) throw new NullPointerException();
		this.reservations = new ReservationsMap(reservations);
	}

	/**
	 * Constructs a reservation manager with no reservations.
	 */
	public ReservationManager() {
		this(new ReservationsMap());
	}
	
	/**
	 * Removes the reservation denoted by the vehicle ID, if that vehicle denoted 
	 * by that ID is reserved.
	 * @param vehicleID Vehicle ID
	 * @return True if the reservation was successfully removed, false otherwise.
	 */
	public boolean remove(int vehicleID) {
		if (reservations.containsKey(vehicleID)) {
			// Remove reservation denoted by vehicle ID.
			reservations.remove(vehicleID);
			return true;
		}
		return false;
	}
	
	/**
	 * Makes a reservation. When making a reservation, the vehicle is simulated as 
	 * it is crossing the intersection. When a collision is detected on simulation, 
	 * the reservation is rejected.
	 * @param intersection Intersection of the vehicle to cross into.
	 * @param arrivalTime Vehicle arrival time.
	 * @param vehicleProperty Properties of the vehicle to be simulated.
	 * @param speed Speed of the vehicle on the intersection. Vehicle's initial speed when its accelerating.
	 * @param acceleration Acceleration of the vehicle in the intersection.
	 * @param heading Heading of the vehicle. It can be either {@code NORTH}, {@code SOUTH}, {@code EAST}, or {@code WEST} defined in {@code SimulationConstants}.
	 * @param direction Direction of the vehicle. It can either be {@code STRAIGHT}, {@code LEFT_TURN}, or {@code RIGHT_TURN} defined in {@code SimulationConstants}.
	 * @param vehicleId ID of the vehicle to reserve a path.
	 * @param timeBaseNs Time base in nanoseconds. This is the time interval between each sample of occupied tiles.
	 * @return True if the reservation is accepted, false otherwise.
	 * @throws IllegalArgumentException if the heading or direction is not supported.+
	 */
	public boolean reserve(Intersection intersection, LocalTime arrivalTime, VehicleProperty vehicleProperty, double speed, double acceleration, int heading, int direction, int vehicleId, int timeBaseNs) {
		// Initialize simulator
		// Tile retriever
		SimTileRetriever tileRetriever = new SimTileRetriever(arrivalTime, speed, acceleration, vehicleProperty, intersection, direction, heading, vehicleId, timeBaseNs);
		// Get vehicle path
		TileTimes path = tileRetriever.simulate();
		// Check the reservations and return results
		boolean notReserved = SimTileChecker.checkForReservation(path, reservations) == -1;
		if (notReserved) {
			reservations.put(vehicleId, path);
		}
		return notReserved;
	}
	
	/**
	 * Returns true if the vehicle has a reservation.
	 * @param vehicleId ID of the vehicle to check.
	 * @return True if the vehicle has a reservation.
	 */
	public boolean isReserved(int vehicleId) {
		return reservations.containsKey(vehicleId);
	}
	
}
