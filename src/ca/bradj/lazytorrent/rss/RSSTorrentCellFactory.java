package ca.bradj.lazytorrent.rss;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.Pair;
import ca.bradj.lazytorrent.prefs.Preferences;

public class RSSTorrentCellFactory implements
		Callback<ListView<Pair<RSSTorrent, String>>, ListCell<Pair<RSSTorrent, String>>> {

	protected static final String MATCH_STYLE = "-fx-background-color: #CCFFCC;";
	private final Preferences prefs;

	public RSSTorrentCellFactory(Preferences prefs) {
		this.prefs = prefs;
	}

	@Override
	public ListCell<Pair<RSSTorrent, String>> call(ListView<Pair<RSSTorrent, String>> arg0) {
		return new ListCell<Pair<RSSTorrent, String>>() {
			@Override
			protected void updateItem(Pair<RSSTorrent, String> item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					return;
				}
				if (prefs.matches(item.getKey().getName())) {
					setText(item.getKey().toUserString() + "[Matches " + item.getValue() + "]");
					setStyle(MATCH_STYLE);
				} else {
					setText(item.getKey().toUserString());
					setStyle(null);
				}
			}
		};
	}
}
