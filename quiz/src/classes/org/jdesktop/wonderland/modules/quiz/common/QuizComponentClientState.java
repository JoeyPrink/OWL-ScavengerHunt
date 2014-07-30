/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.utils.jaxb.QuaternionAdapter;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;

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
  @XmlJavaTypeAdapter(Vector3fAdapter.class)
  public Vector3f getLocation()
  {
    return location;
  }

  public void setLocation(Vector3f location)
  {
    this.location = location;
  }

  @XmlElement
  @XmlJavaTypeAdapter(QuaternionAdapter.class)
  public Quaternion getLook()
  {
    return look;
  }

  public void setLook(Quaternion look)
  {
    this.look = look;
  }
}
