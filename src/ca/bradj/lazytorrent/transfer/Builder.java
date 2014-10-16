package ca.bradj.lazytorrent.transfer;

import java.io.File;
import java.nio.file.Path;

import ca.bradj.common.base.Preconditions2;

import com.google.common.base.Optional;

public class Builder {

	private Optional<Path> destPath = Optional.absent();
	private Optional<String> showName = Optional.absent();
	private Optional<String> upperName = Optional.absent();
	private Optional<String> oldName = Optional.absent();

	Builder() {
	}

	public Builder destinationFolder(File destPath) {
		this.destPath = Optional.of(destPath.toPath());
		return this;
	}

	public Builder showName(String showName) {
		this.showName = Optional.of(showName);
		return this;
	}

	public Builder upperName(String upperName) {
		this.upperName = Optional.of(upperName);
		return this;
	}

	public MoveInfo build() {
		return new MoveInfo(destPath.get(), showName.get(), upperName.get(), oldName);
	}

	public Builder oldName(String name) {
		this.oldName = Optional.of(Preconditions2.checkNotEmpty(name));
		return this;
	}
}
