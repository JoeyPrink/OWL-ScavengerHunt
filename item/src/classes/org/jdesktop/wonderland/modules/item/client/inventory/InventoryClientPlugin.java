package org.jdesktop.wonderland.modules.item.client.inventory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Displays the user's inventory containing all items he/she picked up.
 *
 * Code based on QuickReferenceClientPlugin and LogBookPlugin by
 *
 * @author Pirmin Riedmann
 *
 * adapted by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@Plugin
public class InventoryClientPlugin extends BaseClientPlugin
{

  // For the main menu
  private JMenuItem menuItem = null;

  // The HUD Component displaying the navigation controls
  private HUDComponent hudComponent = null;
  private HUDComponent waitingHudComponent = null;

  private InventoryManager inventoryManager = null;

  public InventoryClientPlugin()
  {
    inventoryManager = new InventoryManager();
  }

  class PleaseWait implements Runnable
  {

    private boolean stop = false;

    public void setStop()
    {
      stop = true;
    }

    @Override
    public void run()
    {
      JLabel textLabel = new JLabel("Loading... Please wait...");
      JLabel aniLabel = new JLabel("/");
      aniLabel.setHorizontalAlignment(SwingConstants.CENTER);

      JPanel waitingPanel = new JPanel(new BorderLayout());
      waitingPanel.add(textLabel, BorderLayout.NORTH);
      waitingPanel.add(aniLabel, BorderLayout.CENTER);

      waitingPanel.setBorder(new MatteBorder(5, 5, 5, 5, new Color(153, 153, 153)));
      waitingPanel.setFont(new java.awt.Font("Tahoma", 1, 14));

      if (waitingHudComponent == null)
      {
        // Create the HUD Panel that displays the navigation controls
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        waitingHudComponent = mainHUD.createComponent(waitingPanel);
        waitingHudComponent.setName("Please wait");
        waitingHudComponent.setPreferredLocation(CompassLayout.Layout.CENTER);
//        waitingHudComponent.setSize(250, 250);

        mainHUD.addComponent(waitingHudComponent);
      }

      waitingHudComponent.setMaximized();
      waitingHudComponent.setVisible(true);

      while (!stop)
      {
        String s = aniLabel.getText();
        if (s.equals("/"))
        {
          aniLabel.setText("-");
        }
        else if (s.equals("-"))
        {
          aniLabel.setText("\\");
        }
        else if (s.equals("\\"))
        {
          aniLabel.setText("|");
        }
        else if (s.equals("|"))
        {
          aniLabel.setText("/");
        }

        try
        {
          Thread.sleep(250);
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(InventoryClientPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

      } // end while loop

      waitingHudComponent.setVisible(false);
    }
  }

  @Override
  public void initialize(ServerSessionManager loginInfo)
  {
    menuItem = new JCheckBoxMenuItem("Item Inventory");
    menuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (menuItem.isSelected() == true)
        {
          PleaseWait waitingRunnable = new PleaseWait();
          Thread waitingThread = new Thread(waitingRunnable);
          waitingThread.start();
          inventoryManager.load();
          waitingRunnable.setStop();

          if (hudComponent == null)
          {
            hudComponent = createHUDComponent();
          }

          hudComponent.setMaximized();
          hudComponent.setVisible(true);
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
  private HUDComponent createHUDComponent()
  {
    // Create the HUD Panel that displays the navigation controls
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

    JPanel itemPanel = inventoryManager.getItemPanel();

    JPanel inventoryPanel = new JPanel(new BorderLayout());
    inventoryPanel.add(itemPanel, BorderLayout.CENTER);
    inventoryPanel.setBorder(new MatteBorder(5, 5, 5, 5, new Color(153, 153, 153)));
    inventoryPanel.setFont(new java.awt.Font("Tahoma", 1, 14));

    hudComponent = mainHUD.createComponent(inventoryPanel);
    hudComponent.setName("Inventory");
    hudComponent.setPreferredLocation(CompassLayout.Layout.CENTER);
//    hudComponent.setSize(600, 500);

    mainHUD.addComponent(hudComponent);

    // Track when the HUD Component is closed. We need to update the state
    // of the check box menu item too. We also need to turn off the camera.
    hudComponent.addEventListener(new HUDEventListener()
    {
      public void HUDObjectChanged(HUDEvent event)
      {
        HUDEvent.HUDEventType hudEventType = event.getEventType();
        if (hudEventType == HUDEvent.HUDEventType.CLOSED
          || hudEventType == HUDEvent.HUDEventType.MINIMIZED)
        {
          menuItem.setSelected(false);
        }
        else
        {
          if (hudEventType == HUDEvent.HUDEventType.MAXIMIZED)
          {
            menuItem.setSelected(true);
          }
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
