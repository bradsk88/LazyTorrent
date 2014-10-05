package ca.bradj.lazytorrent.app;

import java.nio.file.Path;

import ca.bradj.common.base.Preconditions2;
import ca.bradj.lazytorrent.prefs.Preferences;

import com.google.common.base.Preconditions;

public class DefaultAppConfig implements AppConfig {

	private Preferences prefs;
	private AlreadyDownloaded alreadyDLed;
	private Path appRoot;
	private String command;

	public DefaultAppConfig(Preferences prefs, AlreadyDownloaded alreadyDLed,
			Path rootG, String command) {
		super();
		this.prefs = Preconditions.checkNotNull(prefs);
		this.alreadyDLed = Preconditions.checkNotNull(alreadyDLed);
		this.appRoot = Preconditions.checkNotNull(rootG);
		this.command = Preconditions2.checkNotEmpty(command);
	}

	@Override
	public Preferences getPrefs() {
		return prefs;
	}

	@Override
	public AlreadyDownloaded getAlreadyDownloaded() {
		return alreadyDLed;
	}

	@Override
	public Path getRoot() {
		return appRoot;
	}

	@Override
	public String getTorrentCommand() {
		return command;
	}

}
