package ca.bradj.lazytorrent.scrape;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.util.Pair;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.app.RSSTorrentWithConfidenceCellFactory;
import ca.bradj.lazytorrent.app.ScrapedItemsProvider;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class ScrapeListView implements ScrapedItemsProvider {

	private final ListView<WithConfidence<Pair<RSSTorrent, String>>> node;

	public ScrapeListView() {
		this.node = new ListView<>();
		node.setCellFactory(new RSSTorrentWithConfidenceCellFactory());
	}

	public void setItems(Collection<WithConfidence<Pair<RSSTorrent, String>>> candidates) {
		node.getItems().setAll(candidates);
	}

	public Node getNode() {
		return node;
	}

	@Override
	public Collection<Pair<RSSTorrent, String>> getLastScrape() {
		ObservableList<WithConfidence<Pair<RSSTorrent, String>>> items = node.getItems();
		return WithConfidence.stripConfidence(items);
	}

}
