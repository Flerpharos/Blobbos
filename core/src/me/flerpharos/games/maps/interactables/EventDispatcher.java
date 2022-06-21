package me.flerpharos.games.maps.interactables;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class EventDispatcher<K> {

    ArrayList<EventListener<K>> listeners;

    public EventDispatcher() {
        listeners = new ArrayList<>();
    }

    public void register(EventListener<K> listener) {
        this.listeners.add(listener);
    }

    public void dispatchEvent(String name) {
        Iterator<EventListener<K>> iter = listeners.iterator();

        Event<K> event = new Event(name, provideTarget());

        while (iter.hasNext()) {
            EventListener<K> listener = iter.next();

            if (listener.alert(event)) break;
        }
    }

    protected abstract K provideTarget();
}
