package me.flerpharos.games.maps.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.Maps;
import me.flerpharos.games.maps.interactables.Button;
import me.flerpharos.games.maps.util.Score;

public class Leaderboard implements Screen {
    Maps maps;

    Button back;

    boolean preload;
    public boolean prepared;

    Screen nextScreen;

    GlyphLayout titleText;
    float width;

    public Leaderboard(Maps m) {
        maps = m;

        preload = false;
        prepared = false;
    }

    public boolean prepare() {
        if (!preload) {
            preload = true;

            return false;
        } else {

            maps.offsideFont.getData().setScale(1.5f);
            titleText = new GlyphLayout(maps.offsideFont, "Leaderboard");
            width = titleText.width * 0.5f;

            back = new Button(0, maps, new Vector2(-maps.CAMERA_HWIDTH + 10, -maps.CAMERA_HHEIGHT + 10), new Vector2(300, 100), "Back");

            back.register(event -> {
                nextScreen = maps.menu;
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

        maps.batch.end();

        back.load(delta, Button.START, Button.START);

        maps.batch.begin();

        maps.offsideFont.draw(maps.batch, titleText, -width, maps.CAMERA_HHEIGHT * 0.7f);

        int i=0;
        for (Score score : maps.topScores) {
            maps.offsideFont.draw(maps.batch, score.toString(), -300, maps.CAMERA_HHEIGHT * 0.5f - 80 * i++);
        }


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
    public void hide() {}

    @Override
    public void dispose() {

    }
}
