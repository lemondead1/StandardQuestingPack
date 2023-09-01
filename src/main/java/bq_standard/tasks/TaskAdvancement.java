package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.client.gui.editors.tasks.GuiEditTaskAdvancement;
import bq_standard.client.gui.tasks.PanelTaskAdvancement;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskAdvancement implements ITask {
  private final Set<UUID> completeUsers = ConcurrentHashMap.newKeySet();
  public ResourceLocation advID;

  @Override
  public String getUnlocalisedName() {
    return "bq_standard.task.advancement";
  }

  @Override
  public ResourceLocation getFactoryID() {
    return FactoryTaskAdvancement.INSTANCE.getRegistryName();
  }

  public void onAdvancementGet(DBEntry<IQuest> quest, ParticipantInfo pInfo, Advancement advancement) {
    if (advancement == null || advID == null || !advID.equals(advancement.getId())) { return; }
    detect(pInfo, quest);
  }

  @Override
  public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
    if (!(pInfo.PLAYER instanceof EntityPlayerMP) || pInfo.PLAYER.getServer() == null || advID == null) { return; }

    Advancement adv = pInfo.PLAYER.getServer().getAdvancementManager().getAdvancement(advID);
    if (adv == null) { return; }
    PlayerAdvancements playerAdv =
        pInfo.PLAYER.getServer().getPlayerList().getPlayerAdvancements((EntityPlayerMP) pInfo.PLAYER);

    if (playerAdv.getProgress(adv).isDone()) { setComplete(pInfo.UUID); }
    pInfo.markDirty(Collections.singletonList(quest.getID()));
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
    } else {
      completeUsers.remove(uuid);
    }
  }

  @Nullable
  @Override
  @SideOnly(Side.CLIENT)
  public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
    return new PanelTaskAdvancement(rect, this);
  }

  @Override
  @Nullable
  @SideOnly(Side.CLIENT)
  public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
    return new GuiEditTaskAdvancement(parent, quest, this);
  }

  @Override
  public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
    NBTTagList jArray = new NBTTagList();

    completeUsers.forEach((uuid) -> {
      if (users == null || users.contains(uuid)) { jArray.appendTag(new NBTTagString(uuid.toString())); }
    });

    nbt.setTag("completeUsers", jArray);

    return nbt;
  }

  @Override
  public void readProgressFromNBT(NBTTagCompound nbt, boolean merge) {
    if (!merge) { completeUsers.clear(); }
    NBTTagList cList = nbt.getTagList("completeUsers", 8);
    for (int i = 0; i < cList.tagCount(); i++) {
      try {
        completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
      } catch (Exception e) {
        BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
      }
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setString("advancement_id", advID == null ? "" : advID.toString());
    return nbt;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    String id = nbt.getString("advancement_id");
    advID = StringUtils.isNullOrEmpty(id) ? null : new ResourceLocation(id);
  }
}
