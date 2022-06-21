package me.flerpharos.games.maps.interactables;

@FunctionalInterface
public interface EventListener<K>  {

    boolean alert(Event<K> event);
}
