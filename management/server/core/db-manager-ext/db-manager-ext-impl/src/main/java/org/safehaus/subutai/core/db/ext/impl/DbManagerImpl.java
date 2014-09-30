package org.safehaus.subutai.core.db.ext.impl;

import org.safehaus.subutai.core.db.ext.api.*;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.safehaus.subutai.core.db.ext.api.entity.*;

public class DbManagerImpl implements DbManager
{

    private final String PERSISTENCE_UNIT_NAME = "DbManagerExt";
    private       EntityManagerFactory entFactory;
    private       EntityManager entManager;

    public short init()  throws Exception{

        try
        {
            entFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            entManager = entFactory.createEntityManager();
        }
        catch(Exception Ex)
        {

        }

        return 0;
    }
    public short destroy()  throws Exception {

        try
        {

        }
        catch(Exception Ex)
        {

        }
        return 0;
    }

    public List getDataList(String entityName) throws Exception {

        List dataList = null;

        try
        {
            Query q  =  entFactory.createQuery( "select t from "+entityName+" as t" );
            dataList =  q.getResultList();
        }
        catch(Exception Ex)
        {
            throw Ex;
        }

        return dataList;
    }

}
