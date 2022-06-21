package me.flerpharos.games.maps.interactables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.Maps;

public class Slider extends UIElement<Slider> {

    State state;
    final Vector2 position;
    final Vector2 size;
    final Vector2 halfSize;
    final int min, max, step;

    final float invSizeX;

    int value;

    public Slider(int tabOrder, Maps m, Vector2 position, float width, int min, int max, int step) {
        super(tabOrder, m);

        this.state = State.DEFAULT;
        this.position = position;
        this.size = new Vector2(width, 20);
        this.halfSize = size.cpy().scl(0.5f);
        this.invSizeX = 1 / size.x;

        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    protected Slider provideTarget() {
        return this;
    }

    @Override
    void update(float delta, byte alignX, byte alignY) {

        Vector2 transform = halfSize.cpy().scl(alignX, alignY).sub(position);

        if (state == State.ACTIVE) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)
                    && (maps.mousePos.x < -transform.x
                    || maps.mousePos.x > -transform.x + size.x
                    || maps.mousePos.y < -transform.y
                    || maps.mousePos.y > -transform.y + size.y)
            ) {
                state = State.DEFAULT;
                dispatchEvent("change");
            } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                value = MathUtils.round((maps.mousePos.x + transform.x) * invSizeX * (max - min)) + min;
            }
        } else if (maps.mousePos.x > -transform.x
                && maps.mousePos.x < -transform.x + size.x
                && maps.mousePos.y > -transform.y
                && maps.mousePos.y < -transform.y + size.y
        ) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) state = State.ACTIVE;
            else state = State.SELECTED;
        } else state = State.DEFAULT;
    }

    @Override
    void render(float delta, byte alignX, byte alignY) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        maps.shapeRenderer.setProjectionMatrix(maps.camera.projection);
        maps.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Vector2 transform = halfSize.cpy().scl(alignX, alignY).sub(position);
        maps.shapeRenderer.translate(-transform.x, -transform.y, 0);

        maps.shapeRenderer.setColor(switch (state) {
            case SELECTED -> selectedBg;
            case ACTIVE -> activeBg;
            case DEFAULT -> defaultBg;
        });

        maps.shapeRenderer.rect(0, 0, size.x, size.y);

        maps.shapeRenderer.setColor(switch (state) {
            case SELECTED -> selectedStroke;
            case ACTIVE -> activeStroke;
            case DEFAULT -> defaultStroke;
        });

        maps.shapeRenderer.rectLine(0, 0, 0, size.y, 3);
        maps.shapeRenderer.rectLine(size.x, 0, size.x, size.y, 3);
        maps.shapeRenderer.rectLine(0, 0, size.x, 0, 3);
        maps.shapeRenderer.rectLine(0, size.y, size.x, size.y, 3);

        maps.shapeRenderer.setColor(switch (state) {
            case SELECTED -> selectedPrimary;
            case ACTIVE -> activePrimary;
            case DEFAULT -> defaultPrimary;
        });

        maps.shapeRenderer.rect(0, 0, ((float) value) / (max - min) * size.x, size.y);

        maps.shapeRenderer.translate(transform.x, transform.y, 0);

        maps.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        maps.batch.begin();


    }
}
