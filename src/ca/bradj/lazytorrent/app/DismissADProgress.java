package ca.bradj.lazytorrent.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import ca.bradj.lazytorrent.rss.FXThreading;

import com.google.common.base.Preconditions;

public class DismissADProgress implements ChangeListener<Boolean> {

	private final Label label;

	public DismissADProgress(Label label) {
		this.label = Preconditions.checkNotNull(label);
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
		FXThreading.invokeLater(new Runnable() {

			@Override
			public void run() {
				label.setText(null);
			}
		});
	}

}
