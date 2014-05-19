package ca.bradj.lazytorrent.prefs;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ListView;

public class PreferencesListView implements RecordsChangeListener {

	private final ListView<String> node;

	public PreferencesListView(Preferences prefs) {
		prefs.addListener(this);
		this.node = new ListView<>();
		this.node.getItems().setAll(prefs.getList());
	}

	public Node getNode() {
		return node;
	}

	@Override
	public void preferenceAdded(String text) {
		node.getItems().add(text);
		FXCollections.sort(node.getItems());
	}

}
