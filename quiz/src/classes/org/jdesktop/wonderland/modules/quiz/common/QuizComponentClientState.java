/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponentClientState extends CellComponentClientState
{

  private String xmlPath;

  public QuizComponentClientState()
  {
  }

  public String getXmlPath()
  {
    return xmlPath;
  }

  public void setXmlPath(String xmlPath)
  {
    this.xmlPath = xmlPath;
  }
}
