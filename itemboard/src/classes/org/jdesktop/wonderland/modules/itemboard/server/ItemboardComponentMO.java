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
package org.jdesktop.wonderland.modules.itemboard.server;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardCellMessage;
import org.jdesktop.wonderland.modules.itemboard.server.cell.ItemboardCellMO;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 * The server side of the communication component that provides communication
 * between the whiteboard client and server. Requires ChannelComponent to also
 * be connected to the cell prior to construction.
 *
 * @author deronj
 *
 * adapted an reused for the itemboard module by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@ExperimentalAPI
public class ItemboardComponentMO extends CellComponentMO
{

  /**
   * A managed reference to the cell channel communications component
   */
  @UsesCellComponentMO(ChannelComponentMO.class)
  private ManagedReference<ChannelComponentMO> channelComponentRef = null;

  /**
   * Create a new instance of WhiteboardComponentMO.
   *
   * @param cell The cell to which this component belongs.
   * @throws IllegalStateException If the cell does not already have a
   * ChannelComponent IllegalStateException will be thrown.
   */
  public ItemboardComponentMO(CellMO cell)
  {
    super(cell);
  }

  @Override
  public void setLive(boolean isLive)
  {
    super.setLive(isLive);

    if (isLive)
    {
      channelComponentRef.getForUpdate().addMessageReceiver(ItemboardCellMessage.class,
        new ItemboardComponentMOMessageReceiver(cellRef.get()));
    }
    else
    {
      channelComponentRef.getForUpdate().removeMessageReceiver(ItemboardCellMessage.class);
    }
  }

  /**
   * Broadcast the given message to all clients.
   *
   * @param sourceID the originator of this message, or null if it originated
   * with the server
   * @param message The message to broadcast.
   */
  public void sendAllClients(WonderlandClientID clientID, ItemboardCellMessage message)
  {
    ChannelComponentMO channelComponent = channelComponentRef.getForUpdate();
    channelComponent.sendAll(clientID, message);
  }

  @Override
  protected String getClientClass()
  {
    return "org.jdesktop.wonderland.modules.itemboard.client.ItemboardComponent";
  }

  /**
   * Receiver for for whiteboard messages. Note: inner classes of managed
   * objects must be non-static. Benefits from event recorder mechanism by
   * extending AbstractComponentMessageReceiver
   */
  private static class ItemboardComponentMOMessageReceiver extends AbstractComponentMessageReceiver
  {

    public ItemboardComponentMOMessageReceiver(CellMO cellMO)
    {
      super(cellMO);
    }

    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message)
    {
      ItemboardCellMessage cmsg = (ItemboardCellMessage) message;
      ((ItemboardCellMO) getCell()).receivedMessage(sender, clientID, cmsg);
    }

    @Override
    protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message)
    {
      ItemboardCellMessage cmsg = (ItemboardCellMessage) message;
      RecorderManager.getDefaultManager().recordMetadata(cmsg, cmsg.getXMLString());
      UserMO user = UserManager.getUserManager().getUser(clientID);
      RecorderManager.getDefaultManager().recordMetadata(cmsg, "Created by " + user.getUsername() + "[" + user.getIdentity().getFullName() + "]");
    }
  }
}
