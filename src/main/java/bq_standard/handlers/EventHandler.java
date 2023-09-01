package bq_standard.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.advancment_hacks.AdvListenerManager;
import bq_standard.network.handlers.NetLootSync;
import bq_standard.network.handlers.NetTaskInteract;
import bq_standard.tasks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.List;

public class EventHandler {
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRightClickItem(RightClickItem event) {
    if (event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) { return; }

    EntityPlayer player = event.getEntityPlayer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskInteractItem) {
          ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(),
                                                          Blocks.AIR.getDefaultState(), event.getPos(), false);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRightClickBlock(RightClickBlock event) {
    if (event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) { return; }

    EntityPlayer player = event.getEntityPlayer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    IBlockState state = player.world.getBlockState(event.getPos());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskInteractItem) {
          ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), state,
                                                          event.getPos(), false);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onLeftClickBlock(LeftClickBlock event) {
    if (event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) { return; }

    EntityPlayer player = event.getEntityPlayer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    IBlockState state = player.world.getBlockState(event.getPos());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskInteractItem) {
          ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), state,
                                                          event.getPos(), true);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onRightClickEmpty(RightClickEmpty event) // CLIENT SIDE ONLY EVENT
  {
    if (event.getEntityPlayer() == null || !event.getEntityLiving().world.isRemote || event.isCanceled()) { return; }
    NetTaskInteract.requestInteraction(false, event.getHand() == EnumHand.MAIN_HAND);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onLeftClickAir(LeftClickEmpty event) // CLIENT SIDE ONLY EVENT
  {
    if (event.getEntityPlayer() == null || !event.getEntityLiving().world.isRemote || event.isCanceled()) { return; }
    NetTaskInteract.requestInteraction(true, true);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntityAttack(AttackEntityEvent event) {
    if (event.getEntityPlayer() == null || event.getTarget() == null || event.getEntityPlayer().world.isRemote ||
        event.isCanceled()) { return; }

    EntityPlayer player = event.getEntityPlayer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskInteractEntity) {
          ((TaskInteractEntity) task.getValue()).onInteract(pInfo, entry, EnumHand.MAIN_HAND,
                                                            player.getHeldItemMainhand(), event.getTarget(), true);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntityInteract(EntityInteract event) {
    if (event.getEntityPlayer() == null || event.getTarget() == null || event.getEntityPlayer().world.isRemote ||
        event.isCanceled()) { return; }

    EntityPlayer player = event.getEntityPlayer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskInteractEntity) {
          ((TaskInteractEntity) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(),
                                                            event.getTarget(), false);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onItemCrafted(ItemCraftedEvent event) {
    if (event.player == null || event.player.world.isRemote) { return; }

    ParticipantInfo pInfo = new ParticipantInfo(event.player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskCrafting) {
          ((TaskCrafting) task.getValue()).onItemCraft(pInfo, entry, event.crafting.copy());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onItemSmelted(ItemSmeltedEvent event) {
    if (event.player == null || event.player.world.isRemote) { return; }

    ParticipantInfo pInfo = new ParticipantInfo(event.player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskCrafting) {
          ((TaskCrafting) task.getValue()).onItemSmelt(pInfo, entry, event.smelting.copy());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onItemAnvil(AnvilRepairEvent event) {
    if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote) { return; }

    ParticipantInfo pInfo = new ParticipantInfo(event.getEntityPlayer());
    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskCrafting) {
          ((TaskCrafting) task.getValue()).onItemAnvil(pInfo, entry, event.getItemResult().copy());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntityKilled(LivingDeathEvent event) {
    if (event.getSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer) ||
        event.getSource().getTrueSource().world.isRemote || event.isCanceled()) { return; }

    ParticipantInfo pInfo = new ParticipantInfo((EntityPlayer) event.getSource().getTrueSource());
    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskHunt) {
          ((TaskHunt) task.getValue()).onKilledByPlayer(pInfo, entry, event.getEntityLiving(), event.getSource());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntityTamed(AnimalTameEvent event) {
    if (event.getTamer() == null || event.getTamer().world.isRemote || event.isCanceled()) { return; }

    EntityPlayer player = event.getTamer();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskTame) {
          ((TaskTame) task.getValue()).onAnimalTamed(pInfo, entry, event.getEntityLiving());
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onBlockBreak(BlockEvent.BreakEvent event) {
    if (event.getPlayer() == null || event.getPlayer().world.isRemote || event.isCanceled()) { return; }

    ParticipantInfo pInfo = new ParticipantInfo(event.getPlayer());

    for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskBlockBreak) {
          ((TaskBlockBreak) task.getValue()).onBlockBreak(pInfo, entry, event.getState(), event.getPos());
        }
      }
    }
  }

  @SubscribeEvent
  public void onEntityLiving(LivingUpdateEvent event) {
    if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote ||
        event.getEntityLiving().ticksExisted % 20 != 0 ||
        QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) { return; }

    EntityPlayer player = (EntityPlayer) event.getEntityLiving();
    ParticipantInfo pInfo = new ParticipantInfo(player);

    List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

    for (DBEntry<IQuest> entry : actQuest) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof ITaskTickable) {
          ((ITaskTickable) task.getValue()).tickTask(pInfo, entry);
        } else if (task.getValue() instanceof TaskTrigger) {
          ((TaskTrigger) task.getValue()).checkSetup(player, entry);
        }
      }
    }
  }

  @SubscribeEvent
  public void onAdvancement(AdvancementEvent event) {
    if (event.getEntityPlayer() == null || event.getEntity().world.isRemote) { return; }

    ParticipantInfo pInfo = new ParticipantInfo(event.getEntityPlayer());

    for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
      for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
        if (task.getValue() instanceof TaskAdvancement) {
          ((TaskAdvancement) task.getValue()).onAdvancementGet(entry, pInfo, event.getAdvancement());
        }
      }
    }
  }

  @SubscribeEvent
  public void onEntityCreated(EntityJoinWorldEvent event) {
    if (!(event.getEntity() instanceof EntityPlayer) || event.getEntity().world.isRemote) { return; }

    PlayerContainerListener.refreshListener((EntityPlayer) event.getEntity());
  }

  @SubscribeEvent
  public void onServerTick(ServerTickEvent event) {
    if (event.phase != Phase.START ||
        FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % 60 != 0) { return; }

    AdvListenerManager.INSTANCE.updateAll();
  }

  @SubscribeEvent
  public void onPlayerJoin(PlayerLoggedInEvent event) {
    if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
      NetLootSync.sendSync((EntityPlayerMP) event.player);
    }
  }

  @SubscribeEvent
  public void onWorldSave(WorldEvent.Save event) {
    if (!event.getWorld().isRemote && LootSaveLoad.INSTANCE.worldDir != null &&
        event.getWorld().provider.getDimension() == 0) {
      LootSaveLoad.INSTANCE.SaveLoot();
    }
  }
}
