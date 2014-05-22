/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.itemboard.client.cell;

import com.jme.math.Vector2f;
import java.awt.Point;
import java.math.BigInteger;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardApp;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardComponent;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardWindow;
import org.jdesktop.wonderland.modules.itemboard.common.ItemboardUtils;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardCellMessage;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardCellMessage.Action;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardCellMessage.RequestStatus;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardSVGCellClientState;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Client Cell for SVG Whiteboard application.
 *
 * @author nsimpson
 * @author jbarratt
 */
public class ItemboardCell extends App2DCell
{

  private static final Logger LOGGER
    = Logger.getLogger(ItemboardCell.class.getName());
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
    "org/jdesktop/wonderland/modules/itemboard/client/resources/Bundle");
  /**
   * The (singleton) window created by the whiteboard app
   */
  private ItemboardWindow itemboardWin;
  /**
   * The cell client state message received from the server cell
   */
  private ItemboardSVGCellClientState clientState;
  /**
   * The communications component used to communicate with the server
   */
  private ItemboardComponent commComponent;
  private String myUID;
  private boolean synced = false;
  protected final Object actionLock = new Object();

  /**
   * executor to put messages onto a separate queue
   */
  private Executor msgExecutor = Executors.newSingleThreadExecutor();

  /**
   * Create an instance of WhiteboardCell.
   *
   * @param cellID The ID of the cell.
   * @param cellCache the cell cache which instantiated, and owns, this cell.
   */
  public ItemboardCell(CellID cellID, CellCache cellCache)
  {
    super(cellID, cellCache);
    myUID = cellID.toString();
  }

  /**
   * Initialize the whiteboard with parameters from the server.
   *
   * @param clientState the client state to initialize the cell with
   */
  @Override
  public void setClientState(CellClientState state)
  {
    super.setClientState(state);
    clientState = (ItemboardSVGCellClientState) state;
  }

  /**
   * This is called when the status of the cell changes.
   */
  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
    super.setStatus(status, increasing);

    switch (status)
    {
      case ACTIVE:
        if (increasing)
        {
          // The cell is now visible
          commComponent = getComponent(ItemboardComponent.class);
          ItemboardApp itemboardApp = new ItemboardApp("Itemboard", clientState.getPixelScale());
          setApp(itemboardApp);

          // this cell displays the app
          itemboardApp.addDisplayer(this);

          float placementHeight = clientState.getPreferredHeight() + 200/*TODO*/;
          placementHeight *= clientState.getPixelScale().y;
          setInitialPlacementSize(new Vector2f(0f, placementHeight));

          // This app only has one ItemboardWindow, so it is always top-level
          try
          {
            itemboardWin = new ItemboardWindow(this, itemboardApp,
              clientState.getPreferredWidth(), clientState.getPreferredHeight(),
              true, clientState.getPixelScale(), commComponent);
            itemboardApp.setWindow(itemboardWin);
          }
          catch (InstantiationException ex)
          {
            throw new RuntimeException(ex);
          }

          // Make the app window visible
          itemboardWin.setVisibleApp(true);
          itemboardWin.setVisibleUser(this, true);

          // Sync
          sync();
        }
        break;
      case DISK:
        if (!increasing)
        {
          // The cell is no longer visible
          itemboardWin.setVisibleApp(false);
          removeComponent(ItemboardComponent.class);
          App2D.invokeLater(new Runnable()
          {

            public void run()
            {
              itemboardWin.cleanup();
              commComponent = null;
              itemboardWin = null;
            }
          });
        }
        break;
      default:
        break;
    }
  }

  public String getUID()
  {
    return myUID;
  }

  /**
   * Process the actions in a whiteboard message
   *
   * @param msg a whiteboard message
   */
  public void processMessage(final ItemboardCellMessage msg)
  {
    // issue 1017: since message processing can be quite involved, do
    // it off of the Darkstar message handling thread.  The single
    // threaded executor guarantees that messages will be processed
    // in the order they are received.
    msgExecutor.execute(new Runnable()
    {
      public void run()
      {
        processQueuedMessage(msg);
      }
    });
  }

  protected void processQueuedMessage(ItemboardCellMessage msg)
  {
    String msgUID = msg.getCellID().toString();

    if (isSynced())
    {
      LOGGER.fine("itemboard: " + msgUID + " received message: " + msg);
      if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED)
      {
        // this request was denied, create a retry thread
        try
        {
          LOGGER.info("itemboard: scheduling retry of request: " + msg);
          retryRequest(msg.getAction(), msg.getXMLString(),
            msg.getURI(), msg.getPosition(), msg.getZoom());
        }
        catch (Exception e)
        {
          LOGGER.warning("itemboard: failed to create retry request for: " + msg);
        }
      }
      else
      {
        // All messages from the server act as a trigger for retrying waiting requests
        switch (msg.getAction())
        {
          case OPEN_DOCUMENT:
            ((ItemboardApp) this.getApp()).openDocument(msg.getURI(), false);
            break;
          case NEW_DOCUMENT:
            ((ItemboardApp) this.getApp()).newDocument(false);
            break;
          case ADD_ELEMENT:
            Element toAdd = ItemboardUtils.xmlStringToElement(msg.getXMLString());
            ((ItemboardApp) this.getApp()).addElement(toAdd, false);
            break;
          case REMOVE_ELEMENT:
            Element toRemove = ItemboardUtils.xmlStringToElement(msg.getXMLString());
            ((ItemboardApp) this.getApp()).removeElement(toRemove, false);
            break;
          case UPDATE_ELEMENT:
            Element toUpdate = ItemboardUtils.xmlStringToElement(msg.getXMLString());
            ((ItemboardApp) this.getApp()).updateElement(toUpdate, false);
            break;
          case SET_VIEW_POSITION:
            ((ItemboardApp) this.getApp()).setViewPosition(msg.getPosition());
            break;
          case GET_STATE:
            break;
          case SET_STATE:
            if (isSynced())
            {
              String docURI = msg.getURI();
              if (docURI != null)
              {
                // load an SVG document
                ((ItemboardApp) this.getApp()).openDocument(docURI, false);
              }
              else
              {
                // load state from SVG XML string
                SVGDocument svgDocument = (SVGDocument) ItemboardUtils.xmlStringToDocument(msg.getXMLString());
                ((ItemboardApp) this.getApp()).setDocument(svgDocument, false);
              }

              //setViewPosition(msg.getPosition());
              //setZoom(msg.getZoom(), false);
              LOGGER.info("itemboard: synced");
              itemboardWin.showHUDMessage(BUNDLE.getString("Synced"), 3000);
            }
            break;
          case SET_ZOOM:
            ((ItemboardApp) this.getApp()).setZoom(msg.getZoom(), false);
            break;
          default:
            LOGGER.warning("itemboard: unhandled message type: " + msg.getAction());
            break;
        }
        // retry queued requests
        synchronized (actionLock)
        {
          try
          {
            LOGGER.fine("itemboard: waking retry threads");
            actionLock.notify();
          }
          catch (Exception e)
          {
            LOGGER.warning("itemboard: exception notifying retry threads: " + e);
          }
        }
      }
    }
  }

  public void sync()
  {
    sync(!isSynced());
  }

  public void unsync()
  {
    sync(!isSynced());
  }

  public boolean isSynced()
  {
    return synced;
  }

  /**
   * Resynchronize the state of the cell.
   *
   * A resync is necessary when the cell transitions from INACTIVE to ACTIVE
   * cell state, where the cell may have missed state synchronization messages
   * while in the INACTIVE state.
   *
   * Resynchronization is only performed if the cell is currently synced. To
   * sync an unsynced cell, call sync(true) instead.
   */
  public void resync()
  {
    if (isSynced())
    {
      synced = false;
      sync(true);
    }
  }

  public void sync(boolean syncing)
  {
    if ((syncing == false) && (synced == true))
    {
      synced = false;
      LOGGER.info("itemboard: unsynced");
      itemboardWin.showHUDMessage(BUNDLE.getString("Unsynced"), 3000);
      //itemboardWindow.updateMenu();
    }
    else if ((syncing == true) && (synced == false))
    {
      synced = true;
      LOGGER.info("itemboard: requesting sync with shared state");
      itemboardWin.showHUDMessage(BUNDLE.getString("Syncing..."), 3000);
      //itemboardWindow.updateMenu();
      sendRequest(Action.GET_STATE, null, null, null, null);
    }
  }

  protected void sendRequest(Action action, String xmlString, String docURI,
    Point position, Float zoom)
  {

    ItemboardCellMessage msg = new ItemboardCellMessage(getClientID(), getCellID(),
      getUID(), action, xmlString, docURI, position, zoom);
    // send request to server
    LOGGER.fine("itemboard: sending request: " + msg);
    if (commComponent == null)
    {
      commComponent = getComponent(ItemboardComponent.class);
    }
    commComponent.sendMessage(msg);
  }

  /**
   * Retries a whiteboard action request
   *
   * @param action the action to retry
   * @param document the search parameters
   * @param position the image scroll position
   */
  protected void retryRequest(Action action, String xmlString, String docURI, Point position, Float zoom)
  {
    LOGGER.fine("itemboard: creating retry thread for: " + action + ", " + xmlString + ", " + position);
    new ActionScheduler(action, xmlString, docURI, position, zoom).start();
  }

  protected class ActionScheduler extends Thread
  {

    private Action action;
    private String xmlString;
    private String docURI;
    private Point position;
    private Float zoom;

    public ActionScheduler(Action action, String xmlString, String docURI, Point position, Float zoom)
    {
      this.action = action;
      this.xmlString = xmlString;
      this.docURI = docURI;
      this.position = position;
      this.zoom = zoom;
    }

    @Override
    public void run()
    {
      // wait for a retry window
      synchronized (actionLock)
      {
        try
        {
          LOGGER.fine("itemboard: waiting for retry window");
          actionLock.wait();
        }
        catch (Exception e)
        {
          LOGGER.fine("itemboard: exception waiting for retry: " + e);
        }
      }
      // retry this request
      LOGGER.info("itemboard: now retrying: " + action + ", " + xmlString + ", " + position + ", " + zoom);
      sendRequest(action, xmlString, docURI, position, zoom);
    }
  }

  /**
   * Returns the client ID of this cell's session.
   */
  public BigInteger getClientID()
  {
    return getCellCache().getSession().getID();
  }
}
