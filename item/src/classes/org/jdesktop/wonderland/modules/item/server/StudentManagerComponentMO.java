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
package org.jdesktop.wonderland.modules.item.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.item.common.StudentManagerComponentClientState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.NoSnapshot;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Server side cell component for admin tools
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@NoSnapshot
public class StudentManagerComponentMO extends CellComponentMO
{

  private static final Logger LOGGER
    = Logger.getLogger(StudentManagerComponentMO.class.getName());

//  private final ManagedReference<CellMO> cellRef;
  public StudentManagerComponentMO(CellMO cellMO)
  {
    super(cellMO);

//    cellRef = AppContext.getDataManager().createReference(cellMO);
  }

  @Override
  protected String getClientClass()
  {
    return "org.jdesktop.wonderland.modules.item.client.StudentManagerComponent";
  }

  @Override
  public void setLive(boolean live)
  {
    super.setLive(live);
  }

  @Override
  public CellComponentClientState getClientState(CellComponentClientState state,
    WonderlandClientID clientID,
    ClientCapabilities capabilities)
  {
    if (state == null)
    {
      state = new StudentManagerComponentClientState();
    }

    return super.getClientState(state, clientID, capabilities);
  }

//  CellMO getCell()
//  {
//    return cellRef.get();
//  }
}
