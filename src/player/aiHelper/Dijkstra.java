package player.aiHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import scotlandyard.Edge;
import scotlandyard.Graph;
import scotlandyard.Node;
import scotlandyard.Route;

/**
 * 
 * Implementation of Dijkstra's algorithm using a priority queue
 *
 */
public class Dijkstra {

	private Map<Integer, Double> distFromSrc;
	private Map<Integer, Integer> predecessors;
	private Graph<Integer, Route> graph;
	private PriorityQueue<Integer> queue;
	private boolean taxi;
	private boolean bus;
	private boolean under;

	private Dijkstra(Graph<Integer, Route> graph, int source, boolean taxi, boolean bus, boolean under) {
		distFromSrc = new HashMap<Integer, Double>();
		predecessors = new HashMap<Integer, Integer>();
		queue = new PriorityQueue<Integer>(new NodeComparator());
		this.graph = graph;
		this.taxi = taxi;
		this.bus = bus;
		this.under = under;
		distFromSrc.put(source, 0.0);
		for (Node<Integer> node : graph.getNodes()) {
			if (node.data() != source) {
				distFromSrc.put(node.data(), Double.POSITIVE_INFINITY);
				predecessors.put(node.data(), null);
			}
		}
		queue.add(source);
	}

	private void dijkstraRun() {
		while (!queue.isEmpty()) {
			int currentNode = queue.remove();
			for (Edge<Integer, Route> edge : graph.getEdges(currentNode)) {
				if (taxi && edge.data() == Route.Taxi || bus && edge.data() == Route.Bus || under
						&& edge.data() == Route.Underground) {
					int neighbor = edge.other(currentNode);
					// assume all length 1
					double newDist = distFromSrc.get(currentNode) + 1;
					if (newDist < distFromSrc.get(neighbor)) {
						distFromSrc.put(neighbor, newDist);
						predecessors.put(neighbor, currentNode);
						if (!queue.contains(neighbor)) {
							queue.add(neighbor);
						}
					}
				}
			}
		}
	}

	public static Map<Integer, Double> dijkstraRunner(Graph<Integer, Route> graph, int source, boolean taxi,
			boolean bus, boolean under) {
		Dijkstra run = new Dijkstra(graph, source, taxi, bus, under);
		run.dijkstraRun();
		return run.distFromSrc;
	}

	class NodeComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			return distFromSrc.get(o1).compareTo(distFromSrc.get(o2));
		}
	}
}
