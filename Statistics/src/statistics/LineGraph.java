package statistics;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.concurrent.Task;
import javafx.application.Platform;

/**
 * This class represents a simple line graph based on
 * <a href="https://docs.oracle.com/javase/8/javafx/api/toc.htm?is-external=true" title="javafx">
 * <code>javafx</code></a>.
 *
 * @author Mario Schaeper
 */
@SuppressWarnings("restriction")
public class LineGraph {
	private static int graphCount = 0;
	private Group scale;
	private ArrayList<Group> graphs = new ArrayList<Group>();
	private ArrayList<Paint> graphColors = new ArrayList<Paint>();
	private Line line;
	private double x;
	private double y;
	private double width;
	private double height;
	private double xStart;
	private double xEnd;
	private double yStart;
	private double yEnd;
	private double xScaleFactor;
	private double yScaleFactor;

	/**
	 * Creates a line graph scale at x, y with the given width and height.
	 * Start and End represent the unitsizes for each axis.
	 *
	 * @param x X-Coordinate of the origin
	 * @param y Y-Coordinate of the origin
	 * @param width Width of the graph
	 * @param height Height of the graph
	 * @param xStart Min X-value
	 * @param xEnd Max X-value
	 * @param yStart Max Y-value
	 * @param yEnd Min Y-value
	 */
	public LineGraph(double x, double y, double width, double height,
			double xStart, double xEnd, double yStart, double yEnd) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.xScaleFactor = this.width/(this.xEnd-this.xStart);
		this.yScaleFactor = this.height/(this.yEnd-this.yStart);
		this.scale = new Group();
		this.scale.setManaged(false);
		this.line = new Line(this.x, this.y, this.x, this.y-this.height);
		this.line.setStrokeWidth(2.5);
		this.scale.getChildren().add(line);
		this.line = new Line(this.x, this.y, this.x+this.width, this.y);
		this.line.setStrokeWidth(2.5);
		this.scale.getChildren().add(line);
	}

	/**
	 * Adds a graph to the scale.<br/>
	 * Coordinates are arrays with x and y values.
	 *
	 * @param coordinates array of coordinates
	 * @param color the color
	 * @return index of the graph
	 * @throws Exception
	 */
	public int addGraph(double[][] coordinates, Paint color) throws Exception {
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {
				Group graph = new Group();
				graph.setManaged(false);
				if (coordinates.length == 1) {
					if (coordinates[0].length != 2) {
						throw new IllegalArgumentException("Coordinates have to consist of two values");
					}
					graph.getChildren().add(new Line((coordinates[0][0]-xStart)*xScaleFactor,
						(coordinates[0][1]-yStart)*yScaleFactor,
						(coordinates[0][0]-xStart)*xScaleFactor,
						(coordinates[0][1]-yStart)*yScaleFactor));
				}
				for (int i=1;i<coordinates.length;i++) {
					if (coordinates[i].length != 2) {
						throw new IllegalArgumentException("Coordinates have to consist of two values");
					}
					line = new Line(x+(coordinates[i-1][0]-xStart)*xScaleFactor,
							y-(coordinates[i-1][1]-yStart)*yScaleFactor,
							x+(coordinates[i][0]-xStart)*xScaleFactor,
							y-(coordinates[i][1]-yStart)*yScaleFactor);
					line.setStroke(color);
					graph.getChildren().add(line);
				}
				graphs.add(graph);
				graphColors.add(color);
				return null;
			}
		};
		task.run();
		return LineGraph.graphCount++;
	}

	/**
	 * Adds an empty graph to the scale.
	 *
	 * @param color the color
	 * @return index of the graph
	 * @throws Exception
	 */
	public int addGraph(Paint color) throws Exception {
		return this.addGraph(new double[][] {}, color);
	}

	/**
	 * Extends an existing graph by one point.
	 * Coordinates are an array with the x and y values.
	 *
	 * @param graph the index of the graph to extend
	 * @param coordinates the coordinates of the point to add
	 * @throws Exception
	 */
	public void extendGraph(int graph, double[] coordinates) throws Exception {
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (graphs.get(graph).getChildren().size() == 0) {
							line = new Line(
									x+xScaleFactor*(coordinates[0]-xStart),
									y-yScaleFactor*(coordinates[1]-yStart),
									x+xScaleFactor*(coordinates[0]-xStart),
									y-yScaleFactor*(coordinates[1]-yStart));
							line.setStroke(graphColors.get(graph));
							graphs.get(graph).getChildren().add(line);
						}
						line = (Line)(graphs.get(graph).getChildren().get(graphs.get(graph).getChildren().size()-1));
						line = new Line(line.getEndX(), line.getEndY(),
								x+xScaleFactor*(coordinates[0]-xStart),
								y-yScaleFactor*(coordinates[1]-yStart));
						line.setStroke(graphColors.get(graph));
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								graphs.get(graph).getChildren().add(line);
							}
						});
					}
				});
				return null;
			}
		};
		task.run();
	}

	/**
	 * Returns the scale in a
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 *
	 * @return the scale
	 */
	public Group getScaleGroup() {
		return this.scale;
	}

	/**
	 * Returns a graph without the scale in a
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 *
	 * @param graph the index of the graph
	 * @return the graph
	 * @throws IndexOutOfBoundsException if the graph index does not exist
	 */
	public Group getGraphGroup(int graph) throws IndexOutOfBoundsException {
		return this.graphs.get(graph);
	}

	/**
	 * Returns all graphs and the scale in a
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 *
	 * @return the graphs and scale
	 */
	public Group getCompleteGroup() {
		Group group = new Group();
		group.setManaged(false);
		group.getChildren().add(this.scale);
		for (Group g : this.graphs) {
			group.getChildren().add(g);
		}
		return group;
	}
}
