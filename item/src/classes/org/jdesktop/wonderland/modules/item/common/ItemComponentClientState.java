package org.jdesktop.wonderland.modules.item.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponentClientState extends CellComponentClientState
{

  private String xmlPath;
  private String imgPath;
  private Ability[] abilities;

  public ItemComponentClientState()
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

  public String getImgPath()
  {
    return imgPath;
  }

  public void setImgPath(String imgPath)
  {
    this.imgPath = imgPath;
  }

  public Ability[] getAbilities()
  {
    return abilities;
  }

  public void setAbilities(Ability[] abilities)
  {
    this.abilities = abilities;
  }
}
