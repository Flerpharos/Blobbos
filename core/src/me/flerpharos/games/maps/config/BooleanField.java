package me.flerpharos.games.maps.config;

public class BooleanField extends ConfigField<Boolean> {

    BooleanField() {}

    @Override
    public Boolean deserializePart(String value) {
        return Boolean.valueOf(value);
    }
}
