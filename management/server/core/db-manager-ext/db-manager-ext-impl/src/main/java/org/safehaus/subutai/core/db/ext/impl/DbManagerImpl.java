package org.safehaus.subutai.core.db.ext.impl;

import org.safehaus.subutai.core.db.ext.api.*;
import org.safehaus.subutai.core.db.ext.api.entity.*;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DbManagerImpl implements DbManager{


    private EntityManagerFactory entFactory;
    private EntityManager        entManager;
    private Logger LOG = LoggerFactory.getLogger( DbManagerImpl.class.getName() );

    /**
    * Initializes db manager *******************************************************
    */
    public void init()  throws Exception{

        try
        {
            final String PERSISTENCE_UNIT_NAME = "DbManagerExt";
            entFactory = Persistence.createEntityManagerFactory( PERSISTENCE_UNIT_NAME );
            entManager = entFactory.createEntityManager();

            LOG.info( "DbManager Started:Persistence Factory initialized." );

        }
        catch(Exception ex)
        {
            LOG.error( "DbManager,Error in init:", ex );
            throw ex;
        }

    }
    /**
    * Destroy db manager *******************************************************
    */
    public void destroy()  throws Exception {

        try
        {

            if(entManager!=null)
            {
                entManager.close();
            }
            if(entFactory!=null)
            {
                entFactory.close();
            }

            LOG.info( "DbManager Stopped." );

        }
        catch(Exception ex)
        {
            LOG.error( "DbManager,Error in destroy:", ex );
            throw ex;
        }

    }
    /**
    * *******************************************************
    */
    public List getDataList(String entityName) throws Exception {

        List dataList;

        try
        {
            Query q  =  entManager.createQuery( "select t from " + entityName + " as t" );
            dataList  =  q.getResultList();
        }
        catch(Exception ex)
        {
            LOG.error( "DbManager,Error in getDataList:", ex );
            throw ex;
        }

        return dataList;
    }

    /**
    * *******************************************************
    */
    public <T>Object  getData(Class<T> objClass,Object primaryKey) throws Exception {

        Object obj;

        try
        {
            obj =  entManager.find(objClass,primaryKey );
        }
        catch(Exception ex)
        {
            LOG.error( "DbManager,Error in getData:", ex );
            throw ex;
        }

        return obj;
    }
    /**
    * *******************************************************
    */
    public void insertData(Object newObject) throws Exception {

        try
        {
            if(!entManager.getTransaction().isActive())
            {
                entManager.getTransaction().begin();
            }
            entManager.persist(newObject);
            entManager.getTransaction().commit();

        }
        catch(Exception ex)
        {
            entManager.getTransaction().rollback();
            LOG.error( "DbManager,Error in insertData:", ex );
            throw ex;
        }
    }
    /**
    * *******************************************************
    */
    public void removeData(Object delObject) throws Exception {

        try
        {
            if(!entManager.getTransaction().isActive())
            {
                entManager.getTransaction().begin();
            }
            entManager.remove(delObject);
            entManager.getTransaction().commit();

        }
        catch(Exception ex)
        {
            entManager.getTransaction().rollback();
            LOG.error( "DbManager,Error in removeData:", ex );
            throw ex;
        }
    }
    /**
     * *******************************************************
     */

    public void startUpdateMode() throws Exception {

        try
        {
            if(!entManager.getTransaction().isActive())
            {
                entManager.getTransaction().begin();
            }

        }
        catch(Exception ex)
        {
            entManager.getTransaction().rollback();
            LOG.error( "DbManager,Error in startUpdateMode:", ex );
            throw ex;
        }
    }
    /**
     * *******************************************************
     */
    public void commitData() throws Exception {

        try
        {
            if(entManager.getTransaction().isActive())
            {
                entManager.getTransaction().commit();
            }

        }
        catch(Exception ex)
        {
            entManager.getTransaction().rollback();
            LOG.error( "DbManager,Error in updateData:", ex );
            throw ex;
        }
    }
    /**
    * *******************************************************
    */

    public static void main(String[] args) throws Exception {

        /*
        DbManagerImpl DbMan = new DbManagerImpl();

        DbMan.init();

        List dataList = DbMan.getDataList( "CommandResponse" );
        String PrimaryKey = "";

        for (CommandRequest temp : dataList)
        {
             System.out.println(temp.getCommandId());
             PrimaryKey = temp.getCommandId();
        }

        CommandResponseId  commandResponseId = new CommandResponseId() ;
        commandResponseId.setAgentId( "44e842e0-1aee-4e05-bbf9-ba24088dbcad" );
        commandResponseId.setCommandId( "b27ec411-1e24-4886-b1e2-d1f20b268dda" );
        commandResponseId.setResponseNumber( 1 );

        CommandResponse  CmdR = (CommandResponse)DbMan.getData(CommandResponse.class,commandResponseId );
        CmdR.setResponseNumber( 1000 );

        //CommandResponse  CmdR  = entManager.find(CommandResponse.class,commandResponseId );

        System.out.println("--------------------------");
        System.out.println(CmdR.getCommandId() );

        CommandResponse  commandResponse = new CommandResponse() ;
        commandResponse.setAgentId( "44e842e0-1aee-4e05-bbf9-ba24088dbcadXXY" );
        commandResponse.setCommandId( "b27ec411-1e24-4886-b1e2-d1f20b268ddaXXY" );
        commandResponse.setResponseNumber( 111 );

        DbMan.insertData(commandResponse);


        commandResponseId.setAgentId( "44e842e0-1aee-4e05-bbf9-ba24088dbcad" );
        commandResponseId.setCommandId( "b27ec411-1e24-4886-b1e2-d1f20b268dda" );
        commandResponseId.setResponseNumber( 1000 );

        CmdR = (CommandResponse)DbMan.getData(CommandResponse.class,commandResponseId );
        System.out.println(CmdR.getResponseNumber() );

        DbMan.destroy();
        */
    }
}
