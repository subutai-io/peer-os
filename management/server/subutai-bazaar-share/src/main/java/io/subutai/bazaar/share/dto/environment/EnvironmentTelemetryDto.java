package io.subutai.bazaar.share.dto.environment;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
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
