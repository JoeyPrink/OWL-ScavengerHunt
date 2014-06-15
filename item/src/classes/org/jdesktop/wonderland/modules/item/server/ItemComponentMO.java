package org.jdesktop.wonderland.modules.item.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentClientState;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;
import org.jdesktop.wonderland.modules.item.common.ItemOwnerChangeMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponentMO extends CellComponentMO
{

  private String xmlPath;
  private String imgPath;
  private boolean once;

  private Ability[] abilities;

  private String[] owners;

  public ItemComponentMO(CellMO cell)
  {
    super(cell);
  }

  @Override
  public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities)
  {
    if (state == null)
    {
      state = new ItemComponentClientState();
    }
    ((ItemComponentClientState) state).setXmlPath(xmlPath);
    ((ItemComponentClientState) state).setImgPath(imgPath);
    ((ItemComponentClientState) state).setOnce(once);
    ((ItemComponentClientState) state).setAbilities(abilities);
    ((ItemComponentClientState) state).setOwners(owners);

    return super.getClientState(state, clientID, capabilities);
  }

  @Override
  public CellComponentServerState getServerState(CellComponentServerState state)
  {
    if (state == null)
    {
      state = new ItemComponentServerState();
    }
    ((ItemComponentServerState) state).setXmlPath(xmlPath);
    ((ItemComponentServerState) state).setImgPath(imgPath);
    ((ItemComponentServerState) state).setOnce(once);
    ((ItemComponentServerState) state).setAbilities(abilities);
    ((ItemComponentServerState) state).setOwners(owners);

    return super.getServerState(state);
  }

  @Override
  public void setServerState(CellComponentServerState state)
  {
    super.setServerState(state);
    xmlPath = ((ItemComponentServerState) state).getXmlPath();
    imgPath = ((ItemComponentServerState) state).getImgPath();
    once = ((ItemComponentServerState) state).getOnce();
    abilities = ((ItemComponentServerState) state).getAbilities();
    owners = ((ItemComponentServerState) state).getOwners();
  }

  @Override
  protected String getClientClass()
  {
    return "org.jdesktop.wonderland.modules.item.client.ItemComponent";
  }

  @Override
  protected void setLive(boolean live)
  {
    super.setLive(live);

    ChannelComponentMO channel = cellRef.get().getComponent(ChannelComponentMO.class);
    if (live == true)
    {
      channel.addMessageReceiver(ItemOwnerChangeMessage.class,
        (ChannelComponentMO.ComponentMessageReceiver) new ItemOwnerMessageReceiver(cellRef.get()));
//      System.out.println("Registered ItemOwnerMessageReceiver");
    }
    else
    {
      channel.removeMessageReceiver(ItemOwnerChangeMessage.class);
//      System.out.println("Removed ItemOwnerMessageReceiver");
    }
  }

  private static class ItemOwnerMessageReceiver extends AbstractComponentMessageReceiver
  {

    public ItemOwnerMessageReceiver(CellMO cellMO)
    {
      super(cellMO);
    }

    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message)
    {
      ItemComponentMO componentMO = (ItemComponentMO) getCell().getComponent(ItemComponentMO.class);
      ItemOwnerChangeMessage iocm = (ItemOwnerChangeMessage) message;
      componentMO.owners = iocm.getOwners();
//      System.out.println("Set owners in ItemComponentMO.");

//      getCell().sendCellMessage(clientID, message);
      componentMO.cellRef.get().sendCellMessage(clientID, message);
//      System.out.println("Sent message back to clients.");
    }
  }
}
