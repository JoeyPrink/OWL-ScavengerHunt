/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.itemboard.client;

import java.awt.Color;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;

/**
 * Class to manage the selected tool for the whiteboard module
 *
 * @author bhoran
 * @author nsimpson
 *
 * adapted and reused for the itemboard module by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemboardToolManager implements ItemboardCellMenuListener
{

  private boolean hudState = false;

  public enum ItemboardTool
  {

    NEW, SELECTOR, LINE, RECT, ELLIPSE, TEXT, FILL, DRAW, SYNC, UNSYNC, ITEM
  }

  public enum ItemboardColor
  {

    BLACK(Color.BLACK),
    WHITE(Color.WHITE),
    RED(Color.RED),
    GREEN(Color.GREEN),
    BLUE(Color.BLUE);
    private final Color c;

    ItemboardColor(Color c)
    {
      this.c = c;
    }

    Color getColor()
    {
      return c;
    }
  }
  private ItemboardColor currentColor = null;
  private ItemboardTool currentTool = null;
  private boolean filled = false;
  private ItemboardWindow itemboardWindow;

  ItemboardToolManager(ItemboardWindow itemboardWindow)
  {
    this.itemboardWindow = itemboardWindow;
    setTool(ItemboardTool.LINE);
    setColor(ItemboardColor.BLACK);
  }

  // ItemboardCellMenuListener methods
  public void newDoc()
  {
    itemboardWindow.newDocument(true);
  }

  public void openDoc()
  {
    itemboardWindow.showSVGDialog();
  }

  public void selector()
  {
    setTool(ItemboardTool.SELECTOR);
  }

  public void line()
  {
    setTool(ItemboardTool.LINE);
  }

  public void rect()
  {
    setTool(ItemboardTool.RECT);
  }

  public void ellipse()
  {
    setTool(ItemboardTool.ELLIPSE);
  }

  public void text()
  {
    setTool(ItemboardTool.TEXT);
  }

  public void item()
  {
//    System.out.println("here");
    setTool(ItemboardTool.ITEM);
  }

  public void fill()
  {
    setFilled(true);
  }

  public void draw()
  {
    setFilled(false);
  }

  public void black()
  {
    setColor(ItemboardColor.BLACK);
  }

  public void white()
  {
    setColor(ItemboardColor.WHITE);
  }

  public void red()
  {
    setColor(ItemboardColor.RED);
  }

  public void green()
  {
    setColor(ItemboardColor.GREEN);
  }

  public void blue()
  {
    setColor(ItemboardColor.BLUE);
  }

  public void zoomIn()
  {
    //zoomTo(1.1f, true);
  }

  public void zoomOut()
  {
    //zoomTo(0.9f, true);
  }

  public void sync()
  {
    hudState = !hudState;
    itemboardWindow.sync(!itemboardWindow.isSynced());
  }

  public void unsync()
  {
    hudState = !hudState;
    itemboardWindow.sync(!itemboardWindow.isSynced());
  }

  public void toggleHUD()
  {
    if (itemboardWindow.getDisplayMode().equals(DisplayMode.HUD))
    {
      itemboardWindow.setDisplayMode(DisplayMode.WORLD);
    }
    else
    {
      itemboardWindow.setDisplayMode(DisplayMode.HUD);
    }
    itemboardWindow.showControls(true);
  }

  public boolean isOnHUD()
  {
    return (itemboardWindow.getDisplayMode().equals(DisplayMode.HUD));
  }

  private void setTool(ItemboardTool newTool)
  {
    if (currentTool == newTool)
    {
      //no change
      return;
    }
    if (currentTool != null)
    {
      //Untoggle the tool
      itemboardWindow.deselectTool(currentTool);
      currentTool = null;
    }
    if (newTool != null)
    {
      //toggle the new tool
      itemboardWindow.selectTool(newTool);
      currentTool = newTool;
    }
  }

  /**
   * @return the currentTool
   */
  public ItemboardTool getTool()
  {
    return currentTool;
  }

  private void setColor(ItemboardColor newColor)
  {
    if (currentColor == newColor)
    {
      // no change
      return;
    }
    if (currentColor != null)
    {
      // untoggle the color
      itemboardWindow.deselectColor(currentColor);
      currentColor = null;
    }
    if (newColor != null)
    {
      // toggle the new tool
      itemboardWindow.selectColor(newColor);
      currentColor = newColor;
    }
  }

  /**
   * @return the currentColor
   */
  public Color getColor()
  {
    return currentColor.getColor();
  }

  public void setFilled(boolean filled)
  {
    this.filled = filled;
    itemboardWindow.updateMenu();
  }

  public boolean isFilled()
  {
    return filled;
  }
}
