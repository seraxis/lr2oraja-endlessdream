package bms.player.beatoraja.external;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.MainStateListener;
import bms.player.beatoraja.PlayerResource;
import bms.player.beatoraja.decide.MusicDecide;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.external.DiscordRPC.RichPresence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordListener implements MainStateListener {
	private static final Logger logger = LoggerFactory.getLogger(DiscordListener.class);

	private static final String APPLICATION_ID = "1054234988167561277";

	private RichPresence richPresence;
	
	public DiscordListener() {
		try {
			richPresence = new RichPresence(APPLICATION_ID);
			richPresence.connect();
			logger.info("Discord RPC Ready!");
		} catch (Exception e) {
			richPresence = null;
			logger.warn("Failed to initialize Discord RPC: {}", e.getMessage());
		}
	}
	
	@Override
	public void update(MainState state, int status) {
		if (richPresence == null) return;
		
		try {
			RichPresence.RichPresenceData data = new RichPresence.RichPresenceData();
			data.setStartTimestamp(System.currentTimeMillis() / 1000)
				.setLargeImage("bms", null);
			
			if (state instanceof MusicSelector) {
				data.setState("In Music Select Menu");
			} else if (state instanceof MusicDecide) {
				data.setState("Decide Screen");
			} else if (state instanceof BMSPlayer) {
				final PlayerResource resource = state.main.getPlayerResource();
				data.setDetails(resource.getSongdata().getFullTitle() + " / " + resource.getSongdata().getArtist());
				data.setState("Playing: " + resource.getSongdata().getMode() + "Keys");
			} else if (state instanceof MusicResult) {
				data.setState("Result Screen");
			} else if (state instanceof CourseResult) {
				data.setState("Course Result Screen");
			}
			
			richPresence.update(data);
		} catch (Exception e) {
			logger.warn("Failed to update Discord Rich Presence: {}", e.getMessage());
		}
	}
	
	public void close() {
		if (richPresence != null) {
			richPresence.close();
		}
	}
}
