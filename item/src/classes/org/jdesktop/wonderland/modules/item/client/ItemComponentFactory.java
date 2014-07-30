package org.jdesktop.wonderland.modules.item.client;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@CellComponentFactory
public class ItemComponentFactory implements CellComponentFactorySPI
{

  @Override
  public String getDisplayName()
  {
    return "Itemize!";
    //return "Attach Info";
  }

  @Override
  public String getDescription()
  {
    return "Store an information text connected with this object.";
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends CellComponentServerState> T getDefaultCellComponentServerState()
  {
    ItemComponentServerState state = new ItemComponentServerState();
    return (T) state;
  }
}
