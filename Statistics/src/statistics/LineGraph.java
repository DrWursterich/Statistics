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
	private Group scaleGroup = new Group();
	private Group markingGroup = new Group();
	private Group completeGroup = new Group();
	private ArrayList<Graph> graphs = new ArrayList<Graph>();
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
	private double markingSizeX = 0;
	private double markingSizeY = 0;

	private Task<Void> updateGroupsLater = new Task<Void>() {
		@Override protected Void call() throws Exception {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					updateGroups();
				}
			});
			return null;
		}
	};

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

		protected void updateGroup() throws Exception {
			Task<Void> task = new Task<Void>() {
				@Override protected Void call() throws Exception {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (points.size() != 0) {
								Circle circle = new Circle(points.get(0).getRelativeX(),
										points.get(0).getRelativeY(), graphPointRadius, color);
								Tooltip.install(circle, new Tooltip(points.get(0).getX() + " | " + points.get(0).getY()));
								group.getChildren().add(circle);
								for (int i=1;i<points.size();i++) {
									circle = new Circle(points.get(i).getRelativeX(),
											points.get(i).getRelativeY(), graphPointRadius, color);
									Tooltip.install(circle, new Tooltip(points.get(i).getX() + " | " + points.get(i).getY()));
									group.getChildren().add(circle);
									Line line = new Line(points.get(i-1).getRelativeX(), points.get(i-1).getRelativeY(),
													points.get(i).getRelativeX(), points.get(i).getRelativeY());
									line.setStroke(color);
									line.setStrokeWidth(graphStrokeWidth);
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

		this.scaleGroup.setManaged(false);
		this.markingGroup.setManaged(false);
		this.completeGroup.setManaged(false);
		this.updateGroups();
	}

	public LineGraph(double x, double y, double width, double height,
			double xStart, double xEnd, double yStart, double yEnd, Marking marking) {
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
	public int addGraph(double[][] coordinates, Paint color) throws Exception {
		Graph graph = new Graph(color);
		for (int i=0;i<coordinates.length;i++) {
			if (coordinates[i].length != 2) {
				throw new IllegalArgumentException("Coordinates have to consist of two values");
			}
			graph.addPoint(new Point(coordinates[0][0], coordinates[0][1]));
		}
		graphs.add(graph);
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
	 * @return the X-coordinate of the origin.
	 */
	public double getX() {
		return this.xScale;
	}

	/**
	 * Returns the Y-coordinate of the origin.
	 * @return the Y-coordinate of the origin.
	 */
	public double getY() {
		return this.yScale;
	}

	/**
	 * Returns the stroke width of all scale elements.<br/>
	 * Default value: <b>2.5</b>
	 * @return the stroke width of all scale elements.
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
	 * @return the radius of points in graphs.
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
	public void relocate(double x, double y) {
		this.xScale = x;
		this.yScale = y;
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
	 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html?is-external=true" title="javafx.Group">
	 * <code>Group</code></a>.
	 *
	 * @return the scale
	 */
	public Group getScaleGroup() {
		return this.scaleGroup;
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
		return this.completeGroup;
	}

	/**
	 * Returns the distance from the Y-axis to the left side of a marking including the text.
	 * @return width of markings on the Y-axis
	 */
	public double getMarkingSizeX() {
		if (this.marking != null) {
			this.updateGroups();
		}
		return this.markingSizeX;
	}

	/**
	 * Returns the distance from the X-axis to the bottom of a marking including the text.
	 * @return heigth of markings on the X-axis
	 */
	public double getMarkingSizeY() {
		if (this.marking != null) {
			this.updateGroups();
		}
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
	 * Reconstructs all groups asynchronously.<br/>
	 * If a parameter changes, e.g. by moving the graph, the groups have to be
	 * build acording to these changes.<br/>
	 * This method allows for this to happen in a different Threads.
	 *
	 * TODO differentiate wether this has to be used or not.
	 * fx threads should not be able to run the tast synchronously, but somehow are.
	 */
	@SuppressWarnings("unused")
	private void updateGroupsLater() {
		boolean canRun = true;
		if (updateGroupsLater.isRunning()) {
			canRun = updateGroupsLater.cancel();
		}
		if (canRun) {
			updateGroupsLater.run();
		}
	}

	/**
	 * Reconstructs all groups.<br/>
	 * If a parameter changes, e.g. by moving the graph, the groups have to be
	 * build acording to these changes.
	 */
	private void updateGroups() {
		scaleGroup.getChildren().clear();
		completeGroup.getChildren().clear();
		markingGroup.getChildren().clear();
		addLine(xScale, yScale, xScale, yScale-height, scaleStrokeWidth, scaleGroup);
		addLine(xScale, yScale, xScale+width, yScale, scaleStrokeWidth, scaleGroup);
		if (marking != null) {
			int xMarkings = marking.getAmountX()-1;
			int yMarkings = marking.getAmountY()-1;
			for (int i=0;i<=xMarkings;i++) {
				double markingX = xScale+(xEnd-xStart)*xScaleFactor/xMarkings*i;
				addLine(markingX, yScale, markingX, yScale+marking.getMarkingLength(), scaleStrokeWidth, markingGroup);
				Text t = new Text(markingX, yScale+1.5*marking.getMarkingLength(),
						String.format("% " + marking.getDigitsX() + "." + marking.getCommaDigitsX() + "f",
								xStart+(double)i/(double)xMarkings*(xEnd-xStart)));
				t.setFont(marking.getFont());
				t.relocate(t.getX()-t.getLayoutBounds().getWidth()/2, t.getY());
				markingGroup.getChildren().add(t);
				markingSizeY = t.getLayoutBounds().getHeight()+1.5*marking.getMarkingLength();
			}
			for (int i=0;i<=yMarkings;i++) {
				double markingY = yScale-(yEnd-yStart)*yScaleFactor/yMarkings*i;
				addLine(xScale, markingY, xScale-marking.getMarkingLength(), markingY, scaleStrokeWidth, markingGroup);
				Text t = new Text(xScale-1.5*marking.getMarkingLength(), markingY,
						String.format("% " + marking.getDigitsY() + "." + marking.getCommaDigitsY() + "f",
								yStart+(double)i/(double)yMarkings*(yEnd-yStart)));
				t.setFont(marking.getFont());
				t.relocate(t.getX()-t.getLayoutBounds().getWidth(), t.getY()-t.getLayoutBounds().getHeight()/2);
				markingGroup.getChildren().add(t);
				markingSizeX = t.getLayoutBounds().getWidth()+1.5*marking.getMarkingLength();
			}
		}
		scaleGroup.getChildren().add(markingGroup);
		completeGroup.getChildren().add(scaleGroup);
		for (Graph g : graphs) {
			try {
				g.updateGroup();
			} catch (Exception e) {}
			completeGroup.getChildren().add(g.getGroup());
		}
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
		line.setStrokeWidth(scaleStrokeWidth);
		group.getChildren().add(line);
	}
}
