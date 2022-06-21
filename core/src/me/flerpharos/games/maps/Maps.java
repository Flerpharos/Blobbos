package me.flerpharos.games.maps;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

import java.util.LinkedList;
import java.util.Queue;
import me.flerpharos.games.maps.interactables.EventDispatcher;
import me.flerpharos.games.maps.screens.GameScreen;
import me.flerpharos.games.maps.screens.Leaderboard;
import me.flerpharos.games.maps.screens.MainMenu;
import me.flerpharos.games.maps.screens.SettingsMenu;
import me.flerpharos.games.maps.util.Score;
import me.flerpharos.games.maps.util.Text;
import me.flerpharos.games.maps.util.TextLoader;

import java.util.HashSet;

public class Maps extends Game implements InputProcessor {

	static final public boolean DEBUG = false;
	static final public float CAMERA_WIDTH = 1920;
	static final public float CAMERA_HEIGHT = 1080;
	static final public float CAMERA_HWIDTH = CAMERA_WIDTH / 2;
	static final public float CAMERA_HHEIGHT = CAMERA_HEIGHT / 2;
	static final public int BOX2D_SCALE = 10;
	static final public float BOX2D_ISCALE = 1f / BOX2D_SCALE;

	static final float width_20 = CAMERA_WIDTH * 0.05f;
	static final float height_4 = CAMERA_HEIGHT * 0.25f;
	static final float height_40 = CAMERA_HEIGHT * 0.025f;

	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;
	public OrthographicCamera camera;
	public Box2DDebugRenderer debugRenderer;
	final public AssetManager manager = new AssetManager();
	final public World world = new World(new Vector2(0, 0), true);
	public Rectangle screenBounds;
	Texture splash;

	public HashSet<Integer> keys;
	boolean prepared = false;
	boolean preload = false;
	float loadAnimTime = 0;

	public Vector2 mousePos;
	Vector2 screenSpaceScale;

	public Score[] topScores = new Score[10];
	public EventDispatcher<String> keyboardDispatch;
	String kdValue;

	public GameScreen game;
	public Leaderboard board;
	public MainMenu menu;
	SettingsMenu settings;

	public String name;

	AssetDescriptor<BitmapFont> fontDescriptor;
	public BitmapFont offsideFont;

	AssetDescriptor<Sound> quietMusicDescriptor;
	Sound quietMusic;

	public String[] mapQueue = {
			"maps/tutorial/tutorial_enemy.map",
//			"maps/tutorial/rohit1.map",
//			"maps/tutorial/rohit2.map",
			"maps/tutorial/13.map",
			"maps/tutorial/1..map",
			"maps/tutorial/132.map",
			"maps/tutorial/1.5.map",

			//"maps/tutorial/tutorial_4.map"
	};
	public Queue<String> activeMapQueue = new LinkedList<>();
	public float score = 0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		splash = new Texture("background.jpg");
		screenBounds = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		keyboardDispatch = new EventDispatcher<>() {
			@Override
			protected String provideTarget() {
				return kdValue;
			}
		};

		game = new GameScreen(this);
		board = new Leaderboard(this);
		menu = new MainMenu(this);
		settings = new SettingsMenu(this);
		camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);

		manager.setLoader(Text.class,
				new TextLoader(new InternalFileHandleResolver()));

		batch.setProjectionMatrix(camera.combined);

		if (DEBUG) debugRenderer = new Box2DDebugRenderer();

		keys = new HashSet<>();
		mousePos = new Vector2();
		screenSpaceScale = new Vector2(CAMERA_WIDTH / Gdx.graphics.getWidth(), CAMERA_HEIGHT / Gdx.graphics.getHeight());

		for (int i=0; i<topScores.length; i++) {
			topScores[i] = new Score();
		}

		Gdx.input.setInputProcessor(this);

		setScreen(menu);
	}

	public boolean prepare() {
		if (!preload) {
			preload = true;

			manager.load(fontDescriptor = new AssetDescriptor<>(Gdx.files.internal("Offside.fnt"), BitmapFont.class));
			manager.load(quietMusicDescriptor = new AssetDescriptor<Sound>("audio/quiet_music.mp3", Sound.class));
			return false;
		} else {

			quietMusic = manager.get(quietMusicDescriptor);

			quietMusic.setVolume(quietMusic.loop(), 0.5f);

			offsideFont = manager.get(fontDescriptor);
			offsideFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			prepared = true;
			return true;
		}
	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();

		if (keys.contains(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
			System.exit(0);
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mousePos.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
		mousePos.scl(screenSpaceScale);
		mousePos.sub(CAMERA_HWIDTH, CAMERA_HHEIGHT);

		//if (DEBUG) System.out.println(mousePos.cpy().scl(BOX2D_ISCALE));

		if (!manager.update()) {

			batch.setProjectionMatrix(camera.projection);
			shapeRenderer.setProjectionMatrix(camera.projection);

			batch.begin();
			if (activeMapQueue.size() == 0)
				batch.draw(splash, -CAMERA_HWIDTH, -CAMERA_HHEIGHT, CAMERA_WIDTH, CAMERA_HEIGHT);
			batch.end();

			if (loadAnimTime >= 4) loadAnimTime = 0;

			float a = MathUtils.sin(MathUtils.PI * (loadAnimTime - 1.33f)) * height_40;
			float b = MathUtils.sin(MathUtils.PI * (loadAnimTime - 0.66f)) * height_40;
			float c = MathUtils.sin(MathUtils.PI * loadAnimTime) * height_40;

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(1, 1, 1, 0.8f);

			if (activeMapQueue.size() == 0) {
				shapeRenderer.circle(-width_20, -height_4 + a, height_40);
				shapeRenderer.circle(0, -height_4 + b, height_40);
				shapeRenderer.circle(width_20, -height_4 + c, height_40);
			}

			shapeRenderer.end();

			Gdx.gl.glDisable(GL20.GL_BLEND);

			loadAnimTime += delta;
		} else {
			loadAnimTime = 0;

			if (!this.prepared) this.prepare();
			if (!game.prepared) game.prepare();
			if (!menu.prepared) menu.prepare();
			if (!board.prepared) board.prepare();

			if (!manager.isFinished()) return;

			batch.begin();
			getScreen().render(delta);
			batch.end();

			if (DEBUG) debugRenderer.render(world, camera.combined.cpy().scl(BOX2D_SCALE));
		}


	}

	@Override
	public void resize(int x, int y) {

		screenSpaceScale.set(CAMERA_WIDTH / Gdx.graphics.getWidth(), CAMERA_HEIGHT / Gdx.graphics.getHeight());
	}

	@Override
	public void pause() {
		quietMusic.pause();
	}

	@Override
	public void resume() {
		quietMusic.resume();
	}
	
	@Override
	public void dispose () {
		manager.dispose();
		world.dispose();
		batch.dispose();
		splash.dispose();

		game.dispose();
		board.dispose();
		menu.dispose();
		settings.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		keys.add(keycode);

//		if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.DEL) {
//			keyboardDispatch.dispatchEvent("delete");
//		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys.remove(keycode);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {

		if (character == 0x7F || character == 0x08) {
			keyboardDispatch.dispatchEvent("delete");
			return true;
		}

		kdValue = "" + character;
		keyboardDispatch.dispatchEvent("keypress");
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}