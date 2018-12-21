package statistics;

import java.util.ArrayList;
import java.awt.geom.Line2D;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
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
 * <a href="https://docs.oracle.com/javase/8/javafx/api/toc.htm
 * 	?is-external=true" title="javafx">
 * <code>javafx</code></a>.
 * @author Mario Schaeper
 */
public class LineGraph extends VBox {
	private Group scaleGroup = new Group();
	private Group markingGroup = new Group();
	private ArrayList<Graph> graphs = new ArrayList<>();
	private Marking marking = null;
	private int graphCount = 0;
	private double scaleStrokeWidth = 2.5;
	private double graphStrokeWidth = 1;
	private double graphPointRadius = 2.5;
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
			return LineGraph.this.xScale
					+ (this.x - LineGraph.this.xStart)
					* LineGraph.this.xScaleFactor;
		}

		protected double getRelativeY() {
			return LineGraph.this.yScale
					- (this.y - LineGraph.this.yStart)
					* LineGraph.this.yScaleFactor;
		}

		protected boolean isInGraph() {
			return this.x >= LineGraph.this.xStart
					&& this.x <= LineGraph.this.xEnd
					&& this.y >= LineGraph.this.yStart
					&& this.y <= LineGraph.this.yEnd;
		}

		private double getDistance(Point other) {
			return Math.sqrt(
					Math.pow(this.getX() - other.getX(), 2)
					+ Math.pow(this.getY()-other.getY(), 2));
		}

		private Point getLineIntersection(
				double x1, double x2, double x3, double x4,
				double y1, double y2, double y3, double y4) {
			double n = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
			// Intersection might be outside of the start- and end-points
			if (n != 0
					&& Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
				return new Point(
						((x1 * y2 - y1 * x2)
								* (x3 - x4) - (x1 - x2)
								* (x3 * y4 - y3 * x4)
							) / n,
						((x1 * y2 - y1 * x2)
								* (y3 - y4) - (y1 - y2)
								* (x3 * y4 - y3 * x4)
							) / n);
			}
			return null;
		}

		protected Line getLineTo(Point other, Paint color) {
			Point startIntersection = this;
			Point endIntersection = other;
			Point[] intersections = new Point[4];

			if (!this.isInGraph() || !other.isInGraph()) {
				intersections[0] = this.getLineIntersection(
						this.getX(), other.getX(),
						LineGraph.this.xStart, LineGraph.this.xEnd,
						this.getY(), other.getY(),
						LineGraph.this.yEnd, LineGraph.this.yEnd);
				intersections[1] = this.getLineIntersection(
						this.getX(), other.getX(),
						LineGraph.this.xStart, LineGraph.this.xEnd,
						this.getY(), other.getY(),
						LineGraph.this.yStart, LineGraph.this.yStart);
				intersections[2] = this.getLineIntersection(
						this.getX(), other.getX(),
						LineGraph.this.xStart, LineGraph.this.xStart,
						this.getY(), other.getY(),
						LineGraph.this.yStart, LineGraph.this.yEnd);
				intersections[3] = this.getLineIntersection(
						this.getX(), other.getX(),
						LineGraph.this.xEnd, LineGraph.this.xEnd,
						this.getY(), other.getY(),
						LineGraph.this.yStart, LineGraph.this.yEnd);
			}

			for (int i=intersections.length-1;i>=0;i--) {
				if (intersections[i] != null) {
					if (!this.isInGraph()) {
						startIntersection = startIntersection == this
								? intersections[i]
								: (this.getDistance(intersections[i])
										< this.getDistance(startIntersection)
									? intersections[i]
									: startIntersection);
					}
					if (!other.isInGraph()) {
						endIntersection = endIntersection == other
								? intersections[i]
								: (other.getDistance(intersections[i])
										< other.getDistance(endIntersection)
									? intersections[i]
									: endIntersection);
					}
				}
			}

			// The Line might be out of sight
			if ((!this.isInGraph()
						|| !other.isInGraph())
					&& (startIntersection == this
						&& endIntersection == other)) {
				return null;
			}

			Line line = new Line(
					startIntersection.getRelativeX(),
					startIntersection.getRelativeY(),
					endIntersection.getRelativeX(),
					endIntersection.getRelativeY());
			line.setStroke(color);
			line.setStrokeWidth(LineGraph.this.graphStrokeWidth);

			return line;
		}

		protected Circle getCircle(Paint color) {
			Circle circle = new Circle(
					this.getRelativeX(),
					this.getRelativeY(),
					LineGraph.this.graphPointRadius, color);
			Tooltip.install(circle, new Tooltip(this.x + " | " + this.y));
			return circle;
		}
	}

	protected final class Graph {
		private Paint color;
		private ArrayList<Point> points = new ArrayList<>();
		private Group group = new Group();

		protected Graph(Paint color) {
			this.color = color;
			this.group.setManaged(false);
		}

		protected Paint getColor() {
			return this.color;
		}

		protected void addPoint(Point point) {
			this.points.add(point);
			this.updateGroup();
		}

		protected Group getGroup() {
			return this.group;
		}

		protected void updateGroup() {
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Platform.runLater(() -> {
						final ArrayList<Point> points = Graph.this.points;
						final Group group = Graph.this.group;
						group.getChildren().clear();
						if (points.size() != 0) {
							for (int i = 0; i < points.size(); i++) {
								if (points.get(i).isInGraph()) {
									group.getChildren().add(
											points.get(i).getCircle(
												Graph.this.color));
								}
								if (i > 0) {
									Line line = points.get(i - 1).getLineTo(
											points.get(i), Graph.this.color);
									if (line != null) {
										group.getChildren().add(line);
									}
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
		private final double length;
		private final Font font;

		protected Marking(
				int amountX,
				int amountY,
				int digitsX,
				int commaDigitsX,
				int digitsY,
				int commaDigitsY,
				double markingLength,
				Font font) {
			this.amountX = amountX;
			this.amountY = amountY;
			this.digitsX = digitsX;
			this.commaDigitsX = commaDigitsX;
			this.digitsY = digitsY;
			this.commaDigitsY = commaDigitsY;
			this.length = markingLength;
			this.font = font;
		}

		/**
		 * Returns the amount of marks on the X-axis.
		 * @return the amount of marks on the X-axis.
		 */
		public int getAmountX() {
			return this.amountX;
		}

		/**
		 * Returns the amount of marks on the Y-axis.
		 * @return the amount of marks on the Y-axis.
		 */
		public int getAmountY() {
			return this.amountY;
		}

		/**
		 * Returns the amount of digits in front of the comma
		 * to display at a mark on the X-axis
		 * @return the amount of digits in front of the comma
		 * 	to display at a mark on the X-axis
		 */
		public int getDigitsX() {
			return this.digitsX;
		}

		/**
		 * Returns the amount of digits behind the comma
		 * to display at a mark on the X-axis
		 * @return the amount of digits behind the comma
		 * 	to display at a mark on the X-axis
		 */
		public int getCommaDigitsX() {
			return this.commaDigitsX;
		}

		/**
		 * Returns the amount of digits in front of the comma
		 * to display at a mark on the Y-axis
		 * @return the amount of digits in front of the comma
		 * 	to display at a mark on the Y-axis
		 */
		public int getDigitsY() {
			return this.digitsY;
		}

		/**
		 * Returns the amount of digits behind the comma
		 * to display at a mark on the Y-axis
		 * @return the amount of digits behind the comma
		 * 	to display at a mark on the Y-axis
		 */
		public int getCommaDigitsY() {
			return this.commaDigitsY;
		}

		/**
		 * Returns the length of a mark
		 * @return the length of a mark
		 */
		public double getLength() {
			return this.length;
		}

		/**
		 * Returns the font
		 * @return the font
		 */
		public Font getFont() {
			return this.font;
		}

		public double getSizeX(LineGraph graph) {
			final Text text = new Text(String.format(
					"% " + this.digitsX + "." + this.commaDigitsX + "f",
					Math.pow(10, this.digitsX - 1)));
			text.setFont(this.font);
			return text.getLayoutBounds().getHeight()
					+ 1.5 * this.length;
		}

		public double getSizeY(LineGraph graph) {
			final Text text = new Text(String.format(
					"% " + this.digitsY + "." + this.commaDigitsY + "f",
					Math.pow(10, this.digitsX - 1)));
			text.setFont(this.font);
			return text.getLayoutBounds().getWidth()
					+ 1.5 * this.length;
		}
	}

	/**
	 * Creates a line graph scale at x, y with the given width and height.<br/>
	 * Start and End represent the unitsizes for each axis.
	 *
	 * @param x X-Coordinate of the origin
	 * @param y Y-Coordinate of the origin
	 * @param width Width of the graph
	 * @param height Height of the graph
	 * @param xStart Min X-value
	 * @param xEnd Max X-value
	 * @param yStart Min Y-value
	 * @param yEnd Max Y-value
	 */
	public LineGraph(
			double x, double y,
			double width, double height,
			double xStart, double xEnd,
			double yStart, double yEnd) {
		this.xScale = x;
		this.yScale = y;
		this.width = width;
		this.height = height;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.xScaleFactor = this.width / (this.xEnd - this.xStart);
		this.yScaleFactor = this.height / (this.yEnd - this.yStart);

		this.setManaged(false);
		this.setWidth((this.xStart - this.xEnd) * this.xScaleFactor);
		this.setHeight((this.yStart - this.yEnd) * this.yScaleFactor);
		this.scaleGroup.setManaged(false);
		this.markingGroup.setManaged(false);
		this.updateGroups();
	}

	/**
	 * Creates a line graph scale at x, y with the given width and height.<br/>
	 * Start and End represent the unitsizes for each axis.
	 *
	 * @param x X-Coordinate of the origin
	 * @param y Y-Coordinate of the origin
	 * @param width Width of the graph
	 * @param height Height of the graph
	 * @param xStart Min X-value
	 * @param xEnd Max X-value
	 * @param yStart Min Y-value
	 * @param yEnd Max Y-value
	 * @param marking a marking object to use
	 */
	public LineGraph(
			double x, double y,
			double width, double height,
			double xStart, double xEnd,
			double yStart, double yEnd,
			Marking marking) {
		this(x, y, width, height, xStart, xEnd, yStart, yEnd);
		this.setMarking(marking);
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
	public int addGraph(double[][] coordinates, Paint color) {
		Graph graph = new Graph(color);
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i].length != 2) {
				throw new IllegalArgumentException(
						"Coordinates have to consist of two values");
			}
			graph.addPoint(new Point(coordinates[i][0], coordinates[i][1]));
		}
		this.graphs.add(graph);
		this.updateGroups();
		return this.graphCount++;
	}

	/**
	 * Adds an empty graph to the scale.
	 *
	 * @param color the color
	 * @return index of the graph
	 * @throws Exception
	 */
	public int addGraph(Paint color) {
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
	public void extendGraph(int graph, double...coordinates) {
		if (coordinates.length != 2) {
			throw new IllegalArgumentException(
					"Coordinates have to consist of two values");
		}
		this.graphs.get(graph).addPoint(
				new Point(coordinates[0], coordinates[1]));
	}

	/**
	 * Sets the {@link Marking Marking} for the scale.<br/>
	 * <b>null</b> can be used to unset the marking.
	 * @param marking the marking to set
	 */
	public void setMarking(Marking marking) {
		this.marking = marking;
		this.updateGroups();
	}

	/**
	 * Removes the marking.
	 */
	public void removeMarking() {
		this.setMarking(null);
	}

	/**
	 * Returns the X-coordinate of the origin.
	 * @return the X-coordinate of the origin
	 */
	public double getX() {
		return this.xScale;
	}

	/**
	 * Returns the Y-coordinate of the origin.
	 * @return the Y-coordinate of the origin
	 */
	public double getY() {
		return this.yScale;
	}

	/**
	 * Returns the min value of the scale on the X-axis.
	 * @return the min value of the scale on the X-axis
	 */
	public double getXStart() {
		return this.xStart;
	}

	/**
	 * Returns the max value of the scale on the X-axis.
	 * @return the max value of the scale on the X-axis
	 */
	public double getXEnd() {
		return this.xEnd;
	}

	/**
	 * Returns the min value of the scale on the Y-axis.
	 * @return the min value of the scale on the Y-axis
	 */
	public double getYStart() {
		return this.yStart;
	}

	/**
	 * Returns the max value of the scale on the Y-axis.
	 * @return the max value of the scale on the Y-axis
	 */
	public double getYEnd() {
		return this.yEnd;
	}

	/**
	 * Returns the stroke width of all scale elements.<br/>
	 * Default value: <b>2.5</b>
	 * @return the stroke width of all scale elements
	 */
	public double getScaleStrokeWidth() {
		return this.scaleStrokeWidth;
	}

	/**
	 * Returns the stroke width of all graphs.<br/>
	 * Default value: <b>1</b>
	 * @return the stroke width of graphs
	 */
	public double getGraphStrokeWidth() {
		return this.graphStrokeWidth;
	}

	/**
	 * Returns the radius of points in graphs.<br/>
	 * Default value: <b>2.5</b>
	 * @return the radius of points in graphs
	 */
	public double getGraphPointRadius() {
		return this.graphPointRadius;
	}

	/**
	 * Moves the graph to a new X-coordinate.
	 * @param x the new X-coordinate of the origin
	 */
	public void setX(double x) {
		this.xScale = x;
		this.updateGroups();
	}

	/**
	 * Moves the graph to a new Y-coordinate.
	 * @param y the new Y-coordinate of the origin
	 */
	public void setY(double y) {
		this.yScale = y;
		this.updateGroups();
	}

	/**
	 * Moves the graph to a different location.
	 * @param x the new X-coordinate of the origin.
	 * @param y the new Y-coordinate of the origin
	 */
	@Override
	public void relocate(double x, double y) {
		this.xScale = x;
		this.yScale = y;
		this.updateGroups();
	}

	/**
	 * Changes the min value on the scale of the X-axis.
	 * @param xStart Min X-value
	 */
	public void setXStart(double xStart) {
		this.xStart = xStart;
		this.updateGroups();
	}

	/**
	 * Changes the max value on the scale of the X-axis.
	 * @param xEnd Max X-value
	 */
	public void setXEnd(double xEnd) {
		this.xEnd = xEnd;
		this.updateGroups();
	}

	/**
	 * Changes the min value on the scale of the Y-axis.
	 * @param yStart Min Y-value
	 */
	public void setYStart(double yStart) {
		this.yStart = yStart;
		this.yScaleFactor = this.height / (this.yEnd - this.yStart);
		this.updateGroups();
	}

	/**
	 * Changes the max value on the scale of the Y-axis.
	 * @param yEnd Max Y-value
	 */
	public void setYEnd(double yEnd) {
		this.yEnd = yEnd;
		this.yScaleFactor = this.height / (this.yEnd - this.yStart);
		this.updateGroups();
	}

	/**
	 * Changes the scale of the X-axis.
	 * @param xStart Min Y-value
	 * @param xEnd Max Y-value
	 */
	public void setXScale(double xStart, double xEnd) {
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.xScaleFactor = this.width / (this.xEnd - this.xStart);
		this.yScaleFactor = this.height / (this.yEnd - this.yStart);
		this.updateGroups();
	}

	/**
	 * Changes the scale of the Y-axis.
	 * @param yStart Min Y-value
	 * @param yEnd Max Y-value
	 */
	public void setYScale(double yStart, double yEnd) {
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.xScaleFactor = this.width / (this.xEnd - this.xStart);
		this.yScaleFactor = this.height / (this.yEnd - this.yStart);
		this.updateGroups();
	}

	/**
	 * Changes the stroke width of all scale elements.
	 * @param scaleStrokeWidth the new stroke width
	 */
	public void setScaleStrokeWidth(double scaleStrokeWidth) {
		this.scaleStrokeWidth = scaleStrokeWidth;
		this.updateGroups();
	}

	/**
	 * Changes the stroke width of all graphs.
	 * @param graphStrokeWidth the new stroke width
	 */
	public void setGraphStrokeWidth(double graphStrokeWidth) {
		this.graphStrokeWidth = graphStrokeWidth;
		this.updateGroups();
	}

	/**
	 * Changes the radius of points in all graphs.<br/>
	 * A radius of <b>0</b> removes them entirely.
	 * @param graphPointRadius
	 */
	public void setGraphPointradius(double graphPointRadius) {
		this.graphPointRadius = graphPointRadius;
		this.updateGroups();
	}

	/**
	 * Returns the scale in a
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/
	 * 	Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 * @return the scale
	 */
	public Group getScaleGroup() {
		return this.scaleGroup;
	}

	/**
	 * Returns a graph without the scale in a
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/
	 * 	Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 * @param graph the index of the graph
	 * @return the graph
	 * @throws IndexOutOfBoundsException if the graph index does not exist
	 */
	public Group getGraphGroup(int graph) throws IndexOutOfBoundsException {
		return this.graphs.get(graph).getGroup();
	}

	/**
	 * Returns the distance from the Y-axis
	 * to the left side of a marking including the text.
	 * @return width of markings on the Y-axis
	 */
	public double getMarkingSizeX() {
		return this.marking != null ? this.marking.getSizeX(this) : 0;
	}

	/**
	 * Returns the distance from the X-axis
	 * to the bottom of a marking including the text.
	 * @return heigth of markings on the X-axis
	 */
	public double getMarkingSizeY() {
		return this.marking != null ? this.marking.getSizeY(this) : 0;
	}

	/**
	 * Creates a {@link Marking Marking} for a scale.
	 *
	 * @param amountX the amount of marks on the X-axis
	 * @param amountY the amount of marks on the Y-axis
	 * @param digitsX the amount of digits in front of the comma
	 * 	to display at a mark on the X-axis
	 * @param commaDigitsX the amount of digits behind the comma
	 * 	to display at a mark on the X-axis
	 * @param digitsY the amount of digits in front of the comma
	 * 	to display at a mark on the Y-axis
	 * @param commaDigitsY the amount of digits behind the comma
	 * 	to display at a mark on the Y-axis
	 * @param length the length of a mark
	 * @param font the font to use
	 * @return
	 */
	public static Marking marking(
			int amountX, int amountY,
			int digitsX, int commaDigitsX,
			int digitsY, int commaDigitsY,
			double markingLength, Font font) {
		if (font == null) {
			throw new IllegalArgumentException("The font can not be null");
		}
		return (new LineGraph()).new Marking(
				amountX, amountY,
				digitsX, commaDigitsX,
				digitsY, commaDigitsY,
				markingLength, font);
	}

	/**
	 * Reconstructs all groups.<br/>
	 * If a parameter changes, e.g. by moving the graph, the groups have to be
	 * build acording to these changes.
	 */
	private void updateGroups() {
		Platform.runLater(() -> {
			final LineGraph graph = LineGraph.this;
			final Marking marking = graph.marking;
			graph.getChildren().clear();
			graph.scaleGroup.getChildren().clear();
			this.markingGroup.getChildren().clear();
			graph.addLine(
					graph.xScale, graph.yScale,
					graph.xScale, graph.yScale - graph.height,
					graph.scaleStrokeWidth, graph.scaleGroup);
			graph.addLine(
					graph.xScale, graph.yScale,
					graph.xScale + graph.width, graph.yScale,
					graph.scaleStrokeWidth, graph.scaleGroup);
			if (marking != null) {
				int xMarkings = marking.getAmountX() - 1;
				int yMarkings = marking.getAmountY() - 1;
				for (int i = 0; i <= xMarkings; i++) {
					double markingX = graph.xScale
							+ (graph.xEnd - graph.xStart)
								* graph.xScaleFactor / xMarkings * i;
					graph.addLine(
							markingX,
							graph.yScale,
							markingX,
							graph.yScale + marking.getLength(),
							graph.scaleStrokeWidth,
							this.markingGroup);
					Text text = new Text(markingX,
							graph.yScale + 1.5 * marking.getLength(),
							String.format(
									"% " + marking.getDigitsX()
										+ "." + marking.getCommaDigitsX()
										+ "f",
									graph.xStart
										+ (double)i / (double)xMarkings
										* (graph.xEnd - graph.xStart)));
					text.setFont(marking.getFont());
					text.relocate(
							text.getX() - text.getLayoutBounds().getWidth() / 2,
							text.getY());
					this.markingGroup.getChildren().add(text);
				}
				for (int j = 0; j <= yMarkings; j++) {
					double markingY = graph.yScale
							- (graph.yEnd - graph.yStart)
								* graph.yScaleFactor / yMarkings * j;
					graph.addLine(
							graph.xScale, markingY,
							graph.xScale - marking.getLength(), markingY,
							graph.scaleStrokeWidth, this.markingGroup);
					Text text = new Text(
							graph.xScale - 1.5 * marking.getLength(),
							markingY,
							String.format(
									"% " + marking.getDigitsY()
										+ "." + marking.getCommaDigitsY() + "f",
									graph.yStart + (double)j / (double)yMarkings
										* (graph.yEnd - graph.yStart)));
					text.setFont(marking.getFont());
					text.relocate(
							text.getX()
								- text.getLayoutBounds().getWidth(),
							text.getY()
								- text.getLayoutBounds().getHeight() / 2);
					this.markingGroup.getChildren().add(text);
				}
			}
			graph.scaleGroup.getChildren().add(this.markingGroup);
			graph.getChildren().add(graph.scaleGroup);
			for (Graph g : graph.graphs) {
				try {
					g.updateGroup();
				} catch (Exception e) {}
				graph.getChildren().add(g.getGroup());
			}
		});
	}

	/**
	 * Adds a {@link javafx.scene.shape.Line Line}
	 * with a individual strokeWidth to a group.
	 * @param startX the horizontal coordinate
	 * 	of the start point of the line segment
	 * @param startY the vertical coordinate
	 * 	of the start point of the line segment
	 * @param endX the horizontal coordinate
	 * 	of the end point of the line segment
	 * @param endY the vertical coordinate
	 * 	of the end point of the line segment
	 * @param strokeWidth the stroke width of the line
	 * @param group the group to add the line to
	 */
	private void addLine(
			double startX, double startY,
			double endX, double endY,
			double strokeWidth, Group group) {
		Line line = new Line(startX, startY, endX, endY);
		line.setStrokeWidth(this.scaleStrokeWidth);
		group.getChildren().add(line);
	}
}
