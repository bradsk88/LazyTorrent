package ca.bradj.lazytorrent.rss;

import java.util.Collection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import ca.bradj.fx.FXThreading;
import ca.bradj.lazytorrent.prefs.Preferences;

import com.google.common.collect.ImmutableList;

public class RSSListView {

	private final ListView<RSSTorrent> node;
	private final RSSUpdateListener listener = new RSSUpdateListener() {

		@Override
		public void newTorrentsListAvailable(final ImmutableList<RSSTorrent> torrents) {

			FXThreading.invokeLater(new Runnable() {
				@Override
				public void run() {
					node.setItems(FXCollections.observableArrayList(torrents));
				}
			});

		}

	};

	public RSSListView(RSSFeed rss, Preferences prefs) {
		this.node = makeNode(rss, prefs);
	}

	private ListView<RSSTorrent> makeNode(RSSFeed rss, Preferences prefs) {
		ListView<RSSTorrent> listView = new ListView<>();
		listView.setCellFactory(new SimpleRSSTorrentCellFactory(prefs));
		rss.addUpdateListener(listener);
		return listView;
	}

	public Node getNode() {
		return node;
	}

	public void addSelectionListener(ChangeListener<RSSTorrent> l) {
		this.node.getSelectionModel().selectedItemProperty().addListener(l);
	}

	public ObjectProperty<ObservableList<RSSTorrent>> itemsProperty() {
		return node.itemsProperty();
	}

	public Collection<RSSTorrent> getCurrentItems() {
		return node.getItems();
	}

}
