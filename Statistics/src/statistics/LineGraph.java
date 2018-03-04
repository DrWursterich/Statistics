package statistics;

import java.util.ArrayList;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;

@SuppressWarnings("restriction")
public class LineGraph {
	private Group scale;
	private ArrayList<Group> graphs = new ArrayList<Group>();
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

	public void addGraph(double[][] coordinates, Paint color) {
		Group graph = new Group();
		graph.setManaged(false);
		for (int i=1;i<coordinates.length;i++) {
			this.line = new Line(this.x+coordinates[i-1][0]*this.xScaleFactor,
					this.y-coordinates[i-1][1]*this.yScaleFactor,
					this.x+coordinates[i][0]*this.xScaleFactor,
					y-coordinates[i][1]*this.yScaleFactor);
			this.line.setStroke(color);
			graph.getChildren().add(line);
		}
		this.graphs.add(graph);
	}

	public void extendGraph(int graph, double[] coordinates) throws IndexOutOfBoundsException {
		line = (Line)(this.graphs.get(graph).getChildren().get(this.graphs.get(graph).getChildren().size()-1));
		Paint lineColor = line.getStroke();
		line = new Line(line.getEndX(), line.getEndY(),
				this.x+this.xScaleFactor*coordinates[0],
				this.y-this.yScaleFactor*coordinates[1]);
		line.setStroke(lineColor);
		this.graphs.get(graph).getChildren().add(line);
	}

	public Group getScaleGroup() {
		return this.scale;
	}

	public Group getGraphGroup(int graph) throws IndexOutOfBoundsException {
		return this.graphs.get(graph);
	}

	public Group getAllGroup() {
		Group group = new Group();
		group.setManaged(false);
		group.getChildren().add(this.scale);
		for (Group g : this.graphs) {
			group.getChildren().add(g);
		}
		return group;
	}
}
