/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client.inventory;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.item.client.Item;
import org.jdesktop.wonderland.modules.item.client.ItemComponent;
import org.jdesktop.wonderland.modules.item.client.ScavengerHuntStudent;
import org.jdesktop.wonderland.modules.item.client.StudentManager;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;

/**
 *
 * @author ASUS
 */
public class ItemListCellRenderer extends JLabel implements ListCellRenderer
{

  private ArrayList<Item> itemEntryList;

  public ItemListCellRenderer(ArrayList<Item> itemEntryList)
  {
    this.itemEntryList = itemEntryList;
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
  {
    if (!(value instanceof ItemComponent))
    {
      setText(value.toString());
      return this;
    }

    ItemComponent comp = (ItemComponent) value;

    setText(comp.getTitle());
    setOpaque(true);

    Ability[] abilities = comp.getAbilities();

    /////////////////////////////////////////
    // Is the user able to pick up this item?
    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();
    ScavengerHuntStudent me = StudentManager.loadStudentFromFile(userName);
    boolean canPickup = false;
    if (me != null)
    {
      Abilities.Ability myAbility = me.getAbility();

      if (myAbility != null && abilities != null)
      {
        canPickup = Arrays.asList(abilities).contains(myAbility);
      }
    }

    String[] owners = comp.getOwners();
    boolean otherOwners;
    if (owners.length == 0)
    {
      otherOwners = false;
    }
    else if (owners.length == 1 && owners[0].equals(userName))
    {
      otherOwners = false;
    }
    else
    {
      otherOwners = true;
    }

    boolean someoneElseWasFaster = false;
    if (comp.getOnce() && otherOwners)
    {
      someoneElseWasFaster = true;
    }

    Color color = (!someoneElseWasFaster && canPickup) ? Color.RED : Color.GRAY;

    String itemTitle = comp.getTitle();

    ///////////////////////////////////////
    // Does the user already own this item?
    boolean contains = false;
    for (Item item : itemEntryList)
    {
      if (item.getTitle().equals(itemTitle))
      {
        contains = true;
        break;
      }
    }

    if (contains)
    {
      color = Color.BLUE;
    }

    if (isSelected)
    {
      setForeground(new Color(255, 255, 255));
      setBackground(new Color(51, 153, 255));
    }
    else
    {
      setForeground(color);
      setBackground(new Color(204, 204, 204));
    }

    return this;
  }

}
