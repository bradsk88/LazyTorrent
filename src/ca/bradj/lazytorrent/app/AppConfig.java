package ca.bradj.lazytorrent.app;

import java.nio.file.Path;
import java.util.Optional;

import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.scrape.matching.MatchFailHandler;

public interface AppConfig {

	Preferences getPrefs();

	AlreadyDownloaded getAlreadyDownloaded();

	Path getRoot();

	String getTorrentCommand();

	Optional<MatchFailHandler<RSSTorrent>> getMatchFailHandler();

}
