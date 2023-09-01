package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelEntityPreview;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskHunt;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

public class PanelTaskHunt extends CanvasEmpty {
  private final TaskHunt task;

  public PanelTaskHunt(IGuiRect rect, TaskHunt task) {
    super(rect);
    this.task = task;
  }

  @Override
  public void initPanel() {
    super.initPanel();

    ResourceLocation targetRes = new ResourceLocation(task.idName);
    Entity target;

    if (EntityList.isRegistered(targetRes)) {
      target = EntityList.createEntityByIDFromName(targetRes, Minecraft.getMinecraft().world);
      if (target != null) { target.readFromNBT(task.targetTags); }
    } else {
      target = null;
    }

    int progress = task.getUsersProgress(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player));
    String tnm = target != null ? target.getName() : task.idName;

    this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0),
                                   QuestTranslation.translate("bq_standard.gui.kill", tnm) + " " + progress + "/" +
                                   task.required).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));

    if (target != null) {
      this.addPanel(
          new PanelEntityPreview(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 0, 0), 0),
                                 target).setRotationDriven(new ValueFuncIO<>(() -> 15F), new ValueFuncIO<>(
              () -> (float) (Minecraft.getSystemTime() % 30000L / 30000D * 360D))));
    }
  }
}
