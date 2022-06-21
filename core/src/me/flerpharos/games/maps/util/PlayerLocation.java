package me.flerpharos.games.maps.util;

import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.entities.Player;

public class PlayerLocation extends Box2dLocation {

    Player p;

    public PlayerLocation(Player p) {
        this.p = p;
    }

    @Override
    public Vector2 getPosition() {
        return p.getPosition();
    }

    @Override
    public float getOrientation() {
        return 0;
    }
}
