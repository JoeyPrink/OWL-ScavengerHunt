/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponentClientState extends CellComponentClientState
{

  private Quiz quiz;
  private Vector3f location;
  private Quaternion look;

  public QuizComponentClientState()
  {
  }

  public QuizComponentClientState(Quiz quiz, Vector3f location, Quaternion look)
  {
    this.quiz = quiz;
    this.location = location;
    this.look = look;
  }

  public Quiz getQuiz()
  {
    return quiz;
  }

  public void setQuiz(Quiz quiz)
  {
    this.quiz = quiz;
  }

  public Vector3f getLocation()
  {
    return location;
  }

  public void setLocation(Vector3f location)
  {
    this.location = location;
  }

  public Quaternion getLook()
  {
    return look;
  }

  public void setLook(Quaternion look)
  {
    this.look = look;
  }
}
