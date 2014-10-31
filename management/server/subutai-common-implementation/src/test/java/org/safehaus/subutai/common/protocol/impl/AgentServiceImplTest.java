package org.safehaus.subutai.common.protocol.impl;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AgentServiceImplTest
{

    EntityManagerFactory emf;
    EntityManager em;
    EntityTransaction tx;


    @Before
    public void setUp() throws Exception
    {
        emf = Persistence.createEntityManagerFactory( "default" );
        em = emf.createEntityManager();
        tx = em.getTransaction();
    }


    @After
    public void teardown()
    {
        em.close();
        emf.close();
    }


    @Test
    public void testGetEntityManagerFactory() throws Exception
    {

    }


    @Test
    public void testSetEntityManagerFactory() throws Exception
    {

    }


    @Test
    public void testCreateAgent() throws Exception
    {

    }


    @Test
    public void testGetAgent() throws Exception
    {

    }


    @Test
    public void testGetAgents() throws Exception
    {

    }


    @Test
    public void testDeleteAgent() throws Exception
    {

    }
}