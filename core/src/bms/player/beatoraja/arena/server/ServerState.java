package bms.player.beatoraja.arena.server;

import bms.player.beatoraja.arena.network.Address;
import bms.player.beatoraja.arena.network.Peer;
import bms.player.beatoraja.arena.network.PeerList;
import bms.player.beatoraja.arena.network.Score;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerState {
    private Address host;
    private int currentRandomSeed;
    // private ItemSettings itemSettings;
    // private boolean itemModeEnabled;
    private Map<Address, Peer> peers = new HashMap<>();

    public void resetEveryone() {
        peers.forEach((k, v) -> v.reset());
    }

    public Address getHost() {
        return host;
    }

    public void setHost(Address host) {
        this.host = host;
    }

    public int getCurrentRandomSeed() {
        return currentRandomSeed;
    }

    public void setCurrentRandomSeed(int currentRandomSeed) {
        this.currentRandomSeed = currentRandomSeed;
    }

    public Map<Address, Peer> getPeers() {
        return peers;
    }

    public void setPeers(Map<Address, Peer> peers) {
        this.peers = peers;
    }

    public Optional<Peer> getPeer(Address address) {
        return Optional.of(this.peers.get(address));
    }

    public PeerList getPeerList() {
        return new PeerList(this.peers, this.host);
    }
}
