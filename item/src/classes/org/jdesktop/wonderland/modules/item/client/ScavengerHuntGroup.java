/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import java.util.ArrayList;

/**
 *
 * @author ASUS
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
