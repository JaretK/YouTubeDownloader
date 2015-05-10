
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainViewer extends Application {

	/*
	 * controls debug operations throughout the program:
	 * sets grid lines to active
	 */
	private static final boolean DEBUG = true;
	/*
	 * Controls the H and V spacing of the grid and the spacing of the 
	 * buttons HBox
	 */
	private static final double SPACING = 10;

	public static void main(String[] args) {
		launch(args);
	}
	/*
	 * Taken from:
	 * http://docs.oracle.com/javase/8/javafx/get-started-tutorial/form.htm
	 */
	@Override
	public void start(Stage primaryStage) {
		//Set the title for our primary stage
		primaryStage.setTitle("Welcome to YouTubeDownloader");

		//Create the GridPane layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_CENTER);
		grid.setHgap(SPACING);//H width between cols
		grid.setVgap(SPACING);//V height between rows
		/*
		 * adds padding inset
		 * constructor details:
		 * Insets(double top, double right, double bottom, double left)
		 * doubles represent pixels of padding
		 */
		grid.setPadding(new Insets(25,25,25,25));

		//Add the scenetitle and text fields
		Text scenetitle = new Text("Enter Song Information");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		/*
		Described within https://docs.oracle.com/javafx/2/api/javafx/scene/layout/GridPane.html as:

		add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan)
		Adds a child to the gridpane at the specified column,row position and spans.

		*/
		grid.add(scenetitle, 0,0,2,1);

		Label songName = new Label("Song Name:");
		grid.add(songName, 0, 1);
		TextField songNameTextField = new TextField();
		grid.add(songNameTextField, 1, 1);

		Label artistName = new Label("Artist Name:");
		grid.add(artistName, 0, 2);
		TextField artistNameTextField = new TextField();
		grid.add(artistNameTextField, 1, 2);

		Label options = new Label("Options: ");
		grid.add(options, 0,3);
		TextField optionsTextField = new TextField();
		grid.add(optionsTextField, 1, 3);

		//displays the gridLines for debugging
		grid.setGridLinesVisible(DEBUG);
		
		/*
		 * Make the exit and run buttons
		 */
		HBox buttonsHBOX = new HBox(SPACING);
		Button quitBTN = new Button("Exit");
		quitBTN.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		Button runBTN = new Button("Run");
		runBTN.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		buttonsHBOX.setAlignment(Pos.BOTTOM_CENTER);
		buttonsHBOX.getChildren().addAll(quitBTN, runBTN);
		grid.add(buttonsHBOX,0,4,2,1);

		/*
		 * Scene constructor details:
		 * Scene(Parent root, double width, double height)
		 * Creates a scene for a specific root Node with a specific size
		 * 
		 * When width and height are not set, defaults to minimum size needed
		 * to display all the elements
		 */
		//Scene scene = new Scene(grid, 300, 275);
		Scene scene = new Scene(grid);
		primaryStage.setScene(scene);

		primaryStage.show();
	}
}