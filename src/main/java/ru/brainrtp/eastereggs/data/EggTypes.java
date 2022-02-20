package ru.brainrtp.eastereggs.data;

public enum EggTypes {

    BLOCK("BLOCK"),
    ENTITY("ENTITY");

    String type;

    EggTypes(String type) {
        this.type = type;
    }

    public String getStringType() {
        return type;
    }
}
