package bms.player.beatoraja.launcher;

import bms.player.beatoraja.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DiscordConfigurationView implements Initializable {
	@FXML
	public CheckBox discordRichPresence;
	@FXML
	private ComboBox<String> webhookOption;
	@FXML
	private TextField webhookName;
	@FXML
	private TextField webhookAvatar;
	@FXML
	private TextField url;
	@FXML
	private EditableTableView<WebhookInfo> webhookURL;

	private Config config;

	private PlayConfigurationView main;

	public void initialize(URL location, ResourceBundle resources) {

	}

	public void init(PlayConfigurationView main) {
		this.main = main;

		TableColumn<WebhookInfo, String> urlColumn = new TableColumn("Discord WebHook URL");
		urlColumn.setCellValueFactory(p -> p.getValue().urlProperty());
		urlColumn.setSortable(false);
		urlColumn.setMinWidth(510);
		urlColumn.setMinWidth(0);

		webhookURL.getColumns().setAll(urlColumn);
		webhookURL.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	public void update(Config config) {
		this.config = config;
		discordRichPresence.setSelected(config.isUseDiscordRPC());
		webhookName.setText(config.getWebhookName());
		webhookAvatar.setText(config.getWebhookAvatar());
		webhookOption.getSelectionModel().select(config.getWebhookOption());
		WebhookInfo.populateList(webhookURL.getItems(), config.getWebhookUrl());
	}

	public void commit() {
		config.setUseDiscordRPC(discordRichPresence.isSelected());
		config.setWebhookOption(webhookOption.getSelectionModel().getSelectedIndex());
		config.setWebhookName(webhookName.getText());
		config.setWebhookAvatar(webhookAvatar.getText());
		config.setWebhookUrl(WebhookInfo.toURLArray(webhookURL.getItems()));
	}

	@FXML
	public void addWebhookURL() {
		String s = url.getText();
		boolean find = webhookURL.getItems().stream().anyMatch(url -> url.getUrl().equals(s));
		if (!find) {
			webhookURL.addItem(new WebhookInfo(url.getText()));
		}
	}

	@FXML
	public void removeWebhookURL() {
		webhookURL.removeSelectedItems();
	}

	@FXML
	public void moveWebhookURLUp() {
		webhookURL.moveSelectedItemsUp();
	}

	@FXML
	public void moveWebhookURLDown() {
		webhookURL.moveSelectedItemsDown();
	}

	private static class WebhookInfo {
		public StringProperty url;

		public WebhookInfo(String url) {
			setUrl(url);
		}

		public String getUrl() {
			return urlProperty().get();
		}

		public void setUrl(String url) {
			urlProperty().set(url);
		}

		public StringProperty urlProperty() {
			if (url == null) {
				url = new SimpleStringProperty(this, "url");
			}
			return url;
		}

		public static String[] toURLArray(List<WebhookInfo> webhooks) {
			return webhooks.stream().map(WebhookInfo::getUrl).toArray(String[]::new);
		}

		public static void populateList(List<WebhookInfo> webhooks, String[] urls) {
			webhooks.clear();
			for (String url : urls) {
				webhooks.add(new WebhookInfo(url));
			}
		}
	}
}
