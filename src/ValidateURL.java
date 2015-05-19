
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ValidateURL extends Application {
	private Stage stage;
	private boolean returnedValue = false;
	private final String url;
	private final double finalWidth = 800;
	private final WebView browser = new WebView();
	private WebEngine webEngine;

	public ValidateURL(String url){
		this.url = url;
	}

	@Override
	public void start(Stage primaryStage) {
		this.stage = primaryStage;

		Scene scene = new Scene(new Group());
		final BorderPane bp = new BorderPane();
		ValidateButtonBox vbb = new ValidateButtonBox();
		HBox buttonBox = vbb.getHBox();
		bp.setTop(buttonBox);

		webEngine = browser.getEngine();

		browser.getEngine().setOnAlert(new EventHandler<WebEvent<String>>(){
			@Override
			public void handle(WebEvent<String> event) {
				System.out.println("Alert Event - Message: "+event.getData());
			}
		});

		webEngine.load(url);

		bp.setCenter(browser);
		scene.setRoot(bp);

		stage.setScene(scene);
		stage.setWidth(finalWidth);
		vbb.resize();
		stage.showAndWait();
	}

	public boolean urlValidated(){
		return returnedValue;
	}

	public static void main(String[] args) {
		launch(args);
	}

	private class ValidateButtonBox{

		private HBox hbox;
		private int numberChildren;

		public ValidateButtonBox(){
			this.populate();
		}
		public HBox getHBox(){
			return hbox;
		}

		public void resize(){
			numberChildren = hbox.getChildren().size();
			double widthOfEach = (finalWidth-Constants.SPACING*3)/numberChildren;
			for (Node ele : hbox.getChildren()){
				Button b = (Button) ele;
				b.setPrefWidth(widthOfEach);
			}
		}

		public void populate(){
			hbox = new HBox(Constants.SPACING);
			hbox.setAlignment(Pos.CENTER);
			Button quitBTN = new Button("Quit");
			quitBTN.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent event) {
					returnedValue = false;
					browser.getEngine().load(null);
					stage.close();
				}
			});
			
			Button reloadBTN = new Button("Refresh");
			reloadBTN.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent event) {
					webEngine.reload();
				}
			});

			Button useBTN = new Button("Use This URL");
			useBTN.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent event) {
					returnedValue = true;
					browser.getEngine().load(null);
					stage.close();
				}
			});
			hbox.getChildren().addAll(quitBTN, reloadBTN,useBTN);
			hbox.setPadding(new Insets(10,10,10,10));
			HBox.setHgrow(hbox, Priority.ALWAYS);
			hbox.getStylesheets().add("ButtonStyle.css");
			cleanUpHBox(hbox);
		}

		/**
		 * Loops through the elements of the input box and changes their fields
		 * inputBox should be sufficiently small enough that optimization is 
		 * unnecessary
		 * @param inputBox, HBox to clean up
		 */
		public void cleanUpHBox(HBox inputBox){
			ObservableList<Node> childrenList = inputBox.getChildren();
			for (Node nodeEle : childrenList){
				Button buttonEle = (Button) nodeEle;
				buttonEle.setId("rich-blue");
				buttonEle.addEventHandler(MouseEvent.MOUSE_ENTERED
						, new EventHandler<MouseEvent>(){
					@Override
					public void handle(MouseEvent event) {
						buttonEle.setEffect(new DropShadow());
					}
				});
				buttonEle.addEventHandler(MouseEvent.MOUSE_EXITED
						, new EventHandler<MouseEvent>(){
					@Override
					public void handle(MouseEvent event) {
						buttonEle.setEffect(null);
					}
				});
			}
		}
	}
}