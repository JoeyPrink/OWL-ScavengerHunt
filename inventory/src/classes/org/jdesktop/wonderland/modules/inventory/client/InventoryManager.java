package org.jdesktop.wonderland.modules.inventory.client;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;

/**
 * Manages list of all items and creates Panel to display them.
 *
 * Code based on InventoryManager by
 *
 * @author Pirmin Riedman
 *
 * adapted by
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class InventoryManager
{

  private final ItemPanel itemPanel;
  private ArrayList<Item> itemEntryList;

  public InventoryManager()
  {
    itemPanel = new ItemPanel();

    Canvas canvas = JmeClientMain.getFrame().getCanvas();
    itemPanel.setPreferredSize(new Dimension(canvas.getWidth() / 2,
      canvas.getHeight() / 2));

    itemPanel.setBorder(null);

    itemPanel.getList().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        updateItemContent();
      }
    });

    itemEntryList = new ArrayList<Item>();

    updateItemList();
    updateItemContent();
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

  public void loadItems()
  {
    itemEntryList = new ArrayList<Item>();

    try
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();

      File folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + userName + "/items");

      ArrayList<File> files = new ArrayList<File>();
      getFiles(folder, files);

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Unmarshaller marshal = context.createUnmarshaller();

      for (File file : files)
      {
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(".xml"))
        {
          Item unmarshalled = (Item) marshal.unmarshal(new FileReader(filePath));

          // Backslash because it is local file
          String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

          // Cut off extension
          int index = fileName.lastIndexOf(".");
          fileName = fileName.substring(0, index);

          //System.out.println("Searching image file " + fileName);
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
      Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, e);
    }
    catch (FileNotFoundException e)
    {
      Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, e);
    }

    updateItemList();
    updateItemContent();
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

  public JPanel getItemPanel()
  {
    return itemPanel;
  }

  private void updateItemList()
  {
    String[] list;

    if (itemEntryList == null || itemEntryList.isEmpty())
    {
      list = new String[1];
      list[0] = "Empty";
      itemPanel.getTextPane().setText("");
    }
    else
    {
      list = new String[itemEntryList.size()];
      int i = 0;
      for (Item entry : itemEntryList)
      {
        list[i] = entry.getTitle();
        i++;
      }
    }

    itemPanel.getList().setListData(list);
  }

  private void updateItemContent()
  {
    if (itemEntryList == null || itemEntryList.isEmpty())
    {
      itemPanel.getTextPane().setText("");
      itemPanel.getTitleLabel().setText("");
      //itemPanel.getImageLabel().setText("");
      itemPanel.getImageLabel().setIcon(null);
      return;
    }

    int selected = itemPanel.getList().getSelectedIndex();
    if (selected == -1)
    {
      selected = 0;
      itemPanel.getList().setSelectedIndex(selected);
    }

    Item entry = itemEntryList.get(selected);
    String content = entry.getContent();
    content = content.replaceAll("&#xD;", "\n");
    itemPanel.getTextPane().setContentType("text/html");
    itemPanel.getTextPane().setEditable(false);
    itemPanel.getTextPane().setText(content);
    itemPanel.getTitleLabel().setText(entry.getTitle());
    //itemPanel.getImageLabel().setText(entry.getImage());
    ImageIcon ico = new ImageIcon(entry.getImage());
    Image ima = ico.getImage().getScaledInstance(-1, 56, Image.SCALE_DEFAULT);
    itemPanel.getImageLabel().setIcon(new ImageIcon(ima));
  }
}
