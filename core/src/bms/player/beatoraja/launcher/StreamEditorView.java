package bms.player.beatoraja.launcher;

import java.net.URL;
import java.util.ResourceBundle;

import bms.player.beatoraja.PlayerConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;

public class StreamEditorView implements Initializable {

    @FXML
    private CheckBox enableRequest;
    @FXML
    private CheckBox notifyRequest;
    @FXML
    private Spinner<Integer> maxRequestCount;
    
    private PlayerConfig player;

    public void initialize(URL arg0, ResourceBundle arg1) {
    }

    public void update(PlayerConfig player) {
        this.player = player;
        if(this.player == null) {
            return;
        }
        enableRequest.setSelected(this.player.isRequestEnabled());
        notifyRequest.setSelected(this.player.isRequestNotify());
        maxRequestCount.getValueFactory().setValue(this.player.getMaxRequestCount());
    }

    public void commit() {
        if(this.player == null) {
            return;
        }
        player.setRequestEnabled(enableRequest.isSelected());
        player.setRequestNotify(notifyRequest.isSelected());
        player.setMaxRequestCount(maxRequestCount.getValue());
    }
}
