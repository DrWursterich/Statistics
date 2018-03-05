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
	private static StackPane layout = new StackPane();
	private static LineGraph graph = new LineGraph(50, 350, 600, 300, 0, 25, 0, 40);

	public static void main(String...args) {
		try {
			graph.addGraph(new double[][] {{0, 0}, {1, 1}, {2, 4}}, Color.RED);
			layout.getChildren().add(graph.getCompleteGroup());
			Thread t = new Thread() {
				@Override
				public void run() {
					for (int i=0;i<30;i++) {
						try {
							sleep(600);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							graph.extendGraph(0, new double[] {3+i, 8+i});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			try {
				t.start();
			} catch (IllegalThreadStateException e) {
				e.printStackTrace();
			}
			launch(args);
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
	}
}
