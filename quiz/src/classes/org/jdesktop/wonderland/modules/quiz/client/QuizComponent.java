/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import com.jme.bounding.BoundingVolume;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.quiz.client.Quiz.Question.QUESTIONTYPE;

/**
 *
 * @author Lisa Tomes
 */
public class QuizComponent extends CellComponent implements ProximityListener
{
  // start quiz when user comes near object

  @UsesCellComponent
  private HUDComponent quizHUDComponent = null;
  private CollisionListener collisionListener = null;

  public QuizComponent(Cell cell)
  {
    super(cell);
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
    System.out.println("viewEnterExit trigger!");
    getXMLFromServer();
    displayQuiz();
  }

  @Override
  public void setClientState(CellComponentClientState clientState)
  {
    super.setClientState(clientState);
  }

  public void getXMLFromServer()
  {
    // TODO
  }

  /**
   * Creates and returns the top map HUD component.
   */
  private HUDComponent createQuizHUDComponent(Quiz quiz)
  {
    // Create the HUD Panel that displays the navigation controls
    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

    JPanel quizPanel = new JPanel();
    quizPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    ArrayList<Quiz.Question> questions = quiz.getQuestions();
    int numQuestions = questions.size();

    quizPanel.setLayout(new GridLayout(numQuestions, 1, 5, 5));

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

        HashMap<String, Boolean> answers = question.getAnswers();
        int numAnswers = answers.size();

        JPanel answerPanel = new JPanel();
//        answerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        answerPanel.setLayout(new GridLayout(numAnswers, 1));
        questionPanel.add(answerPanel, BorderLayout.CENTER);

        int j = 0;
        for (String answer : answers.keySet())
        {
          JCheckBox cbAnswerJ = new JCheckBox(answer);
          answerPanel.add(cbAnswerJ, j);
          cbAnswerJ.addItemListener(new AnswerCheckboxItemListener());

          j++;
        }
      }
    }

    quizHUDComponent = mainHUD.createComponent(quizPanel);

    quizHUDComponent.setName(quiz.getName());
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
            quizHUDComponent.setVisible(false);
          }
        }
      }
    );

    return quizHUDComponent;
  }

  public void displayQuiz()
  {
    Quiz quiz = Quiz.sampleQuiz(); // TODO: Read real Quiz

    if (quizHUDComponent == null)
    {
      quizHUDComponent = createQuizHUDComponent(quiz);
      quizHUDComponent.setMaximized();
      quizHUDComponent.setVisible(true);
    }
    else if (!quizHUDComponent.isVisible())
    {
      quizHUDComponent.setVisible(true);
    }

    teleport();
  }

  private void teleport()
  {
    System.out.println("woooosh");

//    // teleport in a separate thread, since we don't know which one we
//    // are called on
//    Thread t = new Thread(new Runnable()
//    {
//      public void run()
//      {
//        try
//        {
//                    // teleport!
//          //teleportAudio.play();
//
//          System.out.println("GOTO LOCATION " + serverURL + " " + location);
//
//          ClientContextJME.getClientMain().gotoLocation(serverURL, location, look);
//
//		    //URL url = PortalComponent.class.getResource(
//          //	"resources/" + "Teleport.au")
//          logger.warning("[PortalComponent] going to " + serverURL
//            + " at " + location + ", " + look);
//
//          SoftphoneControlImpl.getInstance().sendCommandToSoftphone(
//            "playFile=" + audioSource + "=" + volume);
//        }
//        catch (IOException ex)
//        {
//          logger.log(Level.WARNING, "Error teleporting", ex);
//        }
//      }
//    }, "Teleporter");
//    t.start();
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
      System.out.println("CollisionListener commitEvent trigger!");
      getXMLFromServer();
      displayQuiz();
    }
  }

  private class AnswerCheckboxItemListener implements ItemListener
  {

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      JCheckBox affected = (JCheckBox) e.getItem();
      System.out.println(affected.getText() + " changed.");
    }

  }
}
