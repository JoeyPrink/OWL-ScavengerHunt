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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardToolManager.ItemboardTool;
import org.w3c.dom.Element;

/**
 * Class to implement mouse listener interfaces.<br>
 * Used to control object creation and movement
 *
 * @author bhoran
 */
public class ItemboardMouseListener implements MouseListener,
  MouseMotionListener, MouseWheelListener
{

  private static final Logger logger
    = Logger.getLogger(ItemboardMouseListener.class.getName());
  private ItemboardWindow itemboardWindow;
  private ItemboardDocument itemboardDocument;
  private Point currentPoint;
  private boolean drawing;
  private boolean moving;
  private Point pressedPoint;

  ItemboardMouseListener(ItemboardWindow itemboardApp, ItemboardDocument itemboardDocument)
  {
    this.itemboardWindow = itemboardApp;
    this.itemboardDocument = itemboardDocument;
  }

  public void mouseClicked(MouseEvent e)
  {
    logger.finest("mouse clicked: " + e.getPoint());
  }

  public void mouseEntered(MouseEvent e)
  {
    logger.finest("mouse entered: " + e.getPoint());
  }

  public void mouseExited(MouseEvent e)
  {
    logger.finest("mouse exited: " + e.getPoint());

    currentPoint = null;
    drawing = false;
    moving = false;
  }

  public void mousePressed(MouseEvent e)
  {
    logger.finest("mouse pressed: " + e.getPoint());

    pressedPoint = new Point(e.getPoint());

    ItemboardTool currentTool = itemboardWindow.getCurrentTool();
    if (currentTool == ItemboardTool.SELECTOR)
    {
      itemboardWindow.singleSelection(pressedPoint);
    }
    else
    {
      drawing = true;
      itemboardWindow.drawingMarker(pressedPoint);
    }
  }

  public void mouseReleased(MouseEvent e)
  {
    logger.finest("mouse released: " + e.getPoint());

    Point releasedPoint = e.getPoint();

    ItemboardTool currentTool = itemboardWindow.getCurrentTool();
    // Check if drawing is true to avoid adding elements when the mouse was not dragged
    if (drawing)
    {
      if (currentTool == ItemboardTool.SELECTOR)
      {
        throw new RuntimeException("itemboard: wrong tool selected");
      }
      Element newElement = itemboardDocument.createElement(currentTool, pressedPoint, releasedPoint);
      if (newElement != null)
      {
        itemboardWindow.addNewElement(newElement, true);
      }
    }
    if (currentTool == ItemboardTool.SELECTOR)
    {
      if (moving)
      {
        itemboardWindow.updateElement(itemboardDocument.moveElement(itemboardWindow.getSelection().getSelectedElement()), true);
        itemboardWindow.setSelection(null);
      }
    }
    currentPoint = null;
    moving = false;
    drawing = false;
  }

  public void mouseDragged(MouseEvent e)
  {
    logger.finest("mouse dragged: " + e.getPoint());

    ItemboardTool currentTool = itemboardWindow.getCurrentTool();
    if (currentTool == ItemboardTool.SELECTOR)
    {
      if (itemboardWindow.getSelection() != null)
      {
        moving = true;
        itemboardWindow.movingMarker(e);
      }
    }
    else
    {
      itemboardWindow.drawingMarker(e.getPoint());
    }
  }

  public void mouseMoved(MouseEvent e)
  {
    logger.finest("mouse moved: " + e.getPoint());
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    if (e.getWheelRotation() < 0)
    {
      //zoomOut();
    }
    else
    {
      //zoomIn();
    }
  }

  /**
   * @return the currentPoint
   */
  public Point getCurrentPoint()
  {
    return currentPoint;
  }

  /**
   * @param currentPoint the currentPoint to set
   */
  public void setCurrentPoint(Point currentPoint)
  {
    this.currentPoint = currentPoint;
  }

  /**
   * @return the pressedPoint
   */
  public Point getPressedPoint()
  {
    return pressedPoint;
  }
}
