package org.jdesktop.wonderland.modules.item.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@XmlRootElement(name = "item-component")
@ServerState
public class ItemComponentServerState extends CellComponentServerState
{

  @XmlElement(name = "xml-path")
  private String xmlPath = "Enter path to item description file";

  @XmlElement(name = "img-path")
  private String imgPath = "Enter path to item image file";

  @XmlElement(name = "once")
  private boolean once = false;

  @XmlElement(name = "abilities")
  private Ability[] abilities = new Ability[]
  {
  };

  @XmlElement(name = "owners")
  private String[] owners = new String[]
  {
  };

  public ItemComponentServerState()
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

  @XmlTransient
  public String getImgPath()
  {
    return imgPath;
  }

  public void setImgPath(String imgPath)
  {
    this.imgPath = imgPath;
  }

  @XmlTransient
  public boolean getOnce()
  {
    return once;
  }

  public void setOnce(boolean once)
  {
    this.once = once;
  }

  @XmlTransient
  public Ability[] getAbilities()
  {
    return abilities;
  }

  public void setAbilities(Ability[] abilities)
  {
    this.abilities = abilities;
  }

  @XmlTransient
  public String[] getOwners()
  {
    return owners;
  }

  public void setOwners(String[] owners)
  {
    this.owners = owners;
  }

  @Override
  public String getServerComponentClassName()
  {
    return "org.jdesktop.wonderland.modules.item.server.ItemComponentMO";
  }
}
