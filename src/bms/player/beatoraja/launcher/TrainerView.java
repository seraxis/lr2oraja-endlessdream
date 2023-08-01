package bms.player.beatoraja.launcher;


import java.util.Arrays;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.launcher.PlayConfigurationView.OptionListCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
	
	
	private PlayerConfig player;


    public void update(PlayerConfig player) {
        this.player = player;
        if(this.player == null) {
            return;
        }
        traineractive.setSelected(player.getTrainerActive());
		laneorder.setPromptText("1234567");

		System.out.println("laneorder updated");
		this.player.setLaneOrder(new int[]{0, 1, 2, 3, 4, 5, 6});
    }

    @FXML
	public void setActive() {
		player.setTrainerActive(traineractive.isSelected());
	}

	@FXML
	public void setRandom() {
		if (this.laneorder == null) {
			System.out.println("RandomTrainer: Lane list null");
			return;
		}

		int[] lanes = this.laneorder.getCharacters().codePoints().map(Character::getNumericValue).map(c -> c-1).toArray();

        int[] has_all = new int[]{0,1,2,3,4,5,6};
		Arrays.sort(has_all);
		int[] l = lanes.clone();
		Arrays.sort(l);

		if (l.length != 7) {
			System.out.println("RandomTrainer: Incorrect number of lanes specified");
			return;
		}

		for (int i = 0; i < has_all.length; i++) {
			if (l[i] != has_all[i]) {
				System.out.println("RandomTrainer: Lanes in incorrect format, falling back to nonran or last ran used");
				return;
			}
		}

		@TODO implement a mode check so you dont force on non 7k game modifyTargetSide
		@TODO implement a reverse LUT for each random permutation 
		player.setLaneOrder(lanes);
	}
}
