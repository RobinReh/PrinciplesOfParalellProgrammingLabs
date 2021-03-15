package amazed.solver;

import amazed.maze.Maze;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <code>ForkJoinSolver</code> implements a solver for <code>Maze</code> objects
 * using a fork/join multi-thread depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */

public class ForkJoinSolver extends SequentialSolver {
	/**
	 * Creates a solver that searches in <code>maze</code> from the start node to a
	 * goal.
	 *
	 * @param maze the maze to be searched
	 */
	public ForkJoinSolver(Maze maze) {
		super(maze);
	}

	/**
	 * Creates a solver that searches in <code>maze</code> from the start node to a
	 * goal, forking after a given number of visited nodes.
	 *
	 * @param maze      the maze to be searched
	 * @param forkAfter the number of steps (visited nodes) after which a parallel
	 *                  task is forked; if <code>forkAfter &lt;= 0</code> the solver
	 *                  never forks new tasks
	 */
	public ForkJoinSolver(Maze maze, int forkAfter) {
		this(maze);
		this.forkAfter = forkAfter;
		visited = new ConcurrentSkipListSet<Integer>();
		done = false;
	}
	
	public ForkJoinSolver(Maze maze, int forkAfter, int start, Set<Integer> visited) {
		this(maze);
		this.start = start;
		this.visited = visited;
	}
	//is the task done or not
	static volatile boolean done;

	/**
	 * Searches for and returns the path, as a list of node identifiers, that goes
	 * from the start node to a goal node in the maze. If such a path cannot be
	 * found (because there are no goals, or all goals are unreacheable), the method
	 * returns <code>null</code>.
	 *
	 * @return the list of node identifiers from the start node to a goal node in
	 *         the maze; <code>null</code> if such a path cannot be found.
	 */
	@Override
	public List<Integer> compute() {
		return parallelSearch();
	}

	private List<Integer> parallelSearch() {
		int steps = 0;
		ArrayList<ForkJoinSolver> forks = new ArrayList<ForkJoinSolver>();
		// one player active on the maze at start
		int player = maze.newPlayer(start);
		// start with start node
		frontier.push(start);
		// as long as not all nodes have been processed
		while (!frontier.empty() && !done) {
			// get the new node to process
			int current = frontier.pop();
			// if current node has a goal
			if (maze.hasGoal(current)) {
				// move player to goal
				maze.move(player, current);
				done = true;
				// search finished: reconstruct and return path
				return pathFromTo(this.start, current);
			}
			// Depending if the node is in the list or not return true or false and add it in the list
			if (visited.add(current)) {
				steps++;
				// move player to current node
				maze.move(player, current);
				
				List<Integer> tempList = new ArrayList<>();
				// for every node nb adjacent to current put it in a temporary list
				for (int nb : maze.neighbors(current)) {
					if (!visited.contains(nb)) {
						tempList.add(nb);
						// add the node to predecessor to show that we can walk there
						predecessor.put(nb, current);
					}
				}
				//check how many nodes we need to visit and if its greater then 1 fork and let other thread run or fork the thread if
				//steps is greater then forkAfter
				for (int i = 0; i < tempList.size(); i++) {
					if (i == 0 || steps <= forkAfter) {
						frontier.push(tempList.get(i));
					} else {
						steps = 0;
						ForkJoinSolver forker = new ForkJoinSolver(maze, forkAfter, tempList.get(i), visited);
						forks.add(forker);
						forker.fork();
					}

				}
			}
		}

		//check every threads path and add it together 
		List<Integer> found;
		for (ForkJoinSolver f : forks) {
			found = f.join();
			if (found != null) {
				List<Integer> path = pathFromTo(start, found.get(0));
				found.remove(0);
				path.addAll(found);
				return path;
			}
		}
		// all nodes explored, no goal found
		return null;

	}
}
