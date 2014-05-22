/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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

  @XmlElement(name = "xml-path")
  private String xmlPath = "Enter path to quiz file";

  public QuizComponentServerState()
  {
  }

  @XmlTransient
  public String getXmlPath()
  {
    return xmlPath;
  }

  public void setXmlPath(String xmlPath)
  {
    this.xmlPath = xmlPath;
  }

  @Override
  public String getServerComponentClassName()
  {
    return "org.jdesktop.wonderland.modules.quiz.server.QuizComponentMO";
  }
}
