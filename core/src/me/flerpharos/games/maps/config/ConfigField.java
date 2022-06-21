package me.flerpharos.games.maps.config;

abstract class ConfigField<T> {

    private T value;
    private String name;

    ConfigField() {}

    public T getValue() {
        return value;
    }

    public void setValue(T t) {
        value = t;
    }

    protected String serializeValue() {
        return getValue().toString();
    }

    final String serialize() {
        return value.getClass().getSimpleName() + " " + name + ": " + serializeValue();
    }

    final boolean deserialize(String line) {
        String[] args = line.split(": ", 2);

        String name = args[0].strip();
        T value = deserializePart(args[1].strip());

        if (value == null) return false;

        this.name = name;
        this.value = value;

        return true;
    }

    abstract T deserializePart(String value);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigField<?> that = (ConfigField<?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    static ConfigField<?> ofClass(String s, String line) throws LoadConfigException {
        ConfigField<?> field = switch (s) {
            case "Boolean" -> new BooleanField();
            case "String" -> new StringField();
            case "Integer" -> new IntField();
            default -> throw new LoadConfigException("Invalid field type " + s);
        };

        if (!field.deserialize(line)) throw new LoadConfigException("Invalid value for field type " + s);

        return field;
    }
}
