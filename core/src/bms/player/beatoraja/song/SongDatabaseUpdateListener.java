package bms.player.beatoraja.song;

import javafx.beans.property.IntegerProperty;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listen current songdata.db's update progress
 */
public class SongDatabaseUpdateListener {
	private final AtomicInteger bmsFiles = new AtomicInteger(0);
	private final AtomicInteger processedBMSFiles = new AtomicInteger(0);
	private final AtomicInteger newBMSFiles = new AtomicInteger(0);

	public void addBMSFilesCount(int count) {
		bmsFiles.addAndGet(count);
	}

	public void addProcessedBMSFilesCount(int count) {
		processedBMSFiles.addAndGet(count);
	}

	public void addNewBMSFilesCount(int count) {
		newBMSFiles.addAndGet(count);
	}

	public int getBMSFilesCount() {
		return bmsFiles.get();
	}

	public int getProcessedBMSFilesCount() {
		return processedBMSFiles.get();
	}

	public int getNewBMSFilesCount() {
		return newBMSFiles.get();
	}
}
