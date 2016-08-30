package player.aiHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.MovePass;
import scotlandyard.Route;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;

/**
 * 
 * Provides helper methods for the AIs, mostly about shortest distances using
 * Dijkstra's algorithm, methods for calculating MrX's possible locations
 *
 */
public class AIHelper {

	ScotlandYardView view;
	Graph<Integer, Route> graph;

	/**
	 * Hashmaps that store all the calculated distances so far from different
	 * sources thus avoiding recalculation when distance from an already
	 * calculated source is requested.
	 */
	private static Map<Integer, Map<Integer, Double>> generalDistance = new HashMap<Integer, Map<Integer, Double>>();
	private static Map<Integer, Map<Integer, Double>> taxiDistance = new HashMap<Integer, Map<Integer, Double>>();
	private static Map<Integer, Map<Integer, Double>> taxiBusDistance = new HashMap<Integer, Map<Integer, Double>>();

	public AIHelper(ScotlandYardView view, Graph<Integer, Route> graph) {
		this.view = view;
		this.graph = graph;
	}

	/**
	 * 
	 * @return A map from Colour to Integer that stores the detective's
	 *         locations.
	 */
	public Map<Colour, Integer> mapDetectiveLocations() {
		Map<Colour, Integer> detectiveLocations = new HashMap<Colour, Integer>();
		detectiveLocations.put(Colour.Blue, view.getPlayerLocation(Colour.Blue));
		detectiveLocations.put(Colour.Green, view.getPlayerLocation(Colour.Green));
		detectiveLocations.put(Colour.Red, view.getPlayerLocation(Colour.Red));
		detectiveLocations.put(Colour.White, view.getPlayerLocation(Colour.White));
		detectiveLocations.put(Colour.Yellow, view.getPlayerLocation(Colour.Yellow));
		return detectiveLocations;
	}

	/**
	 * 
	 * @return List with all the detective's locations.
	 */
	public List<Integer> getDetectiveLocations() {
		return new ArrayList<Integer>(mapDetectiveLocations().values());
	}

	/**
	 * Uses Dijkstra's algorithm to calculate distances from source given. Takes
	 * into account taxis, buses and undergrounds
	 * 
	 * @param source
	 * @return a Map that contains distances from source to each location of the
	 *         map
	 */
	public Map<Integer, Double> getGeneralDistances(int source) {
		if (generalDistance.containsKey(source))
			return generalDistance.get(source);
		Map<Integer, Double> distances = Dijkstra.dijkstraRunner(graph, source, true, true, true);
		generalDistance.put(source, distances);
		return distances;
	}

	/**
	 * Uses Dijkstra's algorithm to calculate distances from source given. Takes
	 * into account taxis only.
	 * 
	 * @param source
	 * @return a Map that contains distances from source to each location of the
	 *         map
	 */
	public Map<Integer, Double> getTaxiDistances(int source) {
		if (taxiDistance.containsKey(source))
			return taxiDistance.get(source);
		Map<Integer, Double> distances = Dijkstra.dijkstraRunner(graph, source, true, false, false);
		taxiDistance.put(source, distances);
		return distances;
	}

	/**
	 * Uses Dijkstra's algorithm to calculate distances from source given. Takes
	 * into account taxis and buses only.
	 * 
	 * @param source
	 * @return a Map that contains distances from source to each location of the
	 *         map
	 */
	public Map<Integer, Double> getTaxiAndBusDistances(int source) {
		if (taxiBusDistance.containsKey(source))
			return taxiBusDistance.get(source);
		Map<Integer, Double> distances = Dijkstra.dijkstraRunner(graph, source, true, true, false);
		taxiBusDistance.put(source, distances);
		return distances;
	}

	/**
	 * Returns the number of the detectives who are closer to source that the
	 * defined distance taking into account only taxis and buses.
	 * 
	 * @param source
	 * @param defineClose
	 * @return the number of detectives who are close to source
	 */
	public int getNumOfDetectivesCloserThan(int source, double defineClose) {
		Map<Integer, Double> distance = getTaxiAndBusDistances(source);
		Map<Colour, Integer> detectiveLocations = mapDetectiveLocations();
		Set<Colour> stuck = getStuckDetectives();
		int detectivesClose = 0;
		for (Map.Entry<Colour, Integer> entry : detectiveLocations.entrySet()) {
			if (!stuck.contains(entry.getKey())) {
				double dist = distance.get(entry.getValue());
				if (dist <= defineClose)
					detectivesClose++;
			}
		}
		return detectivesClose;
	}

	/**
	 * The distance of the nearest non-stuck detective to source.
	 * 
	 * @param source
	 * @return distance to nearest detective
	 */
	public double getMinDetectiveDistance(int source) {
		double minDetectiveDist = Double.POSITIVE_INFINITY;

		Map<Integer, Double> distWithAll = getGeneralDistances(source);
		Map<Colour, Integer> detectiveLocations = mapDetectiveLocations();
		Set<Colour> stuck = getStuckDetectives();
		for (Map.Entry<Colour, Integer> entry : detectiveLocations.entrySet()) {
			if (!stuck.contains(entry.getKey())) {
				double dist = distWithAll.get(entry.getValue());
				if (dist <= minDetectiveDist)
					minDetectiveDist = dist;
			}
		}
		return minDetectiveDist;
	}

	/**
	 * Calculates distance from nearest corner
	 * 
	 * @param source
	 * @return
	 */
	public double getMinDistanceFromCorner(int source) {
		Map<Integer, Double> distWithAll = getGeneralDistances(source);
		Set<Integer> cornerLocations = new HashSet<Integer>();
		cornerLocations.add(2);
		cornerLocations.add(7);
		cornerLocations.add(30);
		cornerLocations.add(189);
		cornerLocations.add(162);
		cornerLocations.add(2);
		cornerLocations.add(5);
		cornerLocations.add(4);
		double minDistanceFromCorner = Double.POSITIVE_INFINITY;
		for (int x : cornerLocations) {
			double dist = distWithAll.get(x);
			if (dist < minDistanceFromCorner)
				minDistanceFromCorner = dist;
		}
		return minDistanceFromCorner;
	}

	/**
	 * Set of unique locations a player can go from source.
	 * 
	 * @param source
	 * @return
	 */
	public Set<Integer> getUniqueTargets(int source) {
		Set<Edge<Integer, Route>> connectedEdges = graph.getEdges(source);
		Set<Edge<Integer, Route>> validEdges = new HashSet<Edge<Integer, Route>>(connectedEdges);
		Set<Integer> uniqueTargets = new HashSet<Integer>();
		for (Edge<Integer, Route> edge : connectedEdges) {
			if (view.getPlayerTickets(Colour.Black, Ticket.fromRoute(edge.data())) <= 0)
				validEdges.remove(edge);
		}
		List<Integer> enemyLocations = getDetectiveLocations();
		for (Edge<Integer, Route> edge : validEdges) {
			if (!enemyLocations.contains(edge.other(source)))
				uniqueTargets.add(edge.other(source));
		}
		return uniqueTargets;
	}

	/**
	 * The number of detectives with low taxi tickets. In this context, low taxi
	 * tickets is defined as the number of detectives whose number of taxi
	 * tickets is less than their "taxi-distance" from <i>location</i>
	 * 
	 * @param location
	 * @return
	 */
	public int getDetectivesWithLowTaxiTickets(int location) {
		Map<Integer, Double> distWithTaxi = getTaxiDistances(location);
		int detectivesWithLowTaxiTickets = 0;
		for (Colour c : view.getPlayers()) {
			if (c != Colour.Black) {
				int taxiTickets = view.getPlayerTickets(c, Ticket.Taxi);
				if (taxiTickets < distWithTaxi.get(view.getPlayerLocation(c)))
					detectivesWithLowTaxiTickets++;
			}
		}
		return detectivesWithLowTaxiTickets;
	}

	private Set<Colour> getStuckDetectives() {
		Set<Colour> stuckDetectives = new HashSet<Colour>();
		if (!(view instanceof AbstractGameState))
			return stuckDetectives;
		AbstractGameState state = (AbstractGameState) view;
		for (Colour c : view.getPlayers()) {
			if (c != Colour.Black) {
				if (state.validMoves(c).contains(MovePass.instance(c))) {
					stuckDetectives.add(c);
				}
			}
		}
		return stuckDetectives;
	}

	/**
	 * 
	 * @param source
	 * @return True if MrX is both on a node with a boat edge and has secret
	 *         tickets. False otherwise
	 */
	public boolean onBoatAndHasSecret(int source) {
		boolean hasSecret = view.getPlayerTickets(Colour.Black, Ticket.Secret) > 0 ? true : false;
		if (!hasSecret)
			return false;

		Set<Edge<Integer, Route>> relatedEdges = graph.getEdges(source);
		boolean onBoat = false;
		for (Edge<Integer, Route> edge : relatedEdges) {
			if (edge.data() == Route.Boat) {
				onBoat = true;
				break;
			}
		}
		if (!onBoat)
			return false;
		return true;
	}

	private Set<Integer> findTargetsWithTickets(int location, Ticket ticket) {
		Set<Integer> accessibleLocations = new HashSet<Integer>();
		Set<Edge<Integer, Route>> allEdges = graph.getEdges(location);
		if (ticket == Ticket.Secret) {
			allEdges.forEach(edge -> accessibleLocations.add(edge.other(location)));
		} else {
			for (Edge<Integer, Route> edge : allEdges) {
				if (Ticket.fromRoute(edge.data()) == ticket)
					accessibleLocations.add(edge.other(location));
			}
		}
		return accessibleLocations;
	}

	/**
	 * Given an old set of MrX's possible locations and the ticket played, it
	 * updated the possible locations
	 * 
	 * @param updatedPossibleLocations
	 * @param t
	 * @return Possible locations of MrX
	 */
	public Set<Integer> calculatePossibleMrXLocations(Set<Integer> updatedPossibleLocations, Ticket t) {
		Set<Integer> oldPossibleLocations = new HashSet<Integer>(updatedPossibleLocations);
		List<Integer> detectivesCurrentLocations = getDetectiveLocations();
		if (view.getRounds().get(view.getRound())) {
			updatedPossibleLocations.clear();
			updatedPossibleLocations.add(view.getPlayerLocation(Colour.Black));
		} else {
			for (int location : oldPossibleLocations) {
				Set<Integer> temporaryTargetsSet = findTargetsWithTickets(location, t);
				if (temporaryTargetsSet.isEmpty())
					updatedPossibleLocations.remove(location);
				Set<Integer> tempSet = new HashSet<Integer>();
				tempSet.addAll(detectivesCurrentLocations);
				temporaryTargetsSet.removeAll(tempSet);
				updatedPossibleLocations.addAll(temporaryTargetsSet);
			}
		}
		return updatedPossibleLocations;
	}

}
