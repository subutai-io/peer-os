package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * HDD resource class
 */
public class DiskResource extends Resource<ByteValueResource>
{
    @JsonProperty( "model" )
    private String model;                   // model
    @JsonProperty( "readSpeed" )
    private double readSpeed = 0;           // disk read speed
    @JsonProperty( "writeSpeed" )
    private double writeSpeed = 0;          // disk write speed
    @JsonProperty( "raid" )
    private boolean raid = false;           // is RAID?


    public DiskResource( @JsonProperty( "value" ) final BigDecimal availableSpace,
                         @JsonProperty( "cost" ) final Double cost, @JsonProperty( "model" ) final String model,
                         @JsonProperty( "readSpeed" ) final double readSpeed,
                         @JsonProperty( "writeSpeed" ) final double writeSpeed,
                         @JsonProperty( "raid" ) final boolean raid )
    {
        super( new ByteValueResource( availableSpace ), ResourceType.DISK, cost );
        this.model = model;
        this.readSpeed = readSpeed;
        this.writeSpeed = writeSpeed;
        this.raid = raid;
    }


    public String getModel()
    {
        return model;
    }


    public double getReadSpeed()
    {
        return readSpeed;
    }


    public double getWriteSpeed()
    {
        return writeSpeed;
    }


    public boolean isRaid()
    {
        return raid;
    }


    /**
     * Usually used to write value to CLI
     */
    @JsonIgnore
    @Override
    public String getWriteValue()
    {
        BigDecimal v = resourceValue.convert( ByteUnit.MB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%s", resourceValue.convert( ByteUnit.MB ), ByteUnit.MB.getAcronym() );
    }
}
