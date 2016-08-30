package player;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import player.aiHelper.AIHelper;
import player.aiHelper.AINode;
import player.aiHelper.DetectiveAIGameState;
import player.aiHelper.Minimax;
import player.aiHelper.Minimax.AIType;
import scotlandyard.Colour;
import scotlandyard.Graph;
import scotlandyard.Move;
import scotlandyard.MoveTicket;
import scotlandyard.Node;
import scotlandyard.Player;
import scotlandyard.Route;
import scotlandyard.ScotlandYardGraphReader;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

/**
 * AI for the detectives. In addition to the Player interface, 
 * it also implements the Spectator interface to keep track
 * of moves and calculate MrX's possible locations
 */
public class DetectiveAIPlayer implements Spectator, Player {

	private ScotlandYardView view;
	private Graph<Integer, Route> graph;
	private Set<Integer> updatedPossibleLocations;
	private AIHelper help;
	private String graphFilename;

	/**
	 * Constructor that takes a a view of the game and the filename of the graph
	 * file.
	 * 
	 * @param view
	 *            A view of the game
	 * @param graphFilename
	 */
	public DetectiveAIPlayer(ScotlandYardView view, String graphFilename) {
		this.view = view;
		this.graphFilename = graphFilename;
		updatedPossibleLocations = new HashSet<Integer>();
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try {
			graph = reader.readGraph(graphFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		help = new AIHelper(view, graph);
		Set<Node<Integer>> nodes = graph.getNodes();
		nodes.forEach(node -> updatedPossibleLocations.add(node.data()));
		updatedPossibleLocations.removeAll(help.getDetectiveLocations());
	}

	/**
	 * Creates a new model based on data from the view and executes the
	 * expectiminimax algorithm. If an error occurs, it chooses a random move.
	 * 
	 * @param location
	 * @param moves
	 */
	@Override
	public Move notify(int location, Set<Move> moves) {
		System.out.println("***LOCATION: " + location);

		AINode currentState = DetectiveAIGameState.createGameState(view.getCurrentPlayer(), updatedPossibleLocations,
				null, view, selectRandomLocation(updatedPossibleLocations), graph, graphFilename);
		Move selectedMove = null;
		try {
			selectedMove = Minimax.executeMinimax(currentState, view.getCurrentPlayer(), AIType.Expectiminimax);
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

	private int selectRandomLocation(Set<Integer> set) {
		Random gen = new Random();
		int num = gen.nextInt(set.size());
		for (int n : set) {
			if (num == 0)
				return n;
			num--;
		}
		return 0;
	}

	/**
	 * Keeps track of all the moves and calculates mrX's possible locations
	 */
	@Override
	public void notify(Move move) {
		if (move instanceof MoveTicket) {
			MoveTicket mt = (MoveTicket) move;
			if (move.colour == Colour.Black)
				updatedPossibleLocations = help.calculatePossibleMrXLocations(updatedPossibleLocations, mt.ticket);
			else {
				updatedPossibleLocations.remove(mt.target);
			}
		}
	}

}
