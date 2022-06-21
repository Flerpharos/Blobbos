package me.flerpharos.games.maps.entities;

public interface IRenderOrderProvider {

    float getRenderOrder();
    void render(float delta);
}
