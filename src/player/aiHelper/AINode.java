package player.aiHelper;

import java.util.Set;
import scotlandyard.Move;

/**
 * Interface for node of game tree
 */
public interface AINode {
	
	/**
	 * Calculates a score for the current game state
	 * @return score of game state
	 */
	public double score();
	
	public double getScore();
	
	public void setScore(double score);
	
	public void simulateMove();
	
	public Set<Move> getSuccessorOptions();
	
	public AINode copy(Move move);
	
	public int getDepth();
	
	public void setDepth(int depth);
	
	public boolean isTerminal();
	
	public Move getUsedMove();
	
}
