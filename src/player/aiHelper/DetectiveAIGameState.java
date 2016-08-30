package player.aiHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MoveTicket;
import scotlandyard.Route;
import scotlandyard.ScotlandYardView;

public class DetectiveAIGameState extends AbstractGameState {

	private Set<Integer> mrXPossibleLocations;
	private Colour detectiveColour;

	private DetectiveAIGameState(Colour detectiveColour, Set<Integer> mrXPossibleLocations, Move move, int detectives,
			List<Boolean> rounds, Graph<Integer, Route> graph, String graphFilename) {

		super(move, detectives, rounds, graph, graphFilename);
		this.mrXPossibleLocations = mrXPossibleLocations;
		this.detectiveColour = detectiveColour;
	}

	/**
	 * Score is -Sum(distance(detective,location) location:mrXPossibleLocations)
	 * If MrX wins, returns -10000, if detectives win, return 10000.
	 * 
	 * @return a score for current game state
	 */
	@Override
	public double score() {
		double score = 0.0;
		Set<Colour> winners = this.winners;
		if (!winners.isEmpty()) {
			if (!getWinningPlayers().contains(Colour.Black))
				return 10000;
			else
				return -10000;
		}
		Map<Integer, Double> distanceSourceLocation = help.getGeneralDistances(getPlayerLocation(detectiveColour));
		for (int loc : mrXPossibleLocations) {
			score = score + distanceSourceLocation.get(loc);
		}
		score = -score;

		return score;
	}

	@Override
	public final void simulateMove() {
		simulateMove(getUsedMove());

		if (getUsedMove().colour == detectiveColour)
			changeColourTo(Colour.Black);
		else
			changeColourTo(detectiveColour);

		Move move = getUsedMove();
		if (move instanceof MoveTicket) {
			MoveTicket mt = (MoveTicket) move;
			if (move.colour == Colour.Black) {
				mrXPossibleLocations = help.calculatePossibleMrXLocations(mrXPossibleLocations, mt.ticket);
			} else {
				mrXPossibleLocations.remove(mt.target);
			}
		} else if (move instanceof MoveDouble) {
			MoveDouble md = (MoveDouble) move;
			mrXPossibleLocations = help.calculatePossibleMrXLocations(mrXPossibleLocations, md.move1.ticket);
			mrXPossibleLocations = help.calculatePossibleMrXLocations(mrXPossibleLocations, md.move2.ticket);
		}

	}

	private void changeColourTo(Colour c) {
		while (getCurrentPlayer() != c)
			nextPlayer();
	}

	@Override
	public AINode copy(Move move) {
		if (move == null) {
			move = getUsedMove();
		}
		AINode state = createGameState(detectiveColour, mrXPossibleLocations, move, this, getMrXRealLocation(), graph,
				graphFilename);

		state.setDepth(this.getDepth() + 1);
		return state;
	}

	/**
	 * Copies without changing the associated move
	 * 
	 * @return
	 */
	public AINode copy() {
		return copy(null);
	}

	/**
	 * @return the mrXPossibleLocations
	 */
	public Set<Integer> getMrXPossibleLocations() {
		return mrXPossibleLocations;
	}

	public static AINode createGameState(Colour detectiveColour, Set<Integer> mrXPossibleLocations, Move move,
			ScotlandYardView view, int location, Graph<Integer, Route> graph, String graphFilename) {

		DetectiveAIGameState newModel = new DetectiveAIGameState(detectiveColour, new HashSet<Integer>(
				mrXPossibleLocations), move, view.getPlayers().size() - 1, view.getRounds(), graph, graphFilename);

		newModel.join(null, Colour.Black, location, createTicketMap(view, Colour.Black));
		newModel.join(null, Colour.Blue, view.getPlayerLocation(Colour.Blue), createTicketMap(view, Colour.Blue));
		newModel.join(null, Colour.Green, view.getPlayerLocation(Colour.Green), createTicketMap(view, Colour.Green));
		newModel.join(null, Colour.Red, view.getPlayerLocation(Colour.Red), createTicketMap(view, Colour.Red));
		newModel.join(null, Colour.White, view.getPlayerLocation(Colour.White), createTicketMap(view, Colour.White));
		newModel.join(null, Colour.Yellow, view.getPlayerLocation(Colour.Yellow), createTicketMap(view, Colour.Yellow));
		while (view.getCurrentPlayer() != newModel.getCurrentPlayer())
			newModel.nextPlayer();
		newModel.mrXLastKnownLocation = view.getPlayerLocation(Colour.Black);
		newModel.round = view.getRound();

		return newModel;
	}
}
