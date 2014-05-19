package ca.bradj.lazytorrent.test;

import java.io.File;

import ca.bradj.common.base.Result;

public class OpenTorrent {

	private static final String TORRENT_PROG = "C:" + File.separator + "Program Files (x86)" + File.separator
			+ "uTorrent" + File.separator + "uTorrent.exe";
	private static final String OPEN_TORRENT = TORRENT_PROG + " /MINIMIZED /DIRECTORY \"D:" + File.separator + "\" \"";

	public static Result ofFile(File f) {
		if (f.exists()) {
			try {
				String command = OPEN_TORRENT + f.getAbsolutePath() + "\"";
				System.out.println(command);
				Runtime rt = Runtime.getRuntime();
				rt.exec(command);
			} catch (Exception e) {
				e.printStackTrace();
				return Result.failure(e.getMessage());
			}
			return Result.success();
		}
		return Result.failure("Unable to open " + f + " in torrent app");
	}
}
