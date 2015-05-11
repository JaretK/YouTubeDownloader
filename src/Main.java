import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(final Stage primaryStage) {
    primaryStage.setTitle("title");
    Group root = new Group();
    Scene scene = new Scene(root, 400, 300, Color.WHITE);

    MenuBar menuBar = new MenuBar();
    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

    Menu menu = new Menu("File");

    MenuItem exitItem = new MenuItem("Exit", null);
    exitItem.setMnemonicParsing(true);
    exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X,KeyCombination.CONTROL_DOWN));
    exitItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        Platform.exit();
      }
    });
    menu.getItems().add(exitItem);
    
    menuBar.getMenus().add(menu);
    root.getChildren().add(menuBar);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
