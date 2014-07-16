/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Lisa Tomes
 */
@XmlRootElement(name = "quiz-component")
@ServerState
public class QuizComponentServerState extends CellComponentServerState
{

  @XmlElement(name = "quiz")
  private Quiz quiz;
  @XmlElement(name = "location")
  private Vector3f location;
  @XmlElement(name = "look")
  private Quaternion look;

  public QuizComponentServerState()
  {
  }

  public QuizComponentServerState(Quiz quiz, Vector3f location, Quaternion look)
  {
    this.quiz = quiz;
    this.location = location;
    this.look = look;
  }

  @Override
  public String getServerComponentClassName()
  {
    return "org.jdesktop.wonderland.modules.quiz.server.QuizComponentMO";
  }

  @XmlElement
  public Quiz getQuiz()
  {
    return quiz;
  }

  public void setQuiz(Quiz quiz)
  {
    this.quiz = quiz;
  }

  @XmlElement
//  @XmlJavaTypeAdapter(Vector3fAdapter.class)
  public Vector3f getLocation()
  {
    return location;
  }

  public void setLocation(Vector3f location)
  {
    this.location = location;
  }

  @XmlElement
//  @XmlJavaTypeAdapter(QuaternionAdapter.class)
  public Quaternion getLook()
  {
    return look;
  }

  public void setLook(Quaternion look)
  {
    this.look = look;
  }
}
