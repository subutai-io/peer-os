package io.subutai.common.drms;


/**
 * Created by talas on 12/7/15.
 */
public class TrustMessage
{
    private String source;
    private String target;
    private String context;
    private TrustedObject trustedObject;
    private Relation relation;

    private String signature;


    public TrustMessage( final String source, final String target, final String context,
                         final TrustedObject trustedObject, final Relation relation, final String signature )
    {
        this.source = source;
        this.target = target;
        this.context = context;
        this.trustedObject = trustedObject;
        this.relation = relation;
        this.signature = signature;
    }


    public String getSource()
    {
        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
    }


    public String getTarget()
    {
        return target;
    }


    public void setTarget( final String target )
    {
        this.target = target;
    }


    public String getContext()
    {
        return context;
    }


    public void setContext( final String context )
    {
        this.context = context;
    }


    public TrustedObject getTrustedObject()
    {
        return trustedObject;
    }


    public void setTrustedObject( final TrustedObject trustedObject )
    {
        this.trustedObject = trustedObject;
    }


    public Relation getRelation()
    {
        return relation;
    }


    public void setRelation( final Relation relation )
    {
        this.relation = relation;
    }


    public String getSignature()
    {
        return signature;
    }


    public void setSignature( final String signature )
    {
        this.signature = signature;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrustMessage ) )
        {
            return false;
        }

        final TrustMessage that = ( TrustMessage ) o;

        if ( source != null ? !source.equals( that.source ) : that.source != null )
        {
            return false;
        }
        if ( target != null ? !target.equals( that.target ) : that.target != null )
        {
            return false;
        }
        if ( context != null ? !context.equals( that.context ) : that.context != null )
        {
            return false;
        }
        if ( trustedObject != null ? !trustedObject.equals( that.trustedObject ) : that.trustedObject != null )
        {
            return false;
        }
        if ( relation != null ? !relation.equals( that.relation ) : that.relation != null )
        {
            return false;
        }
        return !( signature != null ? !signature.equals( that.signature ) : that.signature != null );
    }


    @Override
    public int hashCode()
    {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + ( target != null ? target.hashCode() : 0 );
        result = 31 * result + ( context != null ? context.hashCode() : 0 );
        result = 31 * result + ( trustedObject != null ? trustedObject.hashCode() : 0 );
        result = 31 * result + ( relation != null ? relation.hashCode() : 0 );
        result = 31 * result + ( signature != null ? signature.hashCode() : 0 );
        return result;
    }


    @Override
    public String toString()
    {
        return "TrustMessage{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", context='" + context + '\'' +
                ", trustedObject=" + trustedObject +
                ", relation=" + relation +
                ", signature='" + signature + '\'' +
                '}';
    }
}
