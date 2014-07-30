/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class Quiz implements Serializable
{

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "questions")
  private List<Question> questions;

  public static Quiz copyQuiz(Quiz src)
  {
    if (src == null)
    {
      return null;
    }

    Quiz copyQuiz = new Quiz(src.getName());

    List<Question> questions = src.getQuestions();
    for (Question question : questions)
    {
      Question q1 = new Question(question.getTitle(), question.getText());
      q1.setType(question.getType());

      Map<String, Boolean> answers = question.getAnswers();
      Set<Map.Entry<String, Boolean>> entrySet = answers.entrySet();
      for (Map.Entry<String, Boolean> answerEntry : entrySet)
      {
        q1.getAnswers().put(answerEntry.getKey(), answerEntry.getValue());
      }

      copyQuiz.getQuestions().add(q1);
    }

    return copyQuiz;
  }

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

  public Quiz()
  {
    GregorianCalendar gregor = new GregorianCalendar();

    this.name = "Quiz_" + gregor.getTimeInMillis();
    this.questions = Collections.synchronizedList(new ArrayList<Question>());
  }

  public Quiz(String name)
  {
    this.name = name;
    this.questions = Collections.synchronizedList(new ArrayList<Question>());
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
  public List<Question> getQuestions()
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
    return name + " (" + questions.size() + " Questions)";
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.name);
    hash = 37 * hash + Objects.hashCode(this.questions);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final Quiz other = (Quiz) obj;

    if (!this.name.equals(other.name))
    {
      return false;
    }

    if (this.questions.size() != other.questions.size())
    {
      return false;
    }

    int index = 0;
    for (Question thisquestion : this.questions)
    {
      Question otherquestion = other.questions.get(index);

      if (!thisquestion.equals(otherquestion))
      {
        return false;
      }

      index++;
    }

    return true;
  }

  /**
   *
   */
  @XmlRootElement(name = "Question")
  public static class Question implements Serializable
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
    private Map<String, Boolean> answers;

    public static Question sampleQuestion()
    {
      Question sampleQuestion = new Question("Hitchhiker Question", "What's the answer to life, the universe, and everything?");
      sampleQuestion.getAnswers().put("3", Boolean.FALSE);
      sampleQuestion.getAnswers().put("7", Boolean.FALSE);
      sampleQuestion.getAnswers().put("29", Boolean.FALSE);
      sampleQuestion.getAnswers().put("42", Boolean.TRUE);

      return sampleQuestion;
    }

    public Question()
    {
      this.title = "first question";
      this.type = QUESTIONTYPE.MULTIPLE_CHOICE;
      this.text = "";
      this.answers = Collections.synchronizedMap(new LinkedHashMap<String, Boolean>());
    }

    public Question(String title, String text)
    {
      this.title = title;
      this.type = QUESTIONTYPE.MULTIPLE_CHOICE;
      this.text = text;
      this.answers = Collections.synchronizedMap(new LinkedHashMap<String, Boolean>());
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
    public Map<String, Boolean> getAnswers()
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
      return title + " (" + type + ")";
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 59 * hash + Objects.hashCode(this.title);
      hash = 59 * hash + Objects.hashCode(this.type);
      hash = 59 * hash + Objects.hashCode(this.text);
      hash = 59 * hash + Objects.hashCode(this.answers);
      return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (getClass() != obj.getClass())
      {
        return false;
      }

      final Question other = (Question) obj;

      if (!this.title.equals(other.title))
      {
        return false;
      }

      if (!this.type.toString().equals(other.type.toString()))
      {
        return false;
      }

      if (!this.text.equals(other.text))
      {
        return false;
      }

      if (this.answers.size() != other.answers.size())
      {
        return false;
      }

      for (Map.Entry<String, Boolean> entrySet : this.answers.entrySet())
      {
        String thisanswerS = entrySet.getKey();
        Boolean thisanswerB = entrySet.getValue();

        Boolean otheranswerB = other.answers.get(thisanswerS);

        if (otheranswerB == null) // answer string does not exist
        {
          return false;
        }

        if (thisanswerB.booleanValue() != otheranswerB.booleanValue()) // answer has different true/false value
        {
          return false;
        }
      }

      return true;
    }

  }
}
