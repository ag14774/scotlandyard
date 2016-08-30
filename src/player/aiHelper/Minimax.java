package player.aiHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Move;

/**
 * Simple minimax algorithm with no optimisations.
 */
public class Minimax {

	protected static int MAX_DEPTH;

	private List<AINode> successors;

	private Map<AINode, Double> lastCalculatedScores;
	private AINode root;
	private Move decision;
	private Colour maxColour;
	private boolean stop;

	/**
	 * Takes the initial root of the tree and the Colour of MAX. MAX_DEPTH is
	 * initially set to 2.
	 * 
	 * @param root
	 * @param maxColour
	 */
	public Minimax(AINode root, Colour maxColour) {
		this.root = root;
		this.maxColour = maxColour;
		lastCalculatedScores = new HashMap<AINode, Double>();
		stop = false;
		successors = null;
		MAX_DEPTH = 2;
	}

	/**
	 * Given a node in the tree it returns its successors.
	 * 
	 * @param root
	 * @return The successors of the given node of the tree
	 */
	private List<AINode> successors(AINode root) {
		Set<Move> options = root.getSuccessorOptions();
		return successors(root, options);
	}

	/**
	 * Given a node/gamestate and the possible moves the current player can
	 * make, it simulates the moves to copies of the given node and returns a
	 * set of possible successor game states. It also stores a reference to the
	 * first set of successors.
	 * 
	 * @param initial
	 *            Inital gamestate
	 * @param options
	 *            Possible moves
	 * @return A set of possible gamestates
	 */
	final List<AINode> successors(AINode initial, Set<Move> options) {
		List<AINode> successors = new ArrayList<AINode>();
		for (Move option : options) {
			AINode newState = initial.copy(option);
			newState.simulateMove();
			successors.add(newState);
		}
		if (this.successors == null) {
			this.successors = successors;
		}
		/*
		 * Sort successors based on a previous calculated score, if there is
		 * one. Otherwise, calculate score and sort. Sort in descending order
		 * it's MAX's turn, ascending if otherwise.
		 */
		if (((AbstractGameState) initial).getCurrentPlayer() == maxColour)
			Collections.sort(successors, Collections.reverseOrder(new stateComparator()));
		else
			Collections.sort(successors, new stateComparator());

		return successors;
	}

	/**
	 * Increase MAX_DEPTH
	 */
	final private void increaseDepth() {
		MAX_DEPTH++;
	}

	/**
	 * Run the algorithm once, creating a tree for the current MAX_DEPTH
	 * 
	 * @param initialState
	 *            The initial state
	 * @return A best move
	 */
	Move decision(AINode initialState) {
		double best = maxValue(initialState);

		if (stop)
			return null;

		// From the set all moves, filter the ones that have scored the best
		// score.
		// If more that one exist, select the one with the highest "local"
		// score.
		return MoveFilterer.chooseSafestMove(successors, best);
	}

	private double maxValue(AINode state) {
		if (stop)
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score); // Store score
			return score;
		}
		double value = Double.NEGATIVE_INFINITY;
		List<AINode> successors = successors(state);
		for (AINode s : successors) {
			value = Math.max(value, minValue(s));
			s.setScore(value); // Store score in state
			lastCalculatedScores.put(s, value); // Store value to hashmap, used
												// for move sorting.
		}
		return value;
	}

	private double minValue(AINode state) {
		if (stop)
			return 0.0;
		if (state.isTerminal()) {
			double score = state.score();
			state.setScore(score);
			return score;
		}
		double value = Double.POSITIVE_INFINITY;
		List<AINode> successors = successors(state);
		for (AINode s : successors) {
			value = Math.min(value, maxValue(s));
			s.setScore(value); // Store score in state
			lastCalculatedScores.put(s, value); // Store value to hashmap, used
												// for move sorting.
		}
		return value;
	}

	/**
	 * @return the maxColour
	 */
	public Colour getMaxColour() {
		return maxColour;
	}

	/**
	 * @return the successors
	 */
	public List<AINode> getSuccessors() {
		return successors;
	}

	/**
	 * @return returns true if the time limit has been reached
	 */
	public boolean isStop() {
		return stop;
	}

	/**
	 * Stores value into the hashmap
	 * 
	 * @param s
	 * @param value
	 */
	public void storeValue(AINode s, double value) {
		lastCalculatedScores.put(s, value);
	}

	/**
	 * When the time limit has been reached, call that method to quickly stop
	 * execution of the algorithm
	 */
	private final void stop() {
		stop = true;
	}

	/**
	 * Iteratively executes minimax algorithm each time increasing the depth
	 * Calculations from previous iterations are used to further prune the game
	 * tree
	 */
	private final void run() {
		while (!stop) {
			Move temp = decision(root);
			if (temp != null) {
				successors = null;
				this.decision = temp;
				System.out.println("******Increasing depth...******");
				this.increaseDepth();
			}
		}
	}

	/**
	 * Static method used to start the execution of the algorithm. It starts the
	 * algorithm on a separate thread while the current thread sleeps for a
	 * specified time. Once the time limit has been reached, it sends a stop
	 * signal to the Minimax thread, which causes it to immediately return. The
	 * method returns the move calculated by the last fully explored game tree.
	 * 
	 * @param root
	 *            Initial state of game
	 * @param maxColour
	 *            Colour of MAX
	 * @param type
	 *            Type of AI
	 * @return the move calculated by the last fully explored game tree
	 */
	public final static Move executeMinimax(AINode root, Colour maxColour, AIType type) {
		Minimax calculator;
		switch (type) {
		case AlphaBeta:
			calculator = new AlphaBeta(root, maxColour);
			break;
		case Expectiminimax:
			calculator = new Expectiminimax(root, maxColour);
			break;
		default:
			calculator = new Minimax(root, maxColour);
			break;
		}
		Thread runner = new Thread(calculator::run);
		runner.setName("Minimax");
		runner.start();
		try {
			Thread.sleep(13000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		calculator.stop();
		System.out.println("Max Depth: " + Minimax.MAX_DEPTH);
		return calculator.decision;
	}

	/**
	 * 
	 * Comparator to sort moves based on previous calculations to maximise
	 * pruning
	 *
	 */
	class stateComparator implements Comparator<AINode> {

		@Override
		public int compare(AINode o1, AINode o2) {
			double o1Score;
			double o2Score;
			if (lastCalculatedScores.containsKey(o1))
				o1Score = lastCalculatedScores.get(o1);
			else
				o1Score = o1.getScore();

			if (lastCalculatedScores.containsKey(o2))
				o2Score = lastCalculatedScores.get(o2);
			else
				o2Score = o2.getScore();

			return Double.compare(o1Score, o2Score);

		}

	}

	public enum AIType {
		AlphaBeta, Expectiminimax
	}

}
