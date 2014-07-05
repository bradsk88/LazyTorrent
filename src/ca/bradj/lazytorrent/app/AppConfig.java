package ca.bradj.lazytorrent.app;

import java.io.File;
import java.nio.file.Path;

import ca.bradj.lazytorrent.prefs.Preferences;

public interface AppConfig {

	Preferences getPrefs();

	AlreadyDownloaded getAlreadyDownloaded();

	Path getRoot();

	String getTorrentCommand();


}
