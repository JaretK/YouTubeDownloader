import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class ModalPopup {
	
	Stage stage;
	GridPane grid;
	OptionsFromWindow options;
	
	public ModalPopup(Stage stage){
		this.stage = stage;
		stage.initModality(Modality.APPLICATION_MODAL);
		grid = new GridPane();
		
		//Make Labels and TextFields
		Label fileTypeLabel = new Label("File Type");
		TextField fileType = new TextField();
		HBox fileTypeHBOX = makeHBox(fileTypeLabel, fileType);
		
		Label iTunesPathLabel = new Label("\"Automatically Add to iTunes\" path");
		TextField iTunesPath = new TextField();
		HBox iTunesHBOX = makeHBoxWithChooser(iTunesPathLabel, iTunesPath);
		
		Label tempPathLabel = new Label("Temp File Path ");
		TextField tempPathField = new TextField();
		HBox tempHBOX = makeHBoxWithChooser(tempPathLabel, tempPathField);
		grid.add(fileTypeHBOX , 0, 0);
		grid.add(iTunesHBOX, 0, 1);
		grid.add(tempHBOX, 0, 2);
		
		//buttons
		Button quitBTN = new Button("Quit");
		quitBTN.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		
		Button clearBTN = new Button("Clear");
		clearBTN.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				clearFields(fileType, iTunesPath, tempPathField);
			}
		});
		
		Button okBTN = new Button("Ok");
		okBTN.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				String filetype = fileType.getText();
				String iTunes = iTunesPath.getText();
				String temp = tempPathField.getText();
				options = new OptionsFromWindow(filetype, iTunes,temp);
			}
		});
		
		HBox buttonBox = new HBox(Constants.SPACING);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.getChildren().addAll(quitBTN, clearBTN, okBTN);
	}
	
	/**
	 * Clears all TextFields inputted into the method
	 * @param fields: TextFields to be cleared
	 */
	private void clearFields(TextField... fields){
		for (int i = 0; i < fields.length; i++)
			fields[i].clear();
	}
	
	private HBox makeHBox(Label label, TextField field){
		HBox addHBOX = new HBox(Constants.SPACING);
		addHBOX.getChildren().addAll(label, field);
		addHBOX.setAlignment(Pos.CENTER);
		return addHBOX;
	}
	
	private HBox makeHBoxWithChooser(Label label, TextField field){
		Button findBTN = new Button("find");
		HBox addHBOX = new HBox(Constants.SPACING);
		addHBOX.getChildren().addAll(label, field, findBTN);
		addHBOX.setAlignment(Pos.CENTER);
		
		findBTN.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dc = new DirectoryChooser();
				dc.setTitle("Choose the directory for "+label.getText());
				File chosenDirectory = dc.showDialog(stage);
				field.setText(chosenDirectory.getPath());
			}
		});
		
		return addHBOX;
	}
	
	public void show(){
		Scene scene = new Scene(grid);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
	public static void main(String[] args){		
		
	}
	
}
