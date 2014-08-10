package ca.bradj.lazytorrent.app;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import ca.bradj.common.base.Failable;
import ca.bradj.fx.dialogs.Dialogs;
import ca.bradj.fx.dialogs.TextInputQuestionDialog;
import ca.bradj.lazytorrent.automated.DownloadDaemon;
import ca.bradj.lazytorrent.matching.TorrentMatchings;
import ca.bradj.lazytorrent.rss.FXThreading;
import ca.bradj.lazytorrent.rss.RSSFeed;
import ca.bradj.lazytorrent.rss.TorrentsRSSFeed;
import ca.bradj.lazytorrent.transfer.AlreadyTransferred;
import ca.bradj.lazytorrent.transfer.FileToXBMCDaemon;

public class Main extends Application {

	private static final String USER_CONFIG_FILE = "userconfig";
	private static final String CONFIG_FILE = getAppDataDir() + File.separator + getDotIfNeeded() + "LazyTorrent"
			+ File.separator + "config";
	private static final String NOTIFICATION = "Notification";
	private static final Image NORMAL_IMAGE;
	private static final Image ERROR_IMAGE;
	protected static final String RECORDED = "Recorded logs to %APPDATA/LazyTorrent/logs";
	private static final javafx.scene.image.Image APP_ICON = tryLoadFXImage("normal.png");
	private static final Failable<Path> USER_CANCELLED = Failable.fail("Cancelled by user");
	@SuppressWarnings("rawtypes")
	private static final Failable NO_CONFIG_YET = Failable.fail("System configuration does not yet exist.");
	@SuppressWarnings("rawtypes")
	private static final Failable NO_USER_CONFIG_YET = Failable.fail("User configuration does not yet exist.");
	private static final Failable<String> EMPTY_URL = Failable.fail("User provided empty torrent URL");
	static {
		ERROR_IMAGE = tryLoadImage("error.png");
		NORMAL_IMAGE = tryLoadImage("normal.png");
	}

	private boolean firstTime;
	private TrayIcon trayIcon;

	public static void main(String[] args) {
		launch(args);
	}

	private static String getDotIfNeeded() {
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("NUX")) {
			return ".";
		}
		return "";
	}

	private static String getAppDataDir() {
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN"))
			return System.getenv("APPDATA");
		else if (OS.contains("MAC"))
			return System.getProperty("user.home") + "/Library/Application " + "Support";
		else if (OS.contains("NUX"))
			return System.getProperty("user.home");
		return System.getProperty("user.dir");
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.getIcons().add(APP_ICON);
		firstTime = true;
		Platform.setImplicitExit(false);
		try {
			stage.setOpacity(0);
			stage.show();
			Logger logger = new SimpleLogger();

			Failable<Path> root = getRoot(stage, logger);
			if (root.isFailure()) {
				stage.close();
				System.exit(0);
			}
			Path rootG = root.get();

			Failable<String> torrentsURL = getTorrentFeedURL(rootG);
			if (torrentsURL.isFailure()) {
				stage.close();
				System.exit(0);
			}
			TorrentMatchings m = TorrentMatchings.load(rootG);
			logger.debug("Opened TorrentMatchings at " + m.getFile());

			AlreadyTransferred t = AlreadyTransferred.load(rootG);
			logger.debug("Opened AlreadyTransferred at " + t.getFilename());

			AlreadyDownloaded alreadyDownloaded = AlreadyDownloaded.empty();
			alreadyDownloaded.load(rootG, logger);

			RSSFeed rss = new TorrentsRSSFeed(torrentsURL.get(), alreadyDownloaded, logger);

			final ScheduledExecutorService ex = DownloadDaemon.start(rootG, rss, m.getPreferences(), alreadyDownloaded,
					logger);
			FileToXBMCDaemon fileToXBMCDaemon = new FileToXBMCDaemon();
			final ScheduledExecutorService fileMove = fileToXBMCDaemon.start(logger, m, t);
			final ScheduledExecutorService logSaveClear = LoggerSaveClear.start(rootG, logger);
			createTrayIcon(stage, ex, fileMove, logger, logSaveClear);
			Parent pane = new LazyTorrentsControlPanel(rootG, m, alreadyDownloaded, logger, rss,
					fileToXBMCDaemon.countDownProperty()).getNode();
			Scene scene = new Scene(pane, 1024, 768);
			stage.setOpacity(1.0);
			stage.setScene(scene);
			// hide(stage);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private Failable<String> getTorrentFeedURL(Path root) {

		Failable<String> existing = getExistingTorrentURL(root);
		if (existing.isSuccess()) {
			return existing;
		}

		TextInputQuestionDialog d = Dialogs.newTextInputQuestionDialog();
		Failable<String> answer = d.showDialog();
		if (answer.isSuccess()) {
			if (answer.get().isEmpty()) {
				return EMPTY_URL;
			}
			File userconf = new File(root + File.separator + USER_CONFIG_FILE);
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(userconf))) {
				bw.write(answer.get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return answer;
	}

	@SuppressWarnings("unchecked")
	private Failable<String> getExistingTorrentURL(Path root) {
		File userconf = new File(root + File.separator + USER_CONFIG_FILE);
		if (userconf.exists()) {

			try (BufferedReader br = new BufferedReader(new FileReader(userconf))) {
				String s = br.readLine();
				if (s.isEmpty()) {
					return NO_USER_CONFIG_YET;
				}
				return Failable.ofSuccess(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return NO_USER_CONFIG_YET;
	}

	private Failable<Path> getRoot(Stage stage, Logger logger) {

		Failable<Path> existingRoot = getExistingRoot();
		if (existingRoot.isSuccess()) {
			return existingRoot;
		}

		logger.debug(existingRoot.getReason() + " -- Prompting user.");

		DirectoryChooser fc = new DirectoryChooser();
		File file = fc.showDialog(stage.getOwner());
		if (file == null) {
			return USER_CANCELLED;
		}

		recordDir(file);

		return Failable.ofSuccess(file.toPath());
	}

	private void recordDir(File file) {
		File configFile = new File(CONFIG_FILE);
		try (BufferedWriter br = new BufferedWriter(new FileWriter(configFile))) {
			br.write(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private Failable<Path> getExistingRoot() {
		File rootQ = new File(CONFIG_FILE);
		if (rootQ.exists()) {

			try (BufferedReader br = new BufferedReader(new FileReader(rootQ))) {
				File potential = new File(br.readLine());
				if (potential.exists()) {
					return Failable.ofSuccess(potential.toPath());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return NO_CONFIG_YET;
	}

	public void createTrayIcon(final Stage stage, final ScheduledExecutorService exec,
			final ScheduledExecutorService fileMove, Logger logger, final ScheduledExecutorService logSaveClear) {
		if (SystemTray.isSupported()) {
			// get the SystemTray instance
			SystemTray tray = SystemTray.getSystemTray();

			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					hide(stage);
				}
			});
			// create a action listener to listen for default action executed on
			// the tray icon
			final ActionListener closeListener = new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					exec.shutdownNow();
					fileMove.shutdownNow();
					logSaveClear.shutdownNow();
					System.exit(0);
				}
			};

			ActionListener showListener = new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							stage.show();
						}
					});
					trayIcon.setImage(NORMAL_IMAGE);
				}
			};
			// create a popup menu
			PopupMenu popup = new PopupMenu();

			MenuItem showItem = new MenuItem("Show");
			showItem.addActionListener(showListener);
			popup.add(showItem);

			MenuItem closeItem = new MenuItem("Close");
			closeItem.addActionListener(closeListener);
			popup.add(closeItem);
			// / ... add other items
			// construct a TrayIcon
			trayIcon = new TrayIcon(NORMAL_IMAGE, "LazyTorrent", popup);
			// set the TrayIcon properties
			trayIcon.addActionListener(showListener);
			// ...
			// add the tray image
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}
			// ...
			addLogListeners(logger);
		}
	}

	private void addLogListeners(Logger logger) {
		logger.addLogListener(new LogListener() {

			@Override
			public void newNotificationAdded(final String string) {
				if (trayIcon == null) {
					return;
				}
				FXThreading.invokeLater(new Runnable() {
					@Override
					public void run() {
						trayIcon.displayMessage(NOTIFICATION, string, MessageType.INFO);
					}
				});
			}

			@Override
			public void newMessageAdded(String message) {
				// do nothing
			}

			@Override
			public void newErrorAdded(String string) {
				if (trayIcon == null) {
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						trayIcon.setImage(ERROR_IMAGE);
					}
				});
			}

			@Override
			public void bufferCleared() {
				trayIcon.displayMessage(NOTIFICATION, RECORDED, MessageType.INFO);
			}

			@Override
			public void debugAdded(String string) {
				// do nothin
			}
		});
	}

	private static Image tryLoadImage(String string) {
		try {
			return ImageIO.read(Main.class.getResourceAsStream(string));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static javafx.scene.image.Image tryLoadFXImage(String string) {
		try {
			return new javafx.scene.image.Image(Main.class.getResourceAsStream(string));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void showProgramIsMinimizedMsg() {
		if (firstTime) {
			trayIcon.displayMessage("Running in background.", "Will download new torrents automatically.",
					TrayIcon.MessageType.INFO);
			firstTime = false;
		}
	}

	private void hide(final Stage stage) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (SystemTray.isSupported()) {
					stage.hide();
					showProgramIsMinimizedMsg();
				} else {
					System.exit(0);
				}
			}
		});
	}
}
