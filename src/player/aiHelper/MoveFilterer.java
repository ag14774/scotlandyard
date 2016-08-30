package player.aiHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Move;
import scotlandyard.MoveDouble;
import scotlandyard.MovePass;
import scotlandyard.MoveTicket;
import scotlandyard.Ticket;

public class MoveFilterer {

	/**
	 * Take the best score and the list of possible states and returns the
	 * associated move of the best state. If more than one best states are
	 * found, the score is calculated again on the chosen best states.
	 * 
	 * @param states
	 * @param best
	 * @return the best move
	 */
	public static Move chooseSafestMove(List<AINode> states, double best) {
		double bestScore = Double.NEGATIVE_INFINITY;
		AINode bestState = null;

		List<AINode> bestStates = new ArrayList<AINode>();
		for (AINode state : states) {
			if (state.getScore() == best)
				bestStates.add(state);
		}

		for (AINode state : bestStates) {
			double tempScore = state.score();
			if (tempScore >= bestScore) {
				bestScore = tempScore;
				bestState = state;
			}
		}
		return bestState.getUsedMove();
	}

	/**
	 * Detective filter only used for MrX's AI that assumes that the detective
	 * know MrX's location and selects the first two moves which take him closer
	 * to MrX.
	 * 
	 * @param unfiltered
	 * @param state
	 * @return
	 */
	public static Set<Move> detectiveFilter(Set<Move> unfiltered, AbstractGameState state) {
		if (unfiltered.contains(MovePass.instance(state.getCurrentPlayer())))
			return unfiltered;

		Map<Integer, Double> distWithAll = state.help.getGeneralDistances(state.getMrXRealLocation());
		Set<Move> result = new HashSet<Move>();
		for (int i = 0; i < 2; i++) {
			Move selectedMove = null;
			Double bestDist = Double.POSITIVE_INFINITY;
			for (Move option : unfiltered) {
				double tempDist = distWithAll.get(((MoveTicket) option).target);
				if (tempDist < bestDist) {
					bestDist = tempDist;
					selectedMove = option;
				}
			}
			if (selectedMove != null) {
				unfiltered.remove(selectedMove);
				result.add(selectedMove);
			}
		}
		return result;
	}

	/**
	 * Filters unnecessary moves for MrX.
	 * The rules are:
	 * - No secret moves on a round that MrX is going to show up
	 * - No secret moves when all the options are taxi moves.
	 * - No secret move during the first two rounds
	 * - If on boat node, always use secret moves if secret tickets are available
	 * - Can use double moves ONLY when there are two or more detectives within
	 *   two or less moves away
	 * @param unfiltered
	 * @param state
	 * @return New set of moves
	 */
	public static Set<Move> mrXFilter(Set<Move> unfiltered, AbstractGameState state) {
		List<Boolean> roundVisibility = state.getRounds();

		double detectivesClose = state.help.getNumOfDetectivesCloserThan(state.getMrXRealLocation(), 2.0);

		Set<MoveDouble> doubleMoves = new HashSet<MoveDouble>();
		boolean allTaxiMoves = true;
		boolean hasSecretMoves = false;
		Iterator<Move> it = unfiltered.iterator();
		// check if all moves are taxis and if mrX has secret moves
		// separate all double moves
		while (it.hasNext()) {
			Move m = it.next();
			if (m instanceof MoveDouble) {
				doubleMoves.add((MoveDouble) m);
				it.remove();
			} else {
				if (((MoveTicket) m).ticket != Ticket.Secret)
					allTaxiMoves &= ((MoveTicket) m).ticket == Ticket.Taxi ? true : false;
				else
					hasSecretMoves = true;
			}
		}

		// check if mrX is visible at next round
		boolean mrXVisibleNextRound = false;
		if (state.getRound() + 1 >= roundVisibility.size())
			mrXVisibleNextRound = true;
		else
			mrXVisibleNextRound = roundVisibility.get(state.getRound() + 1);

		// remove secret move if necessary
		int location = state.getMrXRealLocation();
		if (allTaxiMoves || mrXVisibleNextRound || state.getRound() <= 2) {
			it = unfiltered.iterator();
			while (it.hasNext()) {
				MoveTicket m = (MoveTicket) it.next();
				if (m.ticket == Ticket.Secret)
					it.remove();
			}
			Iterator<MoveDouble> doubleIt = doubleMoves.iterator();
			while (doubleIt.hasNext()) {
				MoveDouble m = doubleIt.next();
				if (m.move1.ticket == Ticket.Secret)
					doubleIt.remove();
			}
			// if on boat use secret move
		} else if ((location == 194 || location == 157 || location == 115 || location == 108) && hasSecretMoves) {
			it = unfiltered.iterator();
			while (it.hasNext()) {
				MoveTicket m = (MoveTicket) it.next();
				if (m.ticket != Ticket.Secret)
					it.remove();
			}
			if (detectivesClose > 2) {
				Iterator<MoveDouble> doubleIt = doubleMoves.iterator();
				while (doubleIt.hasNext()) {
					MoveDouble md = doubleIt.next();
					if (md.move2.target != location && md.move1.ticket == Ticket.Secret)
						unfiltered.add(md);
				}
			}
			return unfiltered;
		}

		// detectivesClose > uniqueTaxiTargets?
		if (detectivesClose > 2) {
			Iterator<MoveDouble> doubleIt = doubleMoves.iterator();
			while (doubleIt.hasNext()) {
				MoveDouble md = doubleIt.next();
				if (md.move2.target != location)
					unfiltered.add(md);
			}
		}

		return unfiltered;
	}

}
