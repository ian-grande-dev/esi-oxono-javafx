package be.esi.dev.oxono.observer;

public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    // void notifyObservers(); : Notifications launched only from the model
}
