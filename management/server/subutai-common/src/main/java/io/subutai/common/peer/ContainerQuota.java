package io.subutai.common.peer;


/**
 * Container quota class
 */
public class ContainerQuota
{
    private Integer ram;
    private Integer cpu;
    private Integer opt;
    private Integer home;
    private Integer var;
    private Integer root;


    public ContainerQuota( final Integer ram, final Integer cpu, final Integer opt, final Integer home, final Integer var,
                           final Integer root )
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


    public Integer getOpt()
    {
        return opt;
    }


    public Integer getHome()
    {
        return home;
    }


    public Integer getVar()
    {
        return var;
    }


    public Integer getRoot()
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
