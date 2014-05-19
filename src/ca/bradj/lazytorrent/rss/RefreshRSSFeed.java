package ca.bradj.lazytorrent.rss;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class RefreshRSSFeed implements EventHandler<ActionEvent> {

	private final RSSFeed rss;

	public RefreshRSSFeed(RSSFeed rss) {
		this.rss = rss;
	}

	@Override
	public void handle(ActionEvent arg0) {
		rss.requestRefresh();
	}

}
