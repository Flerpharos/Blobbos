package me.flerpharos.games.maps.interactables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.Maps;

public class Button extends UIElement<Button> {

    State state;
    final Vector2 position;
    final Vector2 size;
    final Vector2 halfSize;
    final GlyphLayout text;

    final float textScale;

    public Button(int tabOrder, Maps m, Vector2 position, Vector2 size, String text) {
        super(tabOrder, m);

        this.state = State.DEFAULT;
        this.position = position;
        this.size = size;
        this.halfSize = size.cpy().scl(0.5f);

        maps.offsideFont.getData().setScale(1);

        textScale = size.y / maps.offsideFont.getLineHeight();
        maps.offsideFont.getData().setScale(textScale);
        this.text = new GlyphLayout(m.offsideFont, text);
    }

    @Override
    protected Button provideTarget() {
        return this;
    }

    @Override
    void update(float delta, byte alignX, byte alignY) {

        Vector2 transform = halfSize.cpy().scl(alignX, alignY).sub(position);

        State prev = state;

        if (maps.mousePos.x > -transform.x
            && maps.mousePos.x < -transform.x + size.x
            && maps.mousePos.y > -transform.y
            && maps.mousePos.y < -transform.y + size.y
        ) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) state = State.ACTIVE;
            else state = State.SELECTED;
        } else state = State.DEFAULT;

        if (prev != state && prev == State.ACTIVE) dispatchEvent("click");
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

        maps.shapeRenderer.translate(transform.x, transform.y, 0);

        maps.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        maps.batch.setProjectionMatrix(maps.camera.projection);
        maps.batch.begin();

        maps.offsideFont.getData().setScale(textScale);

        /*maps.offsideFont.setColor(switch (state) {
            case SELECTED -> selectedPrimary;
            case ACTIVE -> activePrimary;
            case DEFAULT -> defaultPrimary;
        });*/

        maps.offsideFont.draw(maps.batch, text,
                -transform.x + halfSize.x - text.width * 0.5f,
                -transform.y + halfSize.y + text.height * 0.5f);

        maps.batch.end();

    }
}
