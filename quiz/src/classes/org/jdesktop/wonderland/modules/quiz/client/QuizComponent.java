/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.AvatarCollisionEvent;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.quiz.common.Quiz;
import org.jdesktop.wonderland.modules.quiz.common.Quiz.Question.QUESTIONTYPE;
import org.jdesktop.wonderland.modules.quiz.common.QuizComponentClientState;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponent extends CellComponent implements ProximityListener
{
  // start quiz when user comes near object

  private Quiz quiz;
  private Vector3f location;
  private Quaternion look;

  @UsesCellComponent
  private HUDComponent quizHUDComponent = null;
  private CollisionListener collisionListener = null;

  private final Map<JCheckBox, Boolean> correctionModel;

  public QuizComponent(Cell cell)
  {
    super(cell);

    correctionModel = Collections.synchronizedMap(new LinkedHashMap<JCheckBox, Boolean>());
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
    if (status == CellStatus.ACTIVE && increasing)
    {
    }
    else if (status == CellStatus.INACTIVE && !increasing)
    {
    }
    else if (status == CellStatus.VISIBLE)
    {
      if (increasing)
      {
        collisionListener = new CollisionListener();
        Entity ent = ((CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();
        ClientContextJME.getInputManager().addEventListener(collisionListener, ent);
      }
      else
      {
        ClientContextJME.getInputManager().removeEventListener(collisionListener, ((CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
      }
    }

    super.setStatus(status, increasing);
  }

  @Override
  public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID,
    BoundingVolume proximityVolume, int proximityIndex)
  {
    displayQuiz();
  }

  @Override
  public void setClientState(CellComponentClientState clientState)
  {
    super.setClientState(clientState);

    QuizComponentClientState state = (QuizComponentClientState) clientState;

    this.quiz = state.getQuiz();
    this.location = state.getLocation();
    this.look = state.getLook();
  }

  /**
   * Creates and returns the top map HUD component.
   */
  private HUDComponent createQuizHUDComponent()
  {
    // Create the HUD Panel that displays the navigation controls
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

    JPanel quizPanel = new JPanel();
    quizPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    List<Quiz.Question> questions = this.quiz.getQuestions();
    int numQuestions = questions.size();

    int numRows = ((int) (numQuestions / 2)) + 1;
    quizPanel.setLayout(new GridLayout(numRows, 2, 5, 5));

    correctionModel.clear();

    for (int i = 0; i < numQuestions; i++)
    {
      Quiz.Question question = questions.get(i);

      JPanel questionPanel = new JPanel();
      Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
      Border empty = BorderFactory.createEmptyBorder(3, 3, 3, 3);
      questionPanel.setBorder(BorderFactory.createCompoundBorder(etched, empty));
      questionPanel.setLayout(new BorderLayout());
      quizPanel.add(questionPanel, i);

      JLabel lbQuestionText = new JLabel();
      questionPanel.add(lbQuestionText, BorderLayout.NORTH);

      if (question.getType() != QUESTIONTYPE.MULTIPLE_CHOICE)
      {
        lbQuestionText.setText("Sorry, question type not (yet) supported.");
      }
      else
      {
        lbQuestionText.setText(question.getText());

        Map<String, Boolean> answers = question.getAnswers();
        int numAnswers = answers.size();

        JPanel answerPanel = new JPanel();
//        answerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        answerPanel.setLayout(new GridLayout(numAnswers, 1));
        questionPanel.add(answerPanel, BorderLayout.CENTER);

        Set<Map.Entry<String, Boolean>> answerEntrySet = answers.entrySet();

        int j = 0;
        for (Map.Entry<String, Boolean> answerEntry : answerEntrySet)
        {
          String answer = answerEntry.getKey();

          JCheckBox cbAnswerJ = new JCheckBox(answer);
          answerPanel.add(cbAnswerJ, j);
          correctionModel.put(cbAnswerJ, answerEntry.getValue());
          cbAnswerJ.addItemListener(new AnswerCheckboxItemListener());

          j++;
        }
      }
    }

    quizHUDComponent = mainHUD.createComponent(quizPanel);

    quizHUDComponent.setName(this.quiz.getName());
    quizHUDComponent.setPreferredLocation(CompassLayout.Layout.CENTER);

    quizHUDComponent.setSize(500, 500);

    mainHUD.addComponent(quizHUDComponent);

    // Track when the HUD Component is closed.
    quizHUDComponent.addEventListener(
      new HUDEventListener()
      {
        @Override
        public void HUDObjectChanged(HUDEvent event
        )
        {
          HUDEvent.HUDEventType hudEventType = event.getEventType();
          if (hudEventType == HUDEvent.HUDEventType.CLOSED
          || hudEventType == HUDEvent.HUDEventType.MINIMIZED)
          {
//            quizHUDComponent.setVisible(false);

            HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
            quizHUDComponent.setVisible(false);
            hud.removeComponent(quizHUDComponent);
            quizHUDComponent = null;
          }
        }
      }
    );

    return quizHUDComponent;
  }

  public void displayQuiz()
  {
    if (quizHUDComponent == null)
    {
      quizHUDComponent = createQuizHUDComponent();
      quizHUDComponent.setMaximized();
      quizHUDComponent.setVisible(true);
    }
    else if (!quizHUDComponent.isVisible())
    {
      quizHUDComponent.setVisible(true);
    }

    checkAnswers();
  }

  private void teleport()
  {
    // teleport in a separate thread, since we don't know which one we
    // are called on
    // if you say so...
    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          // teleport!

          String serverURL = LoginManager.getPrimary().getServerURL();
          System.out.println("GOTO LOCATION " + serverURL + " " + location);

          ClientContextJME.getClientMain().gotoLocation(serverURL, location, look);

          //URL url = PortalComponent.class.getResource(
          //	"resources/" + "Teleport.au")
          Logger.getLogger(QuizComponent.class.getName()).log(Level.WARNING, "[PortalComponent] going to {0} at {1}, {2}", new Object[]
          {
            serverURL, location, look
          });
        }
        catch (IOException ex)
        {
          Logger.getLogger(QuizComponent.class.getName()).log(Level.WARNING, "Error teleporting", ex);
        }
      }
    }, "Teleporter");
    t.start();
  }

  class CollisionListener extends EventClassListener
  {

    @Override
    public Class[] eventClassesToConsume()
    {
      return new Class[]
      {
        AvatarCollisionEvent.class
      };
    }

    @Override
    public void commitEvent(Event event)
    {
      displayQuiz();
    }
  }

  private class AnswerCheckboxItemListener implements ItemListener
  {

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      checkAnswers();
    }
  }

  private void checkAnswers()
  {
    boolean correct = true;

    Set<Map.Entry<JCheckBox, Boolean>> entrySet = correctionModel.entrySet();
    for (Map.Entry<JCheckBox, Boolean> entry : entrySet)
    {
      JCheckBox checkBox = entry.getKey();
      boolean currentSelectionValue = checkBox.isSelected();
      boolean correctSelectionValue = entry.getValue();
      if (currentSelectionValue != correctSelectionValue)
      {
        correct = false;
        break;
      }
    }

    if (correct)
    {
      teleport();
    }
  }
}
