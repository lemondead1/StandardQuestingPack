package bq_standard.core;

import bq_standard.commands.BQS_Commands;
import bq_standard.commands.BqsComDumpAdvancements;
import bq_standard.core.proxies.CommonProxy;
import bq_standard.handlers.GuiHandler;
import bq_standard.handlers.LootSaveLoad;
import bq_standard.items.ItemLootChest;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

@Mod(modid = BQ_Standard.MODID, version = "@VERSION@", name = BQ_Standard.NAME)
public class BQ_Standard {
  public static final String MODID = "bq_standard";
  public static final String NAME = "Standard Expansion";
  public static final String PROXY = "bq_standard.core.proxies";
  public static final String CHANNEL = "BQ_STANDARD";

  public static boolean hasJEI = false;

  @Instance(MODID)
  public static BQ_Standard instance;

  @SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
  public static CommonProxy proxy;
  public SimpleNetworkWrapper network;
  public static Logger logger;

  public static final Item lootChest = new ItemLootChest();

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
    network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

    proxy.registerHandlers();

    NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ModContainer modContainer = Loader.instance().getIndexedModList().get("bq_standard");
    if (modContainer != null && modContainer.getMod() instanceof BQ_Standard) {
      BQ_Standard modInstance = (BQ_Standard) modContainer.getMod();
      // DO THINGS...
    }
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    if (Loader.isModLoaded("betterquesting")) {
      proxy.registerExpansion();
    }

    hasJEI = Loader.isModLoaded("jei");
  }

  @EventHandler
  public void serverStart(FMLServerStartingEvent event) {
    MinecraftServer server = event.getServer();
    ICommandManager command = server.getCommandManager();
    ServerCommandManager manager = (ServerCommandManager) command;

    manager.registerCommand(new BQS_Commands());
    manager.registerCommand(new BqsComDumpAdvancements());

    LootSaveLoad.INSTANCE.LoadLoot(event.getServer());
  }

  @EventHandler
  public void serverStopped(FMLServerStoppedEvent event) {
    LootSaveLoad.INSTANCE.UnloadLoot();
  }
}
