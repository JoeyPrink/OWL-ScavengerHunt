/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Pirmin
 *
 * Quiz and not Chatlog, because I want to use this class in a other module too
 */
@XmlRootElement(name = "Quiz")
public class Quiz
{

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "questions")
  private ArrayList<Question> questions;

  public static Quiz sampleQuiz()
  {
    Quiz sampleQuiz = new Quiz("Funny Quiz");

    Question q1 = Question.sampleQuestion();
    sampleQuiz.getQuestions().add(q1);

    Question q2 = new Question("Unicorn Question", "What's the number of horns on a unicorn?");
    q2.getAnswers().put("1", Boolean.TRUE);
    q2.getAnswers().put("2", Boolean.FALSE);
    q2.getAnswers().put("35", Boolean.FALSE);
    sampleQuiz.getQuestions().add(q2);

    return sampleQuiz;
  }

  public Quiz(String name)
  {
    this.name = name;
    this.questions = new ArrayList<Question>();
  }

  @XmlTransient
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @XmlTransient
  public ArrayList<Question> getQuestions()
  {
    return questions;
  }

  public void setQuestions(ArrayList<Question> questions)
  {
    this.questions = questions;
  }

  @Override
  public String toString()
  {
    return "Quiz{" + "name=" + name + ", questions=" + questions + '}';
  }

  @XmlRootElement(name = "Question")
  public static class Question
  {

    public enum QUESTIONTYPE
    {

      MULTIPLE_CHOICE,
      TEXT_EXACT_MATCHING,
      TEXT_KEYWORD_SEARCH
    }

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "type")
    private QUESTIONTYPE type;

    @XmlElement(name = "text")
    private String text;

    @XmlElement(name = "answers")
    private HashMap<String, Boolean> answers;

    public static Question sampleQuestion()
    {
      Question sampleQuestion = new Question("Hitchhiker Question", "What's the answer to life, the universe, and everything?");
      sampleQuestion.getAnswers().put("3", Boolean.FALSE);
      sampleQuestion.getAnswers().put("7", Boolean.FALSE);
      sampleQuestion.getAnswers().put("29", Boolean.FALSE);
      sampleQuestion.getAnswers().put("42", Boolean.TRUE);

      return sampleQuestion;
    }

    public Question(String title, String text)
    {
      this.title = title;
      this.type = QUESTIONTYPE.MULTIPLE_CHOICE;
      this.text = text;
      this.answers = new HashMap<String, Boolean>();
    }

    @XmlTransient
    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    @XmlTransient
    public QUESTIONTYPE getType()
    {
      return type;
    }

    public void setType(QUESTIONTYPE type)
    {
      this.type = type;
    }

    @XmlTransient
    public String getText()
    {
      return text;
    }

    public void setText(String text)
    {
      this.text = text;
    }

    @XmlTransient
    public HashMap<String, Boolean> getAnswers()
    {
      return answers;
    }

    public void setAnswers(HashMap<String, Boolean> answers)
    {
      this.answers = answers;
    }

    @Override
    public String toString()
    {
      return "Question{" + "title=" + title + ", type=" + type + ", text=" + text + ", answers=" + answers + '}';
    }
  }
}
