package org.jdesktop.wonderland.modules.item.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponentClientState extends CellComponentClientState
{

  private String title;
  private String description;
  private String imgPath;
  private boolean once;
  private Ability[] abilities;
  private String[] owners;

  public ItemComponentClientState()
  {
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
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

  public boolean getOnce()
  {
    return once;
  }

  public void setOnce(boolean once)
  {
    this.once = once;
  }

  public String[] getOwners()
  {
    return owners;
  }

  public void setOwners(String[] owners)
  {
    this.owners = owners;
  }
}
