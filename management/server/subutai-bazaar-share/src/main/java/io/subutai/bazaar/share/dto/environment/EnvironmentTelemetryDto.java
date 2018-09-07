package io.subutai.bazaar.share.dto.environment;


import java.util.ArrayList;
import java.util.List;


public class EnvironmentTelemetryDto
{
    private List<EnvironmentTelemetryOperation> operations = new ArrayList<>();


    public List<EnvironmentTelemetryOperation> getOperations()
    {
        return operations;
    }


    public void setOperations( final List<EnvironmentTelemetryOperation> operations )
    {
        this.operations = operations;
    }
}
