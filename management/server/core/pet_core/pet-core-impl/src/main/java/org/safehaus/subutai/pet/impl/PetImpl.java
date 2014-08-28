package org.safehaus.subutai.pet.impl;


import org.safehaus.subutai.pet.api.PetManager;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PetImpl implements PetManager {
    @Override
    public String getPetName() {
        return "hello kitty!";
    }
}
