package org.jdesktop.wonderland.modules.item.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;

/**
 * Properties Panel for item component. Possibility to upload and choose xml
 * description and image files for the item. Plus choose users with which
 * abilities should be able to pick up the item information.
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@PropertiesFactory(ItemComponentServerState.class)
public class ItemComponentProperties extends javax.swing.JPanel implements PropertiesFactorySPI
{

  private CellPropertiesEditor editor = null;
  private String originalTitle;
  private String originalDescription;
  private String originalImagePath;
  private boolean originalOnce;
  private Ability[] originalAbilities;

  private final JCheckBox[] boxes;

  public ItemComponentProperties()
  {
    initComponents();

    tfTitle.getDocument().addDocumentListener(new InfoTextFieldListener());
    taDescription.getDocument().addDocumentListener(new InfoTextFieldListener());
    tfImgPath.getDocument().addDocumentListener(new InfoTextFieldListener());

    this.boxes = new JCheckBox[]
    {
      cbRole1, cbRole2, cbRole3, cbRole4
    };

    int i = 0;
    for (JCheckBox box : boxes)
    {
      Ability ability = Abilities.getAbilityFromInt(i);
      String abilityString = Abilities.getStringFromAbility(ability);
      box.setText(abilityString);
      i++;

      box.addActionListener(new AbilityCheckBoxListener());
    }
//    cbRoleAll.addActionListener(new AbilityCheckBoxListener());
  }

  @Override
  public String getDisplayName()
  {
    return "Item Information";
  }

  @Override
  public void setCellPropertiesEditor(CellPropertiesEditor editor)
  {
    this.editor = editor;
  }

  @Override
  public JPanel getPropertiesJPanel()
  {
    return this;
  }

  private void setCheckBoxes(Ability[] abilities)
  {
    for (JCheckBox box : boxes)
    {
      box.setSelected(false);
    }

    for (Ability ability : abilities)
    {
      int index = Abilities.getIntFromAbility(ability);
      if (index > -1 && index < boxes.length)
      {
        boxes[index].setSelected(true);
      }
    }
  }

  @Override
  public void open()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState
      = state.getComponentServerState(ItemComponentServerState.class);

    if (compState != null)
    {
      ItemComponentServerState itemState = (ItemComponentServerState) compState;

      originalTitle = itemState.getTitle();
      tfTitle.setText(originalTitle);

      originalDescription = itemState.getDescription();
      taDescription.setText(originalDescription);

      originalImagePath = itemState.getImgPath();
      tfImgPath.setText(originalImagePath);

      originalAbilities = itemState.getAbilities();
      setCheckBoxes(originalAbilities);

      originalOnce = itemState.getOnce();
      cbOnce.setSelected(originalOnce);

      initImageList();
    }
  }

  private void initImageList()
  {
    DefaultListModel listModel = new DefaultListModel();

    try
    {
      ContentCollection fileRoot = ItemUtils.getFileRoot(ItemUtils.SUBDIRNAME_IMG, "");
      List<ContentNode> children = fileRoot.getChildren();

      for (ContentNode child : children)
      {
        listModel.addElement(child.getName());
      }
    }
    catch (ContentRepositoryException ex)
    {
      listModel.addElement("Sorry, couldn't load files.");
    }

    listImages.setModel(listModel);

    int lastIndex = listModel.getSize() - 1;
    if (lastIndex >= 0)
    {
      listImages.ensureIndexIsVisible(lastIndex);
    }

    selectCurrentElement();
  }

  private void addSomethingToList(String fileName)
  {
    // fileName incl. ".png"
    DefaultListModel listModel = (DefaultListModel) listImages.getModel();

    //if a file with the same name was in the list before it gets overwritten
    listModel.removeElement(fileName);
    listModel.addElement(fileName);

    selectCurrentElement();
  }

  private void selectCurrentElement()
  {
    if (originalImagePath == null || !originalImagePath.contains("/"))
    {
      return;
    }

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();

    String userFromPath = ItemUtils.getUserDirFromPath(originalImagePath);

    if (!userFromPath.equals(userName)) // is it one of my images?
    {
      return; // no
    }

    String curr = ItemUtils.getFileNameFromPath(originalImagePath);

    int numberOfElements = listImages.getModel().getSize();
    for (int i = 0; i < numberOfElements; i++)
    {
      String element = (String) listImages.getModel().getElementAt(i);
      if (element.equals(curr))
      {
        listImages.setSelectedIndex(i);
        listImages.ensureIndexIsVisible(i);
        break;
      }
    }
  }

  @Override
  public void close()
  {
  }

  @Override
  public void restore()
  {
    tfTitle.setText(originalTitle);
    taDescription.setText(originalDescription);
    tfImgPath.setText(originalImagePath);
    selectCurrentElement();
    setCheckBoxes(originalAbilities);
    cbOnce.setSelected(originalOnce);
  }

  @Override
  public void apply()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState
      = state.getComponentServerState(ItemComponentServerState.class);

    // Update values in Server State
    ((ItemComponentServerState) compState).setTitle(tfTitle.getText());
    ((ItemComponentServerState) compState).setDescription(taDescription.getText());
    ((ItemComponentServerState) compState).setImgPath(tfImgPath.getText());

    Ability[] newAbilites = getSelectedAbilities();
    ((ItemComponentServerState) compState).setAbilities(newAbilites);

    ((ItemComponentServerState) compState).setOnce(cbOnce.isSelected());

    editor.addToUpdateList(compState);
  }

  private Ability[] getSelectedAbilities()
  {
    ArrayList<Ability> abilityList = new ArrayList<Ability>();

    for (int i = 0; i < boxes.length; i++)
    {
      JCheckBox box = boxes[i];
      //box.doClick();

      if (box.isSelected())
      {
        abilityList.add(Abilities.getAbilityFromInt(i));
      }
    }

    Ability[] abilities = new Ability[abilityList.size()];
    for (int i = 0; i < abilityList.size(); i++)
    {
      abilities[i] = abilityList.get(i);
    }

    return abilities;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    jPanel3 = new javax.swing.JPanel();
    pnItemDescription = new javax.swing.JPanel();
    lbTitle = new javax.swing.JLabel();
    tfTitle = new javax.swing.JTextField();
    spDesciption = new javax.swing.JScrollPane();
    taDescription = new javax.swing.JTextArea();
    btLoad = new javax.swing.JButton();
    btSave = new javax.swing.JButton();
    pnImgFiles = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    tfImgPath = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    spImages = new javax.swing.JScrollPane();
    listImages = new javax.swing.JList();
    buttonBrowseImg = new javax.swing.JButton();
    jPanel4 = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    pnRolesOnce = new javax.swing.JPanel();
    pnRoles = new javax.swing.JPanel();
    cbRole1 = new javax.swing.JCheckBox();
    cbRole2 = new javax.swing.JCheckBox();
    cbRole3 = new javax.swing.JCheckBox();
    cbRole4 = new javax.swing.JCheckBox();
    pnOnce = new javax.swing.JPanel();
    cbOnce = new javax.swing.JCheckBox();

    setLayout(new java.awt.BorderLayout());

    jPanel3.setLayout(new java.awt.GridLayout(2, 1));

    pnItemDescription.setBorder(javax.swing.BorderFactory.createTitledBorder("Item description"));

    lbTitle.setText("Title:");

    spDesciption.setPreferredSize(new java.awt.Dimension(100, 100));

    taDescription.setColumns(20);
    taDescription.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
    taDescription.setLineWrap(true);
    taDescription.setRows(5);
    taDescription.setWrapStyleWord(true);
    taDescription.setMinimumSize(new java.awt.Dimension(10, 2));
    taDescription.setPreferredSize(new java.awt.Dimension(100, 100));
    spDesciption.setViewportView(taDescription);

    btLoad.setText("Load");
    btLoad.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btLoadActionPerformed(evt);
      }
    });

    btSave.setText("Save");
    btSave.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btSaveActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout pnItemDescriptionLayout = new javax.swing.GroupLayout(pnItemDescription);
    pnItemDescription.setLayout(pnItemDescriptionLayout);
    pnItemDescriptionLayout.setHorizontalGroup(
      pnItemDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnItemDescriptionLayout.createSequentialGroup()
        .addComponent(btLoad)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btSave)
        .addContainerGap(101, Short.MAX_VALUE))
      .addGroup(pnItemDescriptionLayout.createSequentialGroup()
        .addComponent(lbTitle)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(tfTitle))
      .addComponent(spDesciption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    pnItemDescriptionLayout.setVerticalGroup(
      pnItemDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnItemDescriptionLayout.createSequentialGroup()
        .addGroup(pnItemDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(lbTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(tfTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(spDesciption, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(pnItemDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btLoad)
          .addComponent(btSave))
        .addContainerGap(13, Short.MAX_VALUE))
    );

    jPanel3.add(pnItemDescription);

    pnImgFiles.setBorder(javax.swing.BorderFactory.createTitledBorder("Image files"));

    jLabel2.setText("Choose an image file:");

    tfImgPath.setEnabled(false);

    jPanel1.setLayout(new java.awt.GridLayout(1, 1));

    listImages.setModel(new javax.swing.AbstractListModel()
    {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    listImages.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        listImagesValueChanged(evt);
      }
    });
    spImages.setViewportView(listImages);

    jPanel1.add(spImages);

    buttonBrowseImg.setText("Browse");
    buttonBrowseImg.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonBrowseImgActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout pnImgFilesLayout = new javax.swing.GroupLayout(pnImgFiles);
    pnImgFiles.setLayout(pnImgFilesLayout);
    pnImgFilesLayout.setHorizontalGroup(
      pnImgFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnImgFilesLayout.createSequentialGroup()
        .addComponent(buttonBrowseImg)
        .addGap(0, 0, Short.MAX_VALUE))
      .addComponent(tfImgPath)
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
    );
    pnImgFilesLayout.setVerticalGroup(
      pnImgFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnImgFilesLayout.createSequentialGroup()
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(tfImgPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(8, 8, 8)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonBrowseImg)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel3.add(pnImgFiles);

    add(jPanel3, java.awt.BorderLayout.CENTER);

    jPanel4.setLayout(new java.awt.GridLayout(2, 1));

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 119, Short.MAX_VALUE)
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 211, Short.MAX_VALUE)
    );

    jPanel4.add(jPanel5);

    pnRolesOnce.setLayout(new java.awt.BorderLayout());

    pnRoles.setBorder(javax.swing.BorderFactory.createTitledBorder("Who can pick it up?"));
    pnRoles.setLayout(new java.awt.GridLayout(4, 1));

    cbRole1.setText("Role1");
    pnRoles.add(cbRole1);

    cbRole2.setText("Role2");
    pnRoles.add(cbRole2);

    cbRole3.setText("Role3");
    pnRoles.add(cbRole3);

    cbRole4.setText("Role4");
    pnRoles.add(cbRole4);

    pnRolesOnce.add(pnRoles, java.awt.BorderLayout.CENTER);

    pnOnce.setBorder(javax.swing.BorderFactory.createTitledBorder("Availability"));
    pnOnce.setLayout(new java.awt.GridLayout(1, 1));

    cbOnce.setText("Pick up only once");
    cbOnce.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbOnceItemStateChanged(evt);
      }
    });
    pnOnce.add(cbOnce);

    pnRolesOnce.add(pnOnce, java.awt.BorderLayout.SOUTH);

    jPanel4.add(pnRolesOnce);

    add(jPanel4, java.awt.BorderLayout.EAST);
  }// </editor-fold>//GEN-END:initComponents

  private File browse(FileNameExtensionFilter filter)
  {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(filter);
    int outputPath = fileChooser.showOpenDialog(this);
    if (outputPath == JFileChooser.APPROVE_OPTION)
    {
      return fileChooser.getSelectedFile();
    }

    return null;
  }

  private void buttonBrowseImgActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonBrowseImgActionPerformed
  {//GEN-HEADEREND:event_buttonBrowseImgActionPerformed
    final String[] imgExtensions =
    {
      "jpg", "png", "gif"
    };
    FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(null, imgExtensions);
    File imgFile = browse(imgFilter);

    if (imgFile != null)
    {
      try
      {
        if (imgFile.length() > 100000) // 100 KB
        {
          JOptionPane.showMessageDialog(null, "Item upload error: File too large!", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        URL url = ItemUtils.uploadFileToServer(imgFile, ItemUtils.SUBDIRNAME_IMG, "");
        if (url != null)
        {
          addSomethingToList(imgFile.getName());
        }
        else
        {
          JOptionPane.showMessageDialog(null, "File too large!", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      catch (ContentRepositoryException ex)
      {
        Logger.getLogger(ItemComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
        Logger.getLogger(ItemComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }//GEN-LAST:event_buttonBrowseImgActionPerformed

  private void btLoadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btLoadActionPerformed
  {//GEN-HEADEREND:event_btLoadActionPerformed
    String[] items;

    try
    {
      ContentCollection fileRoot = ItemUtils.getFileRoot(ItemUtils.SUBDIRNAME_XML, "");
      List<ContentNode> children = fileRoot.getChildren();

      items = new String[children.size()];
      int index = 0;
      for (ContentNode child : children)
      {
        String childName = child.getName();
        items[index] = childName.substring(0, childName.length() - 4);  // cut off ".xml"
        index++;
      }
    }
    catch (ContentRepositoryException ex)
    {
      items = new String[0];
    }

    if (items.length < 1)
    {
      JOptionPane.showMessageDialog(this, "There are no item files to be loaded.");
    }
    else
    {
      String quiz = (String) JOptionPane.showInputDialog(this,
        "Please select item file:",
        "Select item",
        JOptionPane.QUESTION_MESSAGE,
        null, items, null);

      Item loaded = ItemUtils.getItem(quiz);

      if (loaded != null)
      {
        tfTitle.setText(loaded.getTitle());
        taDescription.setText(loaded.getContent());
      }
    }
  }//GEN-LAST:event_btLoadActionPerformed

  private void btSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSaveActionPerformed
  {//GEN-HEADEREND:event_btSaveActionPerformed
    String title = tfTitle.getText();
    if (!title.trim().equals(""))
    {
      Item i = new Item(title, taDescription.getText());
      String fileName = ItemUtils.makeFileName(title);
      ItemUtils.setItem(fileName, i);

      JOptionPane.showMessageDialog(this, "Item was stored on server.");
    }
  }//GEN-LAST:event_btSaveActionPerformed

  private void cbOnceItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbOnceItemStateChanged
  {//GEN-HEADEREND:event_cbOnceItemStateChanged
    checkDirty();
  }//GEN-LAST:event_cbOnceItemStateChanged

  private void listImagesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_listImagesValueChanged
  {//GEN-HEADEREND:event_listImagesValueChanged
    String fileName = (String) listImages.getSelectedValue();
    if (fileName == null)
    {
      return;
    }

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();

    String wlpath = ItemUtils.makeWlcontentPath(userName, ItemUtils.SUBDIRNAME_IMG, fileName);
    tfImgPath.setText(wlpath);
  }//GEN-LAST:event_listImagesValueChanged

  class InfoTextFieldListener implements DocumentListener
  {

    @Override
    public void insertUpdate(DocumentEvent e)
    {
      checkDirty();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
      checkDirty();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
      checkDirty();
    }
  }

  class AbilityCheckBoxListener implements ActionListener
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      checkDirty();
    }
  }

  private void checkDirty()
  {
    if (editor != null)
    {
      Ability[] currentAbilites = getSelectedAbilities();
      //printAbilities(currentAbilites, "  This are the current abilities:");
      //printAbilities(originalAbilities, "  This are the original abilities:");

      boolean same = compareAbilities(currentAbilites, originalAbilities);

      if (!same || !tfTitle.getText().equals(originalTitle)
        || !taDescription.getText().equals(originalDescription)
        || !tfImgPath.getText().equals(originalImagePath)
        || cbOnce.isSelected() != originalOnce)
      {
        editor.setPanelDirty(ItemComponentProperties.class, true);
      }

      else
      {
        editor.setPanelDirty(ItemComponentProperties.class, false);
      }
    }
  }

  private boolean compareAbilities(Ability[] current, Ability[] original)
  {
    if (current == null)
    {
      return false;
    }

    if (original == null)
    {
      return false;
    }

    if (current.length != original.length)
    {
      return false;
    }

    for (int i = 0; i < current.length; i++)
    {
      if (current[i] != original[i])
      {
        return false;
      }
    }

    return true;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btLoad;
  private javax.swing.JButton btSave;
  private javax.swing.JButton buttonBrowseImg;
  private javax.swing.JCheckBox cbOnce;
  private javax.swing.JCheckBox cbRole1;
  private javax.swing.JCheckBox cbRole2;
  private javax.swing.JCheckBox cbRole3;
  private javax.swing.JCheckBox cbRole4;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JLabel lbTitle;
  private javax.swing.JList listImages;
  private javax.swing.JPanel pnImgFiles;
  private javax.swing.JPanel pnItemDescription;
  private javax.swing.JPanel pnOnce;
  private javax.swing.JPanel pnRoles;
  private javax.swing.JPanel pnRolesOnce;
  private javax.swing.JScrollPane spDesciption;
  private javax.swing.JScrollPane spImages;
  private javax.swing.JTextArea taDescription;
  private javax.swing.JTextField tfImgPath;
  private javax.swing.JTextField tfTitle;
  // End of variables declaration//GEN-END:variables
}
