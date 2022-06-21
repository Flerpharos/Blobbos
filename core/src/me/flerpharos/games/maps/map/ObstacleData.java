package me.flerpharos.games.maps.map;

import com.badlogic.gdx.math.Vector2;

public final class ObstacleData {

    public final Vector2 center;
    public final ObstacleTypes type;

    public ObstacleData(Vector2 location, ObstacleTypes type) {
        this.center = location;
        this.type = type;
    }
}
