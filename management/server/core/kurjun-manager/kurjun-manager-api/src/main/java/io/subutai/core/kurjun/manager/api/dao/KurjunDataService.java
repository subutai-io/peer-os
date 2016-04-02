package io.subutai.core.kurjun.manager.api.dao;


import java.util.List;

import io.subutai.core.kurjun.manager.api.model.Kurjun;


public interface KurjunDataService
{
    public Kurjun getKurjunData( final String url );


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


    /* *************************************************
     *
    */
    public void updateKurjunData( final String fingerprint, final String authId, final String url );


    /* *************************************************
     *
    */
    public void updateKurjunData( final String signedMessage, final String url );


    /* *************************************************
     *
    */
    public Kurjun getKurjunData( final int id );
}
