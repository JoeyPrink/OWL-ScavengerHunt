/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.item.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 *
 * @author ASUS
 */
public class ItemUtils
{

  public final static String SUBDIRNAME_XML = "xml";
  public final static String SUBDIRNAME_IMG = "images";
  public static final String SUBDIRNAME_INI = "scavenger_hunt";

  private static OutputStream openFileForWriting(String path) throws MalformedURLException, IOException
  {
    URL documentURL = AssetUtils.getAssetURL(path);
    URLConnection conn = documentURL.openConnection();
    conn.connect();

    return conn.getOutputStream();
  }

  public static String getFileNameFromPath(String path)
  {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public static boolean setItem(String xmlPath, Item item)
  {
    try
    {
      File file = new File(getFileNameFromPath(xmlPath));

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Marshaller marshal = context.createMarshaller();

      // output pretty printed
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      marshal.marshal(item, file);
//      marshal.marshal(item, System.out);

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

  private static InputStream openFileForReading(String path) throws MalformedURLException, IOException
  {
    URL documentURL = AssetUtils.getAssetURL(path);
    URLConnection conn = documentURL.openConnection();
    conn.connect();

    return conn.getInputStream();
  }

  public static Item getItem(String xmlPath)
  {
    Item toReturn;
    try
    {
      InputStream in = openFileForReading(xmlPath);

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Unmarshaller marshal = context.createUnmarshaller();

      toReturn = (Item) marshal.unmarshal(in);
    }
    catch (IOException e)
    {
      toReturn = null;
    }
    catch (JAXBException ex)
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
