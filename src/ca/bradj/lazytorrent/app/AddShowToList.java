package ca.bradj.lazytorrent.app;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ca.bradj.Layouts;
import ca.bradj.lazytorrent.prefs.Preferences;

public class AddShowToList {

	private static final String ADD_TO_PREFERENCES = "Add to preferences";
	private static final String GENERALIZE = "Generalize show name for future matching";
	private final Stage stage;

	public AddShowToList(String currentShowName, final Preferences preferences) {

		this.stage = new Stage();
		stage.setWidth(600);
		stage.setHeight(200);
		stage.setTitle(GENERALIZE);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.UTILITY);
		VBox controls = Layouts.vbox();
		final TextField textField = new TextField();
		textField.setMinWidth(200);
		textField.setText(currentShowName);
		controls.getChildren().add(textField);
		Button button = new Button(ADD_TO_PREFERENCES);
		button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				preferences.addAndWriteToDisk(textField.getText());
				stage.close();
			}
		});
		controls.getChildren().add(button);
		stage.setScene(new Scene(controls));

	}

	public void showAndWait() {
		stage.showAndWait();
	}

}
