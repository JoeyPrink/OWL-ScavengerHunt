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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardToolManager.ItemboardTool;

/**
 * Simple class that implements KeyListener, originally implemented for the
 * whiteboard module by
 *
 * @author bhoran
 *
 * adapted and reused for the itemboard module by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemboardKeyListener implements KeyListener
{

  private static final int DELTA = 10;
  private ItemboardWindow itemboardWindow;

  ItemboardKeyListener(ItemboardWindow itemboardWindow)
  {
    this.itemboardWindow = itemboardWindow;
  }

  /**
   * Process a key press event
   *
   * @param evt the key press event
   */
  public void keyPressed(KeyEvent evt)
  {
    //svgCanvas.dispatchEvent(evt);
  }

  /**
   * Process a key release event
   *
   * @param evt the key release event
   */
  public void keyReleased(KeyEvent evt)
  {
    ItemboardTool currentTool = itemboardWindow.getCurrentTool();
    ItemboardSelection selection = itemboardWindow.getSelection();
    switch (evt.getKeyCode())
    {
      case KeyEvent.VK_BACK_SPACE:
        if (currentTool == ItemboardTool.SELECTOR && selection != null)
        {
          itemboardWindow.removeElement(selection.getSelectedElement(), true);
        }
        break;
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_KP_LEFT:
        if (currentTool == ItemboardTool.SELECTOR && selection != null)
        {
          moveSelection(selection, 1 - DELTA, 0);
        }
        break;
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_KP_RIGHT:
        if (currentTool == ItemboardTool.SELECTOR && selection != null)
        {
          moveSelection(selection, DELTA, 0);
        }
        break;
      case KeyEvent.VK_UP:
      case KeyEvent.VK_KP_UP:
        if (currentTool == ItemboardTool.SELECTOR && selection != null)
        {
          moveSelection(selection, 0, DELTA);
        }
        break;
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_KP_DOWN:
        if (currentTool == ItemboardTool.SELECTOR && selection != null)
        {
          moveSelection(selection, 0, DELTA);
        }
        break;
      default:
        break;
    }
    //svgCanvas.dispatchEvent(evt);
  }

  private void moveSelection(ItemboardSelection selection, int deltaX, int deltaY)
  {
    itemboardWindow.updateElement(itemboardWindow.moveElement(selection.getSelectedElement(), deltaX, deltaY), true);
  }

  /**
   * Process a key typed event
   *
   * @param evt the key release event
   */
  public void keyTyped(KeyEvent evt)
  {
    //svgCanvas.dispatchEvent(evt);
  }
}
