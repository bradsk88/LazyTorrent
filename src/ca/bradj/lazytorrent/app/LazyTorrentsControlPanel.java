package ca.bradj.lazytorrent.app;

import java.nio.file.Path;
import java.util.Collection;

import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.AccordionBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import application.TorrentMatchings;
import ca.bradj.Layouts;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.prefs.PreferencesListView;
import ca.bradj.lazytorrent.rss.RSSFeed;
import ca.bradj.lazytorrent.rss.RSSListView;
import ca.bradj.lazytorrent.rss.RSSRefreshButton;

import com.google.common.collect.Lists;

public class LazyTorrentsControlPanel {

	private final Parent node;

	public LazyTorrentsControlPanel(Path rootDir, TorrentMatchings m, AlreadyDownloaded alreadyDownloaded,
			Logger logger, RSSFeed rss, ObservableValue<? extends String> countdownProperty) {
		this.node = makeNode(rootDir, rss, m, alreadyDownloaded, logger, countdownProperty);
	}

	private Parent makeNode(Path rootDir, RSSFeed rss, TorrentMatchings m, AlreadyDownloaded alreadyDownloaded,
			Logger logger, ObservableValue<? extends String> countdownProperty) {

		Preferences prefs = m.getPreferences();
		RSSListView rssListView = new RSSListView(rss, prefs);

		PreferencesListView preferencesListView = new PreferencesListView(prefs);

		SelectedTorrentDisplay selectedTorrentDisplay = new SelectedTorrentDisplay(prefs);
		rssListView.addSelectionListener(selectedTorrentDisplay);

		DownloadStagingDisplay downloadStagingDisplay = new DownloadStagingDisplay(rootDir, rssListView, prefs,
				alreadyDownloaded, logger);

		VBox controls = Layouts.vbox();
		controls.getChildren().add(new RSSRefreshButton(rss).getNode());
		controls.getChildren().add(rssListView.getNode());
		controls.getChildren().add(selectedTorrentDisplay.getNode());
		controls.getChildren().add(downloadStagingDisplay.getNode());
		controls.getChildren().add(preferencesListView.getNode());
		TitledPane tp = new TitledPane("Manual Control", controls);
		Collection<TitledPane> panes = Lists.newArrayList();
		panes.add(tp);
		TitledPane tp2 = new TitledPane("Event Log", new LoggerDisplay(logger, countdownProperty).getNode());

		panes.add(tp2);
		TitledPane tp3 = new TitledPane("Failed completed file transfers", new FailedMoveDisplay(m.getUnmovables(),
				logger).getNode());
		panes.add(tp3);
		Accordion a = AccordionBuilder.create().panes(panes).build();
		a.setExpandedPane(tp2);
		return a;
	}

	public Parent getNode() {
		return node;
	}

}
