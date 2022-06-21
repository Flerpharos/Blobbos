package me.flerpharos.games.maps.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import me.flerpharos.games.maps.Maps;
import me.flerpharos.games.maps.map.ObstacleData;
import me.flerpharos.games.maps.map.ObstacleTypes;

public class Obstacle extends Entity {

    final ObstacleTypes type;

    public Obstacle(Maps m, ObstacleData obstacle) {
        super(m);
        this.type = obstacle.type;

        float[] transform = switch (type) {
            case WALL, HIGHWALL -> new float[]{.25f, -4.5f};
            case BUSH, ROCK -> new float[]{0.3f, -2.5f};
        };

        BodyDef wallBodyDef = new BodyDef();
        wallBodyDef.type = BodyDef.BodyType.StaticBody;
        wallBodyDef.position.set(obstacle.center.x + transform[0], obstacle.center.y + transform[1]);

        PolygonShape wallShape = null;

        switch (type) {
            case WALL, HIGHWALL -> {
                wallShape = new PolygonShape();
                wallShape.setAsBox(6.25f, 2.5f);
            }
            case ROCK -> {
                wallShape = new PolygonShape();
                wallShape.setAsBox(4f, 2f);

            }
            case BUSH -> {
                wallShape = new PolygonShape();
                wallShape.setAsBox(5f, 2.5f);
            }
        }

        FixtureDef wallFixture = new FixtureDef();
        wallFixture.shape = wallShape;
        wallFixture.friction = 0.5f;

        this.body = m.world.createBody(wallBodyDef);
        body.createFixture(wallFixture);
        wallShape.dispose();
    }

    @Override
    public void update(float delta) {}

    @Override
    public void render(float delta) {

        float[] transform = switch (type) {
            case WALL, HIGHWALL -> new float[]{2.5f, -45};
            case BUSH, ROCK -> new float[]{3f, -25f};
        };

        m.batch.draw(switch (type) {
                case ROCK -> m.game.rock;
                case BUSH -> m.game.bush;
                case HIGHWALL -> m.game.highwall;
                case WALL -> m.game.wall;
            },
            getPosition().x - 80 - transform[0],
            getPosition().y - 80 - transform[1],
            160, 160);
    }
}
