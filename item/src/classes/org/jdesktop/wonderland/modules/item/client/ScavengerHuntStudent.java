/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;

/**
 * Represents one student who takes part in the Scavenger Hunt.
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
class ScavengerHuntStudent
{

  private String username;
  private Ability ability;

  public ScavengerHuntStudent(String username, Ability role)
  {
    this.username = username;
    this.ability = role;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public Ability getAbility()
  {
    return ability;
  }

  public void setAbility(Ability role)
  {
    this.ability = role;
  }

  @Override
  public String toString()
  {
    return username;
  }

}
