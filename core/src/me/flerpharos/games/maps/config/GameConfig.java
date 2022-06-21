package me.flerpharos.games.maps.config;

import java.util.HashSet;

public class GameConfig {

    final public HashSet<ConfigField<?>> fields;

    public GameConfig() {
        fields = new HashSet<>();
    }

    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();

        for (ConfigField<?> value : fields) {
            stringBuilder.append(value.serialize());
            stringBuilder.append("\n");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }

    public void deserialize(String input) throws LoadConfigException {
        for(String line : input.split("\n")) {
            if (line.strip().equals("")) continue;
            if (line.startsWith("#")) continue;

            String[] partA = line.split(" ", 2);

            fields.add(ConfigField.ofClass(partA[0].strip(), partA[1]));
        }
    }
}
