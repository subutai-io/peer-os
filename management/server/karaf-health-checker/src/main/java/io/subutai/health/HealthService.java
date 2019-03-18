package io.subutai.health;


public interface HealthService
{
    int BUNDLE_COUNT = 280;


    enum State
    {
        LOADING, FAILED, READY
    }

    State getState();
}
