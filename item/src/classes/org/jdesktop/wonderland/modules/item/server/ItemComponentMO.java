package org.jdesktop.wonderland.modules.item.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentClientState;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;
import org.jdesktop.wonderland.modules.item.common.ItemOwnerChangeMessage;
import org.jdesktop.wonderland.modules.item.common.UserAbilityChangeMessage;
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

  private String itemTitle;
  private String itemDescription;
  private String imgPath;
  private Ability[] abilities;
  private boolean once;

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
    ((ItemComponentClientState) state).setTitle(itemTitle);
    ((ItemComponentClientState) state).setDescription(itemDescription);
    ((ItemComponentClientState) state).setImgPath(imgPath);
    ((ItemComponentClientState) state).setAbilities(abilities);
    ((ItemComponentClientState) state).setOnce(once);
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
    ((ItemComponentServerState) state).setTitle(itemTitle);
    ((ItemComponentServerState) state).setDescription(itemDescription);
    ((ItemComponentServerState) state).setImgPath(imgPath);
    ((ItemComponentServerState) state).setAbilities(abilities);
    ((ItemComponentServerState) state).setOnce(once);
    ((ItemComponentServerState) state).setOwners(owners);

    return super.getServerState(state);
  }

  @Override
  public void setServerState(CellComponentServerState state)
  {
    super.setServerState(state);
    itemTitle = ((ItemComponentServerState) state).getTitle();
    itemDescription = ((ItemComponentServerState) state).getDescription();
    imgPath = ((ItemComponentServerState) state).getImgPath();
    abilities = ((ItemComponentServerState) state).getAbilities();
    once = ((ItemComponentServerState) state).getOnce();
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
      channel.addMessageReceiver(UserAbilityChangeMessage.class,
        (ChannelComponentMO.ComponentMessageReceiver) new UserAbilityMessageReceiver(cellRef.get()));
    }
    else
    {
      channel.removeMessageReceiver(ItemOwnerChangeMessage.class);
      channel.removeMessageReceiver(UserAbilityChangeMessage.class);
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

      componentMO.cellRef.get().sendCellMessage(clientID, message);
    }
  }

  private static class UserAbilityMessageReceiver extends AbstractComponentMessageReceiver
  {

    public UserAbilityMessageReceiver(CellMO cellMO)
    {
      super(cellMO);
    }

    @Override
    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message)
    {
      ItemComponentMO componentMO = (ItemComponentMO) getCell().getComponent(ItemComponentMO.class);
//      UserAbilityChangeMessage uacm = (UserAbilityChangeMessage) message;

      componentMO.cellRef.get().sendCellMessage(clientID, message);
    }
  }
}
