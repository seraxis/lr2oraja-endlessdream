package bms.player.beatoraja.obs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.modmenu.ImGuiNotify;

public class ObsWsClient {
	private static final Logger logger = LoggerFactory.getLogger(ObsWsClient.class);
	private volatile WebSocketClient wsClient;
	private volatile boolean isConnected = false;
	private volatile boolean isIdentified = false;
	private volatile boolean isRecording = false;
	private volatile boolean restartRecording = false;
	private volatile boolean autoReconnect = true;
	private volatile boolean isReconnecting = false;
	private volatile boolean isShuttingDown = false;
	private volatile boolean saveRequested = false;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String password;
	private final AtomicLong requestIdCounter = new AtomicLong(0);
	private final URI serverUri;

	private Runnable onCloseHandler;
	private Consumer<Exception> onErrorHandler;
	private Consumer<ObsVersionInfo> onVersionReceived;
	private Consumer<List<String>> onScenesReceived;
	private Consumer<String> onRecordStateChanged;
	private Consumer<String> customMessageHandler;

	public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> reconnectTask;

	private static final int INITIAL_RECONNECT_DELAY_MS = 2000;
	private static final int MAX_RECONNECT_DELAY_MS = 15000;
	private static final double RECONNECT_BACKOFF_MULTIPLIER = 1.25;
	private int currentReconnectDelay = INITIAL_RECONNECT_DELAY_MS;

	public static enum ObsRecordingMode {
		KEEP_ALL(0),
		ON_SCREENSHOT(1),
		ON_REPLAY(2);

		private int value;

		ObsRecordingMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ObsRecordingMode fromValue(int value) {
			for (ObsRecordingMode mode : ObsRecordingMode.values()) {
				if (mode.getValue() == value) {
					return mode;
				}
			}
			throw new IllegalArgumentException("No matching enum for value: " + value);
		}
	}

	private ObsRecordingMode recordingMode = ObsRecordingMode.KEEP_ALL;
	private String outputPath = "";
	private String lastOutputPath = "";

	public static final Map<String, String> OBS_ACTIONS = Map.of(
			"Stop Recording", "StopRecord",
			"Start Recording", "StartRecord");

	public ObsWsClient(Config config) throws URISyntaxException {
		this.serverUri = new URI("ws://" + config.getObsWsHost() + ":" + config.getObsWsPort());
		this.password = config.getObsWsPass();
		this.recordingMode = ObsRecordingMode.fromValue(config.getObsWsRecMode());
		createWebSocketClient();
	}

	private void createWebSocketClient() {
		wsClient = new WebSocketClient(serverUri) {
			@Override
			public void onOpen(ServerHandshake handshake) {
				isConnected = true;
				isReconnecting = false;
				currentReconnectDelay = INITIAL_RECONNECT_DELAY_MS;

				if (reconnectTask != null && !reconnectTask.isDone()) {
					reconnectTask.cancel(false);
					reconnectTask = null;
				}
			}

			@Override
			public void onMessage(String message) {
				try {
					JsonNode json = objectMapper.readTree(message);
					if (!json.has("op")) {
						logger.warn("Received malformed JSON (no op): {}", message);
						return;
					}

					int op = json.get("op").asInt();

					switch (op) {
						case 0: // Hello
							handleHello(json);
							break;
						case 2: // Identified
							isIdentified = true;
							sendRequest("GetVersion");
							sendRequest("GetSceneList");
							sendRequest("GetRecordStatus");
							break;
						case 5: // Event
							handleEvent(json);
							break;
						case 7: // RequestResponse
							handleRequestResponse(json);
							break;
					}
				} catch (Exception e) {
					logger.warn("Error processing message: {}", e.getMessage());
				}

				if (customMessageHandler != null) {
					customMessageHandler.accept(message);
				}
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				boolean wasConnected = isConnected;
				isConnected = false;
				isIdentified = false;

				if (onCloseHandler != null) {
					onCloseHandler.run();
				}

				if (autoReconnect && wasConnected && !isReconnecting && !isShuttingDown) {
					scheduleReconnect();
				}
			}

			@Override
			public void onError(Exception ex) {
				if (ex != null && ex.getMessage() != null && !ex.getMessage().isEmpty()) {
					logger.warn("OBS WebSocket error: {}", ex.getMessage());
				}

				if (onErrorHandler != null) {
					onErrorHandler.accept(ex);
				}
			}
		};
	}

	private void handleEvent(JsonNode json) {
		try {
			JsonNode d = json.get("d");
			if (d == null || !d.has("eventType") || !d.has("eventData")) {
				return;
			}
			String eventType = d.get("eventType").asText();
			JsonNode eventData = d.get("eventData");

			switch (eventType) {
				case "ExitStarted":
				logger.info("OBS is shutting down");
					close();
					break;
				case "AuthenticationFailure":
				case "AuthenticationFailed":
					logger.warn("OBS authentication failed!");
					autoReconnect = false;
					close();
					break;
				case "RecordStateChanged":
					if (eventData.has("outputState")) {
						String outputState = eventData.get("outputState").asText();
						outputPath = eventData.has("outputPath") ? eventData.get("outputPath").asText() : "";
						String notifyMessage = "";

						switch (outputState) {
							case "OBS_WEBSOCKET_OUTPUT_STOPPED":
								isRecording = false;
								notifyMessage = "Recording stopped";
								synchronized (this) {
									if (restartRecording) {
										restartRecording = false;
										if (recordingMode != ObsRecordingMode.KEEP_ALL) {
											final String pathToDelete = outputPath;
											scheduler.execute(() -> {
												File file = new File(pathToDelete);
												if (file.exists() && file.isFile()) {
													file.delete();
												}
											});
										}
										scheduler.schedule(this::requestStartRecord, 500, TimeUnit.MILLISECONDS);
									}
								}
								lastOutputPath = outputPath;
								break;
							case "OBS_WEBSOCKET_OUTPUT_STARTED":
								isRecording = true;
								notifyMessage = "Recording started";
								if (recordingMode != ObsRecordingMode.KEEP_ALL) {
									if (saveRequested) {
										saveRequested = false;
										notifyMessage += ", last recording saved";
									} else {
										if (lastOutputPath != null && !lastOutputPath.isBlank()) {
											final String pathToDelete = lastOutputPath;
											scheduler.execute(() -> {
												File file = new File(pathToDelete);
												if (file.exists() && file.isFile()) {
													file.delete();
												}
											});
											notifyMessage += ", last recording deleted";
										}
									}
								}
								break;
						}

						if (notifyMessage.length() > 0) {
							ImGuiNotify.info(String.format("OBS: %s.", notifyMessage));
						}

						if (onRecordStateChanged != null) {
							onRecordStateChanged.accept(outputState);
						}
					}
					break;
			}
		} catch (Exception e) {
			logger.warn("Error handling event: {}", e.getMessage());
		}
	}

	private void handleHello(JsonNode json) {
		try {
			JsonNode d = json.get("d");
			boolean authRequired = d.has("authentication");

			ObjectNode identifyData = objectMapper.createObjectNode();
			identifyData.put("rpcVersion", 1);

			if (authRequired) {
				if (password == null || password.isEmpty()) {
					logger.warn("Authentication required but no password provided");
					autoReconnect = false;
					close();
					return;
				}

				JsonNode auth = d.get("authentication");
				String challenge = auth.get("challenge").asText();
				String salt = auth.get("salt").asText();

				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] secretHash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
				String secret = Base64.getEncoder().encodeToString(secretHash);

				digest.reset();
				byte[] authHash = digest.digest((secret + challenge).getBytes(StandardCharsets.UTF_8));
				identifyData.put("authentication", Base64.getEncoder().encodeToString(authHash));
			}

			ObjectNode identify = objectMapper.createObjectNode();
			identify.put("op", 1);
			identify.set("d", identifyData);

			send(objectMapper.writeValueAsString(identify));
		} catch (Exception e) {
			logger.warn("Error sending Identify: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleRequestResponse(JsonNode json) {
		try {
			JsonNode d = json.get("d");
			if (d == null || !d.has("responseData") || !d.has("requestType")) {
				return;
			}

			JsonNode responseData = d.get("responseData");
			String requestType = d.get("requestType").asText();

			switch (requestType) {
				case "GetVersion":
					if (responseData.has("obsVersion") && responseData.has("obsWebSocketVersion")) {
						String obsVersion = responseData.get("obsVersion").asText();
						String wsVersion = responseData.get("obsWebSocketVersion").asText();

						if (onVersionReceived != null) {
							onVersionReceived.accept(new ObsVersionInfo(obsVersion, wsVersion));
						}
					}
					break;
				case "GetSceneList":
					JsonNode scenesNode = responseData.path("scenes");
					List<String> sceneNames = new ArrayList<>();

					if (scenesNode.isArray()) {
						for (JsonNode sceneNode : scenesNode) {
							if (sceneNode.has("sceneName")) {
								sceneNames.add(sceneNode.get("sceneName").asText());
							}
						}
					}

					Collections.reverse(sceneNames);

					if (onScenesReceived != null) {
						onScenesReceived.accept(sceneNames);
					}
					break;
				case "GetRecordStatus":
					if (responseData.has("outputActive")) {
						isRecording = responseData.get("outputActive").asBoolean();
					}
					break;
			}
		} catch (Exception e) {
			logger.warn("Error handling request response: {}", e.getMessage());
		}
	}

	private void scheduleReconnect() {
		if (isReconnecting || !autoReconnect || isShuttingDown) {
			return;
		}

		isReconnecting = true;

		reconnectTask = scheduler.schedule(() -> {
			try {
				if (wsClient != null && !wsClient.isClosed()) {
					wsClient.close();
				}

				createWebSocketClient();

				if (!wsClient.connectBlocking(5, TimeUnit.SECONDS)) {
					throw new Exception("Connection timeout");
				}

			} catch (Exception e) {
				// Reconnect delay will be reset once succesfully connected
				currentReconnectDelay = Math.min(
						(int) (currentReconnectDelay * RECONNECT_BACKOFF_MULTIPLIER),
						MAX_RECONNECT_DELAY_MS);

				if (autoReconnect && !isShuttingDown) {
					isReconnecting = false;
					scheduleReconnect();
				}
			}
		}, currentReconnectDelay, TimeUnit.MILLISECONDS);
	}

	// Public methods

	public void connect() throws InterruptedException {
		try {
			if (!wsClient.connectBlocking(5, TimeUnit.SECONDS)) {
				throw new InterruptedException("Connection timeout");
			}
		} catch (Exception e) {
			if (autoReconnect) {
				logger.warn("Initial connection failed: {}", e.getMessage());
				scheduleReconnect();
			} else {
				throw e;
			}
		}
	}

	public void connectAsync() {
		scheduler.execute(() -> {
			try {
				connect();
			} catch (Exception e) {
			}
		});
	}

	private boolean canSendRequest() {
		return isConnected && isIdentified && !isReconnecting;
	}

	public void requestStartRecord() {
		if (!canSendRequest() || isRecording) {
			return;
		}
		sendRequest("StartRecord");
	}

	public void requestStopRecord() {
		if (!canSendRequest() || !isRecording) {
			return;
		}
		sendRequest("StopRecord");
	}

	public void saveLastRecording(String reason) {
		if (!this.isConnected && !canSendRequest()) {
			return;
		}

		final ObsRecordingMode reasonMode = ObsRecordingMode.valueOf(reason);
		if (this.saveRequested || recordingMode == ObsRecordingMode.KEEP_ALL || reasonMode != recordingMode) {
			return;
		}

		this.saveRequested = true;
		ImGuiNotify.info("OBS: Recording will be kept.");
	}

	public void setScene(String sceneName) {
		if (!canSendRequest()) {
			return;
		}
		try {
			ObjectNode requestData = objectMapper.createObjectNode();
			requestData.put("sceneName", sceneName);

			ObjectNode d = objectMapper.createObjectNode();
			d.put("requestType", "SetCurrentProgramScene");
			d.put("requestId", "set-scene-" + requestIdCounter.incrementAndGet());
			d.set("requestData", requestData);

			ObjectNode request = objectMapper.createObjectNode();
			request.put("op", 6);
			request.set("d", d);

			send(objectMapper.writeValueAsString(request));
		} catch (Exception e) {
			logger.warn("Error setting scene: {}", e.getMessage());
		}
	}

	public void sendRequest(String requestType) {
		if (!canSendRequest()) {
			return;
		}
		try {
			ObjectNode d = objectMapper.createObjectNode();
			d.put("requestType", requestType);
			d.put("requestId", requestType.toLowerCase() + "-" + requestIdCounter.incrementAndGet());

			ObjectNode request = objectMapper.createObjectNode();
			request.put("op", 6);
			request.set("d", d);

			send(objectMapper.writeValueAsString(request));
		} catch (Exception e) {
			logger.warn("Error sending request: {}", e.getMessage());
		}
	}

	private void send(String message) {
		if (wsClient != null && wsClient.isOpen()) {
			wsClient.send(message);
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isIdentified() {
		return isIdentified;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public synchronized void restartRecording() {
		if (!canSendRequest() || restartRecording) {
			return;
		}
		if (!isRecording) {
			requestStartRecord();
			return;
		}
		restartRecording = true;
		requestStopRecord();
	}

	public void setAutoReconnect(boolean enabled) {
		this.autoReconnect = enabled;
	}

	public void close() {
		isShuttingDown = true;
		autoReconnect = false;

		if (reconnectTask != null && !reconnectTask.isDone()) {
			reconnectTask.cancel(false);
			reconnectTask = null;
		}

		try {
			scheduler.shutdown();
			if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}

		if (wsClient != null && !wsClient.isClosed()) {
			wsClient.close();
		}
	}

	public static String getActionLabel(String action) {
		for (Map.Entry<String, String> entry : OBS_ACTIONS.entrySet()) {
			if (entry.getValue().equals(action)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void setOnClose(Runnable handler) {
		this.onCloseHandler = handler;
	}

	public void setOnError(Consumer<Exception> handler) {
		this.onErrorHandler = handler;
	}

	public void setOnVersionReceived(Consumer<ObsVersionInfo> handler) {
		this.onVersionReceived = handler;
	}

	public void setOnScenesReceived(Consumer<List<String>> handler) {
		this.onScenesReceived = handler;
	}

	public void setCustomMessageHandler(Consumer<String> handler) {
		this.customMessageHandler = handler;
	}

	public static class ObsVersionInfo {
		private final String obsVersion;
		private final String wsVersion;

		public ObsVersionInfo(String obsVersion, String wsVersion) {
			this.obsVersion = obsVersion;
			this.wsVersion = wsVersion;
		}

		public String getObsVersion() {
			return obsVersion;
		}

		public String getWsVersion() {
			return wsVersion;
		}

		@Override
		public String toString() {
			return "OBS v" + obsVersion + " (WS v" + wsVersion + ")";
		}
	}
}
