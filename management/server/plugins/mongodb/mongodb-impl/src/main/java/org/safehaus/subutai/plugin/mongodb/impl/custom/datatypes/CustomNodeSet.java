package org.safehaus.subutai.plugin.mongodb.impl.custom.datatypes;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by talas on 12/17/14.
 */
public abstract class CustomNodeSet<E> extends HashSet<E> implements Set<E>, Collection<E>
{
    @Override
    public boolean add( E node )
    {
        return super.add( node ) && addNode( node );
    }


    @Override
    public boolean remove( Object node )
    {
        return super.remove( node ) && removeNode( node );
    }


    @Override
    public boolean addAll( Collection<? extends E> c )
    {
        return super.addAll( c );
    }


    @Override
    public void clear()
    {
        clearNode();
        super.clear();
    }


    @Override
    public boolean removeAll( Collection<?> c )
    {
        return super.removeAll( c ) && removeAllNode( c );
    }


    @Override
    public boolean retainAll( Collection<?> c )
    {
        return super.retainAll( c ) && retainAllNode( c );
    }


    protected abstract boolean addNode( E node );


    protected abstract boolean removeNode( Object node );


    protected abstract boolean addAllNode( Collection<? extends E> c );


    protected abstract void clearNode();


    protected abstract boolean removeAllNode( final Collection<?> c );


    protected abstract boolean retainAllNode( final Collection<?> c );
}
