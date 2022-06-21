package me.flerpharos.games.maps.config;

public class IntField extends ConfigField<Integer> {

    IntField() {}

    @Override
    Integer deserializePart(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException unused) {
            return null;
        }
    }
}
