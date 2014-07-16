/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.server;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.quiz.common.Quiz;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentClientState;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponentMO extends CellComponentMO
{

  private Quiz quiz;
  private Vector3f location;
  private Quaternion look;

  public QuizComponentMO(CellMO cell)
  {
    super(cell);
  }

  @Override
  public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities)
  {
    if (state == null)
    {
      state = new QuizComponentClientState();
    }
    ((QuizComponentClientState) state).setQuiz(quiz);
    ((QuizComponentClientState) state).setLocation(location);
    ((QuizComponentClientState) state).setLook(look);

    return super.getClientState(state, clientID, capabilities);
  }

  @Override
  public CellComponentServerState getServerState(CellComponentServerState state)
  {
    if (state == null)
    {
      state = new QuizComponentServerState();
    }
    ((QuizComponentServerState) state).setQuiz(quiz);
    ((QuizComponentServerState) state).setLocation(location);
    ((QuizComponentServerState) state).setLook(look);

    return super.getServerState(state);
  }

  @Override
  public void setServerState(CellComponentServerState state)
  {
    super.setServerState(state);
    quiz = ((QuizComponentServerState) state).getQuiz();
    location = ((QuizComponentServerState) state).getLocation();
    look = ((QuizComponentServerState) state).getLook();
  }

  @Override
  protected String getClientClass()
  {
    return "org.jdesktop.wonderland.modules.quiz.client.QuizComponent";
  }

  // Do I need this?
  @Override
  protected void setLive(boolean live)
  {
    super.setLive(live);
  }
}
