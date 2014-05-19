package ca.bradj.lazytorrent.rss;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class RSSRefreshButton {

	private static final String REFERESH = "Refresh";
	private final Node node;

	public RSSRefreshButton(RSSFeed rss) {
		this.node = makeNode(rss);
	}

	private Node makeNode(RSSFeed rss) {
		Button button = new Button(REFERESH);
		button.setOnAction(new RefreshRSSFeed(rss));
		return button;
	}

	public Node getNode() {
		return node;
	}

}
