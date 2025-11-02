package bms.player.beatoraja.external.DiscordRPC;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RichPresence {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    private final String clientId;
    private final IPCConnection connection;
    private boolean connected = false;
    
    public RichPresence(String clientId) {
        this.clientId = clientId;
        this.connection = createConnection();
    }
    
    private static IPCConnection createConnection() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("windows") ? new WindowsIPCConnection() : new UnixIPCConnection();
    }
    
    public void connect() throws IOException {
        connection.connect();
        handshake();
        connected = true;
    }
    
    private void handshake() throws IOException {
        Map<String, Object> handshake = new HashMap<>();
        handshake.put("v", 1);
        handshake.put("client_id", clientId);
        
        sendPacket(0, MAPPER.writeValueAsString(handshake));
        byte[] response = receivePacket();
        // System.out.println("Handshake: " + new String(response, StandardCharsets.UTF_8));
    }
    
    private void sendPacket(int opCode, String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(8 + payloadBytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(opCode);
        buffer.putInt(payloadBytes.length);
        buffer.put(payloadBytes);
        buffer.flip();
        connection.write(buffer);
    }
    
    private byte[] receivePacket() throws IOException {
        ByteBuffer header = connection.read(8);
        header.order(ByteOrder.LITTLE_ENDIAN);
        int opCode = header.getInt();
        int length = header.getInt();
        
        ByteBuffer payload = connection.read(length);
        // System.out.println("OP Code: " + opCode + "; Length: " + length);
        return payload.array();
    }
    
    public void update(RichPresenceData data) throws IOException {
        if (!connected) throw new IllegalStateException("Not connected to Discord");
        
        ActivityPayload payload = new ActivityPayload();
        payload.cmd = "SET_ACTIVITY";
        payload.nonce = UUID.randomUUID().toString();
        payload.args = new ActivityArgs();
        payload.args.pid = ProcessHandle.current().pid();
        payload.args.activity = data;
        
        sendPacket(1, MAPPER.writeValueAsString(payload));
        byte[] response = receivePacket();
        // System.out.println("Update: " + new String(response, StandardCharsets.UTF_8));
    }
    
    public void close() {
        connection.close();
        connected = false;
    }
    
    // Data classes for Jackson serialization
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RichPresenceData {
        @JsonProperty("state") public String state;
        @JsonProperty("details") public String details;
        @JsonProperty("timestamps") public Timestamps timestamps;
        @JsonProperty("assets") public Assets assets;
        @JsonProperty("party") public Party party;
        @JsonProperty("secrets") public Secrets secrets;
        @JsonProperty("instance") public Boolean instance = true;
        
        public RichPresenceData setState(String state) {
            this.state = state;
            return this;
        }
        
        public RichPresenceData setDetails(String details) {
            this.details = details;
            return this;
        }
        
        public RichPresenceData setStartTimestamp(long start) {
            if (timestamps == null) timestamps = new Timestamps();
            timestamps.start = start;
            return this;
        }
        
        public RichPresenceData setEndTimestamp(long end) {
            if (timestamps == null) timestamps = new Timestamps();
            timestamps.end = end;
            return this;
        }
        
        public RichPresenceData setLargeImage(String key, String text) {
            if (assets == null) assets = new Assets();
            assets.largeImage = key;
            assets.largeText = text;
            return this;
        }
        
        public RichPresenceData setSmallImage(String key, String text) {
            if (assets == null) assets = new Assets();
            assets.smallImage = key;
            assets.smallText = text;
            return this;
        }
        
        public RichPresenceData setParty(String id, int size, int max) {
            if (party == null) party = new Party();
            party.id = id;
            party.size = new int[]{size, max};
            return this;
        }
        
        public RichPresenceData setSecrets(String match, String join, String spectate) {
            if (secrets == null) secrets = new Secrets();
            secrets.match = match;
            secrets.join = join;
            secrets.spectate = spectate;
            return this;
        }
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Timestamps {
        @JsonProperty("start") public Long start;
        @JsonProperty("end") public Long end;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Assets {
        @JsonProperty("large_image") public String largeImage;
        @JsonProperty("large_text") public String largeText;
        @JsonProperty("small_image") public String smallImage;
        @JsonProperty("small_text") public String smallText;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Party {
        @JsonProperty("id") public String id;
        @JsonProperty("size") public int[] size;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Secrets {
        @JsonProperty("match") public String match;
        @JsonProperty("join") public String join;
        @JsonProperty("spectate") public String spectate;
    }
    
    private static class ActivityPayload {
        @JsonProperty("cmd") public String cmd;
        @JsonProperty("args") public ActivityArgs args;
        @JsonProperty("nonce") public String nonce;
    }
    
    private static class ActivityArgs {
        @JsonProperty("pid") public long pid;
        @JsonProperty("activity") public RichPresenceData activity;
    }
}
