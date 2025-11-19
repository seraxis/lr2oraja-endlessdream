package bms.player.beatoraja.launcher;


import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.RandomTrainer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class TrainerView {
	private static final Logger logger = LoggerFactory.getLogger(TrainerView.class);
	
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
		RandomTrainer randomtrainer = new RandomTrainer();
        if(this.player == null) {
            return;
        }
		randomtrainer.setActive(false);
        traineractive.setSelected(randomtrainer.isActive());
		laneorder.setPromptText("1234567");
		if (randomtrainer.getLaneOrder() != null) {
			laneorder.setText(randomtrainer.getLaneOrder());
		} else {
		    randomtrainer.setLaneOrder("1234567");
		}
    }

    @FXML
	public void setActive() {
		RandomTrainer.setActive(traineractive.isSelected());
	}

/*     @FXML
	public void fromHistory () {

	} */

	@FXML
	public void setRandom() {
		RandomTrainer randomtrainer = new RandomTrainer();
		if (this.laneorder == null) {
			logger.warn("RandomTrainer: Lane field empty");
			return;
		}

		int[] lanes = this.laneorder.getCharacters().codePoints().map(Character::getNumericValue).map(c -> c-1).toArray();

        int[] has_all = new int[]{0,1,2,3,4,5,6};
		Arrays.sort(has_all);
		int[] l = lanes.clone();
		Arrays.sort(l);

		if (l.length != 7) {
			logger.warn("RandomTrainer: Incorrect number of lanes specified");
			return;
		}

		for (int i = 0; i < has_all.length; i++) {
			if (l[i] != has_all[i]) {
				logger.warn("RandomTrainer: Lanes in incorrect format, falling back to nonran or last ran used");
				return;
			}
		}

		randomtrainer.setLaneOrder(this.laneorder.getCharacters().toString());
	}
}
