package io.subutai.core.localpeer.impl.dao;


import java.util.List;

import io.subutai.common.exception.DaoException;


public interface NetworkResourceDao<NetworkResource, String>
{
    void create( NetworkResource t ) throws DaoException;

    NetworkResource read( String id ) throws DaoException;

    List<NetworkResource> readAll() throws DaoException;

    NetworkResource update( NetworkResource t ) throws DaoException;

    void delete( NetworkResource t ) throws DaoException;

    NetworkResource findByVni( long vni ) throws DaoException;

    NetworkResource findByP2pSubnet( String p2pSubnet ) throws DaoException;

    NetworkResource findByContainerSubnet( String containerSubnet ) throws DaoException;

    NetworkResource find( NetworkResource networkResource ) throws DaoException;
}