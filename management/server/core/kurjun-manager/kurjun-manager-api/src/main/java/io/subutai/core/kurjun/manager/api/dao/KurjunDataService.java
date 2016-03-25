package io.subutai.core.kurjun.manager.api.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Lists;

import io.subutai.core.kurjun.manager.api.model.Kurjun;


public interface KurjunDataService
{
    public Kurjun getKurjunData( final String id );


    /* *************************************************
     *
     */
    public List<Kurjun> getAllKurjunData();


    /* *************************************************
     *
     */
    public void persistKurjunData( Kurjun item );


    /* *************************************************
     *
     */
    public void removeKurjunData( final String id );


    /* *************************************************
     *
     */
    public void updateKurjunData( final Kurjun item );

}
