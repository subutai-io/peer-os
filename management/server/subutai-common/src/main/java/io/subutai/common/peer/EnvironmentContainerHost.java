package io.subutai.common.peer;


import io.subutai.common.environment.Environment;


public interface EnvironmentContainerHost extends ContainerHost
{
    void setEnvironment( Environment environment );
}
