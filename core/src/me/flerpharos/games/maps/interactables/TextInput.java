package me.flerpharos.games.maps.interactables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import me.flerpharos.games.maps.Maps;

public class TextInput extends UIElement<TextInput> {

    State state;
    final Vector2 position;
    final Vector2 size;
    final Vector2 halfSize;
    final GlyphLayout text;
    public String value;
    int length;

    final float textScale;

    public TextInput(int tabOrder, Maps m, Vector2 position, Vector2 size, String placeholder) {
        super(tabOrder, m);

        this.state = State.DEFAULT;
        this.position = position;
        this.size = size;
        this.halfSize = size.cpy().scl(0.5f);

        textScale = size.y / maps.offsideFont.getLineHeight();
        maps.offsideFont.getData().setScale(textScale);
        this.text = new GlyphLayout(m.offsideFont, placeholder);

        this.value = placeholder;
        this.length = placeholder.length();

        m.keyboardDispatch.register(event -> {
            if (state != State.ACTIVE) return false;
            if (event.name == "delete") delete();
            else add(event.target);;

            maps.offsideFont.getData().setScale(textScale);
            text.setText(m.offsideFont, value);

            if (text.width > size.x - 20) {
                delete();
                text.setText(m.offsideFont, value);
            } else {
                dispatchEvent("change");
            }
            return true;
        });
    }

    @Override
    protected TextInput provideTarget() {
        return this;
    }

    void delete() {
        if (length == 0) return;

        value = value.substring(0, length - 1);
        length --;
    }

    void add(String text) {
        value += text;
        length = value.length();
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

        maps.shapeRenderer.translate(transform.x, transform.y, 0);

        maps.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        maps.batch.setProjectionMatrix(maps.camera.projection);
        maps.batch.begin();



        /*maps.offsideFont.setColor(switch (state) {
            case SELECTED -> selectedPrimary;
            case ACTIVE -> activePrimary;
            case DEFAULT -> defaultPrimary;
        });*/

        maps.offsideFont.getData().setScale(textScale);

        maps.offsideFont.draw(maps.batch, text,
                -transform.x + 20,
                -transform.y + halfSize.y + text.height * 0.5f);

        maps.batch.end();
    }
}
