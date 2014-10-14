package ca.bradj.lazytorrent.app;

import java.nio.file.Path;
import java.util.Optional;

import ca.bradj.common.base.Preconditions2;
import ca.bradj.lazytorrent.prefs.Preferences;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.scrape.matching.MatchFailHandler;

import com.google.common.base.Preconditions;

public class DefaultAppConfig implements AppConfig {

	private Preferences prefs;
	private AlreadyDownloaded alreadyDLed;
	private Path appRoot;
	private String command;
	private Optional<MatchFailHandler<RSSTorrent>> matchFailHandler;

	public DefaultAppConfig(Preferences prefs, AlreadyDownloaded alreadyDLed,
			Path rootG, String command, Optional<MatchFailHandler<RSSTorrent>> handler) {
		super();
		this.prefs = Preconditions.checkNotNull(prefs);
		this.alreadyDLed = Preconditions.checkNotNull(alreadyDLed);
		this.appRoot = Preconditions.checkNotNull(rootG);
		this.command = Preconditions2.checkNotEmpty(command);
		this.matchFailHandler = Preconditions.checkNotNull( handler );
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
	
	@Override
	public Optional<MatchFailHandler<RSSTorrent>> getMatchFailHandler() {
		return matchFailHandler;
	}

}
