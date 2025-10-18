package bms.player.beatoraja.arena.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class Address implements Serializable {
    private String host = "";
    private int port = 0;

    public Address() {

    }

    public Address(Value value) {
        ArrayValue arr = value.asArrayValue();
        String host = arr.get(0).asStringValue().asString();
        int port = arr.get(1).asIntegerValue().asInt();
        this.host = host;
        this.port = port;
    }

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Address(InetSocketAddress inetSocketAddress) {
        this(inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort());
        //   \-> this prevents host name (e.g. localhost vs 127.0.0.1)
    }

    public byte[] pack() {
        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(2);
            packer.packString(host);
            packer.packInt(port);
            packer.close();
            return packer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof Address rhs)
                && rhs.host.equals(this.host)
                && rhs.port == this.port;
    }

    @Override
    public int hashCode() {
        return host.hashCode() ^ (port << 1);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
