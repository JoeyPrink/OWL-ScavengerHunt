/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

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
import org.jdesktop.wonderland.modules.webdav.client.WebdavContentRepository;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentResource;

/**
 *
 * @author ASUS
 */
public class ItemUtils
{

  public final static String SUBDIRNAME_XML = "xml";
  public final static String SUBDIRNAME_IMG = "images";
  public static final String SUBDIRNAME_INI = "scavenger_hunt";

  public static String makeWlcontentPath(String userName, String subDirName, String fileName)
  {
    if (userName == null || userName.equals(""))
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      userName = session.getUserID().getUsername();
    }

    //wlcontent://users/lisa/chat/ancient_person_chat.xml
    return "wlcontent://users/" + userName + "/" + subDirName + "/" + fileName;
  }

  public static String makeFileName(String title)
  {
    return title.replace(" ", "_");
  }

  public static String getExtension(String filePath)
  {
    return filePath.substring(filePath.lastIndexOf("."));
  }

  public static String getFileNameFromPath(String path)
  {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public static String getFileDirFromPath(String path)
  {
    String temp = path.substring(0, path.lastIndexOf("/"));
    return getFileNameFromPath(temp);
  }

  public static String getUserDirFromPath(String path)
  {
    String temp = path.substring(0, path.lastIndexOf("/"));
    temp = temp.substring(0, temp.lastIndexOf("/"));

    return getFileNameFromPath(temp);
  }

  public static boolean setItem(String fileName, Item item)
  {
    try
    {
      File file = new File(fileName + ".xml");

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Marshaller marshal = context.createMarshaller();

      // output pretty printed
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      marshal.marshal(item, file);

      ItemUtils.uploadFileToServer(file, SUBDIRNAME_XML, "");
      return true;
    }
    catch (IOException ex)
    {
      Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    catch (JAXBException ex)
    {
      Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    catch (ContentRepositoryException ex)
    {
      Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }

  public static InputStream openFileForReading(String userName, String subDirName, String fileName) throws MalformedURLException, IOException, ContentRepositoryException
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

  public static Item getItem(String fileNameWithoutEnding)
  {
    Item toReturn;

    try
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();
      InputStream in = openFileForReading(userName, SUBDIRNAME_XML, fileNameWithoutEnding + ".xml");

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Unmarshaller marshal = context.createUnmarshaller();

      toReturn = (Item) marshal.unmarshal(in);
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
