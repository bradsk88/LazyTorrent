package ca.bradj.lazytorrent.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import ca.bradj.common.base.Failable;
import ca.bradj.common.base.Preconditions2;
import ca.bradj.lazytorrent.app.Config;
import ca.bradj.lazytorrent.app.Logger;
import ca.bradj.lazytorrent.matching.TorrentMatch;
import ca.bradj.lazytorrent.matching.TorrentMatchings;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class MoveFinishedTorrents implements Runnable {

	private final Logger logger;
	private final TorrentMatchings prefs;
	private final AlreadyTransferred alreadyTransferred;
	private Path destinationTVFolder;
	private Path finishedfolder;
	private final String unrarCommand;

	public MoveFinishedTorrents(Logger logger, TorrentMatchings matchings,
			AlreadyTransferred already, Path destinationTVFolder,
			Path finishedTorrentsDir, String unrarCmd) {
		this.destinationTVFolder = Preconditions
				.checkNotNull(destinationTVFolder);
		this.logger = Preconditions.checkNotNull(logger);
		this.prefs = Preconditions.checkNotNull(matchings);
		this.alreadyTransferred = Preconditions.checkNotNull(already);
		this.finishedfolder = Preconditions.checkNotNull(finishedTorrentsDir);
		this.unrarCommand = Preconditions2.checkNotEmpty(unrarCmd);
	}

	@Override
	public void run() {

		try {

			if (Files.notExists(finishedfolder)) {
				logger.notification(finishedfolder.toString()
						+ " didn't exist.  Cannot manage files.");
				return;
			}

			int year = DateTime.now().year().get();
			File file = new File(destinationTVFolder.toFile(),
					Integer.toString(year));
			File[] listFiles = finishedfolder.toFile().listFiles();
			for (File i : listFiles) {
				processFile(file, i);
			}
		} catch (Exception e) {
			logger.notification(e.getMessage());
		}

	}

	private void processFile(File file, File i) {
		logger.debug("Assessing " + file.getName() + " for move");
		try {
			String name = i.getName();
			if (alreadyTransferred.matches(name)) {
				logger.debug("An exact match has already been moved for "
						+ name + ", skipping it.");
				return;
			}
			TorrentMatch showName = prefs.getStrongestMatch(i.getName());
			if (alreadyTransferred.matches(showName.getName())) {
				logger.debug(showName + " already exists.  Skipping move.");
				return;
			}
			if (showName.isUnmovable()) {
				logger.debug(name + " is unmovable.");
				return;
			}
			if (showName.isMovie()) {
				logger.debug("Identified " + name + " as a movie.  Skipping.");
				return;
			}
			if (showName.isPreference()) {
				logger.debug("Moving " + showName.getName() + " now");

				try {
					moveFile(file, i, showName.getName(), name);
				} catch (Exception e) {
					logger.error("An exception occurred during the file transfer for "
							+ showName.getName() + "\n" + e.getMessage());
					e.printStackTrace();
				}
				return;
			}
			prefs.addUnmovable(i.getName());
			logger.error("Error: Couldn't assign show name to " + i.getName()
					+ ". [" + showName.getReason() + "]");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while processing: " + i.getName());
		}
	}

	private void moveFile(File destPath, File i, String showName,
			String upperName) {

		MoveInfo moveInfo = MoveInfo.create().destinationFolder(destPath)
				.showName(showName).upperName(upperName).build();

		logger.debug("Move process initiated for: " + moveInfo);

		if (i.isDirectory()) {
			if (directoryContainsIncompleteSetOfRARFiles(i)) {
				logger.error("Directory contains incomple set of RAR files: "
						+ i);
				return;
			}
			for (File f : i.listFiles()) {
				if (isRAR(f)) {
					unRARAndMove(moveInfo, f, false);
					return;
				}
			}
			return;
		}
		if (isMoveType(i)) {
			File destination = new File(destPath.getPath() + File.separator
					+ showName + File.separator + i.getName());
			if (Files.exists(destination.toPath())) {
				return;
			}
			doMoveFile(i, moveInfo, false);
		}
	}

	@SuppressWarnings("unused")
	private Optional<Failable<File>> unRARAndMove(MoveInfo moveInfo, File file,
			boolean isNested) {

		// flip a coin. If heads, unrar this file and check if it
		// needs to be sent to the server. This random aspect is
		// just here to reduce disk usage. The file WILL eventually
		// be transferred.
		if (!isNested && Config.ALLOW_TRANSFER_RANDOMIZATION
				&& new Random().nextBoolean()) {
			return Optional.absent();
		}

		Failable<File> unrarred = unrar(file, moveInfo);
		if (isNested) {
			return Optional.of(unrarred);
		}
		if (unrarred.isSuccess()) {
			doMoveFile(unrarred.get(), moveInfo, true);
		} else {
			logger.log(unrarred.getReason());
		}
		return Optional.absent();
	}

	private boolean isRAR(File f) {
		if (f.getName().toLowerCase().endsWith(".rar")) {
			return true;
		}
		return false;
	}

	private Failable<File> unrar(File f, MoveInfo moveInfo) {

		try {
			String command = unrarCommand + " " + f.getAbsolutePath();
			if (command.contains("%RARFILE%")) {
				command = unrarCommand
						.replace("%RARFILE%", f.getAbsolutePath());
			}
			if (command.contains("%RARFOLDER%")) {
				command = command.replace("%RARFOLDER%", f.getParent());
			}
			File parent = f.getParentFile();
			Process exec = Runtime.getRuntime().exec(command);
			logger.debug("Command is : " + command);
			logger.debug("Waiting for " + f.getName() + " unrar to complete");
			try (@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					exec.getInputStream()))) {
				String line = reader.readLine();
				while (line != null) {
					if (line.trim().startsWith("...")) {
						line = reader.readLine();
						continue;
					}
					if (line.trim().length() > 0) {
						logger.debug(line);
					}
					line = reader.readLine();
				}
				exec.waitFor();
				logger.debug("Unrar of " + f.getName() + " complete");

				if (directoryContainsIncompleteSetOfRARFiles(parent)) {
					return Failable
							.fail("Directory contains incomplete set of RAR files: "
									+ parent);
				}

				ArrayList<File> listFiles = Lists.newArrayList(parent
						.listFiles());
				for (File i : Iterables.filter(listFiles, isNot(f))) {
					if (isMoveType(i)) {
						return Failable.ofSuccess(i);
					}
					if (isRAR(i)) {
						// nested RARs
						Optional<Failable<File>> moved = unRARAndMove(moveInfo,
								i, true);
						if (moved.isPresent()) {
							return moved.get();
						}
					}
				}
				return Failable.fail("Unrarred " + f.getName()
						+ ", but valid file not present");
			}
		} catch (Exception e) {
			logger.notification(e.getMessage());
			e.printStackTrace();
			return Failable.fail(e.getMessage());
		}
	}

	private Predicate<File> isNot(final File f) {
		return new Predicate<File>() {

			@Override
			public boolean apply(File arg0) {
				return !f.equals(arg0);
			}
		};
	}

	private boolean directoryContainsIncompleteSetOfRARFiles(File parent) {

		int expectedNumber = 0;
		boolean gapped = false;
		File[] listFiles = parent.listFiles();
		Collection<File> sortedListFiles = Ordering.<File> natural()
				.sortedCopy(Lists.newArrayList(listFiles));
		for (File i : sortedListFiles) {
			if (i.getPath().endsWith("\\.rar")) {
				continue;
			}
			Optional<Integer> n = getRARNumber(i);
			if (n.isPresent()) {
				if (gapped) {
					return true;
				}
				if (expectedNumber == n.get()) {
					expectedNumber++;
					continue;
				}
				gapped = true;
				expectedNumber++;
			}
		}
		return false;
	}

	private Optional<Integer> getRARNumber(File i) {
		String path = i.getPath();
		String extension = path.substring(path.length() - 3, path.length());
		Matcher matcher = Pattern.compile("r([0-9][0-9])").matcher(extension);
		if (matcher.matches()) {
			try {
				int parseInt = Integer.parseInt(matcher.group(1));
				return Optional.of(parseInt);
			} catch (NumberFormatException e) {
				return Optional.absent();
			}
		}
		return Optional.absent();
	}

	private boolean isMoveType(File i) {
		if (i.getName().endsWith(".mkv")) {
			return true;
		}
		if (i.getName().endsWith(".avi")) {
			return true;
		}
		if (i.getName().endsWith(".mp4")) {
			return true;
		}
		return false;
	}

	private void doMoveFile(File src, MoveInfo moveInfo, boolean deleteAfterMove) {
		File dest = moveInfo.getDestinationFile(src);
		if (Files.exists(dest.toPath()) && src.length() == dest.length()) {
			if (deleteAfterMove) {
				delete(src);
			}
			alreadyTransferred.addAndWriteToDisk(moveInfo.getPrettyName());
			return;
		}
		if (Files.notExists(dest.getParentFile().toPath())) {
			dest.getParentFile().mkdirs();
		}
		try (FileInputStream fis = new FileInputStream(src);
				FileOutputStream fos = new FileOutputStream(dest)) {
			logger.notification("Transferring file " + src.getName() + " to "
					+ dest.getAbsolutePath());
			fos.getChannel().transferFrom(fis.getChannel(), 0,
					fis.getChannel().size());
			logger.log("Transfer ended for " + src.getName());
		} catch (Exception e) {
			logger.notification(e.getMessage());
			e.printStackTrace();
		} finally {
			if (deleteAfterMove) {
				delete(src);
			}
			alreadyTransferred.addAndWriteToDisk(moveInfo.getPrettyName());
		}

	}

	private void delete(File src) {
		boolean delete = src.delete();
		if (!delete) {
			logger.error("Could not delete " + src.getPath());
		}
	}
}
