package ca.bradj.lazytorrent.transfer;

import java.io.File;
import java.nio.file.Path;

import ca.bradj.common.base.Preconditions2;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class MoveInfo {

	private final Path destPath;
	private final String showName;
	private Optional<Path> unrarred;
	private final String upperName;

	public MoveInfo(Path path, String showName, String upperName) {
		this.destPath = Preconditions.checkNotNull(path);
		this.showName = Preconditions2.checkNotEmpty(showName);
		this.upperName = Preconditions2.checkNotEmpty(upperName);
	}

	public static Builder create() {
		return new Builder();
	}

	public File getDestinationFile(File src) {
		return new File(destPath + File.separator + showName + File.separator
				+ src.getName());
	}

	public String getPrettyName() {
		return upperName;
	}

	@Override
	public String toString() {
		return "MoveInfo [destPath=" + destPath + ", showName=" + showName
				+ ", unrarred=" + unrarred + ", upperName=" + upperName + "]";
	}

}
