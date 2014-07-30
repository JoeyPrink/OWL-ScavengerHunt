/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
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
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
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

  private Quiz tempQuiz;
  private float tempX;
  private float tempY;
  private float tempZ;
  private float tempLook;

  DefaultListModel<Question> questionListModel;
  DefaultComboBoxModel<Question.QUESTIONTYPE> questionTypeComboBoxModel;

  List<JTextField> answerTextFields;
  List<JCheckBox> answerCheckBoxes;

  private int currIndex;

  public QuizComponentProperties()
  {
    initComponents();

    currIndex = -1;

    //Init List- and ComboBoxModel
    questionListModel = new DefaultListModel<Question>();
    jlQuestions.setModel(questionListModel);

    questionTypeComboBoxModel = new DefaultComboBoxModel<Question.QUESTIONTYPE>();
    questionTypeComboBoxModel.addElement(Question.QUESTIONTYPE.MULTIPLE_CHOICE);
    cbQuestionType.setModel(questionTypeComboBoxModel);

    //Fill arrays for easy access to all textfields/checkboxes
    answerTextFields = Collections.synchronizedList(new ArrayList<JTextField>());
    answerTextFields.add(tfAnswer1);
    answerTextFields.add(tfAnswer2);
    answerTextFields.add(tfAnswer3);
    answerTextFields.add(tfAnswer4);
    answerTextFields.add(tfAnswer5);
    answerTextFields.add(tfAnswer6);

    answerCheckBoxes = Collections.synchronizedList(new ArrayList<JCheckBox>());
    answerCheckBoxes.add(cbAnswer1);
    answerCheckBoxes.add(cbAnswer2);
    answerCheckBoxes.add(cbAnswer3);
    answerCheckBoxes.add(cbAnswer4);
    answerCheckBoxes.add(cbAnswer5);
    answerCheckBoxes.add(cbAnswer6);

    //Init textfields
    tfX.setText("0.0");
    tfY.setText("0.0");
    tfZ.setText("0.0");
    tfLook.setText("0.0");

    btAdd.setText("Add Question");
    btRemove.setText("Remove Question");
    // Make them the same size
    btAdd.setSize(btRemove.getSize());

    btLoadQuiz.setText("Load Quiz");
    btSaveQuiz.setText("Save Quiz");

    lbSaved.setText(" ");

    //Add document and action listeners
    tfQuestionTitle.getDocument().addDocumentListener(new InfoTextFieldListener());
    cbQuestionType.addActionListener(new AnswerCheckBoxesActionListener());
    tfQuestionText.getDocument().addDocumentListener(new InfoTextFieldListener());

    for (JTextField tf : answerTextFields)
    {
      tf.getDocument().addDocumentListener(new InfoTextFieldListener());
    }

    for (JCheckBox cb : answerCheckBoxes)
    {
      cb.addActionListener(new AnswerCheckBoxesActionListener());
    }

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

  private void setQuizValues()
  {
    if (tempQuiz == null)
    {
      return;
    }

    //Set quiz name
    TitledBorder border = (TitledBorder) lbQuiz.getBorder();
    border.setTitle("Quiz: " + tempQuiz.getName());
    lbQuiz.updateUI();

    //Set quiz questions
    questionListModel.clear();
    List<Question> questions = tempQuiz.getQuestions();
    for (Question question : questions)
    {
      questionListModel.addElement(question);
    }

    updateQuestionPanel();
  }

  private void setTeleportValues()
  {
    tfX.setText(String.valueOf(origX));
    tfY.setText(String.valueOf(origY));
    tfZ.setText(String.valueOf(origZ));

    tfLook.setText(String.valueOf(origLook));
  }

  @Override
  public void open()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState = state.getComponentServerState(QuizComponentServerState.class);
    if (compState != null)
    {
      QuizComponentServerState quizCompState = (QuizComponentServerState) compState;

      //Load original quiz
      origQuiz = quizCompState.getQuiz();
      if (origQuiz != null)
      {
        tempQuiz = Quiz.copyQuiz(origQuiz);
      }
      else
      {
        tempQuiz = new Quiz();
      }

      setQuizValues();

      //Load original locacion and look direction
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

      tempX = origX;
      tempY = origY;
      tempZ = origZ;

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

      tempLook = origLook;

      setTeleportValues();
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
    if (origQuiz != null)
    {
      tempQuiz = Quiz.copyQuiz(origQuiz);

      setQuizValues();
    }

    tempX = origX;
    tempY = origY;
    tempZ = origZ;
    tempLook = origLook;

    setTeleportValues();
  }

  @Override
  public void apply()
  {
    CellServerState state = editor.getCellServerState();
    CellComponentServerState compState = state.getComponentServerState(QuizComponentServerState.class);

    if (compState != null)
    {
//      int selIndex = jlQuestions.getSelectedIndex();
//      storeAnswers(selIndex);

      QuizComponentServerState quizCompState = (QuizComponentServerState) compState;

      //Update values in Server State
      Quiz localQuiz = Quiz.copyQuiz(tempQuiz);
      quizCompState.setQuiz(localQuiz);

      Vector3f localLocation = new Vector3f(
        Float.parseFloat((tfX.getText().isEmpty()) ? "0.0" : tfX.getText()),
        Float.parseFloat((tfY.getText().isEmpty()) ? "0.0" : tfY.getText()),
        Float.parseFloat((tfZ.getText().isEmpty()) ? "0.0" : tfZ.getText()));
      quizCompState.setLocation(localLocation);

      // Set the destination look direction from the text field. If the text
      // field is empty, then set the server state as a zero rotation.
      Quaternion look = new Quaternion();
      Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);
      float angle = (float) Math.toRadians(Float.parseFloat((tfLook.getText().isEmpty()) ? "0.0" : tfLook.getText()));
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
      textFieldChanged(e.getDocument().getDefaultRootElement().getName() + " " + e.getType() + " insert");
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
      textFieldChanged(e.getDocument().getDefaultRootElement().getName() + " " + e.getType() + " remove");
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
      textFieldChanged(e.getDocument().getDefaultRootElement().getName() + " " + e.getType() + " update");
    }
  }

  class AnswerCheckBoxesActionListener implements ActionListener
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      textFieldChanged(e.getSource() + " " + e.getActionCommand() + " actionPerformed");
    }

  }

  private synchronized void textFieldChanged(String action)
  {
    try
    {
      tempX = Float.parseFloat((tfX.getText().isEmpty()) ? "0.0" : tfX.getText());
    }
    catch (NumberFormatException ex)
    {
      tempX = 0.0f;
    }

    try
    {
      tempY = Float.parseFloat((tfY.getText().isEmpty()) ? "0.0" : tfY.getText());
    }
    catch (NumberFormatException ex)
    {
      tempY = 0.0f;
    }

    try
    {
      tempZ = Float.parseFloat((tfZ.getText().isEmpty()) ? "0.0" : tfZ.getText());
    }
    catch (NumberFormatException ex)
    {
      tempZ = 0.0f;
    }

    try
    {
      tempLook = Float.parseFloat((tfLook.getText().isEmpty()) ? "0.0" : tfLook.getText());
    }
    catch (NumberFormatException ex)
    {
      tempLook = 0.0f;
    }

    checkDirty();
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
    boolean quizEqual = tempQuiz.equals(origQuiz);

    boolean telelocEqual = (tempX == origX
      && tempY == origY
      && tempZ == origZ
      && tempLook == origLook);

    return (!quizEqual || !telelocEqual);
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
    lbAnswer1 = new javax.swing.JLabel();
    tfAnswer1 = new javax.swing.JTextField();
    cbAnswer1 = new javax.swing.JCheckBox();
    lbAnswer2 = new javax.swing.JLabel();
    tfAnswer2 = new javax.swing.JTextField();
    cbAnswer2 = new javax.swing.JCheckBox();
    lbAnswer3 = new javax.swing.JLabel();
    tfAnswer3 = new javax.swing.JTextField();
    cbAnswer3 = new javax.swing.JCheckBox();
    lbAnswer4 = new javax.swing.JLabel();
    tfAnswer4 = new javax.swing.JTextField();
    cbAnswer4 = new javax.swing.JCheckBox();
    lbAnswer5 = new javax.swing.JLabel();
    tfAnswer5 = new javax.swing.JTextField();
    cbAnswer5 = new javax.swing.JCheckBox();
    lbAnswer6 = new javax.swing.JLabel();
    tfAnswer6 = new javax.swing.JTextField();
    cbAnswer6 = new javax.swing.JCheckBox();
    btApplyAnswers = new javax.swing.JButton();
    lbSaved = new javax.swing.JLabel();
    lbTeleport = new javax.swing.JPanel();
    lbX = new javax.swing.JLabel();
    tfX = new javax.swing.JTextField();
    lbY = new javax.swing.JLabel();
    tfY = new javax.swing.JTextField();
    lbZ = new javax.swing.JLabel();
    tfZ = new javax.swing.JTextField();
    lbLook = new javax.swing.JLabel();
    tfLook = new javax.swing.JTextField();
    btLoadQuiz = new javax.swing.JButton();
    btSaveQuiz = new javax.swing.JButton();

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
    btRemove.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btRemoveActionPerformed(evt);
      }
    });

    jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

    jLabel1.setText("Title: ");

    jLabel6.setText("Text: ");

    jLabel8.setText("Typ: ");

    cbQuestionType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jPanel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    jPanel4.setLayout(new java.awt.GridLayout(3, 2));

    lbAnswer1.setText("Answer 1: ");
    jPanel4.add(lbAnswer1);
    jPanel4.add(tfAnswer1);
    jPanel4.add(cbAnswer1);

    lbAnswer2.setText("Answer 2: ");
    jPanel4.add(lbAnswer2);
    jPanel4.add(tfAnswer2);
    jPanel4.add(cbAnswer2);

    lbAnswer3.setText("Answer 3: ");
    jPanel4.add(lbAnswer3);
    jPanel4.add(tfAnswer3);
    jPanel4.add(cbAnswer3);

    lbAnswer4.setText("Answer 4: ");
    jPanel4.add(lbAnswer4);
    jPanel4.add(tfAnswer4);
    jPanel4.add(cbAnswer4);

    lbAnswer5.setText("Answer 5: ");
    jPanel4.add(lbAnswer5);
    jPanel4.add(tfAnswer5);
    jPanel4.add(cbAnswer5);

    lbAnswer6.setText("Answer 6: ");
    jPanel4.add(lbAnswer6);
    jPanel4.add(tfAnswer6);
    jPanel4.add(cbAnswer6);

    btApplyAnswers.setText("v/");
    btApplyAnswers.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btApplyAnswersActionPerformed(evt);
      }
    });

    lbSaved.setText("Saved!");

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
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(tfQuestionText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btApplyAnswers))))
          .addComponent(lbSaved, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(tfQuestionTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel8)
          .addComponent(cbQuestionType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(tfQuestionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btApplyAnswers))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lbSaved))
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
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btAdd)
              .addComponent(btRemove))))
        .addContainerGap())
    );
    lbQuizLayout.setVerticalGroup(
      lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(lbQuizLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(lbQuizLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(lbQuizLayout.createSequentialGroup()
            .addComponent(btAdd)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btRemove))
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    lbTeleport.setBorder(javax.swing.BorderFactory.createTitledBorder("Teleport to:"));
    lbTeleport.setLayout(new java.awt.GridLayout(2, 4));

    lbX.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lbX.setText("x: ");
    lbTeleport.add(lbX);
    lbTeleport.add(tfX);

    lbY.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lbY.setText("y: ");
    lbTeleport.add(lbY);
    lbTeleport.add(tfY);

    lbZ.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lbZ.setText("z: ");
    lbTeleport.add(lbZ);
    lbTeleport.add(tfZ);

    lbLook.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lbLook.setText("look: ");
    lbTeleport.add(lbLook);
    lbTeleport.add(tfLook);

    btLoadQuiz.setText("Load Quiz");
    btLoadQuiz.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btLoadQuizActionPerformed(evt);
      }
    });

    btSaveQuiz.setText("Save Quiz");
    btSaveQuiz.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btSaveQuizActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lbQuiz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lbTeleport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(btLoadQuiz)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btSaveQuiz)
            .addGap(0, 0, Short.MAX_VALUE))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lbQuiz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btLoadQuiz)
          .addComponent(btSaveQuiz))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(lbTeleport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private synchronized void updateQuestionPanel()
  {
    tfQuestionTitle.setText("");
    if (cbQuestionType.getItemCount() > 0)
    {
      cbQuestionType.setSelectedIndex(0);
    }
    tfQuestionText.setText("");

    for (JTextField textField : answerTextFields)
    {
      textField.setText("");
    }

    for (JCheckBox checkBox : answerCheckBoxes)
    {
      checkBox.setSelected(false);
    }

    int selIndex = jlQuestions.getSelectedIndex();
    if (selIndex > -1 && selIndex < questionListModel.getSize())
    {
      Question selQuestion = questionListModel.get(selIndex);

      tfQuestionTitle.setText(selQuestion.getTitle());
      cbQuestionType.setSelectedItem(selQuestion.getType());
      tfQuestionText.setText(selQuestion.getText());

      Map<String, Boolean> answers = selQuestion.getAnswers();
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
  }

  private void jlQuestionsValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jlQuestionsValueChanged
  {//GEN-HEADEREND:event_jlQuestionsValueChanged
    int oldIndex = currIndex;
    currIndex = jlQuestions.getSelectedIndex();

    if (oldIndex != currIndex && oldIndex != -1)
    {
      storeAnswers(oldIndex);
    }

    updateQuestionPanel();
  }//GEN-LAST:event_jlQuestionsValueChanged

  private void btAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btAddActionPerformed
  {//GEN-HEADEREND:event_btAddActionPerformed
    int selIndex = jlQuestions.getSelectedIndex();
    storeAnswers(selIndex);

    String questionTitle = JOptionPane.showInputDialog(this, "Please enter question title");
    if (questionTitle != null && !questionTitle.trim().equals(""))
    {
      Question q = new Quiz.Question(questionTitle, "");
      tempQuiz.getQuestions().add(q);
      questionListModel.addElement(q);
      jlQuestions.setSelectedValue(q, true);
    }
  }//GEN-LAST:event_btAddActionPerformed

  private void btApplyAnswersActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btApplyAnswersActionPerformed
  {//GEN-HEADEREND:event_btApplyAnswersActionPerformed
    int selIndex = jlQuestions.getSelectedIndex();
    storeAnswers(selIndex);

    checkDirty();
  }//GEN-LAST:event_btApplyAnswersActionPerformed

  private void btRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btRemoveActionPerformed
  {//GEN-HEADEREND:event_btRemoveActionPerformed
    int selIndex = jlQuestions.getSelectedIndex();
    if (selIndex > -1 && selIndex < questionListModel.getSize())
    {
      Question selQuestion = questionListModel.get(selIndex);
      tempQuiz.getQuestions().remove(selQuestion);
      questionListModel.removeElement(selQuestion);
      jlQuestions.setSelectedIndex(0);
    }
  }//GEN-LAST:event_btRemoveActionPerformed

  private void btSaveQuizActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSaveQuizActionPerformed
  {//GEN-HEADEREND:event_btSaveQuizActionPerformed
    int selIndex = jlQuestions.getSelectedIndex();
    storeAnswers(selIndex);

    String quizName = JOptionPane.showInputDialog(this, "Please enter quiz name");
    if (quizName != null && !quizName.trim().equals(""))
    {
      tempQuiz.setName(quizName);
      QuizUtils.createAndUploadQuizFile(quizName, tempQuiz);

      JOptionPane.showMessageDialog(this, "Quiz was stored on server.");
    }
  }//GEN-LAST:event_btSaveQuizActionPerformed

  private void btLoadQuizActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btLoadQuizActionPerformed
  {//GEN-HEADEREND:event_btLoadQuizActionPerformed
    String[] quizzes;

    try
    {
      ContentCollection fileRoot = QuizUtils.getFileRoot(QuizUtils.SUBDIRNAME_QUIZ, "");
      List<ContentNode> children = fileRoot.getChildren();

      quizzes = new String[children.size()];
      int index = 0;
      for (ContentNode child : children)
      {
        String childName = child.getName();
        quizzes[index] = childName.substring(0, childName.length() - 4);  // cut off ".xml"
        index++;
      }
    }
    catch (ContentRepositoryException ex)
    {
      quizzes = new String[0];
    }

    if (quizzes.length < 1)
    {
      JOptionPane.showMessageDialog(this, "There are no quiz files to be loaded.");
    }
    else
    {
      String quiz = (String) JOptionPane.showInputDialog(this,
        "Please select quiz file:",
        "Select quiz",
        JOptionPane.QUESTION_MESSAGE,
        null, quizzes, null);

      Quiz loaded = QuizUtils.downloadQuizFile(quiz);

      if (loaded != null)
      {
        tempQuiz = Quiz.copyQuiz(loaded);
        if (tempQuiz != null)
        {
          setQuizValues();
        }
      }
    }
  }//GEN-LAST:event_btLoadQuizActionPerformed

  private void storeAnswers(int selIndex)
  {
    if (selIndex > -1 && selIndex < questionListModel.getSize())
    {
      Question selQuestion = questionListModel.get(selIndex);

      selQuestion.setTitle(tfQuestionTitle.getText());
      selQuestion.setType((Question.QUESTIONTYPE) cbQuestionType.getSelectedItem());
      selQuestion.setText(tfQuestionText.getText());

      selQuestion.getAnswers().clear();
      int index = 0;
      for (JTextField tf : answerTextFields)
      {
        selQuestion.getAnswers().put(tf.getText(), answerCheckBoxes.get(index).isSelected());
        index++;
      }
    }

    Thread displayer = new Thread(new StatusDisplayer());
    displayer.start();
  }

  class StatusDisplayer implements Runnable
  {

    @Override
    public void run()
    {
      lbSaved.setText("Saved!");
      try
      {
        Thread.sleep(3000);
      }
      catch (InterruptedException ex)
      {
        Thread.currentThread().interrupt();
      }

      lbSaved.setText(" ");
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btAdd;
  private javax.swing.JButton btApplyAnswers;
  private javax.swing.JButton btLoadQuiz;
  private javax.swing.JButton btRemove;
  private javax.swing.JButton btSaveQuiz;
  private javax.swing.JCheckBox cbAnswer1;
  private javax.swing.JCheckBox cbAnswer2;
  private javax.swing.JCheckBox cbAnswer3;
  private javax.swing.JCheckBox cbAnswer4;
  private javax.swing.JCheckBox cbAnswer5;
  private javax.swing.JCheckBox cbAnswer6;
  private javax.swing.JComboBox cbQuestionType;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList jlQuestions;
  private javax.swing.JLabel lbAnswer1;
  private javax.swing.JLabel lbAnswer2;
  private javax.swing.JLabel lbAnswer3;
  private javax.swing.JLabel lbAnswer4;
  private javax.swing.JLabel lbAnswer5;
  private javax.swing.JLabel lbAnswer6;
  private javax.swing.JLabel lbLook;
  private javax.swing.JPanel lbQuiz;
  private javax.swing.JLabel lbSaved;
  private javax.swing.JPanel lbTeleport;
  private javax.swing.JLabel lbX;
  private javax.swing.JLabel lbY;
  private javax.swing.JLabel lbZ;
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
