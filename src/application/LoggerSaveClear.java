package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import ca.bradj.lazytorrent.app.Logger;

public class LoggerSaveClear {

	private static final String LOG_FILE_DIR = System.getenv("APPDATA") + File.separator + "LazyTorrent"
			+ File.separator + "logs";

	public static ScheduledExecutorService start(final Logger logger) {
		ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
		ex.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				Collection<String> messagesSoFar = logger.getMessageBuffer();
				File fileToWrite = new File(LOG_FILE_DIR + File.separator + DateTime.now().toString("dd-MM-YYYY--hhmm")
						+ ".log");
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite))) {
					for (String i : messagesSoFar) {
						bw.write(i + "\r\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
				} finally {
					logger.clearMessageBuffer();
				}

			}
		}, 12, 12, TimeUnit.HOURS);
		return ex;
	}

}
