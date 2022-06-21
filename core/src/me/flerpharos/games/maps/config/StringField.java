package me.flerpharos.games.maps.config;

public class StringField extends ConfigField<String> {

    StringField() {}

    @Override
    protected String serializeValue() {
        return '"' + getValue() + '"';
    }

    @Override
    String deserializePart(String value) {
        return value.substring(1, value.length() - 2);
    }
}
