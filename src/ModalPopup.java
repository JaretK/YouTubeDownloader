import java.io.File;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ModalPopup extends Application{

	private static final String guessiTunesPath = Constants.userHome+File.separator+"Music"+File.separator+"iTunes";
	private OptionsFromWindow options;
	private final String defaultITunesPath;
	private final String defaultTempPath;
	
	public ModalPopup(String defaultITunesPath, String defaultTempPath){
		this.defaultITunesPath = defaultITunesPath;
		this.defaultTempPath = defaultTempPath;
	}
	
	public void start(Stage primaryStage) {

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_CENTER);
		grid.setHgap(Constants.SPACING);//H width between cols
		grid.setVgap(Constants.SPACING);//V height between rows
		grid.setPadding(new Insets(25,25,25,25));

		//Make Labels and TextFields
		Label fileTypeLabel = new Label("File Type");
		TextField fileType = new TextField(".mp3");
		fileType.setDisable(true);

		Label iTunesPathLabel = new Label("\"Automatically Add to iTunes\" path");
		iTunesPathLabel.setWrapText(true);
		TextField iTunesPath = new TextField(defaultITunesPath);
		HBox iTunesHBOX = makeHBoxWithChooser(iTunesPath, "iTunes Automatically Add to iTunes Folder", primaryStage, ChooserType.iTunesPath);

		Label tempPathLabel = new Label("Temp File Path ");
		TextField tempPathField = new TextField(defaultTempPath);
		HBox tempHBOX = makeHBoxWithChooser(tempPathField, "Temp folder location (try /tmp)", primaryStage, ChooserType.tempPath);
		grid.add(fileTypeLabel, 0,0);
		grid.add(fileType, 1,0 );
		grid.add(iTunesPathLabel, 0,1);
		grid.add(iTunesHBOX, 1,1);
		grid.add(tempPathLabel, 0, 2);
		grid.add(tempHBOX, 1,2);

		//buttons
		Button closeBTN = new Button("Close");
		closeBTN.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				primaryStage.close();
			}
		});

		Button clearBTN = new Button("Clear");
		clearBTN.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				clearFields(iTunesPath, tempPathField);
			}
		});

		Button saveBTN = new Button("Save");
		saveBTN.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				String filetype = fileType.getText();
				String iTunes = iTunesPath.getText();
				String temp = tempPathField.getText();
				if(iTunes != "" && temp != ""){
					options = new OptionsFromWindow(filetype,iTunes,temp);
				}
				closeBTN.fire();
			}
		});

		HBox buttonBox = new HBox(Constants.SPACING);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.getChildren().addAll(closeBTN, clearBTN, saveBTN);
		cleanUpHBOX(buttonBox);
		grid.add(buttonBox, 0, 3, 2, 1);
		grid.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ENTER)
					saveBTN.fire();
			}
		});
		Scene scene = new Scene(grid);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.showAndWait();
	}

	public OptionsFromWindow getOptions(){

		return options;
	}

	private static void clearFields(TextField... fields){
		for (int i = 0; i < fields.length; i++)
			fields[i].clear();
	}

	private static HBox makeHBoxWithChooser(TextField field, String title, Stage stage, ChooserType chooser){
		Button findBTN = new Button("find");
		HBox addHBOX = new HBox(Constants.SPACING);
		addHBOX.getChildren().addAll(field, findBTN);
		addHBOX.setAlignment(Pos.CENTER_LEFT);

		findBTN.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dc = new DirectoryChooser();
				dc.setTitle("Choose the directory for "+title);
				String initPath = (chooser == ChooserType.iTunesPath) ? guessiTunesPath : Constants.tempFilePath ;
				String defaultPath = (chooser == ChooserType.iTunesPath) ? Constants.userHome : "/" ;
				try{
					dc.setInitialDirectory(new File(initPath));
				} catch(java.lang.IllegalArgumentException e){
					dc.setInitialDirectory(new File(defaultPath));
				}
				File chosenDirectory = dc.showDialog(stage);
				if (chosenDirectory != null)
					field.setText(chosenDirectory.getPath());
			}
		});

		return addHBOX;
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
}
