package org.jdesktop.wonderland.modules.itemboard.client;

import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents one item.
 *
 * Code based on DocEntry by
 *
 * @author Pirmin Riedman
 *
 * adapted by
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@XmlRootElement(name = "item")
public class Item
{

  @XmlElement(name = "title")
  private String title = null;

  @XmlElement(name = "content")
  private String content = null;

  private String image = null;

  @XmlElement(name = "date")
  private GregorianCalendar date = null;

  public Item()
  {
  }

  public Item(String title, String content)
  {
    this.title = title;
    this.content = content;
    this.image = null;
    this.date = new GregorianCalendar();
  }

  public Item(String title, String content, String image)
  {
    this.title = title;
    this.content = content;
    this.image = image;
    this.date = new GregorianCalendar();
  }

  @XmlTransient
  public String getTitle()
  {
    return title;
  }

  @XmlTransient
  public String getContent()
  {
    return content;
  }

  public String getImage()
  {
    return image;
  }

  @XmlTransient
  public GregorianCalendar getDate()
  {
    return date;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public void setImage(String image)
  {
    this.image = image;
  }

  public void setDate(GregorianCalendar date)
  {
    this.date = date;
  }

  @Override
  public String toString()
  {
    return title;
  }
}
