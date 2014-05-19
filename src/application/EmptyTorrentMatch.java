package application;

import com.google.common.base.Preconditions;

class EmptyTorrentMatch implements TorrentMatch {

	private static final String FAILURE = "Fail";
	private final String reason;

	public EmptyTorrentMatch(String reason) {
		super();
		this.reason = Preconditions.checkNotNull(reason);
	}

	@Override
	public boolean isMovie() {
		return false;
	}

	@Override
	public String getReason() {
		return reason;
	}

	@Override
	public boolean isUnmovable() {
		return false;
	}

	@Override
	public String getName() {
		return FAILURE;
	}

	@Override
	public boolean isPreference() {
		return false;
	}

}
