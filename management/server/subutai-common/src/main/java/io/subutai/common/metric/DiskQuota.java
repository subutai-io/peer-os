package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


public class DiskQuota
{
    @Expose
    @JsonProperty
    private int home;
    @Expose
    @JsonProperty
    private int opt;
    @Expose
    @JsonProperty
    private int rootfs;
    @Expose
    @JsonProperty
    private int var;


    public int getHome()
    {
        return home;
    }


    public int getOpt()
    {
        return opt;
    }


    public int getRootfs()
    {
        return rootfs;
    }


    public int getVar()
    {
        return var;
    }
}