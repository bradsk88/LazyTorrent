package ca.bradj.lazytorrent.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

public class LoggerSaveClear {

	private static final String LOGDIR = "logs";

	public static ScheduledExecutorService start(final Path rootDir, final Logger logger) {
		ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
		ex.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				Collection<String> messagesSoFar = logger.getMessageBuffer();
				File fileToWrite = new File(rootDir.toFile(), LOGDIR + File.separator
						+ DateTime.now().toString("dd-MM-YYYY--hhmm") + ".log");
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
