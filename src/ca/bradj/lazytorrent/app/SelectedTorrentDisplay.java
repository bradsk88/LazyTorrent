package ca.bradj.lazytorrent.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class SelectedTorrentDisplay implements ChangeListener<RSSTorrent> {

	private static final String CURRENT_SELECTION = "Current Selection: ";
	private static final String NA = "N/A";
	private final Parent node;
	private final Label selectedName;
	private final DownloadThisButton button;
	private final TextField urlDisp;

	public SelectedTorrentDisplay(Preferences prefs) {
		Label nameLabel = new Label(CURRENT_SELECTION);
		this.selectedName = new Label(NA);
		selectedName.setFont(font());
		button = new DownloadThisButton(prefs);
		BorderPane node1 = BorderPaneBuilder.create()
				.left(HBoxBuilder.create().children(nameLabel, selectedName).build()).right(button.getNode()).build();
		urlDisp = new TextField();
		urlDisp.setMinHeight(Pane.USE_PREF_SIZE);
		this.node = VBoxBuilder.create().children(node1, urlDisp).build();
	}

	private static Font font() {
		Font font = Font.font("Arial", FontWeight.BOLD, 12);
		return font;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public void changed(ObservableValue<? extends RSSTorrent> arg0, RSSTorrent arg1, RSSTorrent newVal) {
		if (newVal == null) {
			return;
		}
		String showName = newVal.getName();
		this.selectedName.setText(showName);
		this.button.setShow(showName);
		this.urlDisp.setText(newVal.getURL());
	}
}
