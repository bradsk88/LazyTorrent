package ca.bradj.lazytorrent.app;

import java.nio.file.Path;
import java.util.Collection;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.AccordionBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ca.bradj.Layouts;
import ca.bradj.lazytorrent.matching.TorrentMatchings;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.prefs.PreferencesListView;
import ca.bradj.lazytorrent.rss.RSSFeed;
import ca.bradj.lazytorrent.rss.RSSListView;
import ca.bradj.lazytorrent.rss.RSSRefreshButton;

import com.google.common.collect.Lists;

public class LazyTorrentsControlPanel {

	private final Parent node;

	public LazyTorrentsControlPanel(TorrentMatchings m, Logger logger,
			RSSFeed rss, ObservableValue<? extends String> countdownProperty,
			AppConfig appConfig) {
		this.node = makeNode(appConfig, rss, m, logger, countdownProperty);
	}

	private Parent makeNode(AppConfig appConfig, RSSFeed rss,
			TorrentMatchings m, Logger logger,
			ObservableValue<? extends String> countdownProperty) {

		Preferences prefs = m.getPreferences();
		RSSListView rssListView = new RSSListView(rss, prefs);

		PreferencesListView preferencesListView = new PreferencesListView(prefs);

		SelectedTorrentDisplay selectedTorrentDisplay = new SelectedTorrentDisplay(
				prefs);
		rssListView.addSelectionListener(selectedTorrentDisplay);

		DownloadStagingDisplay downloadStagingDisplay = new DownloadStagingDisplay(
				rssListView, appConfig, logger);

		VBox controls = Layouts.vbox();
		controls.getChildren().add(new RSSRefreshButton(rss).getNode());
		controls.getChildren().add(rssListView.getNode());
		controls.getChildren().add(selectedTorrentDisplay.getNode());
		controls.getChildren().add(downloadStagingDisplay.getNode());
		controls.getChildren().add(preferencesListView.getNode());

		ProgressIndicator prog = new ProgressIndicator();
		prog.setMaxHeight(100);
		prog.setProgress(-1.0);
		HBox loadingAlreadyDownloaded = new HBox();
		loadingAlreadyDownloaded.setAlignment(Pos.TOP_RIGHT);
		loadingAlreadyDownloaded.setMaxHeight(100);
		Label label = new Label("Loading \"Already Downloaded\" list");
		loadingAlreadyDownloaded.getChildren().addAll(label, prog);
		AlreadyDownloaded alreadyDownloaded = appConfig.getAlreadyDownloaded();
		alreadyDownloaded.isFullyLoadedProperty().addListener(
				new DismissADProgress(label));
		prog.progressProperty().bind(alreadyDownloaded.loadProgressProperty());
		TitledPane tp = new TitledPane("Manual Control", controls);
		Collection<TitledPane> panes = Lists.newArrayList();
		panes.add(tp);
		TitledPane tp2 = new TitledPane("Event Log", new LoggerDisplay(logger,
				countdownProperty).getNode());

		panes.add(tp2);
		TitledPane tp3 = new TitledPane("Failed completed file transfers",
				new FailedMoveDisplay(m.getUnmovables()).getNode());
		panes.add(tp3);
		Accordion a = AccordionBuilder.create().panes(panes).build();
		a.setExpandedPane(tp2);
		VBox vBox = new VBox();
		vBox.getChildren().addAll(a, loadingAlreadyDownloaded);
		VBox.setVgrow(a, Priority.ALWAYS);
		return vBox;
	}

	public Parent getNode() {
		return node;
	}

}
