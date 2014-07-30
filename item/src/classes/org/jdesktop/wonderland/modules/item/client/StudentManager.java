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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.ScavengerHuntStudent;
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

  private final String iniFileName = "scavenger_hunt.ini";

  public String getIniFileName()
  {
    return iniFileName;
  }

//  private final ArrayList<ScavengerHuntGroup> groups;
  private final HashMap<String, ScavengerHuntStudent> students;
  private boolean studentsLoaded = false;

  private static StudentManager theInstance = null;

  private ArrayList<UserObserver> observers;

  public void addObserver(UserObserver observer)
  {
    if (!observers.contains(observer))
    {
      observers.add(observer);
    }
  }

  public static StudentManager getInstance()
  {
    if (theInstance == null)
    {
      theInstance = new StudentManager();
    }

    return theInstance;
  }

  private StudentManager()
  {
//    this.groups = new ArrayList<ScavengerHuntGroup>();
    this.students = new HashMap<String, ScavengerHuntStudent>();

    this.observers = new ArrayList<UserObserver>();
  }

  public void saveStudent(ScavengerHuntStudent student)
  {
    try
    {
      createAndUploadNewFile(student.getUsername(),
        Abilities.getIntFromAbility(student.getAbility()), -1);
      students.put(student.getUsername(), student);

      for (UserObserver observer : observers)
      {
        observer.userAbilitesChanged(student);
        break; // only need to do it for the first observer, since StudentManager is a Singleton
      }
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

  public void saveStudents(Enumeration<ScavengerHuntStudent> studentsEnum)
  {
    while (studentsEnum.hasMoreElements())
    {
      ScavengerHuntStudent student = studentsEnum.nextElement();
      saveStudent(student);
    }
  }

  public void loadStudents()
  {
    students.clear();

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);
    PresenceInfo[] infos = pm.getAllUsers();

    for (PresenceInfo info : infos)
    {
      String userName = info.getUserID().getUsername();
      if (!students.containsKey(userName))
      {
//      JOptionPane.showMessageDialog(null, "load student " + userName);
        ScavengerHuntStudent student = loadStudentFromFilePrivate(userName);
        if (student != null)
        {
          students.put(userName, student);
        }
      }
    }

    studentsLoaded = true;
  }

  public HashMap<String, ScavengerHuntStudent> getStudents()
  {
    if (studentsLoaded)
    {
      return students;
    }

    loadStudents();
    return students;
  }

  public ScavengerHuntStudent loadStudentFromFile(String userName)
  {
    if (studentsLoaded)
    {
      return students.get(userName);
    }

    loadStudents();
    return students.get(userName);
  }

  private ScavengerHuntStudent loadStudentFromFilePrivate(String userName)
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
    boolean success = readProperties(children, properties, userName);
//    JOptionPane.showMessageDialog(null, "read success: " + success);
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
      success = readProperties(children, properties, userName);

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

  private boolean readProperties(List<ContentNode> children, Integer[] properties, String userName)
  {
    boolean roleB = false;
    boolean groupB = false;

    for (ContentNode child : children)
    {
      if (child.getName().equals(iniFileName)) // found our ini file
      {
        String filepath = "wlcontent:/" + child.getPath();

        String content = null;
        try
        {
          InputStream in = ItemUtils.openFileForReading(userName,
            ItemUtils.SUBDIRNAME_INI,
            ItemUtils.getFileNameFromPath(filepath));
          content = readString(in);
        }
        catch (IOException ex)
        {
          properties[0] = -1;
          properties[1] = -1;
          return false;
        }
        catch (ContentRepositoryException ex)
        {
          Logger.getLogger(StudentManager.class.getName()).log(Level.SEVERE, null, ex);
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

//  private InputStream openFile(String path) throws MalformedURLException, IOException
//  {
//    URL documentURL = AssetUtils.getAssetURL(path);
//    URLConnection conn = documentURL.openConnection();
//    conn.setUseCaches(false);
//    conn.connect();
//
//    return conn.getInputStream();
//  }
  private String readString(InputStream in) throws IOException
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

  private void createAndUploadNewFile(String userName, int role, int group) throws ContentRepositoryException, IOException
  {
    File newFile = new File(iniFileName);
    writeTemplate(newFile, role, group);
    ItemUtils.uploadFileToServer(newFile, ItemUtils.SUBDIRNAME_INI, userName);
  }

  private void writeTemplate(File file, int role, int group) throws IOException
  {
    FileWriter writer = new FileWriter(file);
    BufferedWriter buffy = new BufferedWriter(writer);

    buffy.write("role=" + role);
    buffy.newLine();

    buffy.write("group=" + group);
    buffy.newLine();

    buffy.close();
  }

  public ArrayList<ScavengerHuntStudent> getStudentsWithAbility(List<Abilities.Ability> abilities)
  {
    ArrayList<ScavengerHuntStudent> students = new ArrayList<ScavengerHuntStudent>();

    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);
    PresenceInfo[] infos = pm.getAllUsers();

    for (PresenceInfo info : infos)
    {
      String userName = info.getUserID().getUsername();

      ScavengerHuntStudent student = loadStudentFromFile(userName);
      if (student != null && student.getAbility() != null && abilities.contains(student.getAbility()))
      {
        students.add(student);
      }
    }

    return students;
  }

}
