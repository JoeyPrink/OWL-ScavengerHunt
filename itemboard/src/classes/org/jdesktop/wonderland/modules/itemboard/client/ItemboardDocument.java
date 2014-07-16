/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.itemboard.client;

import com.jme.math.Vector3f;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog.MESSAGE_TYPE;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.hud.client.HUDDialogComponent;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardToolManager.ItemboardTool;
import org.jdesktop.wonderland.modules.itemboard.common.ItemboardUtils;
import org.jdesktop.wonderland.modules.itemboard.common.cell.ItemboardCellMessage.Action;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * Wraps the SVG document. Class was originally implemented as part of the
 * whiteboard module by
 *
 * @author Bernard Horan
 *
 * Class was eventually extended to also display items (images and multiline
 * text) and therefore also renamed by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemboardDocument implements SVGDocumentLoaderListener
{

  private static final Logger LOGGER
    = Logger.getLogger(ItemboardDocument.class.getName());
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
    "org/jdesktop/wonderland/modules/itemboard/client/resources/Bundle");
  private static final int TEXT_FONT_SIZE = 30;
  private static final int TEXT_FONT_SIZE_ITEM = 14;
  private static final int ITEM_TEXT_LINE_SPACING = 2;
  private static final int ITEM_TEXT_LINE_LENGTH = 30;
  private static final int ITEM_TEXT_MAX_LINES = 10;
  private static final int IMAGE_LONGER_SIDE_SIZE = 100;
  private static final int ITEM_IMAGE_TEXT_SPACE = 5;
  private ItemboardWindow itemboardWindow;
  private Date now;
  private Date then;
  private String docURI;
  private SVGDocument svgDocument;
  private DocumentDialog svgDocumentDialog;
  private HUDDialogComponent dialog;
  private HUDComponent itemChooser;
  protected static final Object readyLock = new Object();

  /**
   * A class for handling the loading of SVG documents. This can be time
   * consuming, so load in a thread
   */
  private class DocumentLoader extends Thread
  {

    private String uri = null;

    public DocumentLoader(String uri)
    {
      this.uri = uri;
    }

    @Override
    public void run()
    {
      if (uri != null)
      {
        svgDocument = (SVGDocument) ItemboardClientUtils.openDocument(uri);
        // loaded an external document
        itemboardWindow.setDocument(svgDocument, false);
      }
    }
  }

  public ItemboardDocument(ItemboardWindow itemboardWindow)
  {
    this.itemboardWindow = itemboardWindow;
  }

  private Element getDocumentElement()
  {
    return svgDocument.getDocumentElement();
  }

  public Element createElement(ItemboardTool currentTool, Point pressedPoint, Point releasedPoint)
  {
    Element element = null;

    switch (currentTool)
    {
      case LINE:
        element = createLineElement(pressedPoint, releasedPoint, itemboardWindow.getCurrentColor(), itemboardWindow.getStrokeWeight());
        break;
      case RECT:
        element = createRectElement(pressedPoint, releasedPoint, itemboardWindow.getToolManager().isFilled());
        break;
      case ELLIPSE:
        element = createEllipseElement(pressedPoint, releasedPoint, itemboardWindow.getToolManager().isFilled());
        break;
      case TEXT:
        element = createTextElement(releasedPoint);
        break;
      case ITEM:
        element = createItemElement(releasedPoint);
        break;
      default:
        break;
    }

    return element;
  }

  public Element createLineElement(Point start, Point end, Color lineColor, Float strokeWeight)
  {
    //Create the line element
    Element line = svgDocument.createElementNS(ItemboardUtils.svgNS, "line");
    line.setAttributeNS(null, "x1", Integer.valueOf(start.x).toString());
    line.setAttributeNS(null, "y1", Integer.valueOf(start.y).toString());
    line.setAttributeNS(null, "x2", Integer.valueOf(end.x).toString());
    line.setAttributeNS(null, "y2", Integer.valueOf(end.y).toString());
    line.setAttributeNS(null, "stroke", ItemboardUtils.constructRGBString(lineColor));
    line.setAttributeNS(null, "stroke-width", Float.toString(strokeWeight));

    String idString = itemboardWindow.getCellUID(itemboardWindow.getApp()) + System.currentTimeMillis();
    line.setAttributeNS(null, "id", idString);
    LOGGER.fine("itemboard: created line: " + line);
    return line;
  }

  public Element createRectElement(Point start, Point end, boolean filled)
  {
    //Create appropriate Rectangle from points
    Rectangle rect = ItemboardUtils.constructRectObject(start, end);

    // Create the rectangle element
    Element rectangle = svgDocument.createElementNS(ItemboardUtils.svgNS, "rect");
    rectangle.setAttributeNS(null, "x", Integer.valueOf(rect.x).toString());
    rectangle.setAttributeNS(null, "y", Integer.valueOf(rect.y).toString());
    rectangle.setAttributeNS(null, "width", Integer.valueOf(rect.width).toString());
    rectangle.setAttributeNS(null, "height", Integer.valueOf(rect.height).toString());
    rectangle.setAttributeNS(null, "stroke", ItemboardUtils.constructRGBString(itemboardWindow.getCurrentColor()));
    rectangle.setAttributeNS(null, "stroke-width", Float.toString(itemboardWindow.getStrokeWeight()));
    rectangle.setAttributeNS(null, "fill", ItemboardUtils.constructRGBString(itemboardWindow.getCurrentColor()));
    if (!filled)
    {
      rectangle.setAttributeNS(null, "fill-opacity", "0");
    }

    String idString = itemboardWindow.getCellUID(itemboardWindow.getApp()) + System.currentTimeMillis();
    rectangle.setAttributeNS(null, "id", idString);

    return rectangle;
  }

  public Element createEllipseElement(Point start, Point end, boolean filled)
  {
    //Create appropriate Rectangle from points
    Rectangle rect = ItemboardUtils.constructRectObject(start, end);
    double radiusX = (rect.getWidth() / 2);
    double radiusY = (rect.getHeight() / 2);
    int centreX = (int) (rect.getX() + radiusX);
    int centreY = (int) (rect.getY() + radiusY);

    // Create the ellipse element
    Element ellipse = svgDocument.createElementNS(ItemboardUtils.svgNS, "ellipse");
    ellipse.setAttributeNS(null, "cx", Integer.valueOf(centreX).toString());
    ellipse.setAttributeNS(null, "cy", Integer.valueOf(centreY).toString());
    ellipse.setAttributeNS(null, "rx", new Double(radiusX).toString());
    ellipse.setAttributeNS(null, "ry", new Double(radiusY).toString());
    ellipse.setAttributeNS(null, "stroke", ItemboardUtils.constructRGBString(itemboardWindow.getCurrentColor()));
    ellipse.setAttributeNS(null, "stroke-width", Float.toString(itemboardWindow.getStrokeWeight()));
    ellipse.setAttributeNS(null, "fill", ItemboardUtils.constructRGBString(itemboardWindow.getCurrentColor()));
    if (!filled)
    {
      ellipse.setAttributeNS(null, "fill-opacity", "0");
    }

    String idString = itemboardWindow.getCellUID(itemboardWindow.getApp()) + System.currentTimeMillis();
    ellipse.setAttributeNS(null, "id", idString);

    return ellipse;
  }

  private class TextGetter implements Runnable
  {

    private Point position;

    public TextGetter(Point position)
    {
      this.position = position;
    }

    public void run()
    {
      if (dialog == null)
      {
        // create a HUD text dialog
        dialog = new HUDDialogComponent(itemboardWindow.getCell());
        dialog.setMessage(BUNDLE.getString("Enter_text"));
        dialog.setType(MESSAGE_TYPE.QUERY);
        dialog.setPreferredLocation(Layout.CENTER);
        dialog.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));

        // add the text dialog to the HUD
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        mainHUD.addComponent(dialog);

        PropertyChangeListener plistener = new PropertyChangeListener()
        {

          public void propertyChange(PropertyChangeEvent pe)
          {
            if (pe.getPropertyName().equals("ok"))
            {
              String value = (String) pe.getNewValue();
              if ((value != null) && (value.length() > 0))
              {
                LOGGER.info("creating text element: " + value + " at " + position);
                Element e = createTextElement(position, value);
                itemboardWindow.addNewElement(e, true);
              }
            }
            if (dialog.isVisible())
            {
              dialog.setVisible(false);
            }
            if (dialog.isWorldVisible())
            {
              dialog.setWorldVisible(false);
            }
            dialog.setValue("");
            dialog.removePropertyChangeListener(this);
            dialog = null;
          }
        };
        dialog.addPropertyChangeListener(plistener);
      }

      dialog.setVisible(itemboardWindow.getDisplayMode() == DisplayMode.HUD);
      dialog.setWorldVisible(itemboardWindow.getDisplayMode() != DisplayMode.HUD);
    }
  };

  public Element createTextElement(Point end)
  {
    TextGetter getter = new TextGetter(end);
    new Thread(getter).start();

    return null;
  }

  public Element createTextElement(Point end, String text)
  {
    // Create the text element
    Element textElement = createTextElement(end, text, TEXT_FONT_SIZE);

    return textElement;
  }

  public Element createTextElement(Point end, String text, int fontSize)
  {
    // Create the text element
    Element textElement = svgDocument.createElementNS(ItemboardUtils.svgNS, "text");
    textElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
    textElement.setAttributeNS(null, "y", Integer.valueOf(end.y).toString());
    textElement.setAttributeNS(null, "fill", ItemboardUtils.constructRGBString(itemboardWindow.getCurrentColor()));
    textElement.setAttributeNS(null, "font-size", String.valueOf(fontSize));
    textElement.setTextContent(text);

    String idString = itemboardWindow.getCellUID(itemboardWindow.getApp()) + System.currentTimeMillis();
    textElement.setAttributeNS(null, "id", idString);

    return textElement;
  }

  /*
   * @param maxLines the maximum number of lines to be displayed
   all lines will be displayed if it is a negative number
   */
  public Element createMultilineTextElement(Point end, ArrayList<String> lines, int fontSize, int maxLines)
  {
    // Create the text element
    Element textElement = createTextElement(end, "", fontSize);

    boolean firstline = true;

    int counter = 0;
    for (String line : lines)
    {
      Element tspanElement = svgDocument.createElementNS(ItemboardUtils.svgNS, "tspan");

      if (!firstline)
      {
        end.setLocation(end.getX(), end.getY() + TEXT_FONT_SIZE_ITEM + ITEM_TEXT_LINE_SPACING);
      }
      firstline = false;

      tspanElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
      tspanElement.setAttributeNS(null, "y", Integer.valueOf(end.y).toString());
      tspanElement.setTextContent(line);

      textElement.appendChild(tspanElement);
      counter++;

      if (maxLines > 0 && counter >= maxLines)
      {
        break;
      }
    }

    return textElement;
  }

  private class ItemGetter implements Runnable
  {

    private Point position;

    public ItemGetter(Point position)
    {
      this.position = position;
    }

    private void getFiles(File folder, ArrayList<File> files)
    {
      for (File fileEntry : folder.listFiles())
      {
        if (fileEntry.isDirectory())
        {
          getFiles(fileEntry, files);
        }
        else
        {
          files.add(fileEntry);
        }
      }
    }

    private File searchImageFile(ArrayList<File> files, String search)
    {
      for (File file : files)
      {
        String filePath = file.getAbsolutePath();
        //System.out.println("file: " + filePath);
        if (!filePath.endsWith(".xml") && file.getName().startsWith(search))
        {
          return file;
        }
      }

      return null;
    }

    public void run()
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();

      File folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + userName + "/items");

      ArrayList<File> files = new ArrayList<File>();
      getFiles(folder, files);

      ArrayList<Item> itemEntryList = new ArrayList<Item>();
      try
      {
        JAXBContext context = JAXBContext.newInstance(Item.class);
        Unmarshaller marshal = context.createUnmarshaller();

        for (File file : files)
        {
          String filePath = file.getAbsolutePath();
          if (filePath.endsWith(".xml"))
          {
            Item unmarshalled = (Item) marshal.unmarshal(new FileReader(filePath));

            // Backslash because it is local file
            String fileName = file.getName();

            // Cut off extension
            int index = fileName.lastIndexOf(".");
            fileName = fileName.substring(0, index);

            File imgFile = searchImageFile(files, fileName);
            if (imgFile != null)
            {
              unmarshalled.setImage(imgFile.getAbsolutePath());
            }
            else
            {
              unmarshalled.setImage("no image");
            }

            itemEntryList.add(unmarshalled);
          }
        }
      }
      catch (JAXBException e)
      {
        Logger.getLogger(ItemboardDocument.class.getName()).log(Level.SEVERE, null, e);
      }
      catch (FileNotFoundException e)
      {
        Logger.getLogger(ItemboardDocument.class.getName()).log(Level.SEVERE, null, e);
      }

      if (itemChooser == null)
      {
        final ItemChooserPanel icPanel = new ItemChooserPanel(itemEntryList);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        itemChooser = mainHUD.createComponent(icPanel, itemboardWindow.getCell());
        itemChooser.setName("Item Chooser");
//        itemChooser.setPreferredLocation(Layout.CENTER);
        itemChooser.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));
        mainHUD.addComponent(itemChooser);

        icPanel.getOKButton().addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            Item selected = icPanel.getSelectedItem();

            if (selected != null)
            {
              // Display image
              File itemImage = new File(selected.getImage());
              LOGGER.info("creating item element: " + itemImage.getName() + " at " + position);
              Element el = createItemElement(position, itemImage);
              itemboardWindow.addNewElement(el, true);

              // Display text
              int width = Integer.parseInt(el.getAttribute("width"));
              position.setLocation(position.getX() + width + ITEM_IMAGE_TEXT_SPACE, position.getY());

              ArrayList<String> lines = ItemboardUtils.formatWithTextWith(selected.getContent(), ITEM_TEXT_LINE_LENGTH);
              LOGGER.info("creating multiline text element: " + lines.size() + " lines at " + position);
              el = createMultilineTextElement(position, lines, TEXT_FONT_SIZE_ITEM, ITEM_TEXT_MAX_LINES);
              itemboardWindow.addNewElement(el, true);
            }

            if (itemChooser.isVisible())
            {
              itemChooser.setVisible(false);
            }

            if (itemChooser.isWorldVisible())
            {
              itemChooser.setWorldVisible(false);
            }

            icPanel.getOKButton().removeActionListener(this);

            itemChooser = null;
          }
        });

        icPanel.getCancelButton().addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            if (itemChooser.isVisible())
            {
              itemChooser.setVisible(false);
            }

            if (itemChooser.isWorldVisible())
            {
              itemChooser.setWorldVisible(false);
            }

            icPanel.getCancelButton().removeActionListener(this);

            itemChooser = null;
          }
        });
      }

      itemChooser.setVisible(true);
      itemChooser.setWorldVisible(true);
    }
  };

  public Element createItemElement(Point end)
  {
    ItemGetter getter = new ItemGetter(end);
    new Thread(getter).start();

    return null;
  }

  public Element createItemElement(Point end, File itemImage)
  {
    // Create the item element
    String name = itemImage.getName();
    String type = name.substring(name.lastIndexOf('.') + 1);

    String base64String = "";
    String imgWidth = "50";
    String imgHeight = "50";
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedImage image = ImageIO.read(itemImage);
      ImageIO.write(image, type, baos);
      baos.flush();

      base64String = DatatypeConverter.printBase64Binary(baos.toByteArray());
      baos.close();

      // scale image's longer size to max size but only if image is bigger (do not enlarge)
      Image imageForSize = image;
      if (image.getWidth() >= image.getHeight() && image.getWidth() > IMAGE_LONGER_SIDE_SIZE)
      {
        imageForSize = image.getScaledInstance(IMAGE_LONGER_SIDE_SIZE, -1, Image.SCALE_DEFAULT);
      }
      else if (image.getHeight() > image.getWidth() && image.getHeight() > IMAGE_LONGER_SIDE_SIZE)
      {
        imageForSize = image.getScaledInstance(-1, IMAGE_LONGER_SIDE_SIZE, Image.SCALE_DEFAULT);
      }

      imgWidth = String.valueOf(imageForSize.getWidth(null));
      imgHeight = String.valueOf(imageForSize.getHeight(null));
    }
    catch (IOException ex)
    {
      Logger.getLogger(ItemboardDocument.class
        .getName()).log(Level.SEVERE, null, ex);
    }

    Element imageElement = svgDocument.createElementNS(ItemboardUtils.svgNS, "image");
    imageElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
    imageElement.setAttributeNS(null, "y", Integer.valueOf(end.y).toString());
    imageElement.setAttributeNS(null, "width", imgWidth);
    imageElement.setAttributeNS(null, "height", imgHeight);

    String xlinkNS = "http://www.w3.org/1999/xlink";
    String uri = "file://" + itemImage.getAbsolutePath().replace('\\', '/');
//    imageElement.setAttributeNS(xlinkNS, "xlink:href", uri);

    imageElement.setAttributeNS(xlinkNS, "xlink:href", "data:image/" + type + ";base64," + base64String);
    String idString = itemboardWindow.getCellUID(itemboardWindow.getApp()) + System.currentTimeMillis();
    imageElement.setAttributeNS(null, "id", idString);

    return imageElement;
  }

  public Element moveElement(Element toMove)
  {
    int xDiff = (int) (itemboardWindow.getCurrentPoint().getX() - itemboardWindow.getPressedPoint().getX());
    int yDiff = (int) (itemboardWindow.getCurrentPoint().getY() - itemboardWindow.getPressedPoint().getY());
    return moveElement(toMove, xDiff, yDiff);
  }

  public Element moveElement(Element toMove, int xDiff, int yDiff)
  {
    Element afterMove = (Element) toMove.cloneNode(true);
    if (afterMove.getTagName().equals("line"))
    {
      int x1 = Integer.parseInt(afterMove.getAttributeNS(null, "x1"));
      int y1 = Integer.parseInt(afterMove.getAttributeNS(null, "y1"));
      int x2 = Integer.parseInt(afterMove.getAttributeNS(null, "x2"));
      int y2 = Integer.parseInt(afterMove.getAttributeNS(null, "y2"));

      afterMove.setAttributeNS(null, "x1", Integer.toString(x1 + xDiff));
      afterMove.setAttributeNS(null, "y1", Integer.toString(y1 + yDiff));
      afterMove.setAttributeNS(null, "x2", Integer.toString(x2 + xDiff));
      afterMove.setAttributeNS(null, "y2", Integer.toString(y2 + yDiff));
    }
    else if (afterMove.getTagName().equals("rect"))
    {
      int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
      int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

      afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
      afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));
    }
    else if (afterMove.getTagName().equals("ellipse"))
    {
      int cx = Integer.parseInt(afterMove.getAttributeNS(null, "cx"));
      int cy = Integer.parseInt(afterMove.getAttributeNS(null, "cy"));

      afterMove.setAttributeNS(null, "cx", Integer.toString(cx + xDiff));
      afterMove.setAttributeNS(null, "cy", Integer.toString(cy + yDiff));
    }
    else if (afterMove.getTagName().equals("text"))
    {
      int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
      int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

      afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
      afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));

      NodeList childNodes = afterMove.getChildNodes();
      int length = childNodes.getLength();
      for (int i = 0; i < length; i++)
      {
        Node child = childNodes.item(i);

        if (child instanceof Element)
        {
          Element childAfterMove = (Element) child;

          if (childAfterMove.getTagName().equals("tspan"))
          {
            int xc = Integer.parseInt(childAfterMove.getAttributeNS(null, "x"));
            int yc = Integer.parseInt(childAfterMove.getAttributeNS(null, "y"));

            childAfterMove.setAttributeNS(null, "x", Integer.toString(xc + xDiff));
            childAfterMove.setAttributeNS(null, "y", Integer.toString(yc + yDiff));
          }
        }
      }
    }
    else if (afterMove.getTagName().equals("image"))
    {
      int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
      int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

      afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
      afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));
    }

    return afterMove;
  }

  /**
   * Loads an SVG document
   *
   * @param uri the URI of the SVG document to load
   * @param notify whether to notify other clients
   */
  public void openDocument(String uri, boolean notify)
  {
    if ((uri == null) || (uri.length() == 0))
    {
      return;
    }

    new DocumentLoader(uri).start();

    if (itemboardWindow.isSynced() && (notify == true))
    {
      // notify other clients
      itemboardWindow.sendRequest(Action.OPEN_DOCUMENT, null, uri, null, null);
    }
  }

  /**
   * Loads an SVG document
   *
   * @param uri the URI of the SVG document to load
   */
  public void openDocument(String uri)
  {
    openDocument(uri, false);
  }

  public void showSVGDialog()
  {
    SwingUtilities.invokeLater(new Runnable()
    {

      public void run()
      {
        svgDocumentDialog = new DocumentDialog(null, false);
        svgDocumentDialog.addActionListener(new java.awt.event.ActionListener()
        {

          public void actionPerformed(java.awt.event.ActionEvent evt)
          {
            svgDocumentDialog.setVisible(false);
            if (evt.getActionCommand().equals("OK"))
            {
              openDocument(svgDocumentDialog.getDocumentURL(), true);
            }
            svgDocumentDialog = null;
          }
        });
        svgDocumentDialog.setVisible(true);
      }
    });
  }

  private void setSVGDialogDocumentURL(String docURI)
  {
    if (svgDocumentDialog != null)
    {
      svgDocumentDialog.setDocumentURL(docURI);
    }
  }

  /**
   * DocumentLoaderListener methods
   */
  /**
   * Called when the loading of a document was started.
   */
  public void documentLoadingStarted(SVGDocumentLoaderEvent e)
  {
    LOGGER.fine("itemboard: document loading started: " + e);
    String message = BUNDLE.getString("Opening");
    message = MessageFormat.format(message, docURI);
    itemboardWindow.showHUDMessage(message);
    setSVGDialogDocumentURL(docURI);
    then = new Date();
  }

  /**
   * Called when the loading of a document was completed.
   */
  public void documentLoadingCompleted(SVGDocumentLoaderEvent e)
  {
    LOGGER.fine("itemboard: document loading completed: " + e);
    now = new Date();
    LOGGER.info("SVG loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
    itemboardWindow.hideHUDMessage(false);
  }

  /**
   * Called when the loading of a document was cancelled.
   */
  public void documentLoadingCancelled(SVGDocumentLoaderEvent e)
  {
    LOGGER.fine("itemboard: document loading cancelled: " + e);
  }

  /**
   * Called when the loading of a document has failed.
   */
  public void documentLoadingFailed(SVGDocumentLoaderEvent e)
  {
    LOGGER.fine("itemboard: document loading failed: " + e);
  }

  public Element importNode(Element importedNode, boolean deep)
  {
    Element element = null;

    if (svgDocument != null)
    {
      // because it may not yet have been received from the server
      element = (Element) svgDocument.importNode(importedNode, deep);
    }

    return element;
  }

  public Node appendChild(Element e)
  {
    return getDocumentElement().appendChild(e);
  }

  public void removeChild(Element rem)
  {
    getDocumentElement().removeChild(rem);
  }

  public Element getElementById(String attributeNS)
  {
    return svgDocument.getElementById(attributeNS);
  }

  public void replaceChild(Element afterMove, Element elementById)
  {
    getDocumentElement().replaceChild(afterMove, elementById);

  }

  public void setSVGDocument(SVGDocument svgDocument)
  {
    this.svgDocument = svgDocument;
  }

  public SVGDocument getSVGDocument()
  {
    return svgDocument;
  }
}
