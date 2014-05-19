package ca.bradj.lazytorrent.scrape;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.RSSTorrentWithConfidenceCellFactory;
import ca.bradj.lazytorrent.app.ScrapedItemsProvider;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class ScrapeListView implements ScrapedItemsProvider {

	private final ListView<WithConfidence<RSSTorrent>> node;

	public ScrapeListView() {
		this.node = new ListView<>();
		node.setCellFactory(new RSSTorrentWithConfidenceCellFactory());
	}

	public void setItems(Collection<WithConfidence<RSSTorrent>> candidates) {
		node.getItems().setAll(candidates);
	}

	public Node getNode() {
		return node;
	}

	@Override
	public Collection<RSSTorrent> getLastScrape() {
		ObservableList<WithConfidence<RSSTorrent>> items = node.getItems();
		return WithConfidence.stripConfidence(items);
	}

}
