package statistics;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;
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
	private static final double POINT_RADIUS = 2.5;
	private static final double SCALE_STROKE = 2.5;
	private static int graphCount = 0;
	private Group scale = new Group();
	private ArrayList<Graph> graphs = new ArrayList<Graph>();
	private Line line;
	private double xScale;
	private double yScale;
	private double width;
	private double height;
	private double xStart;
	private double xEnd;
	private double yStart;
	private double yEnd;
	private double xScaleFactor;
	private double yScaleFactor;

	protected final class Point {
		private final double x;
		private final double y;

		protected Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		protected double getX() {
			return this.x;
		}

		protected double getY() {
			return this.y;
		}

		protected double getRelativeX() {
			return xScale+(this.x-xStart)*xScaleFactor;
		}

		protected double getRelativeY() {
			return yScale-(this.y-yStart)*yScaleFactor;
		}
	}

	protected final class Graph {
		private Paint color;
		private ArrayList<Point> points = new ArrayList<Point>();
		private Group group = new Group();

		protected Graph(Paint color) {
			this.color = color;
			group.setManaged(false);
		}

		protected Paint getColor() {
			return this.color;
		}

		protected void addPoint(Point point) throws Exception {
			this.points.add(point);
			this.updateGroup();
		}

		protected Group getGroup() {
			return this.group;
		}

		private void updateGroup() throws Exception {
			Task<Void> task = new Task<Void>() {
				@Override protected Void call() throws Exception {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (points.size() != 0) {
								Circle circle = new Circle(points.get(0).getRelativeX(),
										points.get(0).getRelativeY(), POINT_RADIUS, color);
								Tooltip.install(circle, new Tooltip(points.get(0).getX() + " | " + points.get(0).getY()));
								group.getChildren().add(circle);
								for (int i=1;i<points.size();i++) {
									circle = new Circle(points.get(i).getRelativeX(),
											points.get(i).getRelativeY(), POINT_RADIUS, color);
									Tooltip.install(circle, new Tooltip(points.get(i).getX() + " | " + points.get(i).getY()));
									group.getChildren().add(circle);
									line = new Line(points.get(i-1).getRelativeX(), points.get(i-1).getRelativeY(),
													points.get(i).getRelativeX(), points.get(i).getRelativeY());
									line.setStroke(color);
									group.getChildren().add(line);
								}
							}
						}
					});
					return null;
				}
			};
			task.run();
		}
	}

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
		this.xScale = x;
		this.yScale = y;
		this.width = width;
		this.height = height;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.xScaleFactor = this.width/(this.xEnd-this.xStart);
		this.yScaleFactor = this.height/(this.yEnd-this.yStart);
		this.scale.setManaged(false);
		this.line = new Line(this.xScale, this.yScale, this.xScale, this.yScale-this.height);
		this.line.setStrokeWidth(SCALE_STROKE);
		this.scale.getChildren().add(line);
		this.line = new Line(this.xScale, this.yScale, this.xScale+this.width, this.yScale);
		this.line.setStrokeWidth(SCALE_STROKE);
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
		Graph graph = new Graph(color);
		for (int i=0;i<coordinates.length;i++) {
			if (coordinates[i].length != 2) {
				throw new IllegalArgumentException("Coordinates have to consist of two values");
			}
			graph.addPoint(new Point(coordinates[0][0], coordinates[0][1]));
		}
		graphs.add(graph);
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
		if (coordinates.length != 2) {
			throw new IllegalArgumentException("Coordinates have to consist of two values");
		}
		graphs.get(graph).addPoint(new Point(coordinates[0], coordinates[1]));
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
		return this.graphs.get(graph).getGroup();
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
		for (Graph g : this.graphs) {
			group.getChildren().add(g.getGroup());
		}
		return group;
	}
}
