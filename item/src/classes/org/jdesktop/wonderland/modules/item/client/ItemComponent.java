package org.jdesktop.wonderland.modules.item.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentClientState;

/**
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponent extends CellComponent implements ContextMenuActionListener
{

  @UsesCellComponent
  private ContextMenuComponent contextMenu;
  private final ContextMenuFactorySPI menuFactory;
  private String menuItemText = "Pick Up";
  private String xmlPath;
  private String imgPath;
  private Ability[] abilities;

  private final String configFileName = "scavenger_hunt.ini";
  private final String roleLineStartsWith = "role";

  private final int PROBLEMS_WITH_FILE = -1;
  private final int NO_ROLE_CONFIG = -2;

  // item glittering effect
  private Node glitteringNode;
  private ProcessorComponent glitteringProcessor;

  public ItemComponent(Cell cell)
  {
    super(cell);

    final ContextMenuItem item = new SimpleContextMenuItem(menuItemText, this);
    menuFactory = new ContextMenuFactorySPI()
    {
      @Override
      public ContextMenuItem[] getContextMenuItems(ContextEvent event)
      {
        return new ContextMenuItem[]
        {
          item
        };
      }
    };
  }

  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
//    System.out.println("Cell status: " + status);

    boolean just_removed = false;

    if (status == CellStatus.ACTIVE && increasing)
    {
//      System.out.println("add context menu factory and item effect");
      contextMenu.addContextMenuFactory(menuFactory);
      displayItemEffect(true);
    }
    else if (status == CellStatus.INACTIVE && !increasing)
    {
//      System.out.println("REMOVE context menu factory and item effect");
      contextMenu.removeContextMenuFactory(menuFactory);
      displayItemEffect(false);

//      System.out.println("Just removed set to TRUE");
      just_removed = true;
    }

    if ((status == CellStatus.RENDERING || status == CellStatus.VISIBLE) && increasing && !just_removed)
    {
//      System.out.println("displayItemEffect");
      displayItemEffect(true);
    }

    super.setStatus(status, increasing);
  }

  @Override
  public void setClientState(CellComponentClientState clientState)
  {
    super.setClientState(clientState);
    xmlPath = ((ItemComponentClientState) clientState).getXmlPath();
    imgPath = ((ItemComponentClientState) clientState).getImgPath();
    abilities = ((ItemComponentClientState) clientState).getAbilities();

//    System.out.println("displayItemEffect called by setClientState");
    displayItemEffect(true);
  }

  @Override
  public void actionPerformed(ContextMenuItemEvent event)
  {
    String clickedLabel = event.getContextMenuItem().getLabel();
    if (clickedLabel.equals(menuItemText))
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();
      ScavengerHuntStudent student = StudentManager.loadStudentFromFile(userName);
      if (student != null)
      {
        Ability userAbility = student.getAbility();

//        if (userAbility != null)
//        {
//          String userAbilityString = Abilities.getStringFromAbility(userAbility);
//          System.out.println("Your ability is: " + userAbilityString);
//        }
//        else
//        {
//          System.out.println("Your ability is null");
//        }
        String itemAbilitiesString = "";
        if (abilities != null)
        {
          for (Ability ability : abilities)
          {
            itemAbilitiesString += Abilities.getStringFromAbility(ability) + " ";
          }
//          System.out.println("The item's abilities are: " + itemAbilitiesString);
        }
//        else
//        {
//          System.out.println("The item's abilities are null");
//        }

        boolean contains = false;

        if (userAbility != null && abilities != null)
        {
          contains = Arrays.asList(abilities).contains(userAbility);
        }

        if (contains)
        {
//          System.out.println("You are allowed to pick up this item.");
          getItemFromServer();
        }
        else
        {
          JOptionPane.showMessageDialog(null,
            "Only the following people can pick up this item: " + itemAbilitiesString,
            "Denial",
            JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void getItemFromServer()
  {
    try
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();

      if (xmlPath.contains("wlcontent"))
      {
        String fileName = get(xmlPath, userName + "/items", null);
        if (imgPath.contains("wlcontent"))
        {
          // get image but save it with same name as description
          // to mark these to belong together
          get(imgPath, userName + "/items", fileName);
        }
      }
    }
    catch (IOException ex)
    {
      Logger.getLogger(ItemComponent.class.getName()).log(Level.SEVERE,
        "Could not open file.");
    }
  }

  private String get(String filePath, String localFolder, String fileName) throws FileNotFoundException, IOException
  {
    File folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + localFolder);

    String fileNameToReturn = fileName;
    // If given file name is null, take own
    if (fileName == null)
    {
      fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

      // Cut off extension for fileNameToReturn
      int index = fileName.lastIndexOf(".");
      fileNameToReturn = fileName.substring(0, index);
    }
    else // else take given and add own extension
    {
      String ownFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
      String ownExtension = ownFileName.substring(ownFileName.lastIndexOf("."));
      fileName += ownExtension;
    }

    //Cell cell = CellCacheBasicImpl.getCurrentActiveCell();
    //String fileName = cell.getName();
    //String fileExtension = filePath.substring(filePath.lastIndexOf("."));
    //fileName += fileExtension;
    File newFile = new File(folder.getAbsolutePath() + "/" + fileName);

    OutputStream out = new FileOutputStream(newFile);

    InputStream in = openFile(filePath);

    writeInToOut(in, out);

    return fileNameToReturn;
  }

  private void writeInToOut(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[32 * 1024];

    int length;
    //copy the file content in bytes
    while ((length = in.read(buffer)) > 0)
    {
      out.write(buffer, 0, length);
    }
    in.close();
    out.close();
  }

  private InputStream openFile(String path) throws IOException
  {
    URL documentURL = AssetUtils.getAssetURL(path);
    URLConnection conn = documentURL.openConnection();
    conn.connect();

    return conn.getInputStream();
  }

  public String getInfoText()
  {
    String text;
    try
    {
      InputStream in = openFile(xmlPath);

      JAXBContext context = JAXBContext.newInstance(Item.class);
      Unmarshaller marshal = context.createUnmarshaller();

      Item unmarshalled = (Item) marshal.unmarshal(in);

      /*
       InputStreamReader inReader = new InputStreamReader(in);
       BufferedReader bufReader = new BufferedReader(inReader);
       String line;
       while ((line = bufReader.readLine()) != null)
       {
       text += line + "\n";
       }

       bufReader.close();
       */
      text = "<h1>" + unmarshalled.getTitle() + "</h1>" + unmarshalled.getContent();
    }
    catch (IOException e)
    {
      /*
       Logger.getLogger(ItemComponent.class.getName()).log(Level.SEVERE,
       "Sorry, could not open xml description file.", e);
       */
      text = "Sorry, could not open xml description file.";
    }
    catch (JAXBException ex)
    {
      Logger.getLogger(ItemComponent.class.getName()).log(Level.SEVERE, null, ex);
      text = "Xml description file does not have the right format.";
    }

    return text;
  }

  private void displayItemEffect(boolean display)
  {
//    // I have no idea what's that for...
//    if (status.ordinal() < CellStatus.RENDERING.ordinal())
//    {
//      System.out.println("What are we doing here?!");
//      return;
//    }

    if (glitteringProcessor != null)
    {
      removeProcessor(glitteringProcessor);
    }

    if (glitteringNode != null)
    {
      detachNode(glitteringNode);
    }

    if (display)
    {
      glitteringNode = new Node();
      glitteringNode.attachChild(loadGlitteringEffect());
      attachNode(glitteringNode);

      glitteringProcessor = new GlitteringProcessorComponent(glitteringNode);
      addProcessor(glitteringProcessor);
    }
  }

  private Node loadGlitteringEffect()
  {
    try
    {
      URL modelURL = AssetUtils.getAssetURL("wla://item-component/tube.kmz/tube.kmz.dep");

      // create a node
      LoaderManager lm = LoaderManager.getLoaderManager();
      DeployedModel dm = lm.getLoaderFromDeployment(modelURL);
//      System.out.println("ITEM: Successfully loaded model: " + dm);
      return dm.getModelLoader().loadDeployedModel(dm, null);
    }
    catch (IOException ex)
    {
//      System.out.println("ITEM: Warning: URL error loading effect: " + ex.toString());
      return null;
    }
  }

  private void attachNode(final Node node)
  {
    SceneWorker.addWorker(new WorkCommit()
    {
      @Override
      public void commit()
      {
        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

        if (renderer instanceof ModelRenderer)
        {
          ModelRenderer modr = ((ModelRenderer) renderer);
          modr.setCollisionEnabled(false);
          Entity mye = modr.getEntity();
          RenderComponent rc = (RenderComponent) mye.getComponent(RenderComponent.class);
          Node root = rc.getSceneRoot();

          root.attachChild(node);

          node.setLocalScale(cell.getLocalToWorldTransform().getScaling(null));
          node.setLocalTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
          node.setLightCombineMode(LightCombineMode.Off);
          ClientContextJME.getWorldManager().addToUpdateList(node);
        }
      }
    });
  }

  private void detachNode(final Node node)
  {
    SceneWorker.addWorker(new WorkCommit()
    {
      @Override
      public void commit()
      {
        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (renderer instanceof ModelRenderer)
        {
          ModelRenderer modr = ((ModelRenderer) renderer);
          modr.setCollisionEnabled(false);
          Entity mye = modr.getEntity();
          RenderComponent rc = (RenderComponent) mye.getComponent(RenderComponent.class);
          Node root = rc.getSceneRoot();

          root.detachChild(node);
        }
      }
    });
  }

  private void addProcessor(ProcessorComponent processor)
  {
    CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
    if (renderer instanceof ModelRenderer)
    {
      Entity e = ((ModelRenderer) renderer).getEntity();
      ProcessorCollectionComponent pcc
        = e.getComponent(ProcessorCollectionComponent.class);
      if (pcc == null)
      {
        pcc = new ProcessorCollectionComponent();
        e.addComponent(pcc.getClass(), pcc);
      }

      pcc.addProcessor(processor);
    }
  }

  private void removeProcessor(ProcessorComponent processor)
  {
    CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
    if (renderer instanceof ModelRenderer)
    {
      Entity e = ((ModelRenderer) renderer).getEntity();
      ProcessorCollectionComponent pcc
        = e.getComponent(ProcessorCollectionComponent.class);
      if (pcc != null)
      {
        pcc.removeProcessor(processor);
      }
    }
  }

  private class GlitteringProcessorComponent extends ProcessorComponent
  {

    // radians per millisecond
    private static final float RATE = (float) Math.PI / 10000f;

    private final Node node;
    private float rotation = 0f;
    private long lastTime = System.currentTimeMillis();

    public GlitteringProcessorComponent(Node node)
    {
      this.node = node;
    }

    @Override
    public void initialize()
    {
      //This method tells the game engine to process our animation everytime a
      //new frame is to be displayed. Typically, the human eye sees 24 frames
      //per second (FPS).
      setArmingCondition(new NewFrameCondition(this));
    }

    @Override
    public void compute(ProcessorArmingCollection pac)
    {
    }

    @Override
    public void commit(ProcessorArmingCollection pac)
    {
      long now = System.currentTimeMillis();
      long dTime = now - lastTime;

      rotation += RATE * dTime;
      rotation %= 2 * Math.PI;

      Quaternion q = new Quaternion();
      q.fromAngleAxis(rotation, new Vector3f(0, 1, 0));
      node.setLocalRotation(q);

      ClientContextJME.getWorldManager().addToUpdateList(node);

      lastTime = now;
    }
  }
}
