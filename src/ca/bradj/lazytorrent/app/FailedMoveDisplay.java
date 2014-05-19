package ca.bradj.lazytorrent.app;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;

public class FailedMoveDisplay {

	private final ListView<String> node;

	public FailedMoveDisplay(ObservableList<String> collection, @SuppressWarnings("unused") Logger logger) {
		this.node = new ListView<>();
		this.node.setItems(collection);
	}

	public Node getNode() {
		return node;
	}

}
