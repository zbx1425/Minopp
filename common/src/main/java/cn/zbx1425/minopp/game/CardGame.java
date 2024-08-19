package cn.zbx1425.minopp.game;

import java.util.*;

public class CardGame {

    public Map<UUID, CardPlayer> players = new HashMap<>();

    public List<Card> deck = new ArrayList<>();

    public Card topCard;
}
