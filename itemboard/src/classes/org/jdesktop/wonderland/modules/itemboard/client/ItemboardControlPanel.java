/*
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

/*
 * ItemboardControlPanel.java
 *
 * Created on Jan 29, 2009, 4:55:50 PM
 */
package org.jdesktop.wonderland.modules.itemboard.client;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardToolManager.ItemboardColor;
import org.jdesktop.wonderland.modules.itemboard.client.ItemboardToolManager.ItemboardTool;

/**
 * ItemboardControlPanel taken from the whiteboard module by
 *
 * @author nsimpson
 *
 * extended to display an additional menu item for adding items to the board by
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemboardControlPanel extends javax.swing.JPanel implements CellMenu
{

  private static final Logger logger = Logger.getLogger(ItemboardControlPanel.class.getName());
  protected boolean fillMode = false;
  protected ArrayList<ItemboardCellMenuListener> cellMenuListeners = new ArrayList();
  protected Map toolMappings;
  protected Map colorMappings;
  protected ItemboardDragGestureListener gestureListener;
  protected ItemboardWindow window;

  public ItemboardControlPanel(ItemboardWindow window)
  {
    this.window = window;
    initComponents();
    initButtonMaps();
    initListeners();
    itemButton.setToolTipText("Add Item from your Inventory");
  }

  public void addCellMenuListener(ItemboardCellMenuListener listener)
  {
    cellMenuListeners.add(listener);
  }

  public void removeCellMenuListener(ItemboardCellMenuListener listener)
  {
    cellMenuListeners.remove(listener);
  }

  private void initButtonMaps()
  {
    toolMappings = Collections.synchronizedMap(new HashMap());
    toolMappings.put(ItemboardTool.SELECTOR, selectButton);
    toolMappings.put(ItemboardTool.LINE, lineButton);
    toolMappings.put(ItemboardTool.RECT, rectangleButton);
    toolMappings.put(ItemboardTool.ELLIPSE, ellipseButton);
    toolMappings.put(ItemboardTool.TEXT, textButton);
    toolMappings.put(ItemboardTool.ITEM, itemButton);

    colorMappings = Collections.synchronizedMap(new HashMap());
    colorMappings.put(ItemboardColor.RED, colorRedButton);
    colorMappings.put(ItemboardColor.GREEN, colorGreenButton);
    colorMappings.put(ItemboardColor.BLUE, colorBlueButton);
    colorMappings.put(ItemboardColor.BLACK, colorBlackButton);
    colorMappings.put(ItemboardColor.WHITE, colorWhiteButton);
  }

  private void initListeners()
  {
    DragSource ds = DragSource.getDefaultDragSource();
    gestureListener = new ItemboardDragGestureListener(window);
    ds.createDefaultDragGestureRecognizer(dragButton,
      DnDConstants.ACTION_COPY_OR_MOVE, gestureListener);
  }

  public void setSelectedColor(ItemboardColor color)
  {
    JButton colorButton = (JButton) colorMappings.get(color);
    depressButton(colorButton, true);
  }

  public ItemboardColor getSelectedColor()
  {
    return ItemboardColor.BLACK;
  }

  /**
   * Gets whether a button is depressed
   *
   * @param button the button to check
   * @return true if the button is depressed, false otherwise
   */
  public boolean isButtonDepressed(JButton button)
  {
    return button.isBorderPainted();
  }

  /**
   * Depress/undepress a button
   *
   * @param button the button to depress
   * @param depress true to depress a button, false to undepress
   */
  public void depressButton(JButton button, boolean depress)
  {
    button.setBorderPainted(depress);
  }

  public void setFillMode()
  {
    fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardFill32x32.png")));
  }

  public void setDrawMode()
  {
    fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardEditStrokeFill32x32.png")));
  }

  public void setOnHUD(boolean onHUD)
  {
    if (onHUD)
    {
      toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDock32x32.png")));
    }
    else
    {
      toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardUndock32x32.png")));
    }
  }

  public void setSynced(boolean synced)
  {
    if (synced == true)
    {
      syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardSync32x32.png")));
    }
    else
    {
      syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardUnsync32x32.png")));
    }
  }

  public void selectTool(ItemboardTool tool)
  {
    JButton button = (JButton) toolMappings.get(tool);
    if (button != null)
    {
      depressButton(button, true);
    }
  }

  public void deselectTool(ItemboardTool tool)
  {
    JButton button = (JButton) toolMappings.get(tool);
    if (button != null)
    {
      depressButton(button, false);
    }
  }

  public void selectColor(ItemboardColor color)
  {
    JButton button = (JButton) colorMappings.get(color);
    if (button != null)
    {
      depressButton(button, true);
    }
  }

  public void deselectColor(ItemboardColor color)
  {
    JButton button = (JButton) colorMappings.get(color);
    if (button != null)
    {
      depressButton(button, false);
    }
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

    toggleHUDButton = new javax.swing.JButton();
    newButton = new javax.swing.JButton();
    selectButton = new javax.swing.JButton();
    lineButton = new javax.swing.JButton();
    rectangleButton = new javax.swing.JButton();
    ellipseButton = new javax.swing.JButton();
    textButton = new javax.swing.JButton();
    itemButton = new javax.swing.JButton();
    fillButton = new javax.swing.JButton();
    colorRedButton = new javax.swing.JButton();
    colorGreenButton = new javax.swing.JButton();
    colorBlueButton = new javax.swing.JButton();
    colorBlackButton = new javax.swing.JButton();
    colorWhiteButton = new javax.swing.JButton();
    syncButton = new javax.swing.JButton();
    dragButton = new javax.swing.JButton();

    setBackground(new java.awt.Color(231, 230, 230));

    toggleHUDButton.setBackground(new java.awt.Color(231, 230, 230));
    toggleHUDButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardDock32x32.png"))); // NOI18N
    toggleHUDButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    toggleHUDButton.setBorderPainted(false);
    toggleHUDButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    toggleHUDButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        toggleHUDButtonActionPerformed(evt);
      }
    });

    newButton.setBackground(new java.awt.Color(231, 230, 230));
    newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardNewDocument32x32.png"))); // NOI18N
    newButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    newButton.setBorderPainted(false);
    newButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    newButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        newButtonActionPerformed(evt);
      }
    });

    selectButton.setBackground(new java.awt.Color(231, 230, 230));
    selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardSelect32x32.png"))); // NOI18N
    selectButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    selectButton.setBorderPainted(false);
    selectButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    selectButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        selectButtonActionPerformed(evt);
      }
    });

    lineButton.setBackground(new java.awt.Color(231, 230, 230));
    lineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardDrawLine32x32.png"))); // NOI18N
    lineButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    lineButton.setBorderPainted(false);
    lineButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    lineButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        lineButtonActionPerformed(evt);
      }
    });

    rectangleButton.setBackground(new java.awt.Color(231, 230, 230));
    rectangleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardDrawRectangle32x32.png"))); // NOI18N
    rectangleButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    rectangleButton.setBorderPainted(false);
    rectangleButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    rectangleButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rectangleButtonActionPerformed(evt);
      }
    });

    ellipseButton.setBackground(new java.awt.Color(231, 230, 230));
    ellipseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardDrawEllipse32x32.png"))); // NOI18N
    ellipseButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    ellipseButton.setBorderPainted(false);
    ellipseButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    ellipseButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        ellipseButtonActionPerformed(evt);
      }
    });

    textButton.setBackground(new java.awt.Color(231, 230, 230));
    textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardAddText32x32.png"))); // NOI18N
    textButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    textButton.setBorderPainted(false);
    textButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    textButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        textButtonActionPerformed(evt);
      }
    });

    itemButton.setBackground(new java.awt.Color(231, 230, 230));
    itemButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardAddItem32x32.png"))); // NOI18N
    itemButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    itemButton.setBorderPainted(false);
    itemButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    itemButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        itemButtonActionPerformed(evt);
      }
    });

    fillButton.setBackground(new java.awt.Color(231, 230, 230));
    fillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardEditStrokeFill32x32.png"))); // NOI18N
    fillButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    fillButton.setBorderPainted(false);
    fillButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    fillButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        fillButtonActionPerformed(evt);
      }
    });

    colorRedButton.setBackground(new java.awt.Color(231, 230, 230));
    colorRedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/red.png"))); // NOI18N
    colorRedButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorRedButton.setBorderPainted(false);
    colorRedButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    colorRedButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        colorRedButtonActionPerformed(evt);
      }
    });

    colorGreenButton.setBackground(new java.awt.Color(231, 230, 230));
    colorGreenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/green.png"))); // NOI18N
    colorGreenButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorGreenButton.setBorderPainted(false);
    colorGreenButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    colorGreenButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        colorGreenButtonActionPerformed(evt);
      }
    });

    colorBlueButton.setBackground(new java.awt.Color(231, 230, 230));
    colorBlueButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/blue.png"))); // NOI18N
    colorBlueButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorBlueButton.setBorderPainted(false);
    colorBlueButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    colorBlueButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        colorBlueButtonActionPerformed(evt);
      }
    });

    colorBlackButton.setBackground(new java.awt.Color(231, 230, 230));
    colorBlackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/black.png"))); // NOI18N
    colorBlackButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorBlackButton.setBorderPainted(false);
    colorBlackButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    colorBlackButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        colorBlackButtonActionPerformed(evt);
      }
    });

    colorWhiteButton.setBackground(new java.awt.Color(231, 230, 230));
    colorWhiteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/white.png"))); // NOI18N
    colorWhiteButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorWhiteButton.setBorderPainted(false);
    colorWhiteButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    colorWhiteButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        colorWhiteButtonActionPerformed(evt);
      }
    });

    syncButton.setBackground(new java.awt.Color(231, 230, 230));
    syncButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardSync32x32.png"))); // NOI18N
    syncButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    syncButton.setBorderPainted(false);
    syncButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
    syncButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        syncButtonActionPerformed(evt);
      }
    });

    dragButton.setBackground(new java.awt.Color(231, 230, 230));
    dragButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/itemboard/client/resources/WhiteboardDrag32x32.png"))); // NOI18N
    dragButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    dragButton.setBorderPainted(false);
    dragButton.setMargin(new java.awt.Insets(0, -4, 0, -4));

    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(layout.createSequentialGroup()
        .add(3, 3, 3)
        .add(toggleHUDButton)
        .add(0, 0, 0)
        .add(newButton)
        .add(0, 0, 0)
        .add(selectButton)
        .add(0, 0, 0)
        .add(lineButton)
        .add(0, 0, 0)
        .add(rectangleButton)
        .add(0, 0, 0)
        .add(ellipseButton)
        .add(0, 0, 0)
        .add(textButton)
        .add(0, 0, 0)
        .add(fillButton)
        .add(0, 0, 0)
        .add(colorRedButton)
        .add(0, 0, 0)
        .add(colorGreenButton)
        .add(0, 0, 0)
        .add(colorBlueButton)
        .add(0, 0, 0)
        .add(colorBlackButton)
        .add(0, 0, 0)
        .add(colorWhiteButton)
        .add(0, 0, 0)
        .add(syncButton)
        .add(0, 0, 0)
        .add(dragButton)
        .add(0, 0, 0)
        .add(itemButton)
        .add(6, 6, 6))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(toggleHUDButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(selectButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(lineButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(rectangleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(ellipseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(fillButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(colorRedButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(colorGreenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(colorBlueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(colorBlackButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(colorWhiteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
        .add(syncButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .add(dragButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .add(itemButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.newDoc();
    }
}//GEN-LAST:event_newButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
    if (!isButtonDepressed(selectButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.selector();
      }
    }
}//GEN-LAST:event_selectButtonActionPerformed

    private void lineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineButtonActionPerformed
    if (!isButtonDepressed(lineButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.line();
      }
    }
}//GEN-LAST:event_lineButtonActionPerformed

    private void rectangleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rectangleButtonActionPerformed
    if (!isButtonDepressed(rectangleButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.rect();
      }
    }
}//GEN-LAST:event_rectangleButtonActionPerformed

    private void ellipseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseButtonActionPerformed
    if (!isButtonDepressed(ellipseButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.ellipse();
      }
    }
}//GEN-LAST:event_ellipseButtonActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
    if (!isButtonDepressed(textButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.text();
      }
    }
}//GEN-LAST:event_textButtonActionPerformed

    private void itemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemButtonActionPerformed
    if (!isButtonDepressed(itemButton))
    {
      Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
      while (iter.hasNext())
      {
        ItemboardCellMenuListener listener = iter.next();
        listener.item();
      }
    }
}//GEN-LAST:event_itemButtonActionPerformed

    private void fillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      fillMode = !fillMode;   // toggle between fill and draw modes
      if (fillMode == true)
      {
        listener.fill();
      }
      else
      {
        listener.draw();
      }
    }
}//GEN-LAST:event_fillButtonActionPerformed

    private void colorRedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorRedButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.red();
    }
}//GEN-LAST:event_colorRedButtonActionPerformed

    private void colorGreenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorGreenButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.green();
    }
}//GEN-LAST:event_colorGreenButtonActionPerformed

    private void colorBlueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBlueButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.blue();
    }
}//GEN-LAST:event_colorBlueButtonActionPerformed

    private void colorBlackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBlackButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.black();
    }
}//GEN-LAST:event_colorBlackButtonActionPerformed

    private void colorWhiteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorWhiteButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.white();
    }
}//GEN-LAST:event_colorWhiteButtonActionPerformed

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.sync();
    }
}//GEN-LAST:event_syncButtonActionPerformed

    private void toggleHUDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleHUDButtonActionPerformed
    Iterator<ItemboardCellMenuListener> iter = cellMenuListeners.iterator();
    while (iter.hasNext())
    {
      ItemboardCellMenuListener listener = iter.next();
      listener.toggleHUD();
    }
}//GEN-LAST:event_toggleHUDButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton colorBlackButton;
  private javax.swing.JButton colorBlueButton;
  private javax.swing.JButton colorGreenButton;
  private javax.swing.JButton colorRedButton;
  private javax.swing.JButton colorWhiteButton;
  private javax.swing.JButton dragButton;
  private javax.swing.JButton ellipseButton;
  private javax.swing.JButton fillButton;
  private javax.swing.JButton lineButton;
  private javax.swing.JButton newButton;
  private javax.swing.JButton rectangleButton;
  private javax.swing.JButton selectButton;
  private javax.swing.JButton syncButton;
  private javax.swing.JButton itemButton;
  private javax.swing.JButton textButton;
  private javax.swing.JButton toggleHUDButton;
  // End of variables declaration//GEN-END:variables
}
