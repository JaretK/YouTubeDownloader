/**
 * License: GNU GPL
 * This file is part of YouTubeDownloader.

    YouTubeDownloader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FooYouTubeDownloader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with YouTubeDownloader.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * WARNING: This code is exceptionally ugly. I don't expect anyone to be able to read it at this point
 * , even though I tried to name variables well and have my logic be easy to follow. There are a lot of 
 * multiple nested methods and code consolidation could be useful here. 
 * 
 * 
 * @author jkarnuta
 *
 */
public class MainViewer extends Application {

	/*
	 * controls debug operations throughout the program:
	 * sets grid lines to active
	 */
	private static final boolean DEBUG = false;

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
	 * instance variables for the text field contents and textArea and progressBar
	 */
	private String songField, artistField, optionsField;
	private TextArea textArea;
	private ProgressBar progressBar;
	private Button runBTN;
	private static final int MAX_PROGRESS = 100; 
	private HBox addHBOX;
	private SpinningState oldState;
	private boolean programRunning = false;
	private boolean isMinimized = true;
	private Stage pStage;
	private Task<Void> pyTask;
	private boolean pulseToTerminate = false;
	private static final String initialOptionsText = "e.g. ytid";

	/*
	 * logging object and associated DateFormat
	 */
	private Logger myLogger;
	private String LOGGER_FILE = "/tmp/YTDL_Logger.log";
	private static final DateFormat lOG_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	/*
	 * associated python script variables
	 */
	private String PYTHON_SCRIPT = "Resources/ytAudio.py";
	private List<String> PY_COMMANDS;
	private String FILE_TYPE = ".mp3";
	private String ITUNES_FILEPATH = "/Volumes/Macintosh HD/Media/iTunes Library/Automatically Add to iTunes.localized";
	private String TEMP_FILEPATH = "/tmp/ytAudio_temp";

	public static void main(String[] args) {
		launch(args);
	}
	/*
	 * Supplemented with:
	 * http://docs.oracle.com/javase/8/javafx/get-started-tutorial/form.htm
	 */
	@Override
	public void start(Stage primaryStage) {
		// init instance variables
		oldState= SpinningState.BEFORE;
		isMinimized = true;
		pStage = primaryStage;

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

		MenuItem Exit = new MenuItem("Exit");
		Exit.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				myLogger.info("Exit");
				ForceExit();
			}
		});
		MenuItem restart = new MenuItem("Restart");
		restart.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				myLogger.info("Restart");
				ForceRestart();
			}
		});
		fileMenu.getItems().addAll(Exit, restart);

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
		TextField optionsTextField = new TextField(initialOptionsText);
		grid.add(optionsTextField, 1, 3);

		/*
		 * Make the exit and run buttons
		 */
		HBox buttonsHBOX = new HBox(SPACING);
		Button quitBTN = new Button("Exit");
		runBTN = new Button("Run");
		buttonsHBOX.setAlignment(Pos.BOTTOM_CENTER);
		buttonsHBOX.getChildren().addAll(quitBTN, runBTN);
		cleanUpHBOX(buttonsHBOX);

		grid.add(buttonsHBOX,0,4,2,1);

		/*
		 * Add event handlers to the buttons using anonymous functions
		 */
		quitBTN.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent e){
				ForceExit();
			}

		});
		runBTN.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				//get textFields
				songField = songNameTextField.getText();
				artistField = artistNameTextField.getText();
				String optionsText = optionsTextField.getText(); 
				optionsField = (optionsText.equals(initialOptionsText)) ? "" : optionsText;

				//check contents. Options field is optional and therefore can be omitted from check
				if(!fieldsValid(songField, artistField)){
					//records the problematic field
					List<String> errList = new ArrayList<String>();
					if(!fieldsValid(songField)) errList.add("Song Field");
					if(!fieldsValid(artistField)) errList.add("Artist Field");

					myLogger.log(Level.SEVERE, "A TextBox field parameter is missing("+errList.toString()+"). Clearing and starting over");
					String error = "A TextBox field parameter is missing\n"+
					"Check: "+errList;
					clearFields(songNameTextField, artistNameTextField, optionsTextField);
					EasyDialogs.makeAlertDialog("Alert: Invalid Arguments",error);
					return;
				}
				//remove anything in grid 0,4 slot 
				ObservableList<Node> gridList = grid.getChildren();
				if(gridList.contains(progressBar)) gridList.remove(progressBar);
				if(gridList.contains(addHBOX)) gridList.remove(addHBOX);

				addSpinningProgressControl(grid,"Extracting YouTube URLs and Preparing to Download");

				//clear textFields
				songNameTextField.clear();
				artistNameTextField.clear();
				optionsTextField.clear();

				//only make the text Area and minimize button if maximized
				//problematic because I create a new textArea each time, but efficiency can be improved later
				if(isMinimized){
					isMinimized = false;
					//if clicked, add new TextArea -> ScrollPane to scene
					textArea = new TextArea();
					textArea.setWrapText(false);
					//width,height
					textArea.setPrefSize(400, 300);
					//add changelistener

					progressBar = new ProgressBar();
					progressBar.setPrefWidth(400);

					grid.getChildren().remove(buttonsHBOX);
					grid.add(textArea, 0, 5, 2, 1);
					primaryStage.sizeToScene();

					//make minimize button
					Button minimizeBTN = new Button("Minimize");
					minimizeBTN.setOnAction(new EventHandler<ActionEvent>(){
						//removes scrollpane and minimize button
						public void handle(ActionEvent event) {
							isMinimized = true;
							buttonsHBOX.getChildren().remove(minimizeBTN);
							
							if(programRunning){
								grid.getChildren().remove(textArea);
								grid.getChildren().remove(buttonsHBOX);
								grid.add(buttonsHBOX, 0, 5, 2, 1);
								Button restoreBTN = new Button("Restore");
								restoreBTN.setOnAction(new EventHandler<ActionEvent>(){
									@Override
									public void handle(ActionEvent event) {
										grid.getChildren().remove(buttonsHBOX);
										grid.add(textArea, 0, 5, 2,1);
										grid.add(buttonsHBOX,0,6,2,1);
										buttonsHBOX.getChildren().remove(restoreBTN);
										buttonsHBOX.getChildren().add(minimizeBTN);
									}
								});
								buttonsHBOX.getChildren().add(restoreBTN);
								cleanUpHBOX(buttonsHBOX);
							}
							else{
								grid.getChildren().remove(buttonsHBOX);
								grid.getChildren().remove(textArea);
								grid.getChildren().remove(getNodeAtIndex(grid,0,4));
								grid.add(buttonsHBOX,0,4,2,1);
							}
							
							primaryStage.sizeToScene();
						}
					});
				
					buttonsHBOX.getChildren().add(minimizeBTN);
					cleanUpHBOX(buttonsHBOX);
					grid.add(buttonsHBOX,0,6,2,1);
				}
				else{
					textArea.clear();
				}

				makeCommandList(
						songField, artistField, optionsField,
						FILE_TYPE,
						ITUNES_FILEPATH,
						TEMP_FILEPATH);
				try {
					handToPython(grid, PY_COMMANDS);
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
		scene.getStylesheets().add("mainViewer.css");
		primaryStage.setScene(scene);
		primaryStage.show();


		//displays the gridLines for debugging
		//adds value to 
		if(DEBUG){
			grid.setGridLinesVisible(true);
			songNameTextField.appendText("Intoxicated");
			artistNameTextField.appendText("Martin Solveig");
			optionsTextField.setText("ytid = 94Rq2TX0wj4");
		}
	}

	private void handToPython(GridPane grid, List<String> command) throws IOException, InterruptedException{
		if(command.size() == 0){
			myLogger.log(Level.SEVERE, "Hand off to python failed. Command(s) missing (length = "+command.size()+")");
			System.err.println("Argument missing in call. Args length: "+command.size());
		}
		//disgusting, but it gets the job done
		pyTask = new Task<Void>(){			
			@Override
			protected Void call() throws Exception {
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(command);
				Process proc = pb.start();
				ParseStreamGobbler outGob = new ParseStreamGobbler(proc.getInputStream(), myLogger, "[Pafy Download]");
				ErrorStreamGobbler errGob = new ErrorStreamGobbler(proc.getErrorStream(), myLogger);
				outGob.commence();
				errGob.commence();
				runBTN.setDisable(true);
				while(!pulseToTerminate && (programRunning = proc.isAlive())){
					boolean outGobEmpty = outGob.isEmpty();
					boolean errGobEmpty = errGob.isEmpty();
					updateProgress(outGob.getProgress(), MAX_PROGRESS);
					SpinningState currentState = outGob.getSpinState();

					if(currentState == SpinningState.DOWNLOADING && oldState == SpinningState.BEFORE){
						oldState = currentState;
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								addProgressBar(grid, 0, 4);
							}
						});
					}
					else if(currentState == SpinningState.ALTERING && oldState == SpinningState.DOWNLOADING){
						oldState = currentState;
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								grid.getChildren().remove(progressBar);
								addSpinningProgressControl(grid, "Converting and Moving Audio File");
							}
						});
					}
					else if(currentState == SpinningState.AFTER && oldState == SpinningState.ALTERING){
						oldState = currentState;
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								addCompleteText(grid);
							}
						});
					}

					if(!outGobEmpty || !errGobEmpty){
						if(!outGobEmpty){
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									textArea.appendText(outGob.dump());
								}
							});
						}
						if(!errGobEmpty){
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									textArea.getStyleClass().add("MainTextArea");
									textArea.appendText(errGob.dump());
									textArea.appendText("Error! Terminating Run.\n");
								}
							});
							pulseToTerminate = true;
							break;
						}
					}
				}
				if(pulseToTerminate){
					myLogger.warning("Terminating streams");
					outGob.terminate();
					errGob.terminate();
					proc.destroyForcibly();
					pulseToTerminate = false;
					programRunning = false;
					runBTN.setDisable(false);
					return null;
				}
				int exitVal = proc.waitFor();
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						if(!outGob.isEmpty())
							textArea.appendText(outGob.dump());
						textArea.appendText("Python run exited with error code "+exitVal+"\n");
					}
				});
				outGob.terminate();
				errGob.terminate();
				runBTN.setDisable(false);
				return null;
			}
		};
		progressBar.progressProperty().bind(pyTask.progressProperty());
		Thread newThread = new Thread(pyTask);
		newThread.start();

	}

	private Node getNodeAtIndex(GridPane grid, int col, int row){
		ObservableList<Node> obsList= grid.getChildren();
		for(Node nd : obsList){
			if(grid.getRowIndex(nd) == row && grid.getColumnIndex(nd) == col){
				return nd;
			}
		}
		return null;
	}

	private void addText(GridPane grid, String Text, int col, int row){
		Node toReplace = getNodeAtIndex(grid, 0, 4);
		grid.getChildren().remove(toReplace);
		Label newLabel = new Label(Text);
		newLabel.setWrapText(true);
		HBox HBOXtoAdd = new HBox(SPACING);
		HBOXtoAdd.setAlignment(Pos.CENTER);
		HBOXtoAdd.getChildren().add(newLabel);

		grid.add(HBOXtoAdd, col, row, 2, 1);
	}

	private void addCompleteText(GridPane grid){
		addText(grid,songField + " by "+artistField
				+" downloaded successfully",0,4);
	}

	private void addSpinningProgressControl(GridPane grid, String labelText){
		addHBOX = new HBox(SPACING);
		Label downloadingState = new Label(labelText);
		ProgressIndicator progressIndicator = new ProgressIndicator();
		addHBOX.getChildren().addAll(downloadingState, progressIndicator);
		addHBOX.setAlignment(Pos.CENTER);
		grid.add(addHBOX, 0, 4, 2, 1);
		pStage.sizeToScene();
	}

	private void addProgressBar(GridPane grid, int col, int row){
		grid.getChildren().remove(addHBOX);
		grid.add(progressBar, col, row, 2, 1);
		pStage.sizeToScene();
	}

	/**
	 * Order must be "song", "artist", "options", "file_type", "itunes location", "tmp location"
	 * @param commands
	 */
	@SuppressWarnings("serial")
	private void makeCommandList(String... commands){
		ArrayList<String> listCommand = new ArrayList<String>(){{
			add("python");
			add("-u");
			add(PYTHON_SCRIPT);
		}};
		for (int i = 0; i < commands.length; i++)
			listCommand.add(commands[i]);
		PY_COMMANDS = listCommand;
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

	private void ForceExit(){
		//Exit. May throw a "ava has been detached already"
		//error, but that isn't a problem 
		Platform.exit();
		System.exit(0);
	}
	
	private void ForceRestart(){
		//asks program to halt execution and restarts
		pulseToTerminate = true;
		pStage.close();
		start(new Stage());
	}

	private void initLogger(){
		Handler loggerHandler;
		try {
			loggerHandler = new FileHandler(LOGGER_FILE);
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