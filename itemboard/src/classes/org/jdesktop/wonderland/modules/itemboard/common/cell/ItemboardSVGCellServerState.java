/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.itemboard.common.cell;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for WhiteboardCellMO.
 *
 * @author nsimpson
 *
 * adapted an reused for the itemboard module by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@XmlRootElement(name = "itemboard-cell-svg")
@ServerState
public class ItemboardSVGCellServerState extends App2DCellServerState implements Serializable
{

  /**
   * The user's preferred width of the whiteboard window.
   */
  @XmlElement(name = "preferredWidth")
  public int preferredWidth = 800;
  /**
   * The user's preferred height of the whiteboard window.
   */
  @XmlElement(name = "preferredHeight")
  public int preferredHeight = 600;
  /**
   * The SVG XML to display
   */
  @XmlElement(name = "svgDocumentXML")
  public String svgDocumentXML = "";
  /**
   * The SVG document to display
   */
  @XmlElement(name = "svgDocumentURI")
  public String svgDocumentURI = "";

  /**
   * Default constructor
   */
  public ItemboardSVGCellServerState()
  {
  }

  public String getServerClassName()
  {
    return "org.jdesktop.wonderland.modules.itemboard.server.cell.ItemboardCellMO";
  }

  public void setPreferredWidth(int preferredWidth)
  {
    this.preferredWidth = preferredWidth;
  }

  @XmlTransient
  public int getPreferredWidth()
  {
    return preferredWidth;
  }

  public void setPreferredHeight(int preferredHeight)
  {
    this.preferredHeight = preferredHeight;
  }

  @XmlTransient
  public int getPreferredHeight()
  {
    return preferredHeight;
  }

  public void setSVGDocumentXML(String svgDocumentXML)
  {
    this.svgDocumentXML = svgDocumentXML;
  }

  @XmlTransient
  public String getSVGDocumentXML()
  {
    return svgDocumentXML;
  }

  public void setSVGDocumentURI(String svgDocumentURI)
  {
    this.svgDocumentURI = svgDocumentURI;
  }

  @XmlTransient
  public String getSVGDocumentURI()
  {
    return svgDocumentURI;
  }

  /**
   * Returns a string representation of this class.
   *
   * @return The server state information as a string.
   */
  @Override
  public String toString()
  {
    return super.toString() + " [ItemboardSVGCellServerState]: "
      + "preferredWidth=" + preferredWidth + ","
      + "preferredHeight=" + preferredHeight + ","
      + "svgDocumentURI=" + svgDocumentURI + ","
      + "svgDocumentXML=" + svgDocumentXML;
  }
}
