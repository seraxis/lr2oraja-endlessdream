package bms.player.beatoraja;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreDataTest {

    @Test
    void calculatesExScore() {
        // given
        var score = new ScoreData();
        score.setEpg(1000);
        score.setLpg(300);
        score.setEgr(10);
        score.setLgr(2);

        score.setEgd(100);
        score.setLgd(100);
        score.setEbd(100);
        score.setLbd(100);
        score.setEpr(100);
        score.setLpr(100);
        score.setEms(100);
        score.setLms(100);

        // when
        var exScore = score.getExscore();

        // then
        assertEquals(2612, exScore);
    }
}
