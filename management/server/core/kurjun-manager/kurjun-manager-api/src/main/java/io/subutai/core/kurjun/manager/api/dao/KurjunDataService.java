package io.subutai.core.kurjun.manager.api.dao;


import java.util.List;

import io.subutai.core.kurjun.manager.api.model.Kurjun;


public interface KurjunDataService
{
    Kurjun getKurjunData( final String url );


    /* *************************************************
     *
     */
    List<Kurjun> getAllKurjunData();


    /* *************************************************
     *
     */
    void persistKurjunData( Kurjun item );


    /* *************************************************
     *
     */
    void removeKurjunData( final String id );


    /* *************************************************
     *
     */
    void updateKurjunData( final Kurjun item );


    /* *************************************************
     *
    */
    void updateKurjunData( final String fingerprint, final String authId, final String url );


    /* *************************************************
     *
    */
    void updateKurjunData( final String signedMessage, final String url );


    /* *************************************************
     *
    */
    Kurjun getKurjunData( final int id );


    /* *************************************************
     *
    */
    void deleteKurjunData( final int id );

}
