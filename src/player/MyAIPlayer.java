package player;

import java.io.IOException;
import java.util.Random;
import java.util.Set;

import player.aiHelper.MyAIGameState;
import player.aiHelper.AINode;
import player.aiHelper.Minimax;
import player.aiHelper.Minimax.AIType;
import scotlandyard.Colour;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.Player;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

/**
 * AI for MrX. Implements the Player and Spectator interface.
 */
public class MyAIPlayer implements Player, Spectator {

	private ScotlandYardView view;
	private Graph<Integer, Route> graph;
	private String graphFilename;

	/**
	 * Constructor that takes a a view of the game and the filename of the graph
	 * file.
	 * 
	 * @param view
	 *            A view of the game
	 * @param graphFilename
	 */
	public MyAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try {
			graph = reader.readGraph(graphFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new model based on data from the view and executes the
	 * alphabeta algorithm with iterative deepening. If an error occurs, it chooses a random move.
	 * 
	 * @param location
	 * @param moves
	 */
	@Override
	public Move notify(int location, Set<Move> moves) {
		System.out.println("***LOCATION: " + location);

		AINode currentState = MyAIGameState.createGameState(null, view, location, graph, graphFilename);
		Move selectedMove = null;

		try {
			selectedMove = Minimax.executeMinimax(currentState, Colour.Black, AIType.AlphaBeta);
			// If error occurs, fall back to random moves!
			if (selectedMove == null)
				throw new NullPointerException("Error detected! Playing random move!");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			int random = (new Random()).nextInt(moves.size());
			for (Move move : moves) {
				if (random == 0) {
					selectedMove = move;
					break;
				}
				random--;
			}
		}

		return selectedMove;
	}

	@Override
	public void notify(Move paramMove) {

	}
}
