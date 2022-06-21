package me.flerpharos.games.maps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Align;
import me.flerpharos.games.maps.Maps;
import me.flerpharos.games.maps.entities.*;
import me.flerpharos.games.maps.map.Map;
import me.flerpharos.games.maps.map.ObstacleData;
import me.flerpharos.games.maps.util.KeybindSystem;
import me.flerpharos.games.maps.util.Text;
import me.flerpharos.games.maps.util.Vector2Graph;

import java.util.ArrayList;
import java.util.Arrays;

public class GameScreen implements Screen {

    Maps maps;
    ArrayList<Enemy> enemies;
    public Player player;
    ArrayList<Bullet> bullets;
    ArrayList<Polygon> bulletExclusionZones;
    ArrayList<Body> entityCollisionZones;
    ArrayList<Obstacle> obstacles;

    //TODO: Optional -- add the mirrors in if you have the time

    ArrayList<Destroyable> deleteArray;

    ArrayList<IRenderOrderProvider> renderables;

    AssetDescriptor<Texture> mapDescriptor;
    AssetDescriptor<Texture> bushDescriptor;
    AssetDescriptor<Texture> highwallDescriptor;
    AssetDescriptor<Texture> wallDescriptor;
    AssetDescriptor<Texture> mirrorDescriptor;
    AssetDescriptor<Texture> rockDescriptor;
    AssetDescriptor<TextureAtlas> roboAtlasDescriptor;
    AssetDescriptor<TextureAtlas> blobboAtlasDescriptor;
    AssetDescriptor<Text> mapDefinitionDescriptor;
    AssetDescriptor<TextureAtlas> pewpewAtlasDescriptor;
    AssetDescriptor<Sound> pewSoundDescriptor;
    AssetDescriptor<Sound> powSoundDescriptor;
    AssetDescriptor<Sound> victorySoundDescriptor;
    AssetDescriptor<Sound> losingSoundDescriptor;
    AssetDescriptor<Sound> cutsceneSoundDescriptor;

    Text mapDefinition;

    public Texture bush;
    public Texture highwall;
    public Texture wall;
    Texture mirror;
    public Texture rock;
    Texture map;
    public TextureAtlas roboAtlas;
    public TextureAtlas blobboAtlas;
    public TextureAtlas pewpewAtlas;
    Sound pewSound;
    Sound powSound;
    Sound victorySound;
    Sound losingSound;
    Sound cutsceneSound;

    public Vector2Graph graph;
    public IndexedAStarPathFinder<Vector2> pathFinder;
    public Heuristic<Vector2> aStarHeuristic = Vector2::dst;
    public GlyphLayout cutsceneText;
    public GlyphLayout scoreCounter;
    public GlyphLayout winText;
    public GlyphLayout loseText;
    public GlyphLayout clickNextText;

    boolean paused;
    boolean preload;
    public boolean prepared;
    boolean noMap;
    boolean loadingMap;
    boolean showedCutscene;
    boolean shouldAdvanceScene;

    boolean shouldShoot;
    float shootTimer = 1000;

    int score;
    boolean won;

    Screen nextScreen;
    private boolean showedEndScene;
    private boolean playedCutsceneAudio;

    public GameScreen(Maps m) {
        maps = m;

        enemies = new ArrayList<>();
        bulletExclusionZones = new ArrayList<>();
        entityCollisionZones = new ArrayList<>();
        bullets = new ArrayList<>();
        obstacles = new ArrayList<>();
        deleteArray = new ArrayList<>();

        renderables = new ArrayList<>();

        scoreCounter = new GlyphLayout();

        paused = false;
        preload = false;
        prepared = false;
        noMap = true;

        player = new Player(m);

        m.keyboardDispatch.register(evt -> {

            if (noMap || !evt.target.equals("f")) return false;
            shouldAdvanceScene = true;
            return true;
        });
    }

    private Body setupEntityCollider(Polygon p) {

        PolygonShape shape = new PolygonShape();
        shape.set(p.getVertices());

        BodyDef def = new BodyDef();
        def.position.set(p.getX(), p.getY());
        def.type = BodyDef.BodyType.StaticBody;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        Body body = maps.world.createBody(def);
        body.createFixture(fixtureDef);

        return body;
    }

    public void loadMap(Map map) {
        //TODO: Optional -- Set up settings to control volumne & or keybindings
        //      -- make sure to remove Settings button if not implemented



       /*
    public Vector2[] bulletReflectors;

    public String cutsceneString;
    public String cutsceneVoicoverFilepath;
        */
        loadingMap = true;

        reset();

        score = map.scoreMax;

        mapDescriptor = new AssetDescriptor<>(map.mapGroundFilepath, Texture.class);
        if (map.cutsceneVoicoverFilepath != null
                && !map.cutsceneVoicoverFilepath.equals(""))
            cutsceneSoundDescriptor = new AssetDescriptor<Sound>(map.cutsceneVoicoverFilepath, Sound.class);
        graph = new Vector2Graph(map.graphPoints, map.connections);
        pathFinder = new IndexedAStarPathFinder<>(graph);
        player.body.setTransform(map.playerLocation, 0);

        for (ObstacleData data : map.obstacles)
            obstacles.add(new Obstacle(maps, data));

        for (Vector2 enemyLoc : map.enemyLocations)
            enemies.add(new Enemy(maps, enemyLoc.x, enemyLoc.y));

        for (Polygon entityCollider : map.entityColliders)
            entityCollisionZones.add(setupEntityCollider(entityCollider));

        bulletExclusionZones.addAll(Arrays.asList(map.bulletColliders));

        renderables.add(player);
        renderables.addAll(obstacles);
        renderables.addAll(enemies);

        maps.offsideFont.getData().setScale(1.5f);
        cutsceneText = new GlyphLayout(
                maps.offsideFont,
                map.cutsceneString,
                Color.WHITE,
                maps.CAMERA_WIDTH * 0.9f,
                Align.center, true);

    }

    public boolean prepare() {
        if (!preload) {
            preload = true;

            maps.manager.load(pewpewAtlasDescriptor = new AssetDescriptor<>("pewpew/PewPew.atlas", TextureAtlas.class));
            maps.manager.load(blobboAtlasDescriptor = new AssetDescriptor<>("blobbo/Blobbo.atlas", TextureAtlas.class));
            maps.manager.load(roboAtlasDescriptor = new AssetDescriptor<>("robo/Robot.atlas", TextureAtlas.class));
            maps.manager.load(bushDescriptor = new AssetDescriptor<>("bush.png", Texture.class));
            maps.manager.load(highwallDescriptor = new AssetDescriptor<>("tall wall.png", Texture.class));
            maps.manager.load(wallDescriptor = new AssetDescriptor<>("wall.png", Texture.class));
            maps.manager.load(mirrorDescriptor = new AssetDescriptor<>("mirror.png", Texture.class));
            maps.manager.load(rockDescriptor = new AssetDescriptor<>("rock.png", Texture.class));
            maps.manager.load(pewSoundDescriptor = new AssetDescriptor<>("audio/pew.wav", Sound.class));
            maps.manager.load(powSoundDescriptor = new AssetDescriptor<Sound>("audio/pow.wav", Sound.class));
            maps.manager.load(victorySoundDescriptor = new AssetDescriptor<Sound>("audio/victory.mp3", Sound.class));
            maps.manager.load(losingSoundDescriptor = new AssetDescriptor<Sound>("audio/lose.mp3", Sound.class));

            return false;
        } else {

            pewpewAtlas = maps.manager.get(pewpewAtlasDescriptor);
            blobboAtlas = maps.manager.get(blobboAtlasDescriptor);
            roboAtlas = maps.manager.get(roboAtlasDescriptor);
            bush = maps.manager.get(bushDescriptor);
            highwall = maps.manager.get(highwallDescriptor);
            wall = maps.manager.get(wallDescriptor);
            mirror = maps.manager.get(mirrorDescriptor);
            rock = maps.manager.get(rockDescriptor);
            pewSound = maps.manager.get(pewSoundDescriptor);
            powSound = maps.manager.get(powSoundDescriptor);
            victorySound = maps.manager.get(victorySoundDescriptor);
            losingSound = maps.manager.get(losingSoundDescriptor);

            maps.offsideFont.getData().setScale(1.5f);
            winText = new GlyphLayout(maps.offsideFont, "You Win!");
            loseText = new GlyphLayout(maps.offsideFont, "You Lose!");

            maps.offsideFont.getData().setScale(.5f);
            clickNextText = new GlyphLayout(maps.offsideFont, "Press F to continue");
            maps.offsideFont.getData().setScale(1f);

            if (mapDefinitionDescriptor == null) return false;

            if (mapDefinition == null)
                mapDefinition = maps.manager.get(mapDefinitionDescriptor);

            if (mapDefinition != null && !loadingMap) loadMap(new Map(mapDefinition.getString()));

            if (mapDescriptor == null) return true;

            if (noMap) {
                maps.manager.load(mapDescriptor);
                if (cutsceneSoundDescriptor != null) maps.manager.load(cutsceneSoundDescriptor);
                noMap = false;
            } else {
                map = maps.manager.get(mapDescriptor);
                if (cutsceneSoundDescriptor != null) cutsceneSound = maps.manager.get(cutsceneSoundDescriptor);
                prepared = true;
                return true;
            }
            return false;
        }
    }

    public void reset() {

        prepared = false;
        noMap = true;

        mapDescriptor = null;
        map = null;

        mapDefinition = null;

        shouldShoot = false;
        shootTimer = 1000;

        showedCutscene = false;
        nextScreen = null;

        won = false;
        score = 0;
        showedEndScene = false;
        playedCutsceneAudio = false;

        cutsceneSound = null;
        cutsceneSoundDescriptor = null;
        shouldAdvanceScene = false;

        for (Body b : entityCollisionZones) maps.world.destroyBody(b);
        for (Obstacle o : obstacles) o.destroy();
        for (Enemy e : enemies) e.destroy();

        deleteArray.clear();
        bullets.clear();
        bulletExclusionZones.clear();
        entityCollisionZones.clear();
        enemies.clear();
        obstacles.clear();
        renderables.clear();

        player.body.setLinearVelocity(0, 0);
        player.body.setAngularVelocity(0);
    }

    void deleteInto(ArrayList<?> any) {
        for (Destroyable o : deleteArray) {
            any.remove(o);
            renderables.remove(o);
            o.destroy();
        }
        deleteArray.clear();
    }

    void update(float delta) {
        score -= 50 * delta;

        if (score < 0) score = 0;

        if (paused || !showedCutscene) {
            return;
        }

        if (maps.keys.contains(Input.Keys.CONTROL_LEFT)
            && maps.keys.contains(Input.Keys.X)
            && maps.keys.contains(Input.Keys.SHIFT_LEFT)
        ) {
            for (Enemy e : enemies) deleteArray.add(e);

            deleteInto(enemies);
        }

        shootTimer += delta;

        if (maps.keys.contains(KeybindSystem.getShoot())) shouldShoot = true;

        if (shouldShoot && shootTimer < 1.1f) {
            shouldShoot = false;
        } else if (shouldShoot && shootTimer > 1.1f) {
            shootTimer = 0;

            Bullet b = new Bullet(maps, player.getPosition().cpy().add(0, 40), new Vector2(100, 100).setAngleRad(player.getRealAngle()));

            bullets.add(b);
            renderables.add(b);

            pewSound.play();
        }

        maps.world.step(delta, 8, 3);

        player.update(delta);

        for (Enemy e : enemies) {
            e.update(delta);

            if (player.checkCollision(e)) {
                nextScreen = maps.menu;
                losingSound.play();
            }
        }

        for (Bullet bullet : bullets) {

            bullet.update(delta);

            boolean collided = bullet.isDead;

            for (Polygon r : bulletExclusionZones)
                if (r.contains(bullet.getPosition())) {
                    collided = true;
                    break;
                }

            for (Enemy e : enemies)
                if (e.checkCollision((bullet))) {
                    e.dead = true;
                    collided = true;
                    score += 500;

                    powSound.play();
                    break;
                }

            if (collided) deleteArray.add(bullet);
        }

        deleteInto(bullets);

        for (Enemy e: enemies)
            if (e.dead) deleteArray.add(e);

        deleteInto(enemies);

        if (enemies.size() == 0) {
            nextScreen = maps.menu;
            won = true;
            victorySound.play();
        }
    }

    @Override
    public void render(float delta) {
        if (!prepared) return;

        if (nextScreen != null && !showedEndScene) {

            if (shouldAdvanceScene) {
                showedEndScene = true;
                shouldAdvanceScene = false;
                maps.setScreen(nextScreen);
                nextScreen = null;
                return;
            }

            maps.batch.end();
            maps.batch.setProjectionMatrix(maps.camera.projection);
            maps.batch.begin();

            GlyphLayout layout = won ? winText : loseText;

            maps.offsideFont.getData().setScale(1.5f);
            maps.offsideFont.draw(maps.batch, layout, -layout.width * 0.5f, layout.height * 0.5f);

            maps.offsideFont.getData().setScale(0.5f);
            maps.offsideFont.draw(maps.batch, clickNextText, -clickNextText.width * 0.5f, -maps.CAMERA_HHEIGHT * 0.9f);
            maps.offsideFont.getData().setScale(1.5f);

            return;
        }

        if (!showedCutscene) {

            if (!playedCutsceneAudio && cutsceneSound != null) {
                cutsceneSound.play();
                playedCutsceneAudio = true;
            }

            if (shouldAdvanceScene) {
                showedCutscene = true;
                shouldAdvanceScene = false;
                if (cutsceneSound != null) cutsceneSound.stop();
            }

            maps.batch.end();
            maps.batch.setProjectionMatrix(maps.camera.projection);
            maps.batch.begin();

            maps.offsideFont.getData().setScale(1.5f);
            maps.offsideFont.draw(maps.batch, cutsceneText, -maps.CAMERA_HWIDTH * 0.9f, cutsceneText.height * 0.5f);

            maps.offsideFont.getData().setScale(0.5f);
            maps.offsideFont.draw(maps.batch, clickNextText, -clickNextText.width * 0.5f, -maps.CAMERA_HHEIGHT * 0.9f);
            maps.offsideFont.getData().setScale(1.5f);

            return;
        }

        shouldAdvanceScene = false;

        update(delta);

        renderables.sort((a, b) -> (int) (b.getRenderOrder() - a.getRenderOrder()));

        Vector3 cameraPos = maps.camera.position;
        Vector2 translate = player.getPosition().sub(cameraPos.x, cameraPos.y).scl(0.1f);

        maps.camera.translate(translate);

        maps.batch.setProjectionMatrix(maps.camera.combined);
        maps.camera.update();

        maps.batch.draw(map, 0, 0);

        for (IRenderOrderProvider renderable : renderables) {
            renderable.render(delta);
        }

        maps.batch.end();
        maps.batch.setProjectionMatrix(maps.camera.projection);
        maps.batch.begin();

        scoreCounter.setText(maps.offsideFont, ((Integer) score).toString());

        maps.offsideFont.draw(maps.batch, scoreCounter, -maps.CAMERA_HWIDTH + 10, maps.CAMERA_HHEIGHT - 10);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        paused = true;
        //TODO: add pause menu
    }

    @Override
    public void resume() {
        paused = false;
        //TODO: remove pause menu
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        bush.dispose();
        highwall.dispose();
        wall.dispose();
        mirror.dispose();
        rock.dispose();
    }
}
