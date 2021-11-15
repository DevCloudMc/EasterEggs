package ru.brainrtp.eastereggs.providers;


import ru.brainrtp.eastereggs.handler.PlaceholderHandler;

public class HandlersProvider {

    private static PlaceholderHandler placeholderHandler;

    public static PlaceholderHandler getPlaceholderHandler() {
        return placeholderHandler;
    }

    public static void setPlaceholderHandler(PlaceholderHandler handler) {
        placeholderHandler = handler;
    }
}
