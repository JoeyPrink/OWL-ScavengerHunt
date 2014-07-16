package org.jdesktop.wonderland.modules.item.client.inventory;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.item.client.Item;
import org.jdesktop.wonderland.modules.item.client.ItemComponent;
import org.jdesktop.wonderland.modules.item.client.ItemUtils;
import org.jdesktop.wonderland.modules.item.client.ScavengerHuntStudent;
import org.jdesktop.wonderland.modules.item.client.StudentManager;
import org.jdesktop.wonderland.modules.item.common.Abilities;

/**
 * Manages list of all items and creates Panel to display them.
 *
 * Code based on InventoryManager by
 *
 * @author Pirmin Riedman
 *
 * adapted by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class InventoryManager
{

  private final ItemPanel itemPanel;
  private ArrayList<Item> itemEntryList;
  private ArrayList<ItemComponent> itemEntryListAll;

  DefaultListModel<ItemComponent> dlm;
  ItemListCellRenderer cellRenderer;

  public InventoryManager()
  {
    itemPanel = new ItemPanel();

    Canvas canvas = JmeClientMain.getFrame().getCanvas();
    itemPanel.setPreferredSize(new Dimension(canvas.getWidth() / 2,
      canvas.getHeight() / 2));

    itemPanel.setBorder(null);

    dlm = new DefaultListModel<ItemComponent>();
    cellRenderer = new ItemListCellRenderer(itemEntryList);

    itemPanel.getList().setModel(dlm);
    itemPanel.getList().setCellRenderer(cellRenderer);

    itemPanel.getList().addListSelectionListener(new ListSelectionListener()
    {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        updateItemContent();
      }
    });

    itemPanel.getReturnButton().addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        int chosenOption = JOptionPane.showConfirmDialog(itemPanel,
          "Do you really want to return this item?",
          "Please confirm",
          JOptionPane.YES_NO_OPTION);
        if (chosenOption == JOptionPane.YES_OPTION)
        {
          removeSelectedItem();
        }
      }

    });

    itemEntryList = new ArrayList<Item>();
    itemEntryListAll = new ArrayList<ItemComponent>();

    updateItemList();
    updateItemContent();
  }

  private void removeSelectedItem()
  {
    JList itemList = itemPanel.getList();
    int selectedIndex = itemList.getSelectedIndex();

    if (selectedIndex > -1 && selectedIndex < dlm.getSize())
    {
      Object listElement = dlm.getElementAt(selectedIndex);
      if (listElement instanceof ItemComponent)
      {
        ItemComponent returnItem = (ItemComponent) listElement;

        boolean wasOwner = removeOwnership(returnItem);
        if (wasOwner)
        {
          removeItemFromUserDirectory(returnItem);
          removeItemFromList(returnItem);
          updateItemContent();
        }
      }
    }
  }

  private boolean removeOwnership(ItemComponent returnItem)
  {
    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();

    if (returnItem.removeOwner(userName))
    {
      JOptionPane.showMessageDialog(null, "Item successfully returned.", "Success", JOptionPane.INFORMATION_MESSAGE);
      return true;
    }
    else
    {
      JOptionPane.showMessageDialog(null, "Error: You cannot return this item "
        + "because you do not own it.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void removeItemFromUserDirectory(ItemComponent returnItem)
  {
    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();
    File folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + userName + "/items");

    String pathToXMLFile = returnItem.getPathToXMLFile();

    String fileName = ItemUtils.getFileNameFromPath(pathToXMLFile);

    for (File childFile : folder.listFiles())
    {
      String childFileName = childFile.getName();
      String childFileNamePlain = childFileName.substring(0, childFileName.lastIndexOf("."));

      String searchFileNamePlain = fileName.substring(0, fileName.lastIndexOf("."));

      if (childFile.isFile() && childFileNamePlain.equals(searchFileNamePlain))
      {
        childFile.delete();
      }
    }
  }

  private void removeItemFromList(ItemComponent removeItem)
  {
    int removeIndex = -1;

    int index = 0;
    for (Item item : itemEntryList)
    {
      if (item.getTitle().equals(removeItem.getTitle())
        && item.getContent().equals(removeItem.getContent()))
      {
        removeIndex = index;
      }

      index++;
    }

    if (removeIndex > -1 && removeIndex < itemEntryList.size())
    {
      itemEntryList.remove(removeIndex);
    }
  }

  private void createListOfAllObjectsItem(Cell cell)
  {
    // As a special case, if the Cell is an AvatarCell, then simply ignore
    // and return, since Avatar Cells are returned by the Cell cache.
    if (cell instanceof AvatarCell)
    {
      return;
    }

    ItemComponent comp = cell.getComponent(ItemComponent.class);
    if (comp != null)
    {
      itemEntryListAll.add(comp);
    }

    // Recursively iterate through all of the Cell's children
    List<Cell> children = cell.getChildren();
    for (Cell child : children)
    {
      createListOfAllObjectsItem(child);
    }
  }

  private void loadAllItems()
  {
    itemEntryListAll = new ArrayList<ItemComponent>();

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();

    // Fetch the client-side Cell cache
    CellCache cache = ClientContext.getCellCache(session);
    if (cache == null)
    {
      Logger.getLogger(InventoryManager.class.getName())
        .log(Level.WARNING, "Unable to find Cell cache for session {0}", session);
      return;
    }

//    itemPanel.getItemLabel().setText("<html>");
    // Loop through all of the root cells and add into the world
    Collection<Cell> rootCells = cache.getRootCells();
    for (Cell rootCell : rootCells)
    {
      createListOfAllObjectsItem(rootCell);
    }

//    itemPanel.getItemLabel().setText(itemPanel.getItemLabel().getText() + "</html>");
//    itemPanel.getItemLabel().repaint();
  }

  public void loadItems()
  {
    loadUserItems(); // has to be done before loadAllItems(),
    // because loadAllItems() uses itemEntryList
    loadAllItems();

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

  private void loadUserItems()
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

    dlm.clear();
    cellRenderer.setItemEntryList(itemEntryList);

    if (itemEntryListAll == null || itemEntryListAll.isEmpty())
    {
      list = new String[1];
      list[0] = "Empty";
      itemPanel.getTextPane().setText("");
    }
    else
    {
      list = new String[itemEntryListAll.size()];
      int i = 0;
      for (ItemComponent comp : itemEntryListAll)
      {
        dlm.addElement(comp);
        list[i] = comp.getTitle();
        i++;
      }
    }
  }

  private void updateItemContent()
  {
    if (itemEntryListAll == null || itemEntryListAll.isEmpty())
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

    // Does the user already have this item?
    ItemComponent comp = itemEntryListAll.get(selected);
    Item entry = null;
    for (Item item : itemEntryList)
    {
      if (item.getTitle().equals(comp.getTitle())
        && item.getContent().equals(comp.getContent()))
      {
        entry = item;
      }
    }

    if (entry != null)  // Yes, he does
    {
      itemPanel.getReturnButton().setEnabled(true);

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
    else  // No, he doesn't
    {
      itemPanel.getReturnButton().setEnabled(false);

      Abilities.Ability[] abilities = comp.getAbilities();

      // Is the user able to pick up this item?
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();
      ScavengerHuntStudent me = StudentManager.loadStudentFromFile(userName);
      boolean canPickup = false;
      if (me != null)
      {
        Abilities.Ability myAbility = me.getAbility();

        if (myAbility != null && abilities != null)
        {
          canPickup = Arrays.asList(abilities).contains(myAbility);
        }
      }

      String[] owners = comp.getOwners();
      boolean otherOwners;
      if (owners.length == 0)
      {
        otherOwners = false;
      }
      else if (owners.length == 1 && owners[0].equals(userName))
      {
        otherOwners = false;
      }
      else
      {
        otherOwners = true;
      }

      boolean someoneElseWasFaster = false;
      if (comp.getOnce() && otherOwners)
      {
        someoneElseWasFaster = true;
      }

      if (!someoneElseWasFaster && canPickup)  // Yes, he is
      {
        itemPanel.getTextPane().setText("You haven't found this object yet.");
      }
      else if (someoneElseWasFaster)  // No, he isn't
      {
        itemPanel.getTextPane().setText("You cannot pick up this item. It can "
          + "be picked up only once and it seems like someone else was faster "
          + "than you. Sorry!" + "\n" + "\n");

        // Who was faster?
        int numOwners = owners.length;
        itemPanel.getTextPane().setContentType("text/plain");
        for (int ownerIndex = 0; ownerIndex < numOwners; ownerIndex++)
        {
          itemPanel.getTextPane().setText(itemPanel.getTextPane().getText()
            + owners[ownerIndex] + "\n");
        }
      }
      else if (!canPickup)  // No, he isn't
      {
        itemPanel.getTextPane().setText("You cannot pick up this object because "
          + "you haven't got the right abilities.");

        // Does anybody else have the item?
        int numOwners = owners.length;

        if (numOwners > 0)  // Yes, someone does
        {
          Random rand = new Random();
          int index = rand.nextInt(numOwners);

          String randomOwnerUserName = owners[index];

          itemPanel.getTextPane().setContentType("text/plain");
          itemPanel.getTextPane().setText(itemPanel.getTextPane().getText() + "\n" + "\n"
            + "HINT: " + randomOwnerUserName + " has already picked up this item.");
        }
        else  // No, nobody owns this item yet
        {
          // Would someone be able to pick it up?
          ArrayList<ScavengerHuntStudent> students = ItemUtils.getStudentsWithAbility(Arrays.asList(abilities));
          int numStudents = students.size();

          if (numStudents > 0)  // Yes, there is someone who can pick it up
          {
            Random rand = new Random();
            int index = rand.nextInt(numStudents);

            ScavengerHuntStudent randomStudent = students.get(index);

            itemPanel.getTextPane().setContentType("text/plain");
            itemPanel.getTextPane().setText(itemPanel.getTextPane().getText() + "\n" + "\n"
              + "HINT: " + randomStudent.getUsername() + " might be able to pick up this object.");
          }
          else  // No, there is nobody, who could pick it up; this should not happen
          {
            itemPanel.getTextPane().setContentType("text/plain");
            itemPanel.getTextPane().setText(itemPanel.getTextPane().getText() + "\n" + "\n"
              + "Apparently no student is able to pick up this object. This must be a mistake. Please contact your teacher.");
          }
        }
      }

      itemPanel.getTitleLabel().setText("");
      //itemPanel.getImageLabel().setText("");
      itemPanel.getImageLabel().setIcon(null);
    }
  }
}
