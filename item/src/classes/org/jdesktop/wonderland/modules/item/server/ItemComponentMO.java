package org.jdesktop.wonderland.modules.item.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentClientState;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponentMO extends CellComponentMO
{

  private String xmlPath;
  private String imgPath;

  private Ability[] abilities;

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
    ((ItemComponentClientState) state).setAbilities(abilities);

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
    ((ItemComponentServerState) state).setAbilities(abilities);

    return super.getServerState(state);
  }

  @Override
  public void setServerState(CellComponentServerState state)
  {
    super.setServerState(state);
    xmlPath = ((ItemComponentServerState) state).getXmlPath();
    imgPath = ((ItemComponentServerState) state).getImgPath();
    abilities = ((ItemComponentServerState) state).getAbilities();
  }

  @Override
  protected String getClientClass()
  {
    return "org.jdesktop.wonderland.modules.item.client.ItemComponent";
  }

  // Do I need this?
  @Override
  protected void setLive(boolean live)
  {
    super.setLive(live);
  }
}
