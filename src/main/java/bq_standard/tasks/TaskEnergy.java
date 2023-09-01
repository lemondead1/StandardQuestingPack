package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IEnergyTask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.client.gui.tasks.PanelTaskEnergy;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskEnergy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskEnergy implements IEnergyTask {
  private final Set<UUID> completeUsers = ConcurrentHashMap.newKeySet();
  private final Map<UUID, Long> userProgress = new ConcurrentHashMap<>();
  private long requiredEnergy;

  @Override
  public String getUnlocalisedName() {
    return BQ_Standard.MODID + ".task.energy";
  }

  @Override
  public int submitEnergy(UUID owner, DBEntry<IQuest> quest, int energy, boolean doSubmit) {
    if (energy == 0 || owner == null || isComplete(owner)) {
      return energy;
    }

    long currentProgress = userProgress.getOrDefault(owner, 0L);
    long maxAdd = Math.max(0, requiredEnergy - currentProgress);
    long toAdd = Math.min(maxAdd, energy);
    int remainder = Math.max(0, (int) (energy - toAdd));
    if (doSubmit) {
      boolean updated = false;
      if (toAdd > 0) {
        updated = true;
        currentProgress += toAdd;
        userProgress.put(owner, currentProgress);
      }
      if (currentProgress >= requiredEnergy) {
        updated = true;
        setComplete(owner);
      }
      if (updated) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(owner);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if (qc != null) {
          qc.markQuestDirty(quest.getID());
        }
      }
    }
    return remainder;
  }

  @Override
  public ResourceLocation getFactoryID() {
    return FactoryTaskEnergy.INSTANCE.getRegistryName();
  }

  @Override
  public void detect(ParticipantInfo participantInfo, DBEntry<IQuest> dbEntry) {
    if (isComplete(participantInfo.UUID)) {
      return;
    }
    InventoryPlayer inv = participantInfo.PLAYER.inventory;

    long currentProgress = userProgress.getOrDefault(participantInfo.UUID, 0L);

    boolean updated = false;

    for (int i = 0; i < inv.getSizeInventory() && currentProgress < requiredEnergy; i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (!stack.isEmpty()) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (storage != null && storage.canExtract()) {
          int toExtract = (int) Math.min(requiredEnergy - currentProgress, Integer.MAX_VALUE / 2);
          int extracted = storage.extractEnergy(toExtract, false);
          if (extracted > 0) {
            currentProgress += extracted;
            updated = true;
            inv.markDirty();
          }
        }
      }
    }
    if (updated) {
      userProgress.put(participantInfo.UUID, currentProgress);
    }
    if (currentProgress >= requiredEnergy) {
      updated = true;
      setComplete(participantInfo.UUID);
    }
    if (updated) {
      participantInfo.markDirty(Collections.singletonList(dbEntry.getID()));
    }
  }

  @Override
  public boolean isComplete(UUID uuid) {
    return completeUsers.contains(uuid);
  }

  @Override
  public void setComplete(UUID uuid) {
    completeUsers.add(uuid);
  }

  @Override
  public void resetUser(@Nullable UUID uuid) {
    if (uuid == null) {
      completeUsers.clear();
      userProgress.clear();
    } else {
      completeUsers.remove(uuid);
      userProgress.remove(uuid);
    }
  }

  @Nullable
  @Override
  public IGuiPanel getTaskGui(IGuiRect iGuiRect, DBEntry<IQuest> dbEntry) {
    return new PanelTaskEnergy(iGuiRect, this);
  }

  @Nullable
  @Override
  public GuiScreen getTaskEditor(GuiScreen guiScreen, DBEntry<IQuest> dbEntry) {
    return null;
  }

  @Override
  public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
    NBTTagList jArray = new NBTTagList();
    NBTTagList progArray = new NBTTagList();

    if (users == null) {
      completeUsers.forEach(uuid -> jArray.appendTag(new NBTTagString(uuid.toString())));

      userProgress.forEach((uuid, data) -> {
        NBTTagCompound pJson = new NBTTagCompound();
        pJson.setString("uuid", uuid.toString());
        pJson.setLong("progress", data);
        progArray.appendTag(pJson);
      });
    } else {
      users.forEach(uuid -> {
        if (completeUsers.contains(uuid)) {
          jArray.appendTag(new NBTTagString(uuid.toString()));
        }

        Long data = userProgress.get(uuid);
        if (data != null) {
          NBTTagCompound pJson = new NBTTagCompound();
          pJson.setString("uuid", uuid.toString());
          pJson.setLong("progress", data);
          progArray.appendTag(pJson);
        }
      });
    }

    nbt.setTag("completeUsers", jArray);
    nbt.setTag("userProgress", progArray);

    return nbt;
  }

  @Override
  public void readProgressFromNBT(NBTTagCompound nbtTagCompound, boolean merge) {
    if (!merge) {
      completeUsers.clear();
      userProgress.clear();
    }

    NBTTagList completedList = nbtTagCompound.getTagList("completeUsers", Constants.NBT.TAG_STRING);
    for (int i = 0; i < completedList.tagCount(); i++) {
      try {
        completeUsers.add(UUID.fromString(completedList.getStringTagAt(i)));
      } catch (Exception e) {
        BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
      }
    }

    NBTTagList progressList = nbtTagCompound.getTagList("userProgress", Constants.NBT.TAG_COMPOUND);
    for (int n = 0; n < progressList.tagCount(); n++) {
      try {
        NBTTagCompound progressTag = progressList.getCompoundTagAt(n);
        UUID uuid = UUID.fromString(progressTag.getString("uuid"));
        long value = progressTag.getLong("progress");
        userProgress.merge(uuid, value, Math::max);
      } catch (Exception e) {
        BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
      }
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
    nbtTagCompound.setLong("RequiredEnergy", requiredEnergy);
    return nbtTagCompound;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbtTagCompound) {
    requiredEnergy = nbtTagCompound.getLong("RequiredEnergy");
  }

  public long getUserProgress(UUID user) {
    return userProgress.getOrDefault(user, 0L);
  }

  public long getRequiredEnergy() {
    return requiredEnergy;
  }
}
