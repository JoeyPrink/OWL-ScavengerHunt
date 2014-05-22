/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.common;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class Abilities
{

  public enum Ability
  {

    ADVENTURER, SCIENTIST, PRIEST, HISTORIAN, UNKNOWN
  }

  public static String getStringFromAbility(Ability ability)
  {
    switch (ability)
    {
      case ADVENTURER:
        return "Adventurer";
      case SCIENTIST:
        return "Scientist";
      case PRIEST:
        return "Priest";
      case HISTORIAN:
        return "Historian";
      default:
        return "";
    }
  }

  public static int getIntFromAbility(Ability ability)
  {
    switch (ability)
    {
      case ADVENTURER:
        return 0;
      case SCIENTIST:
        return 1;
      case PRIEST:
        return 2;
      case HISTORIAN:
        return 3;
      default:
        return -1;
    }
  }

  public static Ability getAbilityFromInt(int number)
  {
    switch (number)
    {
      case 0:
        return Ability.ADVENTURER;
      case 1:
        return Ability.SCIENTIST;
      case 2:
        return Ability.PRIEST;
      case 3:
        return Ability.HISTORIAN;
      default:
        return Ability.UNKNOWN;
    }
  }

}
