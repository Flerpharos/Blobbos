package me.flerpharos.games.maps.interactables;

public class Event<K> {

    public final K target;
    final String name;

    Event(String name, K target) {
        this.target = target;
        this.name = name;
    }
}
