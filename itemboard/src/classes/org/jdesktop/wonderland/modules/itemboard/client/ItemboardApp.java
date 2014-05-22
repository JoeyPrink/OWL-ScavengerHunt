/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
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

import com.jme.math.Vector2f;
import java.awt.Point;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb.ControlChangeListener;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * A 2D SVG whiteboard application
 *
 * @author paulby
 * @author deronj
 * @author nsimpson
 */
@ExperimentalAPI
public class ItemboardApp extends App2D implements ControlChangeListener
{

  /**
   * The single itemboardWindow created by this app
   */
  private ItemboardWindow itemboardWindow;

  /**
   * Create a new instance of WhiteboardApp. This in turn creates and makes
   * visible the single itemboardWindow used by the app.
   *
   * @param appType The type of app (should be WhiteboardAppType).
   * @param width The width (in pixels) of the whiteboard itemboardWindow.
   * @param height The height (in pixels) of the whiteboard itemboardWindow.
   * @param pixelScale The horizontal and vertical pixel sizes (in world meters
   * per pixel).
   */
  public ItemboardApp(String name, Vector2f pixelScale)
  {
    super(name, new ControlArbMulti()
    {
      @Override
      protected void updateKeyFocus(boolean hasControl)
      {
        // calling super.updateKeyFocus() prevents the main window
        // from getting focus. We need the main window to have focus
        // for our key listener to work.
      }
    }, pixelScale);
    controlArb.setApp(this);
    controlArb.addListener(this);
  }

  /**
   * Load an SVG document
   *
   * @param uri the URI of the SVG document to load
   * @param notify whether to notify other clients
   */
  public void openDocument(String uri, boolean notify)
  {
    itemboardWindow.openDocument(uri, notify);
  }

  /**
   * Create a new SVG document
   *
   * @param notify
   */
  public void newDocument(boolean notify)
  {
    itemboardWindow.newDocument(notify);
  }

  /**
   * Set the SVG document
   *
   * @param document the SVG document XML
   * @param notify whether to notify other clients
   */
  public void setDocument(SVGDocument document, boolean notify)
  {
    itemboardWindow.setDocument(document, notify);
  }

  /**
   * Import an Element into an SVG document
   *
   * @param e the element to import
   * @param notify whether to notify other clients
   */
  public Element importElement(final Element e, boolean notify)
  {
    return (Element) itemboardWindow.importElement(e, notify);
  }

  /**
   * Add an Element to an SVG document
   *
   * @param e the element to add
   * @param notify whether to notify other clients
   */
  public void addElement(Element e, boolean notify)
  {
    Element adding = importElement(e, notify);
    if (adding != null)
    {
      itemboardWindow.addElement(adding, notify);
    }
  }

  /**
   * Add a new Element to an SVG document
   *
   * @param e the element to add
   * @param notify whether to notify other clients
   */
  public void addNewElement(Element e, boolean notify)
  {
    itemboardWindow.addNewElement(e, notify);
  }

  /**
   * Remove an Element from an SVG document
   *
   * @param e the element to remove
   * @param notify whether to notify other clients
   */
  public void removeElement(Element e, boolean notify)
  {
    Element removing = importElement(e, notify);
    if (removing != null)
    {
      itemboardWindow.removeElement(removing, notify);
    }
  }

  /**
   * Update an Element in an SVG document
   *
   * @param e the element to update
   * @param notify whether to notify other clients
   */
  public void updateElement(Element e, boolean notify)
  {
    Element updating = importElement(e, notify);
    if (updating != null)
    {
      itemboardWindow.updateElement(updating, notify);
    }
  }

  /**
   * Set the view position
   *
   * @param position the desired position
   */
  public void setViewPosition(Point position)
  {
    itemboardWindow.setViewPosition(position);
  }

  /**
   * Set the zoom
   *
   * @param zoom the zoom factor
   * @param notify whether to notify other clients
   */
  public void setZoom(Float zoom, boolean notify)
  {
    itemboardWindow.setZoom(zoom, notify);
  }

  /**
   * Clean up resources.
   */
  @Override
  public void cleanup()
  {
    super.cleanup();
    if (itemboardWindow != null)
    {
      itemboardWindow.cleanup();
      itemboardWindow = null;
    }
  }

  /**
   * Set the app's window
   *
   * @param itemboardWindow the window
   */
  public void setWindow(ItemboardWindow itemboardWindow)
  {
    this.itemboardWindow = itemboardWindow;
  }

  /**
   * Returns the app's itemboardWindow.
   */
  public ItemboardWindow getWindow()
  {
    return itemboardWindow;
  }

  /**
   * Repaint the canvas
   */
  public void repaintCanvas()
  {
    itemboardWindow.repaintCanvas();
  }

  /**
   * The state of a control arb you are subscribed to may have changed. The
   * state of whether this user has control or the current set of controlling
   * users may have changed.
   *
   * @param controlArb The control arb that changed.
   */
  public void updateControl(ControlArb controlArb)
  {
    itemboardWindow.showControls(controlArb.hasControl());
  }
}
