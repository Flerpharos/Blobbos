package me.flerpharos.games.maps.util;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.Maps;

public class Box2dLocation implements Location<Vector2> {

    Vector2 position;
    float orientation;

    public Box2dLocation() {
        this(new Vector2());
    }

    public Box2dLocation(Vector2 o) {
        this.position = o.cpy();
        this.orientation = 0;
    }

    @Override
    public Vector2 getPosition () {
        return position.cpy().scl(Maps.BOX2D_SCALE);
    }

    @Override
    public float getOrientation () {
        return orientation;
    }

    @Override
    public void setOrientation (float orientation) {
        this.orientation = orientation;
    }

    @Override
    public Location<Vector2> newLocation () {
        return new Box2dLocation();
    }

    @Override
    public float vectorToAngle (Vector2 vector) {
        return MathUtils.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector (Vector2 outVector, float angle) {
        outVector.x = -MathUtils.sin(angle);
        outVector.y = MathUtils.cos(angle);

        return outVector;
    }

}