package ca.bradj.lazytorrent.connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import ca.bradj.common.base.Failable;
import ca.bradj.common.base.Result;
import ca.bradj.lazytorrent.app.EpisodeID;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.rss.RSSTorrent;
import ca.bradj.lazytorrent.test.OpenTorrent;

import com.google.common.collect.Lists;

public class Torrent {

	public static final String TORRENTS_FOLDERNAME = "OpenTorrents";
	private static final Failable<File> ALREADY_DOWNLOADED = Failable.fail("Already downloaded");
	private static final Collection<String> JUNK = Lists.newArrayList("x264", "xvid", "480p", "720p", "1080p", "dl",
			"dd5", "web", "repack", "hdtv", "web-dl", "mkv", "torrent", "the", "2hd", "aac2");

	public static Failable<File> download(Path rootDir, RSSTorrent torrent) throws IOException {
		Path pathname = Paths.get(rootDir + File.separator + TORRENTS_FOLDERNAME);
		File dir = pathname.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String url = torrent.getURL();
		String filename = toFilename(pathname, url);
		File writeFile = new File(filename);
		if (writeFile.exists()) {
			return ALREADY_DOWNLOADED;
		}
		System.out.println("Downloading " + filename);
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		try (FileOutputStream fos = new FileOutputStream(writeFile)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			return Failable.ofSuccess(writeFile);
		}
	}

	private static String toFilename(Path pathName, String url) {
		String[] split = url.split("/");
		String fileEnd = split[split.length - 1];
		String[] removePass = fileEnd.split("\\?");
		fileEnd = removePass[0];
		return pathName + File.separator + fileEnd;
	}

	public static void openAndStart(File file, Logger logger, String torrentCommand) {
		Result ofFile = OpenTorrent.ofFile(file, torrentCommand);
		if (ofFile.isFailure()) {
			logger.error(ofFile.getReason());
		}
	}

	public static Collection<String> getPotentialName(String string) {

		Collection<String> pn = Lists.newArrayList();
		for (String i : string.split("[\\.|-|_|\\s]")) {
			if (EpisodeID.isA(i)) {
				continue;
			}
			if (JUNK.contains(i.toLowerCase())) {
				continue;
			}
			pn.add(i);
		}
		return pn;
	}

	public static String stripJunk(String string) {
		StringBuilder sb = new StringBuilder();
		for (String i : string.split("[\\.|\\-|_|\\s]")) {
			if (JUNK.contains(i.toLowerCase())) {
				continue;
			}
			sb.append(i + ".");
		}
		return sb.toString();
	}
}
