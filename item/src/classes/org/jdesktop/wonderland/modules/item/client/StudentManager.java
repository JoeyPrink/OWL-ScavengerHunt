/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * Responsible for managing (loading, writing) Scavanger Hunt specific user data
 * (ability, group). Attention: group is not working at the moment; all users
 * get put into one group on loading and writing.
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class StudentManager
{

  public static final String iniFileName = "scavenger_hunt.ini";
  private final ArrayList<ScavengerHuntGroup> groups;

  public StudentManager()
  {
    this.groups = new ArrayList<ScavengerHuntGroup>();
  }

  public void saveStudents(Enumeration<ScavengerHuntStudent> studentsEnum)
  {
    while (studentsEnum.hasMoreElements())
    {
      ScavengerHuntStudent student = studentsEnum.nextElement();
      try
      {
//        System.out.println("Writing student " + student
//          + ", new role is " + student.getAbility()
//          + ", group id is " + "-1");  // TODO: find out real group
        createAndUploadNewFile(student.getUsername(),
          Abilities.getIntFromAbility(student.getAbility()), -1);
      }
      catch (ContentRepositoryException ex)
      {
        Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
        Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void loadStudents()
  {
    groups.clear();

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);
    PresenceInfo[] infos = pm.getAllUsers();

    ScavengerHuntGroup group = new ScavengerHuntGroup("Group_1", -1);

    for (PresenceInfo info : infos)
    {
      String userName = info.getUserID().getUsername();

//      ScavengerHuntStudent studentUnknown = new ScavengerHuntStudent(userName, Abilities.Ability.UNKNOWN);
      ScavengerHuntStudent student = loadStudentFromFile(userName);
      if (student != null)
      {
        //add the moment give all students into one group
        //TODO: change this
        group.addStudent(student);
      }
    }

    groups.add(group);
  }

  public ArrayList<ScavengerHuntGroup> getGroups()
  {
    return groups;
  }

  public static ScavengerHuntStudent loadStudentFromFile(String userName)
  {
    ContentCollection fileRoot;
    List<ContentNode> children;
    try
    {
      fileRoot = ItemUtils.getFileRoot(ItemUtils.SUBDIRNAME_INI, userName);
      children = fileRoot.getChildren();
    }
    catch (ContentRepositoryException ex)
    {
      Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }

    // Try read file the first time
    Integer[] properties = new Integer[2]; // first role, second group
    boolean success = readProperties(children, properties);

    if (success)
    {
      return new ScavengerHuntStudent(userName, Abilities.getAbilityFromInt(properties[0]));
    }
    else
    {
      try
      {
        createAndUploadNewFile(userName, -1, -1);
      }
      catch (Exception ex)
      {
        Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }

      try
      {
        fileRoot = ItemUtils.getFileRoot(ItemUtils.SUBDIRNAME_INI, userName);
        children = fileRoot.getChildren();
      }
      catch (ContentRepositoryException ex)
      {
        Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }

      // Try read file the second time
      properties = new Integer[2]; // first role, second group
      success = readProperties(children, properties);

      if (success)
      {
        return new ScavengerHuntStudent(userName, Abilities.getAbilityFromInt(properties[0]));
      }
      else
      {
        return null;
      }
    }
  }

  private static boolean readProperties(List<ContentNode> children, Integer[] properties)
  {
    boolean roleB = false;
    boolean groupB = false;

    for (ContentNode child : children)
    {
      if (child.getName().equals(iniFileName)) // found our ini file
      {
        String filepath = "wlcontent:/" + child.getPath();

        String content;
        try
        {
          InputStream in = openFile(filepath);
          content = readString(in);
        }
        catch (IOException ex)
        {
          properties[0] = -1;
          properties[1] = -1;
          return false;
        }

        StringTokenizer tokenizer = new StringTokenizer(content);
        while (tokenizer.hasMoreTokens())
        {
          String line = tokenizer.nextToken();
          if (line.startsWith("role"))
          {
            String[] s = line.split("=");
            if (s.length != 2)
            {
              properties[0] = -1;
              properties[1] = -1;
              return false;
            }
            else
            {
              properties[0] = Integer.parseInt(s[1].trim());
              roleB = true;
            }
          }
          if (line.startsWith("group"))
          {
            String[] s = line.split("=");
            if (s.length != 2)
            {
              properties[0] = -1;
              properties[1] = -1;
              return false;
            }
            else
            {
              properties[1] = Integer.parseInt(s[1].trim());
              groupB = true;
            }
          }
        }
      }
    }

    return roleB && groupB;
  }

  private static InputStream openFile(String path) throws MalformedURLException, IOException
  {
    URL documentURL = AssetUtils.getAssetURL(path);
    URLConnection conn = documentURL.openConnection();
    conn.connect();

    return conn.getInputStream();
  }

  private static String readString(InputStream in) throws IOException
  {
    char[] buf = new char[2048];
    Reader r = new InputStreamReader(in);
    StringBuilder s = new StringBuilder();
    while (true)
    {
      int n = r.read(buf);
      if (n < 0)
      {
        break;
      }
      s.append(buf, 0, n);
    }
    return s.toString();
  }

  private static void createAndUploadNewFile(String userName, int role, int group) throws ContentRepositoryException, IOException
  {
    File newFile = new File(iniFileName);
    writeTemplate(newFile, role, group);
    ItemUtils.uploadFileToServer(newFile, ItemUtils.SUBDIRNAME_INI, userName);
  }

  private static void writeTemplate(File file, int role, int group) throws IOException
  {
    FileWriter writer = new FileWriter(file);
    BufferedWriter buffy = new BufferedWriter(writer);

    buffy.write("role=" + role);
    buffy.newLine();

    buffy.write("group=" + group);
    buffy.newLine();

    buffy.close();
  }

}
