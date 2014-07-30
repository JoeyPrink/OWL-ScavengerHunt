package org.jdesktop.wonderland.modules.item.common;

import java.util.GregorianCalendar;
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

  @XmlElement(name = "title")
  private String title = "";

  @XmlElement(name = "description")
  private String description = "";

  @XmlElement(name = "img-path")
  private String imgPath = "Enter path to item image file";

  @XmlElement(name = "abilities")
  private Ability[] abilities = new Ability[]
  {
  };

  @XmlElement(name = "once")
  private boolean once = false;

  @XmlElement(name = "owners")
  private String[] owners = new String[]
  {
  };

  public ItemComponentServerState()
  {
    GregorianCalendar gregor = new GregorianCalendar();
    long timeInMillis = gregor.getTimeInMillis();

    if (title.equals(""))
    {
      title = "Title_" + timeInMillis;
    }

    if (description.equals(""))
    {
      description = "Description_" + timeInMillis;
    }
  }

  @XmlTransient
  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @XmlTransient
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
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
  public Ability[] getAbilities()
  {
    return abilities;
  }

  public void setAbilities(Ability[] abilities)
  {
    this.abilities = abilities;
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
