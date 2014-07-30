/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import org.jdesktop.wonderland.modules.item.common.ScavengerHuntStudent;

/**
 *
 * @author ASUS
 */
public interface UserObserver
{

  public abstract void userAbilitesChanged(ScavengerHuntStudent user);
}
