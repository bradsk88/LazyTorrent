package ca.bradj.lazytorrent.app;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ca.bradj.common.base.Confidence;
import ca.bradj.common.base.WithConfidence;
import ca.bradj.lazytorrent.rss.RSSTorrent;

public class RSSTorrentWithConfidenceCellFactory implements
		Callback<ListView<WithConfidence<RSSTorrent>>, ListCell<WithConfidence<RSSTorrent>>> {

	protected static final String HIGHSTYLE = "-fx-background-color: #CCFFCC;";
	protected static final String LOWSTYLE = "-fx-background-color: #FFCCCC;";
	protected static final String MEDSTYLE = "-fx-background-color: #DEDECC;";

	@Override
	public ListCell<WithConfidence<RSSTorrent>> call(ListView<WithConfidence<RSSTorrent>> arg0) {
		return new ListCell<WithConfidence<RSSTorrent>>() {
			@Override
			protected void updateItem(WithConfidence<RSSTorrent> arg0, boolean arg1) {
				super.updateItem(arg0, arg1);
				if (arg0 == null || arg1) {
					setText(null);
					setStyle(null);
					return;
				}
				setText(arg0.getItem().getName());
				setStyle(getStyle(arg0.getConfidence()));
			}

			private String getStyle(Confidence confidence) {
				switch (confidence) {
				case HIGH:
					return HIGHSTYLE;
				case LOW:
					return LOWSTYLE;
				case MEDIUM:
					return MEDSTYLE;
				}
				throw new IllegalArgumentException();
			}
		};
	}
}
