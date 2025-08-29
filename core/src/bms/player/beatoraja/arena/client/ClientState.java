package bms.player.beatoraja.arena.client;

import io.github.catizard.jlr2arenaex.network.Address;
import io.github.catizard.jlr2arenaex.network.Peer;
import bms.player.beatoraja.song.SongData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientState {
    private Address remoteId;
    private Address host;
    private SelectedSong selectedSongRemote = new SelectedSong("", "", "", "");
    private Map<Address, Peer> peers = new HashMap<>();
    // Extra fields
    // For play/score related data reference
    private SongData currentSongData;
    // For auto-select once
    private Boolean autoSelectFlag = false;
    // Random seed
    private final AtomicInteger randomSeed = new AtomicInteger();

    public Map<Address, Peer> getPeers() {
        return peers;
    }

    public Address getHost() {
        return host;
    }

    public Address getRemoteId() {
        return remoteId;
    }

    public SelectedSong getSelectedSongRemote() {
        return selectedSongRemote;
    }

    public void setPeers(Map<Address, Peer> peers) {
        this.peers = peers;
    }

    public void setHost(Address host) {
        this.host = host;
    }

    public void setRemoteId(Address remoteId) {
        this.remoteId = remoteId;
    }

    public void setSelectedSongRemote(SelectedSong selectedSongRemote) {
        this.selectedSongRemote = selectedSongRemote;
    }

    public SongData getCurrentSongData() {
        return currentSongData;
    }

    public void setCurrentSongData(SongData currentSongData) {
        this.currentSongData = currentSongData;
    }

    public Boolean getAutoSelectFlag() {
        return autoSelectFlag;
    }

    public void setAutoSelectFlag(Boolean autoSelectFlag) {
        this.autoSelectFlag = autoSelectFlag;
    }

    public int getRandomSeed() {
        return randomSeed.get();
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed.set(randomSeed);
    }

    public Optional<Integer> getMaxScore() {
        if (currentSongData == null) {
            return Optional.empty();
        }
        return Optional.of(currentSongData.getNotes() * 2);
    }

    public Optional<Integer> getMaxCombo() {
        if (currentSongData == null) {
            return Optional.empty();
        }
        return Optional.of(currentSongData.getNotes());
    }
}
