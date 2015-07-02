/**
 * License: GNU GPL
 * This file is part of YouTubeDownloader.

    YouTubeDownloader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    YouTubeDownloader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with YouTubeDownloader.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
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
public class MainApp extends Application {

	/*
	 * controls debug operations throughout the program:
	 * sets grid lines to active
	 */
	private static final boolean DEBUG = false;

	/*
	 * instance variables for the text field contents and textArea and progressBar
	 */
	private String songField, artistField, optionsField;
	private TextArea textArea;
	private ProgressBar progressBar;
	private Button runBTN;
	private Menu prefMenu;
	private Menu toolsMenu;
	private static final int MAX_PROGRESS = 100; 
	private HBox buttonsHBOX;
	private HBox addHBOX;
	private HBox HBOXtoAdd;
	private SpinningState oldState;

	/*
	 * Other variables (look @ names)
	 */
	private boolean programRunning = false;
	private boolean isMinimized = true;
	private Stage pStage;
	private Task<Void> pyTask;
	private boolean pulseToTerminate = false;
	private static final String initialOptionsText = "ytid=";

	/*
	 * logging object and associated DateFormat
	 */
	private Logger myLogger;
	private String LOGGER_FILE = "/tmp/YTDL_Logger.log";
	private static final DateFormat lOG_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	/*
	 * associated python script variables
	 */
	private String AUDIO_SCRIPT = new ExtractToFile("/ytAudio.zip", "ytAudio.zip", myLogger).getFilePath();
	private String GET_URL_SCRIPT = new ExtractToFile("/ytGetURL.zip","ytGetURL.zip",myLogger).getFilePath();
	private String ytURL;
	private Label adHocLabel;
	private List<String> PY_COMMANDS;
	private String FILE_TYPE;
	private String ITUNES_FILEPATH;
	private String TEMP_FILEPATH;
	private OptionsFromWindow options;

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
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		grid.getColumnConstraints().add(col1);
		bpRoot.setLeft(grid);
		grid.setAlignment(Pos.TOP_CENTER);
		grid.setHgap(Constants.SPACING);//H width between cols
		grid.setVgap(Constants.SPACING);//V height between rows

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
		TextField optionsTextField = new TextField(initialOptionsText);
		grid.add(optionsTextField, 1, 3);



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
				myLogger.info("Restarting " + this.getClass().getName() + " from YouTubeDownloader");
				ForceRestart();
			}
		});
		fileMenu.getItems().addAll(Exit, restart);

		prefMenu = new Menu("Preferences");

		MenuItem updatePrefs = new MenuItem("Update");
		updatePrefs.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				getPreferencesWindow(grid);
			}
		});
		prefMenu.getItems().addAll(updatePrefs);

		toolsMenu = new Menu("Tools");
		MenuItem verifyContents = new MenuItem("Verify Contents");
		verifyContents.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
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
					clearFields(songNameTextField, artistNameTextField, optionsTextField);
					return;
				}
				//remove anything in grid 0,4 slot 
				ObservableList<Node> gridList = grid.getChildren();
				if(gridList.contains(progressBar)) gridList.remove(progressBar);
				if(gridList.contains(addHBOX)) gridList.remove(addHBOX);
				addSpinningProgressControl(grid,"Extracting YouTube URLs and Preparing to Download", true, HBOXtoAdd);

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
								Button restoreBTN = new Button("Restore");
								restoreBTN.setOnAction(new EventHandler<ActionEvent>(){
									@Override
									public void handle(ActionEvent event) {
										grid.getChildren().remove(buttonsHBOX);

										grid.add(textArea, 0, 5, 2,1);
										grid.add(buttonsHBOX,0,6,2,1);
										buttonsHBOX.getChildren().remove(restoreBTN);
										buttonsHBOX.getChildren().add(minimizeBTN);
										pStage.sizeToScene();
									}
								});
								buttonsHBOX.getChildren().add(restoreBTN);
								cleanUpHBOX(buttonsHBOX);
								pStage.sizeToScene();
							}
							else{
								grid.getChildren().remove(textArea);

							}

							primaryStage.sizeToScene();
						}
					});

					buttonsHBOX.getChildren().add(minimizeBTN);
					cleanUpHBOX(buttonsHBOX);
					grid.add(buttonsHBOX,0,6,2,1);
				}

				try {
					getURL(true, grid);
				} catch (InterruptedException | IOException e) {
					myLogger.severe(e.getMessage());
				}

			}
		});
		toolsMenu.getItems().add(verifyContents);

		menuBar.getMenus().addAll(fileMenu, prefMenu, toolsMenu);
		bpRoot.setTop(menuBar);

		/*
		 * Make the exit and run buttons
		 */
		buttonsHBOX = new HBox(Constants.SPACING);
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
					clearFields(songNameTextField, artistNameTextField, optionsTextField);
					return;
				}
				//remove anything in grid 0,4 slot 
				ObservableList<Node> gridList = grid.getChildren();
				if(gridList.contains(progressBar)) gridList.remove(progressBar);
				if(gridList.contains(addHBOX)) gridList.remove(addHBOX);
				if(grid.getChildren().contains(HBOXtoAdd)) grid.getChildren().remove(HBOXtoAdd);
				addSpinningProgressControl(grid,"Extracting YouTube URLs and Preparing to Download", false, null);

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
										textArea.setPrefSize(400, 300);
										grid.add(textArea, 0, 5, 2,1);
										grid.add(buttonsHBOX,0,6,2,1);
										buttonsHBOX.getChildren().remove(restoreBTN);
										buttonsHBOX.getChildren().add(minimizeBTN);
										pStage.sizeToScene();
									}
								});
								System.out.println("restore should be added");
								buttonsHBOX.getChildren().add(restoreBTN);
								cleanUpHBOX(buttonsHBOX);
								pStage.sizeToScene();
							}
							else{
								grid.getChildren().remove(buttonsHBOX);
								grid.getChildren().remove(textArea);
								grid.add(buttonsHBOX,0,5,2,1);
								pStage.sizeToScene();
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
				//				final Animation expand = new Transition() {
				//		            { setCycleDuration(Duration.millis(250)); }
				//		            protected void interpolate(double frac) {
				//		              final double curWidth = expandedWidth * (1.0 - frac);
				//		              setPrefWidth(curWidth);
				//		              setTranslateX(-expandedWidth + curWidth);
				//		            }
				//		          };

				getPreferences();
				try {
					getURL(false, grid);
				} catch (InterruptedException | IOException e) {
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
			songNameTextField.appendText("Ghosts");
			artistNameTextField.appendText("Mako");
			optionsTextField.setText("");
		}
	}

	private void getURL(boolean verifyFirst, GridPane grid) throws InterruptedException, IOException{
		@SuppressWarnings("serial")
		ArrayList<String> listCommand = new ArrayList<String>(){{
			add("python");
			add("-u");
			add(GET_URL_SCRIPT);
			add(songField);
			add(artistField);
			add(optionsField);
		}};
		programRunning = true;
		adHocLabel = new Label("");
		Task<Void> getURLTask = new Task<Void>(){
			@Override
			protected Void call() throws Exception {
				Process proc = new ProcessBuilder().command(listCommand).start();
				BasicParseGobbler gobbler = new BasicParseGobbler(proc.getInputStream(), myLogger, "[ytURL]");
				ErrorStreamGobbler errGobbler = new ErrorStreamGobbler(proc.getErrorStream(), myLogger);
				gobbler.commence();
				errGobbler.commence();
				while((proc.isAlive())){
					boolean outGobEmpty = gobbler.isEmpty();
					if(!outGobEmpty){
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								textArea.appendText(gobbler.dump());
							}
						});
					}
				}
				final int exitVal = proc.waitFor();
				Platform.runLater(new Runnable(){
					public void run() {
						if(!gobbler.isEmpty())
							textArea.appendText(gobbler.dump());
						textArea.appendText("ytGetURL exited with error code: "+ exitVal +"\n\n");
					}
				});
				gobbler.terminate();
				errGobbler.terminate();
				updateMessage(gobbler.getIdentifiedString());
				return null;
			};
		};
		adHocLabel.textProperty().bind(getURLTask.messageProperty());;
		adHocLabel.textProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				ytURL = adHocLabel.getText();
				if(verifyFirst){
					verifyURL(ytURL, grid);
				}
				else{
					makeCommandList(AUDIO_SCRIPT,
							songField, artistField, optionsField,
							FILE_TYPE,
							ITUNES_FILEPATH,
							TEMP_FILEPATH,
							ytURL);
					System.out.println(PY_COMMANDS);
					try {
						handToPython(grid, PY_COMMANDS);
					} catch (IOException | InterruptedException e) {
						myLogger.severe(e.getMessage());
					}
				}
			}
		});
		Thread newThread = new Thread(getURLTask);
		newThread.start();
		ytURL = null;
	}

	private void handToPython(GridPane grid, List<String> command) throws IOException, InterruptedException{
		if(command.size() == 0){
			myLogger.log(Level.SEVERE, "Hand off to python failed. Command(s) missing (length = "+command.size()+")");
			System.err.println("Argument missing in call. Args length: "+command.size());
		}
		if(!grid.getChildren().contains(progressBar)){
			if(grid.getChildren().contains(addHBOX)){
				grid.getChildren().remove(addHBOX);
			}
			grid.add(progressBar, 0, 4, 2,1);
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
				prefMenu.setDisable(true);
				toolsMenu.setDisable(true);
				oldState = SpinningState.BEFORE;
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
								//addProgressBar(grid, 0, 4);
							}
						});
					}
					else if(currentState == SpinningState.ALTERING && oldState == SpinningState.DOWNLOADING){
						oldState = currentState;
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								addSpinningProgressControl(grid, "Converting and Moving Audio File", true, progressBar);
							}
						});
					}
					else if(currentState == SpinningState.AFTER && oldState == SpinningState.ALTERING){
						oldState = currentState;
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								addCompleteText(grid, addHBOX);
								prefMenu.setDisable(false);
								toolsMenu.setDisable(false);
								runBTN.setDisable(false);
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
									addErrorText(grid, "An Error Occured", 0,4);
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
					prefMenu.setDisable(false);
					toolsMenu.setDisable(false);
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
				prefMenu.setDisable(false);
				toolsMenu.setDisable(false);
				return null;
			}
		};
		progressBar.progressProperty().bind(pyTask.progressProperty());
		Thread newThread = new Thread(pyTask);
		newThread.start();

	}

	/**
	 * Adds text to grid at specified (col, row) and styles it in accordance to .ErrorText in mainViewer.css
	 * @param grid = GridPane to add text to
	 * @param Text = String to insert
	 * @param col = col
	 * @param row = row
	 */
	private void addErrorText(GridPane grid, String Text, int col, int row){
		Label errorText = addText(grid, Text, col, row, true);
		errorText.getStyleClass().add("ErrorText");
	}

	/**
	 * Adds text to grid at specified (col, row)
	 * @param grid = GridPane to add text to
	 * @param Text = String to insert
	 * @param col = column to insert
	 * @param row = row to insert
	 * @return Label that contains the text that was inserted (in case it needs to be modified)
	 */
	private Label addText(GridPane grid, String Text, int col, int row, boolean replace){
		//		Node toMove = getNodeAtIndex(grid, col, row); //finds Node at current location and replaces it
		//		grid.getChildren().remove(toMove);
		//		if(!replace){
		//			grid.add(toMove, col, row+1, 2, 1);
		//		}

		Label newLabel = new Label(Text);
		newLabel.setWrapText(true);
		HBOXtoAdd = new HBox(Constants.SPACING);
		HBOXtoAdd.setAlignment(Pos.CENTER);
		HBOXtoAdd.getChildren().add(newLabel);

		grid.add(HBOXtoAdd, col, row, 2, 1);
		pStage.sizeToScene();
		return newLabel;
	}

	private void addCompleteText(GridPane grid, Node toRemove){
		if(toRemove != null) grid.getChildren().remove(toRemove);
		grid.getChildren().remove(progressBar);
		addText(grid,songField + " by "+artistField
				+" downloaded successfully",0,4, true);
	}

	private void addSpinningProgressControl(GridPane grid, String labelText, boolean remove, Node toRemove){
		if (remove){
			if (grid.getChildren().contains(toRemove))
				grid.getChildren().remove(toRemove);
		}
		addHBOX = new HBox(Constants.SPACING);
		Label downloadingState = new Label(labelText);
		ProgressIndicator progressIndicator = new ProgressIndicator();
		addHBOX.getChildren().addAll(downloadingState, progressIndicator);
		addHBOX.setAlignment(Pos.CENTER);
		grid.add(addHBOX, 0, 4, 2, 1);
		pStage.sizeToScene();
	}

	/**
	 * Order must be "song", "artist", "options", "file_type", "itunes location", "tmp location"
	 * @param commands
	 */
	@SuppressWarnings("serial")
	private void makeCommandList(String scriptLocation, String... commands){
		ArrayList<String> listCommand = new ArrayList<String>(){{
			add("python");
			add("-u");
			add(scriptLocation);
		}};
		for (int i = 0; i < commands.length; i++){
			listCommand.add(commands[i]);
		}
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
		//Exit. May throw a "java has been detached already"
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

	private void verifyURL(String url, GridPane grid){

		ValidateURL vurl = new ValidateURL(url);
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(pStage.getScene().getWindow());
		vurl.start(stage);
		if(vurl.urlValidated()){
			getPreferences();
			makeCommandList(AUDIO_SCRIPT,
					songField, artistField, optionsField,
					FILE_TYPE,
					ITUNES_FILEPATH,
					TEMP_FILEPATH,
					ytURL);
			try {
				handToPython(grid, PY_COMMANDS);
			} catch (IOException | InterruptedException e) {
				myLogger.severe(e.getMessage());
			}
		}
		else{
			ForceRestart();
		}
	}
	
	private void writeJSONToFile(File fileToWrite) throws IOException{
		String toWrite = new ExtractResource("/YTDLSettings.json", myLogger).toString();
		System.out.println(toWrite);
		FileWriter fw = new FileWriter(fileToWrite);
		fw.write(toWrite);
		fw.close();
	}


	public File getPreferencesFilePath(){
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		String filePath = prefs.get("filePath", null);
		if (filePath == null) return null;
		
		File retFile = new File(filePath);
		if(!retFile.exists())
			try {
				retFile.createNewFile();
				writeJSONToFile(retFile);
			} catch (IOException e) {
				myLogger.warning(e.getMessage());
			}
		try {
			BufferedReader br = new BufferedReader(new FileReader(retFile));
			if(br.readLine() == null){
				writeJSONToFile(retFile);
			}
			br.close();
		} catch (IOException e) {
			myLogger.severe(e.getMessage());
		}
		return retFile;
	}

	public void setPreferencesFilePath(File file) {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		if (file != null) {
			prefs.put("filePath", file.getPath());
		} else {
			prefs.remove("filePath");
		}
	}

	private void getPreferencesWindow(GridPane grid){
		File prefFilePath = getPreferencesFilePath();
		OptionsParser op = new OptionsParser(prefFilePath, myLogger);
		ModalPopup mp = new ModalPopup(op.getItunesPath(), op.getTempPath());
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(pStage.getScene().getWindow());
		mp.start(stage);
		options = mp.getOptions();
		savePreferences();

	}

	//Called right before making python arguments list
	private void getPreferences(){
		if(options != null){
			this.FILE_TYPE = options.file_type;
			this.ITUNES_FILEPATH = options.itunes_path;
			this.TEMP_FILEPATH = options.temp_path;
			return;
		}
		File prefFilePath = getPreferencesFilePath();
		OptionsParser op = new OptionsParser(prefFilePath, myLogger);
		this.FILE_TYPE = op.getFileType();
		this.ITUNES_FILEPATH = op.getItunesPath();
		this.TEMP_FILEPATH = op.getTempPath();
	}

	private void savePreferences(){
		File f = getPreferencesFilePath();
		if (f == null) {
			System.out.println("nullFileFound");
			f = new File(Constants.tempFilePath+File.separator+"ytDownloader");
			try {
				f.createNewFile();
				myLogger.info("Made new preferences file at: "+f.getAbsolutePath());
			} catch (IOException e) {
				myLogger.severe(e.getMessage());
			}
			setPreferencesFilePath(f);
			populateFileWithBoilerPlate(f);
		}
		OptionsParser op = new OptionsParser(f);
		if(options != null){
			op.setFileType(options.file_type);
			op.setItunesPath(options.itunes_path);
			op.save();
		}
	}

	private void populateFileWithBoilerPlate(File f){
		//boilerplate located within class as resource
		try {
			InputStream boilerplateStream = ClassLoader.getSystemResourceAsStream("YTDLSettings.json");
			BufferedReader br = new BufferedReader(new InputStreamReader(boilerplateStream));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			if(!f.exists()) f.createNewFile();
			FileWriter fw = new FileWriter(f);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			myLogger.severe(e.getMessage());
		}

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
			myLogger.info("Running on "+Constants.OPERATING_SYSTEM);
			if(!Constants.OPERATING_SYSTEM.equals("Mac OS X")){
				myLogger.warning("This is an untested platform. Proceed with caution");
				System.err.println(Constants.OPERATING_SYSTEM + " is an untested platform");
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