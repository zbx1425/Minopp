package cn.zbx1425.minopp.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardPlayer {

    public UUID uuid;
    public String name;

    public List<Card> hand = new ArrayList<>();

    public enum State {
        PLAY,
        PLAY_DRAWN_1,
        PENALTY_DRAW,
    }
}
