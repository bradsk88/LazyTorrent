

import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

public final class Layouts {

	private Layouts() {
	}

	public static VBox vbox() {
		return vBoxBuild().build();
	}

	@SuppressWarnings("rawtypes")
	public static VBoxBuilder<? extends VBoxBuilder> vBoxBuild() {
		return VBoxBuilder.create().spacing(5).fillWidth(true);
	}

}
