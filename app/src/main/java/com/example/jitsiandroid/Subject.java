package com.example.jitsiandroid;

/**
 * Created by kasumi on 11/23/17.
 */

public interface Subject {
    void registerObserver(Observer coordinatesObserver);
    void removeObserver(Observer coordinatesObserver);
    void notifyObservers();
}