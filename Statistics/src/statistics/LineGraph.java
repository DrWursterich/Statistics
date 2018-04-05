package statistics;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
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
	private double markingSizeX = 0;
	private double markingSizeY = 0;

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
	 * Defines a marking for a {@link LineGraph LineGraph}.
	 * @author Mario Schaeper
	 */
	public final class Marking {
		private final int amountX;
		private final int amountY;
		private final int digitsX;
		private final int commaDigitsX;
		private final int digitsY;
		private final int commaDigitsY;
		private final double markingLength;
		private final Font font;

		protected Marking(int amountX, int amountY, int digitsX, int commaDigitsX,
				int digitsY, int commaDigitsY, double markingLength, Font font) {
			this.amountX = amountX;
			this.amountY = amountY;
			this.digitsX = digitsX;
			this.commaDigitsX = commaDigitsX;
			this.digitsY = digitsY;
			this.commaDigitsY = commaDigitsY;
			this.markingLength = markingLength;
			this.font = font;
		}

		/**
		 * Returns the amount of marks on the X-axis.
		 * @return the amount of marks on the X-axis.
		 */
		public int getAmountX() {
			return amountX;
		}

		/**
		 * Returns the amount of marks on the Y-axis.
		 * @return the amount of marks on the Y-axis.
		 */
		public int getAmountY() {
			return amountY;
		}

		/**
		 * Returns the amount of digits in front of the comma to display at a mark on the X-axis
		 * @return the amount of digits in front of the comma to display at a mark on the X-axis
		 */
		public int getDigitsX() {
			return digitsX;
		}

		/**
		 * Returns the amount of digits behind the comma to display at a mark on the X-axis
		 * @return the amount of digits behind the comma to display at a mark on the X-axis
		 */
		public int getCommaDigitsX() {
			return commaDigitsX;
		}

		/**
		 * Returns the amount of digits in front of the comma to display at a mark on the Y-axis
		 * @return the amount of digits in front of the comma to display at a mark on the Y-axis
		 */
		public int getDigitsY() {
			return digitsY;
		}

		/**
		 * Returns the amount of digits behind the comma to display at a mark on the Y-axis
		 * @return the amount of digits behind the comma to display at a mark on the Y-axis
		 */
		public int getCommaDigitsY() {
			return commaDigitsY;
		}

		/**
		 * Returns the length of a mark
		 * @return the length of a mark
		 */
		public double getMarkingLength() {
			return markingLength;
		}

		/**
		 * Returns the font
		 * @return the font
		 */
		public Font getFont() {
			return font;
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
		this.addLine(this.xScale, this.yScale, this.xScale, this.yScale-this.height, SCALE_STROKE, this.scale);
		this.addLine(this.xScale, this.yScale, this.xScale+this.width, this.yScale, SCALE_STROKE, this.scale);
	}

	public LineGraph(double x, double y, double width, double height,
			double xStart, double xEnd, double yStart, double yEnd, Marking marking) {
		this(x, y, width, height, xStart, xEnd, yStart, yEnd);
		this.addMarking(marking);
	}

	/**
	 * Neccessary for factory functions for inner classes.
	 */
	private LineGraph() {}

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
	 * Adds a {@link Marking Marking} to the scale.
	 * @param marking the marking to add
	 */
	public void addMarking(Marking marking) {
		int xMarkings = marking.getAmountX()-1;
		int yMarkings = marking.getAmountY()-1;
		Group group = new Group();
		group.setManaged(false);
		for (int i=0;i<=xMarkings;i++) {
			double markingX = this.xScale+(this.xEnd-this.xStart)*this.xScaleFactor/xMarkings*i;
			addLine(markingX, this.yScale, markingX, this.yScale+marking.getMarkingLength(), SCALE_STROKE, group);
			Text t = new Text(markingX, this.yScale+1.5*marking.getMarkingLength(),
					String.format("% " + marking.getDigitsX() + "." + marking.getCommaDigitsX() + "f",
							this.xStart+(double)i/(double)xMarkings*(this.xEnd-this.xStart)));
			t.setFont(marking.getFont());
			t.relocate(t.getX()-t.getLayoutBounds().getWidth()/2, t.getY());
			group.getChildren().add(t);
			this.markingSizeY = t.getLayoutBounds().getHeight()+1.5*marking.getMarkingLength();
		}
		for (int i=0;i<=yMarkings;i++) {
			double markingY = this.yScale-(this.yEnd-this.yStart)*this.yScaleFactor/yMarkings*i;
			addLine(xScale, markingY, this.xScale-marking.getMarkingLength(), markingY, SCALE_STROKE, group);
			Text t = new Text(this.xScale-1.5*marking.getMarkingLength(), markingY,
					String.format("% " + marking.getDigitsY() + "." + marking.getCommaDigitsY() + "f",
							this.yStart+(double)i/(double)yMarkings*(this.yEnd-this.yStart)));
			t.setFont(marking.getFont());
			t.relocate(t.getX()-t.getLayoutBounds().getWidth(), t.getY()-t.getLayoutBounds().getHeight()/2);
			group.getChildren().add(t);
			this.markingSizeY = t.getLayoutBounds().getWidth()+1.5*marking.getMarkingLength();
		}
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						scale.getChildren().add(group);
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

	/**
	 * Returns the distance from the Y-axis to the left side of a marking including the text.
	 * @return width of markings on the Y-axis
	 */
	public double getMarkingSizeX() {
		return this.markingSizeX;
	}

	/**
	 * Returns the distance from the X-axis to the bottom of a marking including the text.
	 * @return heigth of markings on the X-axis
	 */
	public double getMarkingSizeY() {
		return this.markingSizeY;
	}

	/**
	 * Creates a {@link Marking Marking} for a scale.
	 *
	 * @param amountX the amount of marks on the X-axis
	 * @param amountY the amount of marks on the Y-axis
	 * @param digitsX the amount of digits in front of the comma to display at a mark on the X-axis
	 * @param commaDigitsX the amount of digits behind the comma to display at a mark on the X-axis
	 * @param digitsY the amount of digits in front of the comma to display at a mark on the Y-axis
	 * @param commaDigitsY the amount of digits behind the comma to display at a mark on the Y-axis
	 * @param markingLength the length of a mark
	 * @param font the font to use
	 * @return
	 */
	public static Marking marking(int amountX, int amountY, int digitsX, int commaDigitsX,
			int digitsY, int commaDigitsY, double markingLength, Font font) {
		if (font == null) {
			throw new IllegalArgumentException("The font can not be null");
		}
		return (new LineGraph()).new Marking(amountX, amountY, digitsX,
				commaDigitsX, digitsY, commaDigitsY, markingLength, font);
	}

	/**
	 * Adds a {@link javafx.scene.shape.Line Line} with a individual strokeWidth to a group.
	 * @param startX the horizontal coordinate of the start point of the line segment
	 * @param startY the vertical coordinate of the start point of the line segment
	 * @param endX the horizontal coordinate of the end point of the line segment
	 * @param endY the vertical coordinate of the end point of the line segment
	 * @param strokeWidth the stroke width of the line
	 * @param group the group to add the line to
	 */
	private void addLine(double startX, double startY, double endX, double endY, double strokeWidth, Group group) {
		Line line = new Line(startX, startY, endX, endY);
		line.setStrokeWidth(SCALE_STROKE);
		group.getChildren().add(line);
	}
}
