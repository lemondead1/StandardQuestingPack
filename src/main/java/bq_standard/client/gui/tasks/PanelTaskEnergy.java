package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class PanelTaskEnergy extends CanvasEmpty {
  private final TaskEnergy task;

  public PanelTaskEnergy(IGuiRect rect, TaskEnergy task) {
    super(rect);
    this.task = task;
  }

  @Override
  public void initPanel() {
    super.initPanel();

    UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
    long progress = task.getUserProgress(uuid);
    long requiredEnergy = task.getRequiredEnergy();
    boolean completed = task.isComplete(uuid);

    String s = progress + "/" + requiredEnergy + "RF\n";
    if (progress >= requiredEnergy || completed) {
      s += TextFormatting.GREEN + QuestTranslation.translate("betterquesting.tooltip.complete");
    } else {
      s += TextFormatting.RED + QuestTranslation.translate("betterquesting.tooltip.incomplete");
    }

    addPanel(new PanelTextBox(new GuiRectangle(0, 0, 200, 50), s));
  }
}
