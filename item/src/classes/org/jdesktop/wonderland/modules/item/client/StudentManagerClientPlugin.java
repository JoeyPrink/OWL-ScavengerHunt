/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.item.common.Abilities;

/**
 * Adds a menu item to the Tools menu which shows the StudentManagerPanel in a
 * HUD when clicked (only allowed for admin users).
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@Plugin
public class StudentManagerClientPlugin extends BaseClientPlugin
{

  // For the main menu
  private JMenuItem menuItem = null;
  private final String whoami = "Student Manager";

  // The HUD Component displaying the navigation controls
  private HUDComponent hudComponent = null;

  // whether or not we are an admin
  private boolean admin = false;
  private boolean adminChecked = false;

  private StudentManager studentManager = null;

  public StudentManagerClientPlugin()
  {
    studentManager = new StudentManager();
  }

  @Override
  public void initialize(ServerSessionManager loginInfo)
  {
    menuItem = new JCheckBoxMenuItem("Student Manager");
    menuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (menuItem.isSelected() == true)
        {
          Cell us = ViewManager.getViewManager().getPrimaryViewCell();

          // do we need to check if we are an administrator?
          synchronized (StudentManagerClientPlugin.this)
          {
            if (!adminChecked)
            {
              admin = (us.getComponent(StudentManagerComponent.class) != null);
            }
          }

          // only enable the menu for administrators
          if (admin)
          {
            studentManager.loadStudents();
            StudentManagerPanel smPanel = new StudentManagerPanel();
            smPanel.doWork(studentManager);

            if (hudComponent == null)
            {
              hudComponent = createHUDComponent(smPanel);
            }

            hudComponent.setMaximized();
            hudComponent.setVisible(true);
          }
          else
          {
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            String userName = session.getUserID().getUsername();
            ScavengerHuntStudent student = StudentManager.loadStudentFromFile(userName);
            if (student != null)
            {
              Abilities.Ability userAbility = student.getAbility();

              if (userAbility != null)
              {
                String userAbilityString = Abilities.getStringFromAbility(userAbility);
                JOptionPane.showMessageDialog(null, "Your role is: "
                  + userAbilityString, "Info", JOptionPane.INFORMATION_MESSAGE);
              }
              else
              {
                JOptionPane.showMessageDialog(null, "Sorry, an error occured. "
                  + "It seems that you have no abilities.", "Error", JOptionPane.ERROR_MESSAGE);
              }
            }
            else
            {
              JOptionPane.showMessageDialog(null, "Sorry, an error occured. "
                + "It seems that you are no student.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            menuItem.setSelected(false);
          }
        }
        else
        {
          if (hudComponent != null)
          {
            hudComponent.setVisible(false);
          }
        }
      }
    });

    super.initialize(loginInfo);
  }

  public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell)
  {
    // recheck if we are an administrator next time a menu is displayed
    synchronized (this)
    {
      adminChecked = false;
    }
  }

  @Override
  protected void activate()
  {
    JmeClientMain.getFrame().addToToolsMenu(menuItem);
    menuItem.setSelected(false);
  }

  @Override
  protected void deactivate()
  {
    // If there is a HUD Component, then make it invisible
    if (hudComponent != null)
    {
      hudComponent.setVisible(false);
    }

    // Remove the menu item
    JmeClientMain.getFrame().removeFromToolsMenu(menuItem);
  }

  /**
   * Creates and returns the top map HUD component.
   */
  private HUDComponent createHUDComponent(StudentManagerPanel smPanel)
  {
    // Create the HUD Panel that displays the navigation controls
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

    hudComponent = mainHUD.createComponent(smPanel);
    hudComponent.setName(whoami);
    hudComponent.setPreferredLocation(CompassLayout.Layout.CENTER);
    hudComponent.setSize(500, 500);

    mainHUD.addComponent(hudComponent);

    // Track when the HUD Component is closed. We need to update the state
    // of the check box menu item too. We also need to turn off the camera.
    hudComponent.addEventListener(new HUDEventListener()
    {
      @Override
      public void HUDObjectChanged(HUDEvent event)
      {
        HUDEvent.HUDEventType hudEventType = event.getEventType();
        if (hudEventType == HUDEvent.HUDEventType.CLOSED
          || hudEventType == HUDEvent.HUDEventType.MINIMIZED)
        {
          menuItem.setSelected(false);
        }
        else if (hudEventType == HUDEvent.HUDEventType.MAXIMIZED)
        {
          menuItem.setSelected(true);
        }
      }
    });

    return hudComponent;
  }

  @Override
  public void cleanup()
  {
    // If there is a HUD Component, then remove it from the HUD and clean
    // it up. This really should happen when the primary view cell is
    // disconnected, but we do this here just in case.
    if (hudComponent != null)
    {
      HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
      mainHUD.removeComponent(hudComponent);
    }
    super.cleanup();
  }
}
