
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.sun.glass.ui.Robot;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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

	/*
	 * Gets the current OS
	 */
	private static final String OPERATING_SYSTEM = System.getProperty("os.name");

	/*
	 * instance variables for the text field contents and textArea
	 */
	private String songField, artistField, optionsField;
	private TextArea textArea;

	/*
	 * synchronized queues for handling StreamGobblers
	 */
	private Queue<String> errQueue, outQueue;

	/*
	 * logging object and associated DateFormat
	 */
	private Logger myLogger;
	private static final DateFormat lOG_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	public static void main(String[] args) {
		launch(args);
	}
	/*
	 * Supplemented with:
	 * http://docs.oracle.com/javase/8/javafx/get-started-tutorial/form.htm
	 */
	@Override
	public void start(Stage primaryStage) {
		//make logger and filehandler (to tmp directory 'cause its just to diagnose errors for myself)
		myLogger = Logger.getLogger(this.getClass().getName());
		initLogger();
		/*
		 * make boarderpane root
		 */
		BorderPane bpRoot = new BorderPane();

		//Set the title for our primary stage
		primaryStage.setTitle("Welcome to YouTubeDownloader");

		//Create the GridPane layout
		GridPane grid = new GridPane();
		bpRoot.setCenter(grid);
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


		/*
		 * Make MenuBar
		 */
		MenuBar menuBar = new MenuBar();

		Menu fileMenu = new Menu("File");
		Menu prefMenu = new Menu("Preferences");

		menuBar.getMenus().addAll(fileMenu, prefMenu);
		bpRoot.setTop(menuBar);

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

		/*
		 * Make the exit and run buttons
		 */
		HBox buttonsHBOX = new HBox(SPACING);
		Button quitBTN = new Button("Exit");
		Button runBTN = new Button("Run");
		buttonsHBOX.setAlignment(Pos.BOTTOM_CENTER);
		buttonsHBOX.getChildren().addAll(quitBTN, runBTN);
		cleanUpHBOX(buttonsHBOX);

		grid.add(buttonsHBOX,0,4,2,1);

		/*
		 * Add event handlers to the buttons using anonymous functions
		 */
		quitBTN.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent e){
				//Exit. May throw a "ava has been detached already"
				//error, but that isn't a problem 
				Platform.exit();
			}

		});
		runBTN.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				//get textFields
				songField = songNameTextField.getText();
				artistField = artistNameTextField.getText();
				optionsField = optionsTextField.getText();

				//check contents. Options field is optional and therefore can be omitted from check
				if(!fieldsValid(songField, artistField)){
					//records the problematic field
					List<String> errList = new ArrayList<String>();
					if(!fieldsValid(songField)) errList.add("Song Field");
					if(!fieldsValid(artistField)) errList.add("Artist Field");

					myLogger.log(Level.SEVERE, "A TextBox field parameter is missing("+errList.toString()+"). Clearing and starting over");
					System.err.println("A TextBox field parameter is missing");
					System.err.print("Check: ");
					System.err.println(errList);
					clearFields(songNameTextField, artistNameTextField, optionsTextField);
					return;
				}

				//clear textFields
				songNameTextField.clear();
				artistNameTextField.clear();
				optionsTextField.clear();

				//only make the text Area and minimize button if maximized
				//problematic because I create a new textArea each time, but efficiency can be improved later
				if(textArea == null){
					//if clicked, add new TextArea -> ScrollPane to scene
					textArea = new TextArea();
					textArea.setWrapText(false);
					//width,height
					textArea.setPrefSize(400, 400);
					//add changelistener


					grid.getChildren().remove(buttonsHBOX);
					grid.add(textArea,0,4,2,1);
					primaryStage.sizeToScene();

					//make minimize button
					Button minimizeBTN = new Button("Minimize");
					minimizeBTN.setOnAction(new EventHandler<ActionEvent>(){
						//removes scrollpane and minimize button
						public void handle(ActionEvent event) {
							grid.getChildren().remove(buttonsHBOX);
							grid.getChildren().remove(textArea);
							grid.add(buttonsHBOX,0,4,2,1);
							buttonsHBOX.getChildren().remove(minimizeBTN);
							textArea = null;
							primaryStage.sizeToScene();
						}

					});
					buttonsHBOX.getChildren().add(minimizeBTN);
					cleanUpHBOX(buttonsHBOX);
					grid.add(buttonsHBOX,0,5,2,1);
				}
				//USE PYTHON SUBPROCESS TO REDIRECT PRINT STATEMENTS?
				@SuppressWarnings("serial")
				List<String> pyCommands = new ArrayList<String>(){{
					add("python");
					add("-u");
					add("Resources/ytdl_test.py");
					add(songField);
					add(artistField);
					add(optionsField);
				}};
				try {
					handToPython(pyCommands);
				} catch (IOException | InterruptedException e) {
					myLogger.severe(e.getMessage());
				}
			}

		});
		/*
		 * If ENTER is pressed, clicks the runBTN to activate python
		 */
		grid.setOnKeyPressed(new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ENTER)
					runBTN.fire();
			}

		});

		/*
		 * Scene constructor details:
		 * Scene(Parent root, double width, double height)
		 * Creates a scene for a specific root Node with a specific size
		 * 
		 * When width and height are not set, defaults to minimum size needed
		 * to display all the elements
		 */
		//Scene scene = new Scene(grid, 300, 275);
		Scene scene = new Scene(bpRoot);
		primaryStage.setScene(scene);

		primaryStage.show();


		//displays the gridLines for debugging
		//adds value to 
		if(DEBUG){
			grid.setGridLinesVisible(true);
			songNameTextField.appendText("Test Song Name");
			artistNameTextField.appendText("Test Artist Name");
			optionsTextField.appendText("Option 1 = test 1, Option 2 = test 2");
		}
	}

	private void handToPython(List<String> command) throws IOException, InterruptedException{

		if(command.size() == 0){
			myLogger.log(Level.SEVERE, "Hand off to python failed. Command(s) missing (length = "+command.size()+")");
			System.err.println("Argument missing in call. Args length: "+command.size());
		}
		//disgusting, but it gets the job done
		Task<Void> pyTask = new Task<Void>(){
			@Override
			protected Void call() throws Exception {
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(command);
				Process proc = pb.start();
				StreamGobbler outGob = new StreamGobbler(proc.getInputStream(),myLogger);
				outGob.start();
				while(proc.isAlive())
					if(!outGob.isEmpty()){
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								textArea.appendText(outGob.dump());
							}
						});
					}
				int exitVal = proc.waitFor();
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						if(!outGob.isEmpty())
							textArea.appendText(outGob.dump());
						textArea.appendText("Python run exited with error code "+exitVal);
					}
				});
				outGob.terminate();
				return null;
			}

		};
		Thread newThread = new Thread(pyTask);
		newThread.start();

	}
	/**
	 * Clears all TextFields inputted into the method
	 * @param fields: TextFields to be cleared
	 */
	private void clearFields(TextField... fields){
		for (int i = 0; i < fields.length; i++)
			fields[i].clear();
	}

	/**
	 * 
	 * @param fields: string value of textfields being checked 
	 * @return false if any field is equal to an empty string 
	 */
	private boolean fieldsValid(String... fields){
		for (int i = 0; i < fields.length; i++)
			if(fields[i].equals("")) return false;
		return true;
	}

	/**
	 * Loops through the elements of the input box and changes their fields
	 * inputBox should be sufficiently small enough that optimization is 
	 * unnecessary
	 * @param inputBox, HBox to clean up
	 */
	private void cleanUpHBOX(HBox inputBox){
		for (Node nodeEle : inputBox.getChildren()){
			Button buttonEle = (Button) nodeEle;
			buttonEle.setPrefSize(100,20);
			buttonEle.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		}
	}

	private void appendLine(String toAppend){
		textArea.appendText(toAppend+"\n");
	}

	private void initLogger(){
		Handler loggerHandler;
		try {
			loggerHandler = new FileHandler("/tmp/YTDL_Logger.log");
			loggerHandler.setFormatter(new Formatter(){
				@Override
				public String format(LogRecord record) {
					StringBuilder builder = new StringBuilder(1000);
					builder.append("(");
					builder.append(lOG_DATE_FORMAT.format(new Date(record.getMillis()))).append(") - ");
					builder.append("[").append(record.getSourceClassName()).append(".");
					builder.append(record.getSourceMethodName()).append("]\n");
					builder.append("<").append(record.getLevel()).append("> - ");
					builder.append(formatMessage(record));
					builder.append("\n");
					return builder.toString();
				}
			});
			loggerHandler.setLevel(Level.ALL);
			myLogger.addHandler(loggerHandler);
			myLogger.info("YouTubeDownloader Logging File Initialized");
			myLogger.info("Running on "+OPERATING_SYSTEM);
			if(!OPERATING_SYSTEM.equals("Mac OS X")){
				myLogger.warning("This is an untested platform. Proceed with caution");
				System.err.println(OPERATING_SYSTEM + " is an untested platform");
			}

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}