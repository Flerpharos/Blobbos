package me.flerpharos.games.maps.interactables;

import com.badlogic.gdx.graphics.Color;
import me.flerpharos.games.maps.Maps;

public abstract class UIElement<K> extends EventDispatcher<K> implements ITabOrderProvider {

    public static final byte START = 0;
    public static final byte END = 2;
    public static final byte CENTER = 1;

    public enum State {
        SELECTED, ACTIVE, DEFAULT
    }

    protected static final Color activeStroke = Color.valueOf("#ffffc8");
    protected static final Color activeBg = Color.valueOf("#413C5C");
    protected static final Color activePrimary = Color.valueOf("#ff000099");

    protected static final Color selectedStroke = Color.valueOf("#ffffff5");
    protected static final Color selectedBg = Color.valueOf("#413C5C");
    protected static final Color selectedPrimary = Color.valueOf("#ffa02feb");

    protected static final Color defaultPrimary = Color.valueOf("#20DFDF");
    protected static final Color defaultBg = Color.valueOf("#413C5C");
    protected static final Color defaultStroke = Color.valueOf("#806020DF");

    private final int tabOrder;
    protected final Maps maps;

    protected UIElement(int tabOrder, Maps m) {
        this.tabOrder = tabOrder;
        maps = m;
    }

    @Override
    public final int priority() {
        return tabOrder;
    }

    abstract void update(float delta, byte alignX, byte alignY);
    abstract void render(float delta, byte alignX, byte alignY);

    public final void load(float delta, byte alignX, byte alignY) {
        update(delta, alignX, alignY);
        render(delta, alignX, alignY);
    }

    public final void load(float delta, byte alignX) {
        load(delta, alignX, CENTER);
    }

    public final void load(float delta) {
        load(delta, CENTER);
    }

}
