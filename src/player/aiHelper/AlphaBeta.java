package player.aiHelper;

import java.util.List;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Move;

/**
 * Minimax with Alphabeta Further pruning is done by applying specific rules to
 * moves and filtering the moves that are not useful in any way.
 */
public class AlphaBeta extends Minimax {

	public AlphaBeta(AINode root, Colour maxColour) {
		super(root, maxColour);
	}

	/**
	 * Calculate moves, filter the moves and return a list of states for the remaining moves.
	 * @param root
	 * @return
	 */
	private List<AINode> successors(AINode root) {
		Set<Move> options = root.getSuccessorOptions();
		if (((MyAIGameState) root).getCurrentPlayer() == Colour.Black)
			return successors(root, MoveFilterer.mrXFilter(options, (MyAIGameState) root));
		else
			return successors(root, MoveFilterer.detectiveFilter(options, (MyAIGameState) root));
	}

	/**
	 * Run the algorithm once, creating a tree for the current MAX_DEPTH
	 * 
	 * @param initialState
	 *            The initial state
	 * @return A best move
	 */
	@Override
	Move decision(AINode initialState) {
		double best = maxValue(initialState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		if (isStop())
			return null;

		return MoveFilterer.chooseSafestMove(getSuccessors(), best);
	}

	private double maxValue(AINode state, double alpha, double beta) {
		if (isStop())
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = Double.NEGATIVE_INFINITY;
		List<AINode> successors = successors(state);
		for (AINode s : successors) {
			double tempValue = minValue(s, alpha, beta); //Score for "s"
			value = Math.max(value, tempValue); //Score for "state"
			s.setScore(tempValue); //Set score to the tempValue
			storeValue(s, tempValue); //Store it for the next iteration
			if (value >= beta)
				return value;
			alpha = Math.max(alpha, value);
		}
		return value;
	}

	private double minValue(AINode state, double alpha, double beta) {
		if (isStop())
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = Double.POSITIVE_INFINITY;
		List<AINode> successors = successors(state);
		for (AINode s : successors) {
			double tempValue;
			if (((MyAIGameState) s).getCurrentPlayer() == getMaxColour())
				tempValue = maxValue(s, alpha, beta);
			else
				tempValue = minValue(s, alpha, beta);
			value = Math.min(value, tempValue);
			s.setScore(tempValue);
			storeValue(s, tempValue);
			if (value <= alpha)
				return value;
			beta = Math.min(beta, value);
		}
		return value;
	}

}
