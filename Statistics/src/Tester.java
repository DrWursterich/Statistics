import statistics.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

@SuppressWarnings("restriction")
public class Tester extends Application {
	private static StackPane layout = new StackPane();
	private static LineGraph graph = new LineGraph(50, 350, 600, 300, 0, 25, 0, 40);

	public static void main(String...args) {
		graph.setMarking(LineGraph.marking(6, 11, 2, 0, 2, 0, 6,
				Font.font("verdana", FontWeight.LIGHT, FontPosture.REGULAR, 10)));
		graph.addGraph(new double[][] {{0, 0}, {1, 1}, {2, 4}}, Color.RED);
		try {
			Thread t = new Thread() {
				@Override
				public void run() {
					for (int i=0;i<25;i++) {
						if (i%2==1) {
							graph.setYScale(graph.getYStart(), graph.getYEnd()-1);
						}
						try {
							sleep(600);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							graph.extendGraph(0, (i+3)<24?(3+i):26, i==5?50:8+i);
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
		layout.getChildren().add(graph.getCompleteGroup());
		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
	}
}
