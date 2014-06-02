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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.modules.security.server.service.GroupMemberResource;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;
import org.jdesktop.wonderland.server.spatial.CellMOListener;
import org.jdesktop.wonderland.server.spatial.UniverseManager;

/**
 * Server plugin for StudentManager component. Asks for every avatar cell if the
 * user is part of the admin group and, if yes, adds the StudentManagerComponent
 * to the avatar cell.
 *
 * Idea based on admin tools by
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 *
 * adapted by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@Plugin
public class StudentManagerServerPlugin
  implements ServerPlugin, CellMOListener, Serializable
{

  private static final Logger LOGGER
    = Logger.getLogger(StudentManagerServerPlugin.class.getName());

  @Override
  public void initialize()
  {
    AppContext.getManager(UniverseManager.class).addCellListener(this);
  }

  @Override
  public void cellAdded(CellMO cell)
  {
    if (cell instanceof AvatarCellMO)
    {
      ResourceMap rm = new ResourceMap();
      GroupMemberResource gmr = new GroupMemberResource("admin");
      rm.put(gmr.getId(), new ActionMap(gmr, new ViewAction()));

      SecurityManager sm = AppContext.getManager(SecurityManager.class);
      sm.doSecure(rm, new AddAdminToolsTask(cell));
    }
  }

  @Override
  public void cellRemoved(CellMO cell)
  {
  }

  private static class AddAdminToolsTask implements SecureTask, Serializable
  {

    private final ManagedReference<CellMO> cellRef;

    public AddAdminToolsTask(CellMO cell)
    {
      cellRef = AppContext.getDataManager().createReference(cell);
    }

    @Override
    public void run(ResourceMap granted)
    {
      ActionMap am = granted.values().iterator().next();
      if (am.isEmpty())
      {
        return;
      }

      CellMO cell = cellRef.get();
      cell.addComponent(new StudentManagerComponentMO(cell));

      LOGGER.log(Level.WARNING, "StudentManagerComponent added to {0}", cell);
    }
  }
}
