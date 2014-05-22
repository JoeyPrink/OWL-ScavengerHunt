/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentServerState;

/**
 *
 * @author Lisa Tomes
 */
@CellComponentFactory
public class QuizComponentFactory implements CellComponentFactorySPI
{

  @Override
  @SuppressWarnings("unchecked")
  public <T extends CellComponentServerState> T getDefaultCellComponentServerState()
  {
    QuizComponentServerState state = new QuizComponentServerState();
    return (T) state;
  }

  @Override
  public String getDisplayName()
  {
    return "Quiz";
  }

  @Override
  public String getDescription()
  {
    return "Add a Quiz to an object";
  }
}
