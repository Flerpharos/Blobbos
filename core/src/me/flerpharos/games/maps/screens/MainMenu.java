package me.flerpharos.games.maps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import me.flerpharos.games.maps.Maps;

import me.flerpharos.games.maps.interactables.Button;
import me.flerpharos.games.maps.interactables.TextInput;
import me.flerpharos.games.maps.interactables.UIElement;
import me.flerpharos.games.maps.util.Score;
import me.flerpharos.games.maps.util.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainMenu implements Screen {

    Maps maps;

    Button exit;
    Button start;
    Button toSettings;
    Button toLeader;
    Button toTutorial;
    TextInput name;


    AssetDescriptor<Texture> bgDescriptor;
    Texture background;

    boolean preload;
    public boolean prepared;

    Screen nextScreen;
    private boolean startingGame;

    public MainMenu(Maps m) {
        maps = m;

        preload = false;
        prepared = false;
    }

    public boolean prepare() {
        if (!preload) {
            preload = true;

            maps.manager.load(bgDescriptor = new AssetDescriptor<Texture>("background.jpg", Texture.class));
            return false;
        } else {

            background = maps.manager.get(bgDescriptor);

            name = new TextInput(0, maps, new Vector2(-maps.CAMERA_HWIDTH + 10, -maps.CAMERA_HHEIGHT + 10), new Vector2(600, 80), "Your name here");
            exit = new Button(0, maps, new Vector2(maps.CAMERA_HWIDTH - 10, -maps.CAMERA_HHEIGHT * 0.9f), new Vector2(600, 100), "Exit");
            start = new Button(0, maps, new Vector2(maps.CAMERA_HWIDTH - 10, maps.CAMERA_HHEIGHT * -0.5f), new Vector2(600, 100), "Start");
            toLeader = new Button(0, maps, new Vector2(maps.CAMERA_HWIDTH - 10, -maps.CAMERA_HHEIGHT * 0.7f), new Vector2(600, 100), "Leaderboard");
            toSettings = new Button(0, maps, new Vector2(0, -maps.CAMERA_HHEIGHT * 0.1f), new Vector2(600, 100), "Settings");
            toTutorial = new Button(0, maps, new Vector2(0, maps.CAMERA_HHEIGHT * 0.3f), new Vector2(600, 100), "Tutorial");

            name.register(event -> {
                maps.name = event.target.value;
                return true;
            });

            start.register(event -> {
                maps.activeMapQueue.clear();

                for (String s : maps.mapQueue) {
                    maps.activeMapQueue.add(s);
                }

                maps.game.prepared = false;
                maps.game.loadingMap = false;

                startingGame = true;

                return true;
            });

            toLeader.register(event -> {
                nextScreen = maps.board;
                return true;
            });

            exit.register(event -> {
                Gdx.app.exit();
                System.exit(0);
                return true;
            });

            prepared = true;
            return true;
        }
    }

    @Override
    public void render(float delta) {

        if (nextScreen != null) {
            maps.setScreen(nextScreen);
            nextScreen = null;
            return;
        }

        if (!prepared) return;

        if (maps.activeMapQueue != null && maps.activeMapQueue.size() != 0) {
            if (!startingGame && !maps.game.won) {
                maps.activeMapQueue.clear();
                return;
            }

            maps.game.prepared = false;
            maps.game.loadingMap = false;

            maps.score += maps.game.score;

            maps.game.reset();
            maps.manager.load(maps.game.mapDefinitionDescriptor = new AssetDescriptor<>(maps.activeMapQueue.poll(), Text.class));

            nextScreen = maps.game;

            if (startingGame) startingGame = false;

            return;
        } else if (maps.game.prepared && maps.activeMapQueue.size() == 0) {
            maps.game.mapDefinitionDescriptor = null;

            ArrayList<Score> scores = new ArrayList(Arrays.asList(maps.topScores));
            scores.add(new Score(maps.name, (int) (maps.score + maps.game.score)));

            maps.game.reset();
            maps.game.prepared = false;
            maps.game.loadingMap = false;

            Collections.sort(scores, Collections.reverseOrder());

            maps.topScores = new Score[10];

            for (int i=0; i<maps.topScores.length; i++) {
                maps.topScores[i] = scores.get(i);
            }

            maps.score = 0;
        }

        maps.batch.draw(background, -maps.CAMERA_HWIDTH, -maps.CAMERA_HHEIGHT, maps.CAMERA_WIDTH, maps.CAMERA_HEIGHT);

        maps.offsideFont.getData().setScale(1.5f);
        maps.offsideFont.draw(maps.batch, "Attack of the Blobbos", maps.CAMERA_HWIDTH * 0.35f, 0, 600, Align.center, true);

        maps.batch.end();

        name.load(delta, UIElement.START, UIElement.START);
        exit.load(delta, UIElement.END, UIElement.CENTER);
        start.load(delta, UIElement.END, UIElement.CENTER);
        // toTutorial.load(delta);
        // toSettings.load(delta);
        toLeader.load(delta, UIElement.END, UIElement.CENTER);

        maps.batch.begin();
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void show() {}
    @Override
    public void hide() { }

    @Override
    public void dispose() {
        background.dispose();
    }
}
