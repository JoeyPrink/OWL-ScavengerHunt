/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentClientState;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponent extends CellComponent implements ContextMenuActionListener
{

  @UsesCellComponent
  private ContextMenuComponent contextMenu;
  private final ContextMenuFactorySPI menuFactory;
  private String menuItemText = "quizz";
  private String xmlPath;

  public QuizComponent(Cell cell)
  {
    super(cell);

    // TODO: Start Quiz when User clicks on Object (not via Menu Item)
    final ContextMenuItem item = new SimpleContextMenuItem(menuItemText, this);
    menuFactory = new ContextMenuFactorySPI()
    {
      @Override
      public ContextMenuItem[] getContextMenuItems(ContextEvent event)
      {
        return new ContextMenuItem[]
        {
          item
        };
      }
    };
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
    if (status == CellStatus.ACTIVE && increasing)
    {
      contextMenu.addContextMenuFactory(menuFactory);
    }
    else
    {
      if (status == CellStatus.INACTIVE && !increasing)
      {
        contextMenu.removeContextMenuFactory(menuFactory);
      }
    }

    super.setStatus(status, increasing);
  }

  @Override
  public void setClientState(CellComponentClientState clientState)
  {
    super.setClientState(clientState);
    xmlPath = ((QuizComponentClientState) clientState).getXmlPath();
  }

  @Override
  public void actionPerformed(ContextMenuItemEvent event)
  {
    if (event.getContextMenuItem().getLabel().equals(menuItemText))
    {
      getXMLFromServer();
      displayQuiz();
    }
  }

  public void getXMLFromServer()
  {
    // TODO
  }

  public void displayQuiz()
  {
    // TODO
  }
}
