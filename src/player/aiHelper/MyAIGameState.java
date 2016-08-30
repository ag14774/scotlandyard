package player.aiHelper;

import java.util.List;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.Route;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;

public class MyAIGameState extends AbstractGameState {

	private MyAIGameState(Move move, int detectives, List<Boolean> rounds, Graph<Integer, Route> graph,
			String graphFilename) {
		super(move, detectives, rounds, graph, graphFilename);
	}

	/**
	 * Score takes into account, minimum detective distance, distance from
	 * nearest corner, number of unique targets, detectives with low taxi
	 * tickets, secret ticket count, whether on a node with a boat route or not.
	 * If MrX wins, returns 200, if detectives win, return -200.
	 * 
	 * @return a score for current game state
	 */
	@Override
	public double score() {
		
		double score = 0.0;
		Set<Colour> winners = this.winners;
		if (!winners.isEmpty()) {
			if (getWinningPlayers().contains(Colour.Black))
				return 200;
			else
				return -200;
		}

		// parameter 1
		double minDetectiveDist = help.getMinDetectiveDistance(playerMap.get(Colour.Black).getLocation());

		// parameter 2, overall position
		double minDistanceFromCorner = help.getMinDistanceFromCorner(playerMap.get(Colour.Black).getLocation());

		// parameter 3, unique targets
		Set<Integer> uniqueTargets = help.getUniqueTargets(playerMap.get(Colour.Black).getLocation());
		if (uniqueTargets.isEmpty())
			return -200;

		// parameter 4
		int detectivesWithLowTaxiTickets = help.getDetectivesWithLowTaxiTickets(playerMap.get(Colour.Black)
				.getLocation());

		int secretMoveCount = getPlayerTickets(Colour.Black, Ticket.Secret);

		int onBoatAndHasSecret = help.onBoatAndHasSecret(playerMap.get(Colour.Black).getLocation()) ? 1 : 0;

		score += 5 * minDetectiveDist;
		score += 2.5 * secretMoveCount;
		score += 2 * uniqueTargets.size();
		score += 1.5 * onBoatAndHasSecret;
		score += 1 * minDistanceFromCorner;
		score += 1 * detectivesWithLowTaxiTickets;

		return score;
	}

	@Override
	public final void simulateMove() {
		simulateMove(getUsedMove());
		nextPlayer();
	}

	@Override
	public AINode copy(Move move) {
		AINode state = createGameState(move, this, getMrXRealLocation(), graph, graphFilename);
		state.setDepth(this.getDepth() + 1);
		return state;
	}

	public static AINode createGameState(Move move, ScotlandYardView view, int location, Graph<Integer, Route> graph,
			String graphFilename) {
		MyAIGameState newModel = new MyAIGameState(move, view.getPlayers().size() - 1, view.getRounds(), graph,
				graphFilename);
		newModel.join(null, Colour.Black, location, createTicketMap(view, Colour.Black));
		newModel.join(null, Colour.Blue, view.getPlayerLocation(Colour.Blue), createTicketMap(view, Colour.Blue));
		newModel.join(null, Colour.Green, view.getPlayerLocation(Colour.Green), createTicketMap(view, Colour.Green));
		newModel.join(null, Colour.Red, view.getPlayerLocation(Colour.Red), createTicketMap(view, Colour.Red));
		newModel.join(null, Colour.White, view.getPlayerLocation(Colour.White), createTicketMap(view, Colour.White));
		newModel.join(null, Colour.Yellow, view.getPlayerLocation(Colour.Yellow), createTicketMap(view, Colour.Yellow));
		while (view.getCurrentPlayer() != newModel.getCurrentPlayer())
			newModel.nextPlayer();
		newModel.round = view.getRound();
		return newModel;
	}

}
