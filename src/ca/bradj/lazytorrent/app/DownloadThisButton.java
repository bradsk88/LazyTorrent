package ca.bradj.lazytorrent.app;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import ca.bradj.lazytorrent.prefs.Preferences;

public class DownloadThisButton {

	private static final String BUTTON_TEXT = "I want to download this";
	private final Button button;
	private String currentShowName;

	public DownloadThisButton(final Preferences preferences) {
		this.button = new Button(BUTTON_TEXT);
		button.setDisable(true);
		button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				new AddShowToList(currentShowName, preferences).showAndWait();
			}
		});
	}

	public void setShow(String showName) {
		this.currentShowName = showName;
		this.button.setDisable(showName == null);
	}

	public Button getNode() {
		return button;
	}

}
