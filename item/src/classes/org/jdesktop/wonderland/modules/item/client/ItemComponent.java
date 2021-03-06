package org.jdesktop.wonderland.modules.item.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
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
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
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
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.item.common.Abilities;
import org.jdesktop.wonderland.modules.item.common.Abilities.Ability;
import org.jdesktop.wonderland.modules.item.common.ItemComponentClientState;
import org.jdesktop.wonderland.modules.item.common.ItemOwnerChangeMessage;
import org.jdesktop.wonderland.modules.item.common.ScavengerHuntStudent;
import org.jdesktop.wonderland.modules.item.common.UserAbilityChangeMessage;

/**
 * Adds a context menu item to the object with which a user (with the
 * appropriate abilities) can pick up (download) the item information.
 *
 * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
 */
public class ItemComponent extends CellComponent implements ContextMenuActionListener, UserObserver
{

  @UsesCellComponent
  private ContextMenuComponent contextMenu;
  private final ContextMenuFactorySPI menuFactory;
  private String menuItemText = "Pick Up";
  private String title;
  private String description;
  private String imgPath;
  private Ability[] abilities;
  private boolean once;
  private String[] owners;

  private StudentManager studentManager;

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

    studentManager = StudentManager.getInstance();
    studentManager.addObserver(this);

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
  public void actionPerformed(ContextMenuItemEvent event)
  {
    String clickedLabel = event.getContextMenuItem().getLabel();
    if (clickedLabel.equals(menuItemText))
    {
      WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
      String userName = session.getUserID().getUsername();
//      studentManager.loadStudents();
      ScavengerHuntStudent student = studentManager.loadStudentFromFile(userName);
      if (student != null)
      {
        Ability userAbility = student.getAbility();

        String itemAbilitiesString = "";
        if (abilities != null)
        {
          for (Ability ability : abilities)
          {
            itemAbilitiesString += Abilities.getStringFromAbility(ability) + " ";
          }
        }

        boolean isAble = false;

        if (userAbility != null && abilities != null)
        {
          isAble = Arrays.asList(abilities).contains(userAbility);
        }

        boolean otherOwners;
        if (owners.length == 0)
        {
          otherOwners = false;
        }
        else if (owners.length == 1 && owners[0].equals(userName))
        {
          otherOwners = false;
        }
        else
        {
          otherOwners = true;
        }

        if (once && otherOwners)
        {
          JOptionPane.showMessageDialog(null,
            "You cannot pick up this item. It can be picked up only once and "
            + "it seems like someone else was faster than you.", "Denial",
            JOptionPane.ERROR_MESSAGE);
        }
        else if (!isAble)
        {
          JOptionPane.showMessageDialog(null,
            "Only the following people can pick up this item: "
            + itemAbilitiesString, "Denial",
            JOptionPane.ERROR_MESSAGE);
        }
        else
        {
          boolean success = getItemFromServer();

          if (success)
          {
            boolean alreadyInList = false;
            for (String owner : owners)
            {
              if (owner.equals(userName))
              {
                alreadyInList = true;
                break;
              }
            }

            if (!alreadyInList)
            {
              int newLength = owners.length + 1;
              String[] ownersNew = new String[newLength];
              for (int i = 0; i < newLength - 1; i++)
              {
//                System.out.println("  " + owners[i]);
                ownersNew[i] = owners[i];
              }

              ownersNew[newLength - 1] = userName;
              owners = ownersNew;
//              System.out.println("Added " + userName + " to list of item owners.");

              ItemOwnerChangeMessage msg = new ItemOwnerChangeMessage(cell.getCellID(), owners);
              cell.sendCellMessage(msg);
            }

            JOptionPane.showMessageDialog(null, "Item information was picked "
              + "up. You are now able to see it in your Inventory.",
              "Success",
              JOptionPane.INFORMATION_MESSAGE);
          }
          else
          {
            JOptionPane.showMessageDialog(null, "Item information could not be "
              + "picked up.",
              "Failure",
              JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }
  }

  private boolean getItemFromServer()
  {
    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    String userName = session.getUserID().getUsername();

    String fileName = ItemUtils.makeFileName(title);
    File folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + userName + "/items");
    File file = new File(folder.getAbsolutePath() + "/" + fileName + ".xml");

    Item item = new Item(title, description);

    boolean success = true;
    try
    {
      JAXBContext context = JAXBContext.newInstance(Item.class);
      Marshaller marshal = context.createMarshaller();

      // output pretty printed
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      marshal.marshal(item, file);

      if (imgPath.contains("wlcontent"))
      {
        String extension = ItemUtils.getExtension(imgPath);

        folder = ClientContext.getUserDirectory("/cache/wlcontent/users/" + userName + "/items");
        file = new File(folder.getAbsolutePath() + "/" + fileName + extension);

        file.createNewFile();

        OutputStream out = new FileOutputStream(file);
        String userDir = ItemUtils.getUserDirFromPath(imgPath);
//        JOptionPane.showMessageDialog(null, "userDir: " + userDir);
        InputStream in = ItemUtils.openFileForReading(userDir,
          ItemUtils.SUBDIRNAME_IMG,
          ItemUtils.getFileNameFromPath(imgPath));
        writeInToOut(in, out);
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(ItemComponent.class.getName()).log(Level.SEVERE, null, ex);
      success = false;
    }

    return success;
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

  @Override
  protected void setStatus(CellStatus status, boolean increasing)
  {
    boolean just_removed = false;

    if (status == CellStatus.ACTIVE && increasing)
    {
      contextMenu.addContextMenuFactory(menuFactory);
      displayItemEffect(!once || owners.length < 1);

      ChannelComponent channel = cell.getComponent(ChannelComponent.class);
      channel.addMessageReceiver(ItemOwnerChangeMessage.class, new ItemComponentMessageReceiver());
      channel.addMessageReceiver(UserAbilityChangeMessage.class, new ItemComponentMessageReceiver());
    }
    else if (status == CellStatus.INACTIVE && !increasing)
    {
      contextMenu.removeContextMenuFactory(menuFactory);
      displayItemEffect(false);

      ChannelComponent channel = cell.getComponent(ChannelComponent.class);
      channel.removeMessageReceiver(ItemOwnerChangeMessage.class);
      channel.removeMessageReceiver(UserAbilityChangeMessage.class);

      just_removed = true;
    }

    if ((status == CellStatus.RENDERING || status == CellStatus.VISIBLE) && increasing && !just_removed)
    {
      displayItemEffect(!once || owners.length < 1);
    }

    super.setStatus(status, increasing);
  }

  @Override
  public void setClientState(CellComponentClientState clientState
  )
  {
    super.setClientState(clientState);
    title = ((ItemComponentClientState) clientState).getTitle();
    description = ((ItemComponentClientState) clientState).getDescription();
    imgPath = ((ItemComponentClientState) clientState).getImgPath();
    abilities = ((ItemComponentClientState) clientState).getAbilities();
    once = ((ItemComponentClientState) clientState).getOnce();

    owners = ((ItemComponentClientState) clientState).getOwners();
    displayItemEffect(!once || owners.length < 1);
  }

  public String getTitle()
  {
    return title;
  }

  public String getDescription()
  {
    return description;
  }

  public Ability[] getAbilities()
  {
    return abilities;
  }

  public boolean getOnce()
  {
    return once;
  }

  public String[] getOwners()
  {
    return owners;
  }

  public boolean removeOwner(String userName)
  {
    int removeIndex = -1;

    for (int index = 0; index < owners.length; index++)
    {
      if (owners[index].equals(userName))
      {
        removeIndex = index;
        break;
      }
    }

    // Nothing to remove
    if (removeIndex == -1)
    {
      return false;
    }

    // Make new array one element smaller than old one
    String[] newOwners = new String[owners.length - 1];
    int newIndex = 0;
    for (int index = 0; index < owners.length; index++)
    {
      // Copy old array into new one but leave out userName to delete
      if (index != removeIndex)
      {
        newOwners[newIndex] = owners[index];
        newIndex++;
      }
    }
    owners = newOwners;

    ItemOwnerChangeMessage msg = new ItemOwnerChangeMessage(cell.getCellID(), owners);
    cell.sendCellMessage(msg);

    return true;
  }

  @Override
  public void userAbilitesChanged(ScavengerHuntStudent user)
  {
    String[] userS = new String[2];
    userS[0] = user.getUsername();
    userS[1] = Abilities.getStringFromAbility(user.getAbility());

    UserAbilityChangeMessage msg = new UserAbilityChangeMessage(cell.getCellID(), userS);
    cell.sendCellMessage(msg);
  }

  class ItemComponentMessageReceiver implements ComponentMessageReceiver
  {

    @Override
    public void messageReceived(CellMessage message)
    {
      if (message instanceof ItemOwnerChangeMessage)
      {
        ItemOwnerChangeMessage iocm = (ItemOwnerChangeMessage) message;
        if (!iocm.getSenderID().equals(cell.getCellCache().getSession().getID()))
        {
          owners = iocm.getOwners();
        }

        System.out.println("Received new owners for item " + cell.getName());
        for (String owner : owners)
        {
          System.out.println("  " + owner);
        }

        displayItemEffect(!once || owners.length < 1);
      }
      else if (message instanceof UserAbilityChangeMessage)
      {
        UserAbilityChangeMessage uacm = (UserAbilityChangeMessage) message;
        if (!uacm.getSenderID().equals(cell.getCellCache().getSession().getID()))
        {
          String[] user = uacm.getUser();
          if (user != null)
          {
            ScavengerHuntStudent scavengerHuntStudent = new ScavengerHuntStudent(user[0], Abilities.getAbilityFromString(user[1]));
            studentManager.getStudents().put(user[0], scavengerHuntStudent);

            System.out.println("Received new abilites for user "
              + scavengerHuntStudent.getUsername() + ": "
              + scavengerHuntStudent.getAbility());
          }
        }
      }
    }
  }

  /**
   * The following methods for surrounding the object with glitter so that
   * everyone recognizes it as an item which can be picked up as well as the art
   * (kmz files) that are used for that reason are from the cell component for
   * admin tools
   *
   * @author Jonathan Kaplan <jonathankap@gmail.com>
   *
   * adapted by
   * @author Lisa Tomes <lisa.tomes@student.tugraz.at>
   */
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
      return dm.getModelLoader().loadDeployedModel(dm, null);
    }
    catch (IOException ex)
    {
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
