package bms.player.beatoraja.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.TableDataAccessor;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;

public class ResourceConfigurationView implements Initializable {

	@FXML
	private ListView<String> bmsroot;
	@FXML
	private TextField url;
	@FXML
	private EditableTableView<TableInfo> tableurl;
    @FXML
    private EditableTableView<TableInfo> available_tables;
	@FXML
	private CheckBox updatesong;

	private Config config;
	
	private PlayConfigurationView main;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
	
	void init(PlayConfigurationView main) {
		this.main = main;

        TableColumn<TableInfo,String> nameColumn = new TableColumn<TableInfo,String>("NAME/STATUS");
        nameColumn.setCellValueFactory((p) -> p.getValue().nameStatusProperty());
        nameColumn.setSortable(false);
        nameColumn.setMinWidth(180);
        nameColumn.setMinWidth(0);

		TableColumn<TableInfo,String> commentColumn = new TableColumn<TableInfo,String>("COMMENT");
		commentColumn.setCellValueFactory((p) -> p.getValue().commentProperty());
		commentColumn.setSortable(false);
		commentColumn.setMinWidth(180);
		commentColumn.setMinWidth(0);

		TableColumn<TableInfo,String> urlColumn = new TableColumn<TableInfo,String>("URL");
		urlColumn.setCellValueFactory((p) -> p.getValue().urlProperty());
		urlColumn.setSortable(false);
		urlColumn.setMinWidth(300);
		urlColumn.setMinWidth(0);
	  
 		tableurl.getColumns().setAll(nameColumn, commentColumn, urlColumn);
 		tableurl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

    public void update(Config config) {
    	this.config = config;
		bmsroot.getItems().setAll(config.getBmsroot());
		updatesong.setSelected(config.isUpdatesong());
		TableInfo.populateList(tableurl.getItems(), config.getTableURL());
	}

	public void commit() {
		config.setBmsroot(bmsroot.getItems().toArray(new String[0]));
		config.setUpdatesong(updatesong.isSelected());
		config.setTableURL(TableInfo.toUrlArray(tableurl.getItems()));
	}

    @FXML
	public void refreshLocalTableInfo() {
		ResourceBundle bundle = ResourceBundle.getBundle("resources.UIResources");
		// Functions in ResourceConfigurationView are not run on the JavaFX Application Thread and so this little
		// workaround has to be performed here to allow the progress bar to function as expected.
		final Stage[] loadingBarStage = new Stage[1];
		Runnable progressRunnable = () -> {
			// JavaFX UI code must be run inside a Platform run context
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0] = new Stage();
					loadingBarStage[0].setResizable(false);
					// This modality freezes the launcher/primary stage
					loadingBarStage[0].initModality(Modality.APPLICATION_MODAL);
					loadingBarStage[0].setTitle(bundle.getString("PROGRESS_TABLE_TITLE"));
					// This prevents users from seeing typical windowing system buttons
					loadingBarStage[0].initStyle(StageStyle.UTILITY);

					ProgressBar progressBar = new ProgressBar();
					progressBar.setPrefWidth(300);

					Label messageLabel = new Label(bundle.getString("PROGRESS_TABLE_LABEL"));

					VBox root = new VBox(10);
					root.setStyle("-fx-padding: 20; -fx-alignment: center;");
					root.getChildren().addAll(messageLabel, progressBar);

					Scene scene = new Scene(root);
					loadingBarStage[0].setScene(scene);

					// Prevents closing. This has the side effect of preventing windowing system close requests but
					// the application can still be force killed by the user if necessary
					loadingBarStage[0].setOnCloseRequest(Event::consume);
					loadingBarStage[0].show();
				}
			});
		};

		Runnable loadTableRunnable = () -> {
			String[] urls = TableInfo.toUrlArray(tableurl.getItems());
			TableDataAccessor tda = new TableDataAccessor(config.getTablepath());
			HashMap<String,String> urlToTableNameMap = tda.readLocalTableNames(urls);
			for (TableInfo tableInfo : tableurl.getItems()) {
				String tableName = (urlToTableNameMap == null) ? null : urlToTableNameMap.get(tableInfo.getUrl());
				tableInfo.setNameStatus((tableName == null) ? "not loaded" : tableName);
			}

			// Once again, JavaFX UI code must be run inside a Platform context. Hide progress bar and resume
			// normal launcher behaviour
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0].hide();
				}
			});
		};

		new Thread(progressRunnable).start();
		new Thread(loadTableRunnable).start();
	}

    @FXML
	public void loadAllTables() {
		commit();
		ResourceBundle bundle = ResourceBundle.getBundle("resources.UIResources");
		try {
			Files.createDirectories(Paths.get(config.getTablepath()));
		} catch (IOException e) {
		}

		final Stage[] loadingBarStage = new Stage[1];
		Runnable progressRunnable = () -> {
			// JavaFX UI code must be run inside a Platform run context
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0] = new Stage();
					loadingBarStage[0].setResizable(false);
					// This modality freezes the launcher/primary stage
					loadingBarStage[0].initModality(Modality.APPLICATION_MODAL);
					loadingBarStage[0].setTitle(bundle.getString("PROGRESS_TABLE_TITLE"));
					// This prevents users from seeing typical windowing system buttons
					loadingBarStage[0].initStyle(StageStyle.UTILITY);

					ProgressBar progressBar = new ProgressBar();
					progressBar.setPrefWidth(300);

					Label messageLabel = new Label(bundle.getString("PROGRESS_TABLE_LABEL"));

					VBox root = new VBox(10);
					root.setStyle("-fx-padding: 20; -fx-alignment: center;");
					root.getChildren().addAll(messageLabel, progressBar);

					Scene scene = new Scene(root);
					loadingBarStage[0].setScene(scene);

					// Prevents closing. This has the side effect of preventing windowing system close requests but
					// the application can still be force killed by the user if necessary
					loadingBarStage[0].setOnCloseRequest(Event::consume);
					loadingBarStage[0].show();
				}
			});
		};

		Runnable loadTableRunnable = () -> {
			try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(config.getTablepath()))) {
				paths.forEach((p) -> {
					if(p.toString().toLowerCase().endsWith(".bmt")) {
						try {
							Files.deleteIfExists(p);
						} catch (IOException ignored) {
						}
					}
				});
			} catch (IOException ignored) {
			}

			TableDataAccessor tda = new TableDataAccessor(config.getTablepath());
			tda.updateTableData(config.getTableURL());
			refreshLocalTableInfo();

			// Once again, JavaFX UI code must be run inside a Platform context. Hide progress bar and resume
			// normal launcher behaviour
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0].hide();
				}
			});
		};

		new Thread(progressRunnable).start();
		new Thread(loadTableRunnable).start();
	}

    @FXML
	public void loadSelectedTables() {
		commit();
		ResourceBundle bundle = ResourceBundle.getBundle("resources.UIResources");
		try {
			Files.createDirectories(Paths.get(config.getTablepath()));
		} catch (IOException e) {
		}

		final Stage[] loadingBarStage = new Stage[1];
		Runnable progressRunnable = () -> {
			// JavaFX UI code must be run inside a Platform run context
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0] = new Stage();
					loadingBarStage[0].setResizable(false);
					// This modality freezes the launcher/primary stage
					loadingBarStage[0].initModality(Modality.APPLICATION_MODAL);
					loadingBarStage[0].setTitle(bundle.getString("PROGRESS_TABLE_TITLE"));
					// This prevents users from seeing typical windowing system buttons
					loadingBarStage[0].initStyle(StageStyle.UTILITY);

					ProgressBar progressBar = new ProgressBar();
					progressBar.setPrefWidth(300);

					Label messageLabel = new Label(bundle.getString("PROGRESS_TABLE_LABEL"));

					VBox root = new VBox(10);
					root.setStyle("-fx-padding: 20; -fx-alignment: center;");
					root.getChildren().addAll(messageLabel, progressBar);

					Scene scene = new Scene(root);
					loadingBarStage[0].setScene(scene);

					// Prevents closing. This has the side effect of preventing windowing system close requests but
					// the application can still be force killed by the user if necessary
					loadingBarStage[0].setOnCloseRequest(Event::consume);
					loadingBarStage[0].show();
				}
			});
		};

		Runnable loadTableRunnable = () -> {
			TableDataAccessor tda = new TableDataAccessor(config.getTablepath());
			String[] urls = TableInfo.toUrlArray(tableurl.getSelectionModel().getSelectedItems());
			tda.updateTableData(urls);
			refreshLocalTableInfo();

			// Once again, JavaFX UI code must be run inside a Platform context. Hide progress bar and resume
			// normal launcher behaviour
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0].hide();
				}
			});
		};

		new Thread(progressRunnable).start();
		new Thread(loadTableRunnable).start();
	}

    @FXML
	public void loadNewTables() {
		commit();
		ResourceBundle bundle = ResourceBundle.getBundle("resources.UIResources");
		try {
			Files.createDirectories(Paths.get(config.getTablepath()));
		} catch (IOException e) {
		}

		final Stage[] loadingBarStage = new Stage[1];
		Runnable progressRunnable = () -> {
			// JavaFX UI code must be run inside a Platform run context
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0] = new Stage();
					loadingBarStage[0].setResizable(false);
					// This modality freezes the launcher/primary stage
					loadingBarStage[0].initModality(Modality.APPLICATION_MODAL);
					loadingBarStage[0].setTitle(bundle.getString("PROGRESS_TABLE_TITLE"));
					// This prevents users from seeing typical windowing system buttons
					loadingBarStage[0].initStyle(StageStyle.UTILITY);

					ProgressBar progressBar = new ProgressBar();
					progressBar.setPrefWidth(300);

					Label messageLabel = new Label(bundle.getString("PROGRESS_TABLE_LABEL"));

					VBox root = new VBox(10);
					root.setStyle("-fx-padding: 20; -fx-alignment: center;");
					root.getChildren().addAll(messageLabel, progressBar);

					Scene scene = new Scene(root);
					loadingBarStage[0].setScene(scene);

					// Prevents closing. This has the side effect of preventing windowing system close requests but
					// the application can still be force killed by the user if necessary
					loadingBarStage[0].setOnCloseRequest(Event::consume);
					loadingBarStage[0].show();
				}
			});
		};

		Runnable loadTableRunnable = () -> {
			TableDataAccessor tda = new TableDataAccessor(config.getTablepath());
			tda.loadNewTableData(config.getTableURL());
			refreshLocalTableInfo();

			// Once again, JavaFX UI code must be run inside a Platform context. Hide progress bar and resume
			// normal launcher behaviour
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					loadingBarStage[0].hide();
				}
			});
		};

		new Thread(progressRunnable).start();
		new Thread(loadTableRunnable).start();
	}


    @FXML
	public void addSongPath() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("楽曲のルートフォルダを選択してください");
		File f = chooser.showDialog(null);
		if (f != null) {
			final String defaultPath = new File(".").getAbsoluteFile().getParent() + File.separatorChar;;
			String targetPath = f.getAbsolutePath();
			if(targetPath.startsWith(defaultPath)) {
				targetPath = f.getAbsolutePath().substring(defaultPath.length());
			}
			boolean unique = true;
			for (String path : bmsroot.getItems()) {
				if (path.equals(targetPath) || targetPath.startsWith(path + File.separatorChar)) {
					unique = false;
					break;
				}
			}
			if (unique) {
				bmsroot.getItems().add(targetPath);
				main.loadBMSPath(targetPath);
			}
		}
	}

    @FXML
	public void onSongPathDragOver(DragEvent ev) {
		Dragboard db = ev.getDragboard();
		if (db.hasFiles()) {
			ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		}
		ev.consume();
	}

    @FXML
	public void songPathDragDropped(final DragEvent ev) {
		Dragboard db = ev.getDragboard();
		if (db.hasFiles()) {
			for (File f : db.getFiles()) {
				if (f.isDirectory()) {
					final String defaultPath = new File(".").getAbsoluteFile().getParent() + File.separatorChar;;
					String targetPath = f.getAbsolutePath();
					if(targetPath.startsWith(defaultPath)) {
						targetPath = f.getAbsolutePath().substring(defaultPath.length());
					}
					boolean unique = true;
					for (String path : bmsroot.getItems()) {
						if (path.equals(targetPath) || targetPath.startsWith(path + File.separatorChar)) {
							unique = false;
							break;
						}
					}
					if (unique) {
						bmsroot.getItems().add(targetPath);
						main.loadBMSPath(targetPath);
					}
				}
			}
		}
	}

    @FXML
	public void removeSongPath() {
		bmsroot.getItems().removeAll(bmsroot.getSelectionModel().getSelectedItems());
	}

    @FXML
	public void addTableURL() {
		String s = url.getText();
		if (s.startsWith("http") && !tableurl.getItems().contains(s)) {
			tableurl.addItem(new TableInfo(url.getText()));
		}
	}

    @FXML
	public void removeTableURL() {
		tableurl.removeSelectedItems();
	}

	public void moveTableURLUp() {
		tableurl.moveSelectedItemsUp();
	}

	public void moveTableURLDown() {
		tableurl.moveSelectedItemsDown();
	}

 	private static class TableInfo {
        public Map<String, Pair<String, String>> tableNameComment = Map.ofEntries(
				// Java string literals are UTF-16 by default, my apologies for escape code spaghetti
				//
				// An entry is defined by (TableURL --- > [ TableName, TableDescription ])
				// sl,st,stardust,starlight
                Map.entry("https://mqppppp.neocities.org/StardustTable.html", new Pair<String, String>("Stardust", "Beginner \u26061-\u26067")),
				Map.entry("https://djkuroakari.github.io/starlighttable.html", new Pair<String, String>("Stardust", "Intermediate \u26067-\u260612")),
				Map.entry("https://stellabms.xyz/sl/table.html", new Pair<String, String>("Satellite", "Insane \u260611-\u260519")),
				Map.entry("https://stellabms.xyz/st/table.html", new Pair<String, String>("Stella", "High Insane to Overjoy \u260519-\u2605\u26057")),
				// the insanes
				Map.entry("https://darksabun.club/table/archive/normal1/", new Pair<String, String>("\u901a\u5e38\u96e3\u6613\u5ea6\u8868 (Normal 1)", "Beginner to Intermediate \u26061-\u260612")),
				Map.entry("https://darksabun.club/table/archive/insane1/", new Pair<String, String>("\u767a\u72c2BMS\u96e3\u6613\u5ea6\u8868 (Insane 1)", "Insane \u26051-\u260525")),
				Map.entry("http://rattoto10.jounin.jp/table.html", new Pair<String, String>("NEW GENERATION \u901a\u5e38 (Normal 2)", "Post 2016 Normal Table \u26061-\u260612")),
				Map.entry("http://rattoto10.jounin.jp/table_insane.html", new Pair<String, String>("NEW GENERATION \u767a\u72c2 (Insane 2)", "Post 2016 Insane Table \u26051-\u260525")),
				// overjoy
				Map.entry("https://rattoto10.jounin.jp/table_overjoy.html", new Pair<String, String>("NEW GENERATION overjoy", "New overjoy. \u2605\u26050-\u2605\u26057")),
				// stream + chordjack
				Map.entry("https://lets-go-time-hell.github.io/code-stream-table/", new Pair<String, String>("16分乱 (16th streams)", "Chordstream focus. Satellite difficulty")),
				Map.entry("https://lets-go-time-hell.github.io/Arm-Shougakkou-table/", new Pair<String, String>("ウーデオシ小学校 (Ude table)", "Chordjack/wide chords focus. Satellite difficulty")),
				Map.entry("https://su565fx.web.fc2.com/Gachimijoy/gachimijoy.html", new Pair<String, String>("gachimijoy", "Hard chordjack. \u2605\u26050-\u2605\u26057")),
				// stellaverse quirked up
				Map.entry("https://stellabms.xyz/so/table.html", new Pair<String, String>("Solar", "Gimmick chart focus. Satellite difficulty")),
				Map.entry("https://stellabms.xyz/sn/table.html", new Pair<String, String>("Supernova", "Gimmick chart focus. Stella difficulty")),
				// osu
				Map.entry("https://air-afother.github.io/osu-table/", new Pair<String, String>("osu!", "Table for osu! star rating")),
				// AI
				Map.entry("https://bms.hexlataia.xyz/tables/ai.html", new Pair<String, String>("Hex's AI", "Algorithmically assigned difficulty. Insane and LN range")),
				// Library
				Map.entry("https://bms.hexlataia.xyz/tables/db.html", new Pair<String, String>("発狂難易度データベース (Hex's DB)", "Manually assigned difficulty. Insane \u26050-\u260525+")),
				Map.entry("https://bms.hexlataia.xyz/tables/olduploader.html", new Pair<String, String>("旧アプロダ表 (Hex's Old uploader)", "Manually assigned difficulty. Mostly Insane \u260610-\u260525+ with LN + Scratch ratings")),
				Map.entry("https://stellabms.xyz/upload.html", new Pair<String, String>("Stella Uploader", "Stellaverse uploader. Insane \u26051-\u260525+")),
				Map.entry("https://exturbow.github.io/github.io/index.html", new Pair<String, String>("BMS図書館 (Turbow's Toshokan)", "Rates BMS event submissions. Wide difficulty \u26061-\u260525+")),
				//Map.entry("http://upl.konjiki.jp/", new Pair<String, String>("差分アップローダー (Black train uploader)", "Manually assigned difficulty. Insane \u26050-\u260525+")),
				// beginner
				Map.entry("http://fezikedifficulty.futene.net/list.html", new Pair<String, String>("池田的 (Ikeda's Beginner)", "Beginner focused table. 19 levels \u26061-\u260611+")),
				// LN
				Map.entry("https://ladymade-star.github.io/luminous/table.html", new Pair<String, String>("Luminous", "Newer LN table. ◆1-◆26")),
				Map.entry("https://vinylhouse.web.fc2.com/lntougou/difficulty.html", new Pair<String, String>("Longnote統合表", "◆1-◆27")),
				Map.entry("http://flowermaster.web.fc2.com/lrnanido/gla/LN.html", new Pair<String, String>("LN難易度", "◆1-◆26")),
				Map.entry("https://skar-wem.github.io/ln/", new Pair<String, String>("LN Curtain", "Full/inverse LN charts. ◆1-◆25")),
				Map.entry("http://cerqant.web.fc2.com/zindy/table.html", new Pair<String, String>("zindy LN", "Difficult shield stair patterns. Hard LN ◆15-◆25")),
				Map.entry("https://notepara.com/glassist/lnoj", new Pair<String, String>("LNoverjoy", "Hard LN table. ◆15-◆25")),
				// Scratch
				Map.entry("https://egret9.github.io/Scramble/", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("http://minddnim.web.fc2.com/sara/3rd_hard/bms_sara_3rd_hard.html", new Pair<String, String>("皿難易度表(3rd) (Sara 3rd)", "Scratch table")),
				// delay
				Map.entry("https://lets-go-time-hell.github.io/Delay-joy-table/", new Pair<String, String>("ディレイjoy (delayjoy)", "Delay focus. Wide difficulty with heavy stella bias")),
				Map.entry("https://kamikaze12345.github.io/github.io/delaytrainingtable/table.html", new Pair<String, String>("DELAY Training Table", "Very wide comprehensive delay table. \u26051-\u2605\u26057")),
				Map.entry("https://wrench616.github.io/Delay/", new Pair<String, String>("Delay小学校", "Intermediate delay table. \u26051-\u260524")),
				// High Diff
				Map.entry("https://darksabun.club/table/archive/old-overjoy/", new Pair<String, String>("Overjoy (旧) (Old overjoy)", "Newer scratch table")),
				Map.entry("https://monibms.github.io/Dystopia/dystopia.html", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("https://www.firiex.com/tables/joverjoy", new Pair<String, String>("Scramble", "Newer scratch table")),
				// Hard Judge
				Map.entry("https://plyfrm.github.io/table/timing/", new Pair<String, String>("Timing Table (VHard Table)", "Newer scratch table")),
				// Artst search
				Map.entry("https://plyfrm.github.io/table/bmssearch/index.html", new Pair<String, String>("BMSSearch Artists", "Newer scratch table")),
				// DP
				Map.entry("https://stellabms.xyz/dp/table.html", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("https://stellabms.xyz/dpst/table.html", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("https://deltabms.yaruki0.net/table/data/dpdelta_head.json", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("https://deltabms.yaruki0.net/table/data/insane_head.json", new Pair<String, String>("Scramble", "Newer scratch table")),
				Map.entry("http://ereter.net/dpoverjoy/", new Pair<String, String>("Scramble", "Newer scratch table"))
        );

		public StringProperty url;
		public void setUrl(String value) { urlProperty().set(value); }
		public String getUrl() { return urlProperty().get(); }
		public StringProperty urlProperty() { 
			if (url == null) url = new SimpleStringProperty(this, "url");
			return url; 
		}
		public StringProperty nameStatus;
		public void setNameStatus(String value) { nameStatusProperty().set(value); }
		public String getNameStatus() { return nameStatusProperty().get(); }
		public StringProperty nameStatusProperty() { 
			if (nameStatus == null) nameStatus = new SimpleStringProperty(this, "nameStatus");
			return nameStatus; 
		}

		public StringProperty comment;
		public void setComment(String value) { commentProperty().set(value); }
		public String commentStatus() { return commentProperty().get(); }
		public StringProperty commentProperty() {
			if (comment == null) comment = new SimpleStringProperty(this, "comment");
			return comment;
		}

		public TableInfo(String url) {
			setUrl(url);
			Pair<String, String> nameComment = tableNameComment.get(url);
			if (nameComment == null) {
				setNameStatus("");
				setComment("");
			} else {
				setNameStatus(nameComment.getKey());
				setComment(nameComment.getValue());
			}
		}

		public static String[] toUrlArray(List<TableInfo> list) {
			String[] urls = new String[list.size()];
			int i = 0;
			for (TableInfo tableInfo : list) {
				urls[i++] = tableInfo.getUrl();
			}
			return urls;
		}

		public static void populateList(List<TableInfo> list, String[] urls) {
			list.clear();
			for (String url : urls) {
				list.add(new TableInfo(url));
			}
		}
	}
}
