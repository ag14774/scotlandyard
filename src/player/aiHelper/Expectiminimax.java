package player.aiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Move;

public class Expectiminimax extends Minimax {

	/**
	 * Expectiminimax algorithm - AI for detectives
	 * @param root
	 * @param maxColour
	 */
	public Expectiminimax(AINode root, Colour maxColour) {
		super(root, maxColour);
	}
	
	private List<AINode> successors(AINode root, boolean chance) {
		if(chance)
			return chanceNodes((DetectiveAIGameState) root);
		Set<Move> options = root.getSuccessorOptions();
		if (((DetectiveAIGameState) root).getCurrentPlayer() == Colour.Black)
			return successors(root, MoveFilterer.mrXFilter(options, (AbstractGameState)root));
		else
			return successors(root, options);
	}
	
	private List<AINode> chanceNodes(DetectiveAIGameState root){
		Set<Integer> possibleLocations = root.getMrXPossibleLocations();
		List<AINode> successors = new ArrayList<AINode>();
		for (int loc : possibleLocations) {
			AINode newState = root.copy();
			((AbstractGameState)newState).setMrXLocation(loc);
			successors.add(newState);
		}
		return successors;
	}
	
	@Override
	Move decision(AINode initialState) {
		double best = maxValue(initialState);
		if (isStop())
			return null;

		return MoveFilterer.chooseSafestMove(getSuccessors(), best);
	}
	
	private double maxValue(AINode state) {
		if (isStop())
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = Double.NEGATIVE_INFINITY;
		List<AINode> successors = successors(state,false);
		for (AINode s : successors) {
			double tempValue = chanceValue(s);
			value = Math.max(value, tempValue);
			s.setScore(tempValue);
			storeValue(s, tempValue);
		}
		return value;
	}

	private double minValue(AINode state) {
		if (isStop())
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = Double.POSITIVE_INFINITY;
		List<AINode> successors = successors(state,false);
		for (AINode s : successors) {
			double tempValue = maxValue(s);
			value = Math.min(value, tempValue);
			s.setScore(tempValue);
			storeValue(s, tempValue);
		}
		return value;
	}
	
	private double chanceValue(AINode state){
		if (isStop())
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = 0;
		List<AINode> successors = successors(state,true);
		for (AINode s : successors) {
			double tempValue = minValue(s);
			value = value + 1.0/successors.size() * tempValue;
			s.setScore(tempValue);
			storeValue(s, tempValue);
		}
		return value;
	}

}
