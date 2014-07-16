/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.quiz.common.Quiz;
import org.jdesktop.wonderland.modules.quiz.common.Quiz.Question;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentServerState;

/**
 *
 * @author Lisa Tomes
 */
@PropertiesFactory(QuizComponentServerState.class)
public class QuizComponentProperties extends javax.swing.JPanel implements PropertiesFactorySPI
{

  private CellPropertiesEditor editor = null;

  private Quiz origQuiz;
  private float origX;
  private float origY;
  private float origZ;
  private float origLook;

  DefaultListModel<Question> dlm;
  DefaultComboBoxModel<Question.QUESTIONTYPE> dcbm;

  ArrayList<JTextField> answerTextFields;
  ArrayList<JCheckBox> answerCheckBoxes;

  public QuizComponentProperties()
  {
    initComponents();

    dlm = new DefaultListModel<Question>();
    jlQuestions.setModel(dlm);

    btAdd.setText("Add Question");
    btRemove.setText("Remove Question");
    btAdd.setSize(btRemove.getSize());

    dcbm = new DefaultComboBoxModel<Question.QUESTIONTYPE>();
    dcbm.addElement(Question.QUESTIONTYPE.MULTIPLE_CHOICE);
    cbQuestionType.setModel(dcbm);

    answerTextFields = new ArrayList<JTextField>();
    answerTextFields.add(tfAnswer1);
    answerTextFields.add(tfAnswer2);
    answerTextFields.add(tfAnswer3);
    answerTextFields.add(tfAnswer4);
    answerTextFields.add(tfAnswer5);
    answerTextFields.add(tfAnswer6);

    answerCheckBoxes = new ArrayList<JCheckBox>();
    answerCheckBoxes.add(cbAnswer1);
    answerCheckBoxes.add(cbAnswer2);
    answerCheckBoxes.add(cbAnswer3);
    answerCheckBoxes.add(cbAnswer4);
    answerCheckBoxes.add(cbAnswer5);
    answerCheckBoxes.add(cbAnswer6);

    tfX.setText("0");
    tfY.setText("0");
    tfZ.setText("0");
    tfLook.setText("0");

    tfX.getDocument().addDocumentListener(new InfoTextFieldListener());
    tfY.getDocument().addDocumentListener(new InfoTextFieldListener());
    tfZ.getDocument().addDocumentListener(new InfoTextFieldListener());
    tfLook.getDocument().addDocumentListener(new InfoTextFieldListener());
  }

  @Override
  public String getDisplayName()
  {
    return "Manage Quiz";
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
    CellComponentServerState compState = state.getComponentServerState(QuizComponentServerState.class);
    if (compState != null)
    {
      QuizComponentServerState quizCompState = (QuizComponentServerState) compState;

      origQuiz = quizCompState.getQuiz();
      if (origQuiz != null)
      {
        TitledBorder border = (TitledBorder) lbQuiz.getBorder();
        border.setTitle("Quiz: " + origQuiz.getName());
        dlm.clear();
        ArrayList<Question> questions = origQuiz.getQuestions();
        for (Question question : questions)
        {
          dlm.addElement(question);
        }
      }

      Vector3f origin = quizCompState.getLocation();
      if (origin != null)
      {
        origX = origin.getX();
        origY = origin.getY();
        origZ = origin.getZ();
      }
      else
      {
        origX = 0.0f;
        origY = 0.0f;
        origZ = 0.0f;
      }

      Quaternion lookAt = quizCompState.getLook();
      if (lookAt != null)
      {
        float lookDirection = (float) Math.toDegrees(lookAt.toAngleAxis(new Vector3f()));
        origLook = lookDirection;
      }
      else
      {
        origLook = 0.0f;
      }
    }
  }

  @Override
  public void close()
  {
    // Do nothing
  }

  @Override
  public void restore()
  {
    // TODO: Set quiz textfields

    tfX.setText(String.valueOf(origX));
    tfY.setText(String.valueOf(origY));
    tfZ.setText(String.valueOf(origZ));

    tfLook.setText(String.valueOf(origLook));
  }

  @Override
  public void apply()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState = state.getComponentServerState(QuizComponentServerState.class);

    if (compState != null)
    {
      QuizComponentServerState quizCompState = (QuizComponentServerState) compState;

      //Update values in Server State
      Quiz localQuiz = Quiz.sampleQuiz(); // TODO: Read real Quiz
      quizCompState.setQuiz(localQuiz);
      System.out.println("Set sample Quiz.");

      Vector3f localLocation = new Vector3f(
        Float.parseFloat(tfX.getText()),
        Float.parseFloat(tfY.getText()),
        Float.parseFloat(tfZ.getText()));
      quizCompState.setLocation(localLocation);

      // Set the destination look direction from the text field. If the text
      // field is empty, then set the server state as a zero rotation.
      Quaternion look = new Quaternion();
      Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);
      float angle = (float) Math.toRadians(Float.parseFloat(tfLook.getText()));
      look.fromAngleAxis((float) angle, axis);
      quizCompState.setLook(look);

      editor.addToUpdateList(compState);
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

  private void checkDirty()
  {
    if (editor != null)
    {
      editor.setPanelDirty(QuizComponentProperties.class, isDirty());
    }
  }

  private boolean isDirty()
  {
    return ((tfX.getText() == null ? String.valueOf(origX) != null : !tfX.getText().equals(String.valueOf(origX)))
      || (tfY.getText() == null ? String.valueOf(origY) != null : !tfY.getText().equals(String.valueOf(origY)))
      || (tfZ.getText() == null ? String.valueOf(origZ) != null : !tfZ.getText().equals(String.valueOf(origZ)))
      || (tfLook.getText() == null ? String.valueOf(origLook) != null : !tfLook.getText().equals(String.valueOf(origLook))));
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

    lbQuiz = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jlQuestions = new javax.swing.JList();
    btAdd = new javax.swing.JButton();
    btRemove = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    tfQuestionTitle = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    tfQuestionText = new javax.swing.JTextField();
    jLabel8 = new javax.swing.JLabel();
    cbQuestionType = new javax.swing.JComboBox();
    jPanel4 = new javax.swing.JPanel();
    jLabel7 = new javax.swing.JLabel();
    tfAnswer1 = new javax.swing.JTextField();
    cbAnswer1 = new javax.swing.JCheckBox();
    jLabel12 = new javax.swing.JLabel();
    tfAnswer4 = new javax.swing.JTextField();
    cbAnswer4 = new javax.swing.JCheckBox();
    jLabel9 = new javax.swing.JLabel();
    tfAnswer2 = new javax.swing.JTextField();
    cbAnswer2 = new javax.swing.JCheckBox();
    jLabel11 = new javax.swing.JLabel();
    tfAnswer5 = new javax.swing.JTextField();
    cbAnswer5 = new javax.swing.JCheckBox();
    jLabel10 = new javax.swing.JLabel();
    tfAnswer3 = new javax.swing.JTextField();
    cbAnswer3 = new javax.swing.JCheckBox();
    jLabel13 = new javax.swing.JLabel();
    tfAnswer6 = new javax.swing.JTextField();
    cbAnswer6 = new javax.swing.JCheckBox();
    lbTeleport = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    tfX = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    tfY = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    tfZ = new javax.swing.JTextField();
    jLabel5 = new javax.swing.JLabel();
    tfLook = new javax.swing.JTextField();

    lbQuiz.setBorder(javax.swing.BorderFactory.createTitledBorder("Quiz:"));

    jlQuestions.setModel(new javax.swing.AbstractListModel()
    {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jlQuestions.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        jlQuestionsValueChanged(evt);
      }
    });
    jScrollPane1.setViewportView(jlQuestions);

    btAdd.setText("jButton1");
    btAdd.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btAddActionPerformed(evt);
      }
    });

    btRemove.setText("jButton2");

    jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jLabel1.setText("Title: ");

    jLabel6.setText("Text: ");

    jLabel8.setText("Typ: ");

    cbQuestionType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jPanel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    jPanel4.setLayout(new java.awt.GridLayout(3, 2));

    jLabel7.setText("Answer 1: ");
    jPanel4.add(jLabel7);
    jPanel4.add(tfAnswer1);
    jPanel4.add(cbAnswer1);

    jLabel12.setText("Answer 4: ");
    jPanel4.add(jLabel12);
    jPanel4.add(tfAnswer4);
    jPanel4.add(cbAnswer4);

    jLabel9.setText("Answer 2: ");
    jPanel4.add(jLabel9);
    jPanel4.add(tfAnswer2);
    jPanel4.add(cbAnswer2);

    jLabel11.setText("Answer 5: ");
    jPanel4.add(jLabel11);
    jPanel4.add(tfAnswer5);
    jPanel4.add(cbAnswer5);

    jLabel10.setText("Answer 3: ");
    jPanel4.add(jLabel10);
    jPanel4.add(tfAnswer3);
    jPanel4.add(cbAnswer3);

    jLabel13.setText("Answer 6: ");
    jPanel4.add(jLabel13);
    jPanel4.add(tfAnswer6);
    jPanel4.add(cbAnswer6);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel6))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(tfQuestionTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbQuestionType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addComponent(tfQuestionText))))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(tfQuestionTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel8)
          .addComponent(cbQuestionType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(tfQuestionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout lbQuizLayout = new javax.swing.GroupLayout(lbQuiz);
    lbQuiz.setLayout(lbQuizLayout);
    lbQuizLayout.setHorizontalGroup(
      lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(lbQuizLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(lbQuizLayout.createSequentialGroup()
            .addComponent(jScrollPane1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btAdd, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(btRemove, javax.swing.GroupLayout.Alignment.TRAILING))))
        .addContainerGap())
    );
    lbQuizLayout.setVerticalGroup(
      lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(lbQuizLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(lbQuizLayout.createSequentialGroup()
            .addComponent(btAdd)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btRemove))
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    lbTeleport.setBorder(javax.swing.BorderFactory.createTitledBorder("Teleport to:"));
    lbTeleport.setLayout(new java.awt.GridLayout(2, 4));

    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel2.setText("x: ");
    lbTeleport.add(jLabel2);
    lbTeleport.add(tfX);

    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel3.setText("y: ");
    lbTeleport.add(jLabel3);
    lbTeleport.add(tfY);

    jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel4.setText("z: ");
    lbTeleport.add(jLabel4);
    lbTeleport.add(tfZ);

    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel5.setText("look: ");
    lbTeleport.add(jLabel5);
    lbTeleport.add(tfLook);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lbQuiz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lbTeleport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lbQuiz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lbTeleport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jlQuestionsValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jlQuestionsValueChanged
  {//GEN-HEADEREND:event_jlQuestionsValueChanged
    int selIndex = jlQuestions.getSelectedIndex();
    if (selIndex > -1 && selIndex < dlm.getSize())
    {
      Question selQuestion = dlm.get(selIndex);

      tfQuestionTitle.setText(selQuestion.getTitle());
      cbQuestionType.setSelectedItem(selQuestion.getType());
      tfQuestionText.setText(selQuestion.getText());

      for (JTextField textField : answerTextFields)
      {
        textField.setText("");
      }

      for (JCheckBox checkBox : answerCheckBoxes)
      {
        checkBox.setSelected(false);
      }

      HashMap<String, Boolean> answers = selQuestion.getAnswers();
      Set<Map.Entry<String, Boolean>> entrySet = answers.entrySet();
      int index = 0;
      for (Map.Entry<String, Boolean> entry : entrySet)
      {
        answerTextFields.get(index).setText(entry.getKey());
        answerCheckBoxes.get(index).setSelected(entry.getValue());
        index++;

        if (index == 6)
        {
          break; // we only have 6 text fields / checkboxes
        }
      }
    }
  }//GEN-LAST:event_jlQuestionsValueChanged

  private void btAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btAddActionPerformed
  {//GEN-HEADEREND:event_btAddActionPerformed
    // TODO
  }//GEN-LAST:event_btAddActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btAdd;
  private javax.swing.JButton btRemove;
  private javax.swing.JCheckBox cbAnswer1;
  private javax.swing.JCheckBox cbAnswer2;
  private javax.swing.JCheckBox cbAnswer3;
  private javax.swing.JCheckBox cbAnswer4;
  private javax.swing.JCheckBox cbAnswer5;
  private javax.swing.JCheckBox cbAnswer6;
  private javax.swing.JComboBox cbQuestionType;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel11;
  private javax.swing.JLabel jLabel12;
  private javax.swing.JLabel jLabel13;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList jlQuestions;
  private javax.swing.JPanel lbQuiz;
  private javax.swing.JPanel lbTeleport;
  private javax.swing.JTextField tfAnswer1;
  private javax.swing.JTextField tfAnswer2;
  private javax.swing.JTextField tfAnswer3;
  private javax.swing.JTextField tfAnswer4;
  private javax.swing.JTextField tfAnswer5;
  private javax.swing.JTextField tfAnswer6;
  private javax.swing.JTextField tfLook;
  private javax.swing.JTextField tfQuestionText;
  private javax.swing.JTextField tfQuestionTitle;
  private javax.swing.JTextField tfX;
  private javax.swing.JTextField tfY;
  private javax.swing.JTextField tfZ;
  // End of variables declaration//GEN-END:variables
}
