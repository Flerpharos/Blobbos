package me.flerpharos.games.maps.entities;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import me.flerpharos.games.maps.Maps;

public class Player extends Entity {
    Polygon collider;

    Vector2 tempForce;

    int realRotation = 0;

    public Player(Maps m) {
        super(m);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(0, 0);

        CircleShape shape = new CircleShape();
        shape.setRadius(20 * m.BOX2D_ISCALE);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.5f;
        fixture.friction = 0.5f;

        body = m.world.createBody(def);
        body.createFixture(fixture);
        body.setLinearDamping(10f);

        tempForce = new Vector2(1, 0);

        shape.dispose();
    }

    public void update(float delta) {
        if (m.keys.contains(Input.Keys.A)) turnLeft();
        else if (m.keys.contains(Input.Keys.D)) turnRight();
        else body.setAngularVelocity(0);

        if (m.keys.contains(Input.Keys.W)) accelerate();
        else if (m.keys.contains(Input.Keys.S)) decelerate();
        else body.setLinearDamping(1f);

        realRotation = MathUtils.round(1.27323954474f * getRealAngle());
    }

    public float getRealAngle() {
        float ang = body.getAngle() % MathUtils.PI2;

        if (ang < 0) ang += MathUtils.PI2;

        return ang;
    }

    public boolean checkCollision(Enemy e) {
        return getPositionReal().dst(e.getPositionReal()) < 5;
    }

    private TextureRegion getFromOrientation() {
        return switch (realRotation) {
            case 0, 8 -> m.game.roboAtlas.findRegion("right");
            case 1, 9 -> m.game.roboAtlas.findRegion("backright");
            case 2, 10 -> m.game.roboAtlas.findRegion("back");
            case 3, 11 -> m.game.roboAtlas.findRegion("backleft");
            case 4, 12 -> m.game.roboAtlas.findRegion("left");
            case 5, 13 -> m.game.roboAtlas.findRegion("frontleft");
            case 6, 14 -> m.game.roboAtlas.findRegion("front");
            case 7, 15 -> m.game.roboAtlas.findRegion("frontright");
            default -> throw new IllegalStateException("Unexpected value: " + realRotation);
        };
    }

    @Override
    public void render(float delta) {

        m.batch.draw(getFromOrientation(), getPosition().x - 80, getPosition().y - 40, 160, 160);
    }

    public void turnLeft() {
        body.setAngularVelocity(4f);
    }

    public void turnRight() {
        body.setAngularVelocity(-4f);
    }

    public void accelerate() {


        body.applyForceToCenter(tempForce.cpy().setAngleRad(body.getAngle()).scl(10000 * m.BOX2D_ISCALE), true);

        body.setLinearVelocity(body.getLinearVelocity().clamp(0, 100 * m.BOX2D_ISCALE));
    }

    public void decelerate() {
        body.setLinearDamping(10f);
    }
}
