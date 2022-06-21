package me.flerpharos.games.maps.util;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;

public class Vector2Connection implements Connection<Vector2> {

    Vector2Connection(Vector2 from, Vector2 to) {
        this.from = from;
        this.to = to;
    }

    Vector2 from;
    Vector2 to;

    float dstCache;

    @Override
    public float getCost() {
        return dstCache != 0 ? dstCache : (dstCache = from.dst(to));
    }

    @Override
    public Vector2 getFromNode() {
        return from.cpy();
    }

    @Override
    public Vector2 getToNode() {
        return to.cpy();
    }
}
