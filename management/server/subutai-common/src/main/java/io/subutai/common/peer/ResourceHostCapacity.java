package io.subutai.common.peer;


public class ResourceHostCapacity
{
    private double ram;
    private double disk;
    private double cpu;


    public ResourceHostCapacity( final double ram, final double disk, final double cpu )
    {
        this.ram = ram;
        this.disk = disk;
        this.cpu = cpu;
    }


    public double getRam()
    {
        return ram;
    }


    public double getDisk()
    {
        return disk;
    }


    public double getCpu()
    {
        return cpu;
    }


    public void setRam( final double ram )
    {
        this.ram = ram;
    }


    public void setDisk( final double disk )
    {
        this.disk = disk;
    }


    public void setCpu( final double cpu )
    {
        this.cpu = cpu;
    }


    @Override
    public String toString()
    {
        return "ResourceHostCapacity{" + "ram=" + ram + ", disk=" + disk + ", cpu=" + cpu + '}';
    }
}
