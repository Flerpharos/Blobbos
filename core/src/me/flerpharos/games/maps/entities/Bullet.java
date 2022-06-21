package me.flerpharos.games.maps.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.flerpharos.games.maps.Maps;

public class Bullet implements IRenderOrderProvider, Destroyable {
    Vector2 position;
    Vector2 velocity;
    Maps m;

    float animState = 0;

    public boolean isDead = false;

    public Bullet(Maps m, Vector2 position, Vector2 velocity) {
        this.position = position;
        this.velocity = velocity;
        this.m = m;
    }

    public Vector2 getPosition() {
        return position.cpy();
    }

    public void update(float delta) {
        this.position.add(velocity.cpy().scl(delta));
        Vector3 projection = m.camera.project(new Vector3(position.x, position.y, 0));
        if (!m.screenBounds.contains(projection.x, projection.y)) isDead = true;
    }

    @Override
    public float getRenderOrder() {
        return position.y * Maps.BOX2D_ISCALE;
    }

    @Override
    public void render(float delta) {

        animState += delta * 8;

        if (animState >= 3) animState = 0.1f;

        TextureRegion drawable = m.game.pewpewAtlas.findRegion("bullet", MathUtils.ceil(animState));
        float width = drawable.getRegionWidth();
        float height = drawable.getRegionHeight();

        m.batch.draw(
                drawable,
                this.position.x - width * 0.5f,
                this.position.y - height * 0.5f,
                width * 0.5f,
                height * 0.5f,
                width, height,
                1, 1,
                velocity.angleDeg());
    }

    @Override
    public void destroy() {}
}
