/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.quiz.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.quiz.common.Quiz;
import org.jdesktop.wonderland.modules.webdav.client.WebdavContentRepository;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentResource;

/**
 *
 * @author ASUS
 */
public class QuizUtils
{

  public final static String SUBDIRNAME_QUIZ = "quiz";

  public static String getFileNameFromPath(String path)
  {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  /**
   *
   * @param quizName Name of the quiz without file ending
   * @param quiz
   * @return
   */
  public static boolean createAndUploadQuizFile(String quizName, Quiz quiz)
  {
    try
    {
      File file = new File(quizName + ".xml");

      JAXBContext context = JAXBContext.newInstance(Quiz.class);
      Marshaller marshal = context.createMarshaller();

      // output pretty printed
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      marshal.marshal(quiz, file);

      QuizUtils.uploadFileToServer(file, SUBDIRNAME_QUIZ, "");
      return true;
    }
    catch (IOException ex)
    {
      Logger.getLogger(QuizUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    catch (JAXBException ex)
    {
      Logger.getLogger(QuizUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    catch (ContentRepositoryException ex)
    {
      Logger.getLogger(QuizUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }

  private static InputStream openFileForReading(String userName, String subDirName, String fileName) throws MalformedURLException, IOException, ContentRepositoryException
  {
//    URL documentURL = AssetUtils.getAssetURL(path);
//    URLConnection conn = documentURL.openConnection();
//    conn.connect();
//
//    return conn.getInputStream();

    ServerSessionManager manager = LoginManager.getPrimary();
    WebdavContentRepository repo = (WebdavContentRepository) ContentRepositoryRegistry.getInstance().getRepository(manager);
    WebdavContentCollection nodo = repo.getUserRoot(userName);
    WebdavContentCollection subDir = (WebdavContentCollection) nodo.getChild(subDirName);
    WebdavContentResource resource = (WebdavContentResource) subDir.getChild(fileName);

    return resource.getInputStream();
  }

  /**
   *
   * @param quizName Name of the quiz without file ending
   * @return
   */
  public static Quiz downloadQuizFile(String quizName)
  {
    Quiz toReturn;
    try
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();
      InputStream in = openFileForReading(userName, SUBDIRNAME_QUIZ, quizName + ".xml");

      JAXBContext context = JAXBContext.newInstance(Quiz.class);
      Unmarshaller marshal = context.createUnmarshaller();

      toReturn = (Quiz) marshal.unmarshal(in);
    }
    catch (Exception ex)
    {
      toReturn = null;
    }

    return toReturn;
  }

  /**
   * Fetches the user's root directory for all xml files using the current
   * primary server.
   *
   * Code based on sample cell and learning poster by
   *
   * @author Jordan Slott <jslott@dev.java.net>
   * @author Ronny Standtke <ronny.standtke@fhnw.ch>
   * @author Johanna Pirker <jpirker@iicm.edu>
   *
   * adapted by
   * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
   * @param subDirName
   * @param userName can be null or empty
   *
   * @return the user's root directory using the current primary server
   * @throws
   * org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException
   */
  public static ContentCollection getFileRoot(String subDirName, String userName) throws ContentRepositoryException
  {
    ContentRepositoryRegistry r = ContentRepositoryRegistry.getInstance();
    ServerSessionManager session = LoginManager.getPrimary();

    // Try to find the desired sub-directory or create it if it doesn't exist
    ContentCollection userRoot;
    if (userName != null && !userName.equals(""))
    {
      userRoot = r.getRepository(session).getUserRoot(userName);
    }
    else
    {
      userRoot = r.getRepository(session).getUserRoot();
    }

    ContentNode node = (ContentNode) userRoot.getChild(subDirName);
    if (node == null)
    {
      node = (ContentNode) userRoot.createChild(subDirName, ContentNode.Type.COLLECTION);
    }
    else
    {
      if (!(node instanceof ContentCollection))
      {
        node.getParent().removeChild(subDirName);
        node = (ContentNode) userRoot.createChild(subDirName, ContentNode.Type.COLLECTION);
      }
    }

    return (ContentCollection) node;
  }

  public static URL uploadFileToServer(File file, String subDirName, String userName) throws ContentRepositoryException, IOException
  {
    String fileName = file.getName();

    ContentCollection fileRoot = getFileRoot(subDirName, userName);

    ContentNode resource = fileRoot.getChild(fileName);
    if (resource == null)
    {
      resource = fileRoot.createChild(fileName, ContentNode.Type.RESOURCE);
    }
    else
    {
      if (!(resource instanceof ContentResource))
      {
        resource.getParent().removeChild(fileName);
        resource = fileRoot.createChild(fileName, ContentNode.Type.RESOURCE);
      }
    }

    // Here the upload-magic happens
    ((ContentResource) resource).put(file);

    return ((ContentResource) resource).getURL();
  }
}
