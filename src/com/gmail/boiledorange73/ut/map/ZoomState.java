package com.gmail.boiledorange73.ut.map;

public class ZoomState {
    int minzoom;
    int maxzoom;
    int currentzoom;

    public ZoomState(int minzoom, int maxzoom, int currentzoom) {
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.currentzoom = currentzoom;
    }

    public ZoomState() {
        this(0, 0, 0);
    }
}
