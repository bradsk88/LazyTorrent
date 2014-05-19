package ca.bradj.lazytorrent.rss;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ca.bradj.lazytorrent.prefs.Preferences;

public class RSSTorrentCellFactory implements Callback<ListView<RSSTorrent>, ListCell<RSSTorrent>> {

	protected static final String MATCH_STYLE = "-fx-background-color: #CCFFCC;";
	private final Preferences prefs;

	public RSSTorrentCellFactory(Preferences prefs) {
		this.prefs = prefs;
	}

	@Override
	public ListCell<RSSTorrent> call(ListView<RSSTorrent> arg0) {
		return new ListCell<RSSTorrent>() {
			@Override
			protected void updateItem(RSSTorrent item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText(null);
					return;
				}
				setText(item.toUserString());
				if (prefs.matches(item.getName())) {
					setStyle(MATCH_STYLE);
				} else {
					setStyle(null);
				}
			}
		};
	}
}
