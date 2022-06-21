package me.flerpharos.games.maps.entities;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.utils.RayConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import me.flerpharos.games.maps.Maps;
import me.flerpharos.games.maps.util.Box2dLocation;
import me.flerpharos.games.maps.util.Box2dRaycastCollisionDetector;
import me.flerpharos.games.maps.util.PlayerLocation;

import java.util.Iterator;

public class Enemy extends Entity implements Steerable<Vector2> {

    int realRotation = 0;

    public static final int SECONDS_TO_PATHFIND = 3;

    public boolean dead = false;

    boolean tagged;

    float secondsSincePathfound = SECONDS_TO_PATHFIND;
    GraphPath<Vector2> path;
    int pathTracingIndex = 0;

    protected SteeringBehavior<Vector2> steeringBehavior;
    protected SteeringBehavior<Vector2> looking;
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());
    private final SteeringAcceleration<Vector2> lookingOutput = new SteeringAcceleration<>(new Vector2());

    public Enemy(Maps m, float x, float y) {
        super(m);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        CircleShape shape = new CircleShape();
        shape.setRadius(3);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.01f;
        fixture.friction = 0.5f;

        body = m.world.createBody(def);
        body.createFixture(fixture);
        body.setLinearDamping(7f);
        body.setAngularDamping(10f);

        shape.dispose();

        steeringBehavior = new PrioritySteering<Vector2>(this);
        ((PrioritySteering) steeringBehavior).add(
                new Seek(this, new PlayerLocation(m.game.player))
        );
        ((PrioritySteering) steeringBehavior).add(
                new RaycastObstacleAvoidance(this,
                        new CentralRayWithWhiskersConfiguration(this, 12, 6, MathUtils.HALF_PI * 0.5f),
                        new Box2dRaycastCollisionDetector(m.world)));

        looking = new LookWhereYouAreGoing<>(this);
    }

    GraphPath<Vector2> pathfind() {

        secondsSincePathfound = 0;
        pathTracingIndex = 0;

        DefaultGraphPath<Vector2> path = new DefaultGraphPath<>();

        if (m.game.graph == null) return null;

        Vector2 playerPos = m.game.player.getPositionReal();
        Vector2 position = getPositionReal();

        Vector2 toNodePosition = m.game.graph.getClosestNode(playerPos);

        if (toNodePosition.dst2(playerPos) > position.dst2(playerPos)) {
            return this.path = new GraphPath<Vector2>() {
                @Override
                public int getCount() {return 1;}
                @Override
                public Vector2 get(int index) {return playerPos;}
                @Override
                public void add(Vector2 node) {}
                @Override
                public void clear() {}
                @Override
                public void reverse() {}
                @Override
                public Iterator<Vector2> iterator() {return null;}
            };
        }

        m.game.pathFinder.searchNodePath(
            m.game.graph.getClosestNode(position),
            toNodePosition,
            m.game.aStarHeuristic, path);

        return this.path = path;
    }

    Vector2 getNextPathfindingNode() {
        return path == null || pathTracingIndex > path.getCount() ? null : path.get(pathTracingIndex ++);
    }

    public void update(float delta) {

        realRotation = MathUtils.round(1.27323954474f * getRealAngle());

        //secondsSincePathfound += delta;

//        if (secondsSincePathfound >= SECONDS_TO_PATHFIND || (path != null && pathTracingIndex > path.getCount())) {
//            pathfind();
//        }
//
//        if (path != null) {
//            if (steeringBehavior == null || secondsSincePathfound == 0)
//                steeringBehavior = new Seek<>(this, new Box2dLocation(getNextPathfindingNode()));
//            else if ((pathTracingIndex >= 1
//                    && getPosition().dst2(path.get(pathTracingIndex - 1)) < 3)) {
//                pathfind();
//                steeringBehavior = new Seek<>(this, new Box2dLocation(getNextPathfindingNode()));
//            }
//        }

        if (steeringBehavior == null) return;

        steeringBehavior.calculateSteering(steeringOutput);
        looking.calculateSteering(lookingOutput);

        boolean anyAccelerations = false;

        if (lookingOutput.angular != 0) {
            body.applyTorque(lookingOutput.angular, true);
            anyAccelerations = true;
        }

        if (!steeringOutput.linear.isZero()) {
            body.applyForceToCenter(steeringOutput.linear, true);
            anyAccelerations = true;
        }
        if (steeringOutput.angular != 0) {
            body.applyTorque(steeringOutput.angular, true);
            anyAccelerations = true;
        }

        if (anyAccelerations) {

            Vector2 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            float maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float)Math.sqrt(currentSpeedSquare)));
            }

            float maxAngVelocity = getMaxAngularSpeed();
            if (body.getAngularVelocity() > maxAngVelocity) {
                body.setAngularVelocity(maxAngVelocity);
            }
        }
    }

    private float getRealAngle() {
        float ang = body.getAngle() % MathUtils.PI2;

        if (ang < 0) ang += MathUtils.PI2;

        return ang;
    }

    private TextureRegion getFromOrientation() {
        return switch (realRotation) {
            case 0, 8 -> m.game.blobboAtlas.findRegion( "BLOB_RIGHT");
            case 1, 9 -> m.game.blobboAtlas.findRegion( "BLOB_UPRIGHT");
            case 2, 10 -> m.game.blobboAtlas.findRegion("BLOB_UP");
            case 3, 11 -> m.game.blobboAtlas.findRegion("BLOB_UPLEFT");
            case 4, 12 -> m.game.blobboAtlas.findRegion("BLOB_LEFT");
            case 5, 13 -> m.game.blobboAtlas.findRegion("BLOB_DOWNLEFT");
            case 6, 14 -> m.game.blobboAtlas.findRegion("BLOB_DOWN");
            case 7, 15 -> m.game.blobboAtlas.findRegion("BLOB_DOWNRIGHT");
            default -> throw new IllegalStateException("Unexpected value: " + realRotation);
        };
    }

    @Override
    public void render(float delta) {
        m.batch.draw(getFromOrientation(), getPosition().x - 40, getPosition().y - 40, 80, 80);
    }

    public boolean checkCollision(Bullet b) {
        return getPosition().dst2(b.getPosition()) < 1000;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return 3;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getMaxLinearSpeed() {
        return 15;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getMaxLinearAcceleration() {
        return 15;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getMaxAngularSpeed() {
        return 50;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getMaxAngularAcceleration() {
        return 100;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return MathUtils.atan2(vector.y, vector.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = MathUtils.sin(angle);
        outVector.y = MathUtils.cos(angle);

        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }
}
