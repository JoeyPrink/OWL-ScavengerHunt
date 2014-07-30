/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import org.jdesktop.wonderland.modules.item.common.ScavengerHuntStudent;
import java.util.ArrayList;

/**
 * Represents one group of students who take part in the Scavenger Hunt.
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ScavengerHuntGroup
{

  private String groupName;
  private int groupID;
  private final ArrayList<ScavengerHuntStudent> students = new ArrayList();

  public ScavengerHuntGroup(String groupName, int groupID)
  {
    this.groupName = groupName;
    this.groupID = groupID;
  }

  public String getGroupName()
  {
    return groupName;
  }

  public void setGroupName(String groupName)
  {
    this.groupName = groupName;
  }

  public int getGroupID()
  {
    return groupID;
  }

  public void setGroupID(int groupID)
  {
    this.groupID = groupID;
  }

  public ArrayList<ScavengerHuntStudent> getStudents()
  {
    return students;
  }

  public void addStudent(ScavengerHuntStudent student)
  {
    this.students.add(student);
  }

}
