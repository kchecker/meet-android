package com.example.jitsiandroid;

/**
 * Created by kasumi on 11/23/17.
 */

public interface Observer {
    void onCoordinatesChanged(int startX, int startY, int endX, int endY);
}
