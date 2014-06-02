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
package org.jdesktop.wonderland.modules.item.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * StudentManager cell component. Gets added by a server plugin to avatars of
 * users who have admin rights. When a user wants to open the panel to manage
 * students the presence of this component can be checked in order to determine
 * if the panel should be opened.
 *
 * Idea based on admin tools by
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 *
 * adapted by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class StudentManagerComponent extends CellComponent
{

  private static final Logger LOGGER
    = Logger.getLogger(StudentManagerComponent.class.getName());

  // invisibility effect
//  private Node invisibleNode;
//  private ProcessorComponent invisibleProcessor;
  public StudentManagerComponent(Cell cell)
  {
    super(cell);
  }

  @Override
  public void setClientState(CellComponentClientState clientState)
  {
    super.setClientState(clientState);
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
    super.setStatus(status, increasing);
  }

//  private Node loadInvisibleEffect()
//  {
//    try
//    {
//      URL modelURL = AssetUtils.getAssetURL("wla://admin-tools/tube.kmz/tube.kmz.dep");
//
//      // create a node
//      LoaderManager lm = LoaderManager.getLoaderManager();
//      DeployedModel dm = lm.getLoaderFromDeployment(modelURL);
//      return dm.getModelLoader().loadDeployedModel(dm, null);
//    }
//    catch (IOException ex)
//    {
//      LOGGER.log(Level.WARNING, "URL error loading effect", ex);
//      return null;
//    }
//  }
//  private void attachNode(final Node node)
//  {
//    SceneWorker.addWorker(new WorkCommit()
//    {
//      public void commit()
//      {
//        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//        if (renderer instanceof AvatarImiJME)
//        {
//          Node root = ((AvatarImiJME) renderer).getAvatarCharacter().getJScene().getExternalKidsRoot();
//          root.attachChild(node);
//
//          node.setLocalTranslation(new Vector3f(0f, 1f, 0f));
//          node.setLightCombineMode(LightCombineMode.Off);
//          ClientContextJME.getWorldManager().addToUpdateList(node);
//        }
//      }
//    });
//  }
//  private void detachNode(final Node node)
//  {
//    SceneWorker.addWorker(new WorkCommit()
//    {
//      public void commit()
//      {
//        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//        if (renderer instanceof AvatarImiJME)
//        {
//          Node root = ((AvatarImiJME) renderer).getAvatarCharacter().getJScene().getExternalKidsRoot();
//          root.detachChild(node);
//        }
//      }
//    });
//  }
//  private void addProcessor(ProcessorComponent processor)
//  {
//    CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//    if (renderer instanceof AvatarImiJME)
//    {
//      Entity e = ((AvatarImiJME) renderer).getAvatarCharacter();
//      ProcessorCollectionComponent pcc
//        = e.getComponent(ProcessorCollectionComponent.class);
//      if (pcc == null)
//      {
//        pcc = new ProcessorCollectionComponent();
//        e.addComponent(pcc.getClass(), pcc);
//      }
//
//      pcc.addProcessor(processor);
//    }
//  }
//  private void removeProcessor(ProcessorComponent processor)
//  {
//    CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//    if (renderer instanceof AvatarImiJME)
//    {
//      Entity e = ((AvatarImiJME) renderer).getAvatarCharacter();
//      ProcessorCollectionComponent pcc
//        = e.getComponent(ProcessorCollectionComponent.class);
//      if (pcc != null)
//      {
//        pcc.removeProcessor(processor);
//      }
//    }
//  }
//  static class InvisibleProcessorComponent extends ProcessorComponent
//  {
//
//    // radians per millisecond
//    private static final float RATE = (float) Math.PI / 10000f;
//
//    private final Node node;
//    private float rotation = 0f;
//    private long lastTime = System.currentTimeMillis();
//
//    public InvisibleProcessorComponent(Node node)
//    {
//      this.node = node;
//    }
//
//    @Override
//    public void initialize()
//    {
//      setArmingCondition(new NewFrameCondition(this));
//    }
//
//    @Override
//    public void compute(ProcessorArmingCollection pac)
//    {
//    }
//
//    @Override
//    public void commit(ProcessorArmingCollection pac)
//    {
//      long now = System.currentTimeMillis();
//      long dTime = now - lastTime;
//
//      rotation += RATE * dTime;
//      rotation %= 2 * Math.PI;
//
//      Quaternion q = new Quaternion();
//      q.fromAngleAxis(rotation, new Vector3f(0, 1, 0));
//      node.setLocalRotation(q);
//      ClientContextJME.getWorldManager().addToUpdateList(node);
//
//      lastTime = now;
//    }
//  }
}
