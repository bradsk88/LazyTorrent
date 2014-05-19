package ca.bradj.lazytorrent.app;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.connection.Torrent;
import ca.bradj.lazytorrent.matching.Matching;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

//AlreadyDownloadedTest
public class AlreadyDownloaded {

	private final Collection<String> delegate = Lists.newArrayList();

	AlreadyDownloaded(Collection<String> loaded) {
		delegate.addAll(loaded);
	}

	public void add(RSSTorrent i) {
		delegate.add(i.getName());
	}

	public static AlreadyDownloaded load(Path path) {
		Preconditions.checkNotNull(path);
		Collection<String> loaded = getAlreadyDownloaded(path);
		return new AlreadyDownloaded(loaded);
	}

	private static Collection<String> getAlreadyDownloaded(Path path) {
		Preconditions.checkNotNull(path);

		// TODO BJ May 19, 2014 Check the download target for downloaded shows.
		Collection<String> loaded = Lists.newArrayList();
		File folder = new File(path + File.separator + Torrent.TORRENTS_FOLDERNAME);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File[] listOfFiles = folder.listFiles();
		for (File i : listOfFiles) {
			if (i.isFile()) {
				loaded.add(i.getName());
			}
		}
		return loaded;
	}

	public boolean hasStrongMatch(RSSTorrent in) {
		String name = in.getName();
		for (String alreadyDownloaded : delegate) {

			Failable<Double> percent = Matching.getMatchPercent(name, alreadyDownloaded);
			if (percent.isSuccess()) {
				return true;
			}
		}
		return false;
	}

	public boolean isSameNameAndEpisode(RSSTorrent in) {
		String name = in.getName();
		for (String alreadyDownloaded : delegate) {

			double percent = Matching.getNameOverlap(name, alreadyDownloaded);
			if (percent >= 0.50) {
				Failable<EpisodeID> parse = EpisodeID.parse(name);
				Failable<EpisodeID> parse2 = EpisodeID.parse(alreadyDownloaded);
				if (parse.equals(parse2)) {
					return true;
				}
			}
		}
		return false;
	}

}
