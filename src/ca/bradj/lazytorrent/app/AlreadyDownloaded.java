package ca.bradj.lazytorrent.app;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.util.Pair;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.connection.Torrent;
import ca.bradj.lazytorrent.matching.Matching;
import ca.bradj.lazytorrent.rss.FXThreading;
import ca.bradj.lazytorrent.rss.RSSTorrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

//AlreadyDownloadedTest
public class AlreadyDownloaded {

	static final int SEEDING_DATE = 9999;
	private final Map<Integer, Collection<String>> delegate;
	private final ReadOnlyDoubleWrapper loadProgress = new ReadOnlyDoubleWrapper(0.0);
	private final ReadOnlyBooleanWrapper isFullyLoaded = new ReadOnlyBooleanWrapper(false);

	private AlreadyDownloaded(Map<Integer, Collection<String>> in) {
		TreeMap<Integer, Collection<String>> map = Maps.newTreeMap(Ordering.<Integer> natural().reverse());
		map.putAll(in);
		this.delegate = Collections.synchronizedMap(map);
	}

	public static AlreadyDownloaded empty() {
		Map<Integer, Collection<String>> in = Collections.emptyMap();
		return new AlreadyDownloaded(in);
	}

	public static AlreadyDownloaded with(Map<Integer, Collection<String>> in) {
		return new AlreadyDownloaded(in);
	}

	public ReadOnlyBooleanProperty isFullyLoadedProperty() {
		return isFullyLoaded.getReadOnlyProperty();
	}

	public ReadOnlyDoubleProperty loadProgressProperty() {
		return loadProgress.getReadOnlyProperty();
	}

	public void add(RSSTorrent i) {
		delegate.get(SEEDING_DATE).add(i.getName());
	}

	public void load(final Path path, final Path tvDest, final Logger log) {
		Preconditions.checkNotNull(path);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					getAlreadyDownloaded(path, tvDest, delegate);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		});
		t.start();
	}

	private void getAlreadyDownloaded(Path rootPath, Path destinationPath, Map<Integer, Collection<String>> toFill) {
		Preconditions.checkNotNull(rootPath);

		Collection<String> seeding = Lists.newArrayList();
		File folder = new File(rootPath + File.separator + Torrent.TORRENTS_FOLDERNAME);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File[] listOfFiles = folder.listFiles();
		for (File i : listOfFiles) {
			if (i.isFile()) {
				seeding.add(i.getName());
			}
		}
		toFill.put(SEEDING_DATE, seeding);

		int thisYear = DateTime.now().getYear();
		Collection<Pair<Integer, File>> yearsToCheck = Lists.newArrayList();
		for (int i = thisYear; i > thisYear - 100; i--) {
			File yearFolder = new File(destinationPath.toFile(),Integer.toString(i));
			if (yearFolder.exists()) {
				yearsToCheck.add(new Pair<>(i, yearFolder));
			}
		}
		double i = 0;
		double total = yearsToCheck.size();
		for (Pair<Integer, File> year : yearsToCheck) {
			loadProgress.set(i / total);
			Set<String> yearShows = Sets.newHashSet();
			for (File f : year.getValue().listFiles()) {
				yearShows.addAll(getShowNames(f, f.getName()));
			}
			toFill.put(year.getKey(), yearShows);
			i++;
		}
		FXThreading.invokeLater(new Runnable() {

			@Override
			public void run() {
				loadProgress.set(1.0);
				isFullyLoaded.set(true);
			}
		});
	}

	private static Collection<? extends String> getShowNames(File f, String showname) {
		if (f.isDirectory()) {
			Set<String> folderShows = Sets.newHashSet();
			for (File f2 : f.listFiles()) {
				if (isRAR(f2)) {
					return show(showname, f2);
				}
				folderShows.addAll(getShowNames(f2, showname));
			}
			return folderShows;
		}
		if (isExtraFile(f)) {
			return Collections.emptyList();
		}
		return show(showname, f);

	}

	private static Collection<? extends String> show(String showname, File f2) {
		Failable<EpisodeID> parse = EpisodeID.parse(f2.getName());
		if (parse.isSuccess()) {
			String s = String.format("%02d", parse.get().getSeason());
			String e = String.format("%02d", parse.get().getEpisode());
			String episode = showname + " S" + s + "E" + e;
			return Lists.newArrayList(episode);
		}
		return Lists.newArrayList(FilenameUtils.removeExtension(f2.getName()));
	}

	private static boolean isRAR(File f2) {
		String lowName = f2.getName().toLowerCase();
		if (lowName.endsWith(".rar")) {
			return true;
		}
		if (lowName.endsWith(".r00")) {
			return true;
		}
		if (lowName.endsWith(".r01")) {
			return true;
		}
		return false;
	}

	private static boolean isExtraFile(File f2) {
		if (f2.getName().endsWith("jpg")) {
			return true;
		}
		if (f2.getName().endsWith("png")) {
			return true;
		}
		if (f2.getName().endsWith("nfo")) {
			return true;
		}
		if (f2.getName().endsWith("sfv")) {
			return true;
		}

		return false;
	}

	public boolean hasStrongMatch(RSSTorrent in) {
		String name = in.getName();
		Map<Integer, Collection<String>> map = delegate;
		if (!isFullyLoaded.get()) {
			map = ImmutableMap.copyOf(delegate);
		}
		for (Entry<Integer, Collection<String>> year : map.entrySet()) {
			for (String alreadyDownloaded : year.getValue()) {
				Failable<Double> percent = Matching.getMatchPercent(name, alreadyDownloaded);
				if (percent.isSuccess()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasSameNameAndEpisode(RSSTorrent in) {
		String name = in.getName();
		Map<Integer, Collection<String>> map = delegate;
		if (!isFullyLoaded.get()) {
			map = ImmutableMap.copyOf(delegate);
		}
		for (Entry<Integer, Collection<String>> year : map.entrySet()) {
			for (String alreadyDownloaded : year.getValue()) {

				double percent = Matching.getNameOverlap(name, alreadyDownloaded);
				if (percent >= 0.50) {
					Failable<EpisodeID> parse = EpisodeID.parse(name);
					Failable<EpisodeID> parse2 = EpisodeID.parse(alreadyDownloaded);
					if (parse.equals(parse2)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean shouldDownload(RSSTorrent torrent) {
		if (hasSameNameAndEpisode(torrent)) {
			return false;
		}
		if (!isFullyLoaded.get()) {
			return false;
		}
		return true;
	}

}
