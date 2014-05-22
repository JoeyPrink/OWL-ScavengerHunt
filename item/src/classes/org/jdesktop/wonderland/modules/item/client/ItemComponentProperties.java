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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentServerState;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
@PropertiesFactory(ItemComponentServerState.class)
public class ItemComponentProperties extends javax.swing.JPanel implements PropertiesFactorySPI
{

  private CellPropertiesEditor editor = null;
  private String originalXmlPath;
  private String originalImagePath;
  private Ability[] originalAbilities;

  private final String subDirNameXml = "xml";
  private final String subDirNameImg = "images";

  private final JCheckBox[] boxes;

  public ItemComponentProperties()
  {
    initComponents();

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
    cbRoleAll.addActionListener(new AbilityCheckBoxListener());

    txtPath.getDocument().addDocumentListener(new InfoTextFieldListener());
    imgPath.getDocument().addDocumentListener(new InfoTextFieldListener());

    buttonBrowse.setText("Browse XML");
    buttonBrowseImg.setText("Browse Images");
  }

  @Override
  public String getDisplayName()
  {
    return "Item properties";
    //return "Object information";
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

  @Override
  public void open()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState
      = state.getComponentServerState(ItemComponentServerState.class);

    if (compState != null)
    {
      ItemComponentServerState itemState = (ItemComponentServerState) compState;

      originalXmlPath = itemState.getXmlPath();
      txtPath.setText(originalXmlPath);

      originalImagePath = itemState.getImgPath();
      imgPath.setText(originalImagePath);

      originalAbilities = itemState.getAbilities();
      //printAbilities(originalAbilities, "This are the original abilities:");

      updateLists();
      setCheckBoxes(originalAbilities);
    }
  }

  private void updateLists()
  {
    updateXMLList();
    updateImageList();
  }

  private void updateXMLList()
  {
    loadList(listFiles, subDirNameXml);
    selectCurrentElement(listFiles, originalXmlPath);
  }

  private void updateImageList()
  {
    loadList(listImages, subDirNameImg);
    selectCurrentElement(listImages, originalImagePath);
  }

  private void loadList(JList list, String subDirName)
  {
    DefaultListModel listModel = new DefaultListModel();

    try
    {
      ContentCollection fileRoot = getFileRoot(subDirName);
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
    list.setModel(listModel);
  }

  private void selectCurrentElement(JList list, String originalFilePath)
  {
    if (originalFilePath == null)
    {
      return;
    }

    String curr = originalFilePath.substring(originalFilePath.lastIndexOf("/") + 1);

    int numberOfElements = list.getModel().getSize();
    for (int i = 0; i < numberOfElements; i++)
    {
      String element = (String) list.getModel().getElementAt(i);
      if (element.equals(curr))
      {
        list.setSelectedIndex(i);
      }
    }
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
  public void close()
  {
  }

  @Override
  public void restore()
  {
    txtPath.setText(originalXmlPath);
    selectCurrentElement(listFiles, originalXmlPath);

    imgPath.setText(originalImagePath);
    selectCurrentElement(listImages, originalImagePath);

    //printAbilities(originalAbilities, "This are the original abilities:");
    setCheckBoxes(originalAbilities);
  }

  @Override
  public void apply()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState
      = state.getComponentServerState(ItemComponentServerState.class);

    // Update values in Server State
    ((ItemComponentServerState) compState).setXmlPath(txtPath.getText());
    ((ItemComponentServerState) compState).setImgPath(imgPath.getText());

    Ability[] newAbilites = getSelectedAbilities();
    //printAbilities(newAbilites, "This are the currently selected abilities:");

    ((ItemComponentServerState) compState).setAbilities(newAbilites);

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

    jLabel1 = new javax.swing.JLabel();
    txtPath = new javax.swing.JTextField();
    jScrollPaneFiles = new javax.swing.JScrollPane();
    listFiles = new javax.swing.JList();
    buttonBrowse = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    imgPath = new javax.swing.JTextField();
    jScrollPaneImages = new javax.swing.JScrollPane();
    listImages = new javax.swing.JList();
    buttonBrowseImg = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    cbRole1 = new javax.swing.JCheckBox();
    cbRole2 = new javax.swing.JCheckBox();
    cbRole3 = new javax.swing.JCheckBox();
    cbRole4 = new javax.swing.JCheckBox();
    cbRoleAll = new javax.swing.JCheckBox();

    jLabel1.setText("Path to item description file:");

    listFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    listFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        listFilesValueChanged(evt);
      }
    });
    jScrollPaneFiles.setViewportView(listFiles);

    buttonBrowse.setText("Browse XML");
    buttonBrowse.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonBrowseActionPerformed(evt);
      }
    });

    jLabel2.setText("Want to add an image?");

    listImages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    listImages.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        listImagesValueChanged(evt);
      }
    });
    jScrollPaneImages.setViewportView(listImages);

    buttonBrowseImg.setText("Browse Images");
    buttonBrowseImg.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonBrowseImgActionPerformed(evt);
      }
    });

    jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel3.setText("Who can pick it up?");

    cbRole1.setText("Role1");

    cbRole2.setText("Role2");

    cbRole3.setText("Role3");

    cbRole4.setText("Role4");

    cbRoleAll.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onClickEverybody(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(cbRoleAll)
          .addComponent(cbRole4)
          .addComponent(cbRole3)
          .addComponent(jLabel3)
          .addComponent(cbRole1)
          .addComponent(cbRole2))
        .addContainerGap(16, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(cbRoleAll)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbRole1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbRole2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbRole3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbRole4)
        .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(imgPath)
          .addComponent(txtPath, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonBrowse, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonBrowseImg, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPaneFiles, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPaneImages))
        .addGap(18, 18, 18)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPaneFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(buttonBrowse)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(imgPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPaneImages, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonBrowseImg)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Fetches the user's root directory for all xml files using the current
   * primary server.
   *
   * Code based on sample cell and learning poster by
   *
   * @author Jordan Slott <jslott@dev.java.net>
   * @author Ronny Standtke <ronny.standtke@fhnw.ch>
   * @author Johanna Pirker <jpirker@iicm.edu>
   *
   * adapted by
   *
   * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
   * @param subDirName
   *
   * @return the user's root directory using the current primary server
   * @throws
   * org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException
   */
  public ContentCollection getFileRoot(String subDirName) throws ContentRepositoryException
  {
    ContentRepositoryRegistry r = ContentRepositoryRegistry.getInstance();
    ServerSessionManager session = LoginManager.getPrimary();

    // Try to find the desired sub-directory or create it if it doesn't exist
    ContentCollection userRoot = r.getRepository(session).getUserRoot();
    ContentNode node = (ContentNode) userRoot.getChild(subDirName);
    if (node == null)
    {
      node = (ContentNode) userRoot.createChild(subDirName, Type.COLLECTION);
    }
    else
    {
      if (!(node instanceof ContentCollection))
      {
        node.getParent().removeChild(subDirName);
        node = (ContentNode) userRoot.createChild(subDirName, Type.COLLECTION);
      }
    }

    return (ContentCollection) node;
  }

  private URL uploadFileToServer(File file, String subDirName)
    throws ContentRepositoryException, IOException
  {
    String fileName = file.getName();

    if (file.length() > 100000) // 100 KB
    {
      System.out.println("Item upload error: File too large!");
      return null;
    }

    ContentCollection fileRoot = getFileRoot(subDirName);

    ContentNode resource = fileRoot.getChild(fileName);
    if (resource == null)
    {
      resource = fileRoot.createChild(fileName, Type.RESOURCE);
    }
    else
    {
      if (!(resource instanceof ContentResource))
      {
        resource.getParent().removeChild(fileName);
        resource = fileRoot.createChild(fileName, Type.RESOURCE);
      }
    }

    // Here the upload-magic happens
    ((ContentResource) resource).put(file);

    return ((ContentResource) resource).getURL();
  }

  private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonBrowseActionPerformed
  {//GEN-HEADEREND:event_buttonBrowseActionPerformed
    FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
    File xmlFile = browse(xmlFilter);

    if (xmlFile != null)
    {
      try
      {
        URL url = uploadFileToServer(xmlFile, subDirNameXml);
        if (url != null)
        {
          updateXMLList();
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
  }//GEN-LAST:event_buttonBrowseActionPerformed

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

  private void listFilesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_listFilesValueChanged
  {//GEN-HEADEREND:event_listFilesValueChanged
    String fileName = (String) listFiles.getSelectedValue();
    if (fileName == null)
    {
      return;
    }

    boolean found = false;
    try
    {
      ContentCollection fileRoot = getFileRoot(subDirNameXml);
      List<ContentNode> children = fileRoot.getChildren();

      for (ContentNode child : children)
      {
        if (child.getName().equals(fileName))
        {
          // Only one slash, because there is already one at the beginning
          // of child path
          txtPath.setText("wlcontent:/" + child.getPath());
          found = true;
        }
      }

      if (!found)
      {
        setSorryText();
      }
    }
    catch (ContentRepositoryException ex)
    {
      setSorryText();
    }
  }//GEN-LAST:event_listFilesValueChanged

  private void setSorryText()
  {
    txtPath.setText("Sorry, couldn't load file path.");
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
        URL url = uploadFileToServer(imgFile, subDirNameImg);
        if (url != null)
        {
          updateImageList();
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

  private void listImagesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_listImagesValueChanged
  {//GEN-HEADEREND:event_listImagesValueChanged
    String fileName = (String) listImages.getSelectedValue();
    if (fileName == null)
    {
      return;
    }

    boolean found = false;
    try
    {
      ContentCollection fileRoot = getFileRoot(subDirNameImg);
      List<ContentNode> children = fileRoot.getChildren();

      for (ContentNode child : children)
      {
        if (child.getName().equals(fileName))
        {
          // Only one slash, because there is already one at the beginning
          // of child path
          imgPath.setText("wlcontent:/" + child.getPath());
          found = true;
        }
      }

      if (!found)
      {
        setSorryText();
      }
    }
    catch (ContentRepositoryException ex)
    {
      setSorryText();
    }
  }//GEN-LAST:event_listImagesValueChanged

  private void onClickEverybody(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onClickEverybody
  {//GEN-HEADEREND:event_onClickEverybody
    boolean everybody = cbRoleAll.isSelected();

    if (everybody)
    {
      cbRole1.setSelected(true);
      cbRole2.setSelected(true);
      cbRole3.setSelected(true);
      cbRole4.setSelected(true);
    }
    else
    {
      cbRole1.setSelected(false);
      cbRole2.setSelected(false);
      cbRole3.setSelected(false);
      cbRole4.setSelected(false);
    }
  }//GEN-LAST:event_onClickEverybody

  private void printAbilities(Ability[] abilities, String info)
  {
    if (abilities == null)
    {
      System.out.println("  null");
      return;
    }

    for (Ability ability : abilities)
    {
      System.out.println("  " + Abilities.getIntFromAbility(ability));
    }
  }

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

      if (!same || !txtPath.getText().equals(originalXmlPath)
        || !imgPath.getText().equals(originalImagePath))
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
  private javax.swing.JButton buttonBrowse;
  private javax.swing.JButton buttonBrowseImg;
  private javax.swing.JCheckBox cbRole1;
  private javax.swing.JCheckBox cbRole2;
  private javax.swing.JCheckBox cbRole3;
  private javax.swing.JCheckBox cbRole4;
  private javax.swing.JCheckBox cbRoleAll;
  private javax.swing.JTextField imgPath;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPaneFiles;
  private javax.swing.JScrollPane jScrollPaneImages;
  private javax.swing.JList listFiles;
  private javax.swing.JList listImages;
  private javax.swing.JTextField txtPath;
  // End of variables declaration//GEN-END:variables
}