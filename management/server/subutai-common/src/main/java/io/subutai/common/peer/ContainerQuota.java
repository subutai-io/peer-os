package io.subutai.common.peer;


/**
 * Container quota class
 */
public class ContainerQuota
{
    private Integer ram;
    private Integer cpu;
    private double opt;
    private double home;
    private double var;
    private double root;


    public ContainerQuota( final Integer ram, final Integer cpu, final double opt, final double home, final double var,
                           final double root )
    {
        this.ram = ram;
        this.cpu = cpu;
        this.opt = opt;
        this.home = home;
        this.var = var;
        this.root = root;
    }


    public Integer getRam()
    {
        return ram;
    }


    public Integer getCpu()
    {
        return cpu;
    }


    public double getOpt()
    {
        return opt;
    }


    public double getHome()
    {
        return home;
    }


    public double getVar()
    {
        return var;
    }


    public double getRoot()
    {
        return root;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ContainerQuota{" );
        sb.append( "ram=" ).append( ram );
        sb.append( ", cpu=" ).append( cpu );
        sb.append( ", opt=" ).append( opt );
        sb.append( ", home=" ).append( home );
        sb.append( ", var=" ).append( var );
        sb.append( ", root=" ).append( root );
        sb.append( '}' );
        return sb.toString();
    }
}
