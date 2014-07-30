package org.jdesktop.wonderland.modules.item.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.HoverEvent;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Displays item info text when user hovers over object.
 *
 * Code based on TooltipCellComponent by
 *
 * @author Jordan Slott <jslott@dev.java.net>
 *
 * adapted by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@Plugin
public class ItemComponentClientPlugin extends BaseClientPlugin
{

  private InfoHoverListener listener = null;
  private HUDComponent hudComponent = null;

  @Override
  public void initialize(ServerSessionManager sessionManager)
  {
    listener = new InfoHoverListener();
    super.initialize(sessionManager);
  }

  @Override
  protected void activate()
  {
    SceneManager.getSceneManager().addSceneListener(listener);
  }

  @Override
  protected void deactivate()
  {
    SceneManager.getSceneManager().removeSceneListener(listener);

    // Remove the component from the HUD.
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    mainHUD.removeComponent(hudComponent);
    hudComponent = null;
  }

  /**
   * Listner for the Scene Manager hover event
   */
  private class InfoHoverListener extends EventClassListener
  {

    @Override
    public Class[] eventClassesToConsume()
    {
      return new Class[]
      {
        HoverEvent.class
      };
    }

    @Override
    public void commitEvent(Event event)
    {
      HoverEvent hoverEvent = (HoverEvent) event;
      Cell cell = hoverEvent.getPrimaryCell();

      if (cell == null || hoverEvent.isStart() == false)
      {
        if (hudComponent != null)
        {
          hideInfoHUDComponent();
        }
        return;
      }

      ItemComponent comp = cell.getComponent(ItemComponent.class);
      if (comp == null)
      {
        if (hudComponent != null)
        {
          hideInfoHUDComponent();
        }
        return;
      }

      Canvas canvas = JmeClientMain.getFrame().getCanvas();
      MouseEvent mouseEvent = hoverEvent.getMouseEvent();
      Point location = mouseEvent.getPoint();
      location.y = canvas.getHeight() - location.y;
      String text = "<h1>" + comp.getTitle() + "</h1>" + comp.getDescription();
      showInfoHUDComponent(text, location, canvas);
    }

    private void hideInfoHUDComponent()
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
          hudComponent.setVisible(false);
          hud.removeComponent(hudComponent);
          hudComponent = null;
        }
      });
    }

    private void showInfoHUDComponent(final String infoText,
      final Point point, final Canvas canvas)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
          if (hudComponent != null)
          {
            hudComponent.setVisible(false);
            hud.removeComponent(hudComponent);
            hudComponent = null;
          }

          int maxWidth = canvas.getWidth() / 2;
          int maxHeight = canvas.getHeight() / 4;

          JTextPane text = new JTextPane();
          text.setPreferredSize(new Dimension(maxWidth, maxHeight));
          text.setContentType("text/html");
          text.setEditable(false);
          text.setText(infoText);

          JScrollPane scroll = new JScrollPane();
          //scroll.setPreferredSize(new Dimension(canvas.getWidth() / 2,
          //  canvas.getHeight() / 4));
          scroll.setViewportView(text);
          scroll.setBorder(null);

          JPanel infoPanel = new JPanel(new BorderLayout());
          //infoPanel.setPreferredSize(new Dimension(canvas.getWidth() / 2,
          //  canvas.getHeight() / 4));
          infoPanel.add(scroll, BorderLayout.CENTER);
          infoPanel.setBorder(new MatteBorder(5, 5, 5, 5, new Color(153, 153, 153)));
          infoPanel.setFont(new java.awt.Font("Tahoma", 1, 14));

          hudComponent = hud.createComponent(infoPanel);
          hudComponent.setName("Object info");
          hudComponent.setDecoratable(false);
          hudComponent.setVisible(false);
          hudComponent.setLocation(point);
          hud.addComponent(hudComponent);

          hudComponent.setVisible(true);
        }
      });
    }
  }
}
