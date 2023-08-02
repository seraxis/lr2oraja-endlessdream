package bms.player.beatoraja.launcher;


import java.util.Arrays;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.launcher.PlayConfigurationView.OptionListCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class TrainerView {
	
	@FXML
	private CheckBox traineractive;
	@FXML
	private Button setbutton;
	@FXML
	private TextField laneorder;

/* 	@FXML
	private ListView<String> history; */
	
	
	private PlayerConfig player;



    public void update(PlayerConfig player) {
        this.player = player;
        if(this.player == null) {
            return;
        }
		player.setTrainerActive(false);
        traineractive.setSelected(player.getTrainerActive());
		laneorder.setPromptText("1234567");
		if (this.player.getLaneOrder() != null) {
			laneorder.setText(this.player.getLaneOrder());
		} else {
		    this.player.setLaneOrder("1234567");
		}
    }

    @FXML
	public void setActive() {
		player.setTrainerActive(traineractive.isSelected());
	}

/*     @FXML
	public void fromHistory () {

	} */

	@FXML
	public void setRandom() {
		if (this.laneorder == null) {
			Logger.getGlobal().warning("RandomTrainer: Lane field empty");
			return;
		}

		int[] lanes = this.laneorder.getCharacters().codePoints().map(Character::getNumericValue).map(c -> c-1).toArray();

        int[] has_all = new int[]{0,1,2,3,4,5,6};
		Arrays.sort(has_all);
		int[] l = lanes.clone();
		Arrays.sort(l);

		if (l.length != 7) {
			Logger.getGlobal().warning("RandomTrainer: Incorrect number of lanes specified");
			return;
		}

		for (int i = 0; i < has_all.length; i++) {
			if (l[i] != has_all[i]) {
				Logger.getGlobal().warning("RandomTrainer: Lanes in incorrect format, falling back to nonran or last ran used");
				return;
			}
		}

		player.setLaneOrder(this.laneorder.getCharacters().toString());
	}
}
