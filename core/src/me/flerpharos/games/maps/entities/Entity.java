package me.flerpharos.games.maps.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import me.flerpharos.games.maps.Maps;

abstract public class Entity implements IRenderOrderProvider, Destroyable {

    Maps m;
    public Body body;

    Entity(Maps m) {
       this.m = m;
    }

    @Override
    public float getRenderOrder() {
        return body.getPosition().y;
    }

    public Vector2 getPositionReal() {return this.body.getPosition();}
    public Vector2 getPosition() {
        return this.body.getPosition().scl(m.BOX2D_SCALE);
    }

    abstract public void update(float delta);

    public Fixture getFixture() {
        return this.body.getFixtureList().get(0);
    }

    public abstract void render(float delta);

    @Override
    public void destroy() {
        m.world.destroyBody(body);
    }
}
