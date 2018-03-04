import statistics.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
//import javafx.scene.control.*;
//import javafx.scene.paint.*;
//import javafx.scene.shape.*;
//import javafx.event.*;

@SuppressWarnings("restriction")
public class Tester extends Application {
	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		LineGraph graph = new LineGraph(50, 350, 600, 300, 0, 25, 0, 40);
		graph.addGraph(new double[][] {{0, 0}, {1, 1}, {2, 4}}, Color.RED);
		graph.extendGraph(0, new double[] {3, 8});
		StackPane layout = new StackPane();
		layout.getChildren().add(graph.getAllGroup());
		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
	}
}
