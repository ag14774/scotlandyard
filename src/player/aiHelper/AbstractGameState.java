package player.aiHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scotlandyard.Colour;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.Route;
import scotlandyard.ScotlandYardView;
import scotlandyard.Ticket;
import solution.ScotlandYardModel;

/**
 * An AbstractGameState is a representation of the state of the game that
 * implements AINode and all of the node related methods e.g score()
 */
abstract public class AbstractGameState extends ScotlandYardModel implements AINode {

	AIHelper help; // AIHelper provides various methods used in score()
	Graph<Integer, Route> graph;

	private int depth;
	private Move move;
	private double score;
	String graphFilename;

	AbstractGameState(Move move, int detectives, List<Boolean> rounds, Graph<Integer, Route> graph, String graphFilename) {
		super(detectives, rounds, graphFilename);
		this.graph = graph;
		this.move = move;
		this.graphFilename = graphFilename;
		depth = 0;
		score = 0.0;
		help = new AIHelper(this, graph);
	}

	/**
	 * @return the move associated with the game state
	 */
	@Override
	public final Move getUsedMove() {
		return move;
	}

	@Override
	abstract public double score();

	final void simulateMove(Move move) {
		play(move);
	}

	/**
	 * Set MrX's real location to loc
	 * 
	 * @param loc
	 *            The location to set for MrX
	 */
	public final void setMrXLocation(int loc) {
		playerMap.get(Colour.Black).setLocation(loc);
	}

	/**
	 * Play the move associated with the state
	 */
	@Override
	abstract public void simulateMove();

	/**
	 * Create a copy of this state associating a new move with the newly created
	 * state
	 */
	@Override
	abstract public AINode copy(Move move);

	final static Map<Ticket, Integer> createTicketMap(ScotlandYardView view, Colour c) {
		Map<Ticket, Integer> map = new HashMap<Ticket, Integer>();
		for (Ticket ticket : Ticket.values()) {
			map.put(ticket, view.getPlayerTickets(c, ticket));
		}
		return map;
	}

	/**
	 * @return depth at which the node is located
	 */
	@Override
	public final int getDepth() {
		return depth;
	}

	/**
	 * Determines whether the node is terminal or not. That is, if it reached
	 * max depth or game is over.
	 */
	@Override
	public boolean isTerminal() {
		if (isGameOver() || getDepth() >= Minimax.MAX_DEPTH) {
			return true;
		}
		return false;
	}

	/**
	 * Set depth to given value
	 */
	@Override
	public final void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return a set of valid moves for the current player
	 */
	@Override
	public final Set<Move> getSuccessorOptions() {
		return validMoves(getCurrentPlayer());
	}

	/**
	 * 
	 * @return MrX's real location
	 */
	public final int getMrXRealLocation() {
		return this.playerMap.get(Colour.Black).getLocation();
	}

	/**
	 * Returns score if it has already been calculated
	 * If not, calculates score first
	 */
	@Override
	public final double getScore() {
		if (score == 0) {
			return score();
		}
		return score;
	}

	/**
	 * Sets the score to given value
	 */
	@Override
	public final void setScore(double score) {
		this.score = score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 5;
		int result = 1;
		result = prime * result + depth;
		result = prime * result + playerMap.get(Colour.Black).getLocation();
		result = prime * result + playerMap.get(Colour.Blue).getLocation();
		result = prime * result + playerMap.get(Colour.Green).getLocation();
		result = prime * result + playerMap.get(Colour.Red).getLocation();
		result = prime * result + playerMap.get(Colour.White).getLocation();
		result = prime * result + playerMap.get(Colour.Yellow).getLocation();
		result = prime * result + ((move == null) ? 0 : move.hashCode());
		result = prime * result + round;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractGameState))
			return false;
		AbstractGameState other = (AbstractGameState) obj;
		if (depth != other.depth)
			return false;
		if (playerMap.get(Colour.Black).getLocation() != other.playerMap.get(Colour.Black).getLocation())
			return false;
		if (playerMap.get(Colour.Blue).getLocation() != other.playerMap.get(Colour.Blue).getLocation())
			return false;
		if (playerMap.get(Colour.Green).getLocation() != other.playerMap.get(Colour.Green).getLocation())
			return false;
		if (playerMap.get(Colour.Red).getLocation() != other.playerMap.get(Colour.Red).getLocation())
			return false;
		if (playerMap.get(Colour.White).getLocation() != other.playerMap.get(Colour.White).getLocation())
			return false;
		if (playerMap.get(Colour.Yellow).getLocation() != other.playerMap.get(Colour.Yellow).getLocation())
			return false;
		if (move == null) {
			if (other.move != null)
				return false;
		} else if (!move.equals(other.move))
			return false;
		if (round != other.round)
			return false;
		return true;
	}

}
