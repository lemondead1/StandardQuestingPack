package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.storage.DBEntry;
import bq_standard.network.handlers.NetTaskCheckbox;
import bq_standard.tasks.TaskCheckbox;
import net.minecraft.client.Minecraft;

public class PanelTaskCheckbox extends CanvasEmpty {
  private final DBEntry<IQuest> quest;
  private final TaskCheckbox task;

  public PanelTaskCheckbox(IGuiRect rect, DBEntry<IQuest> quest, TaskCheckbox task) {
    super(rect);
    this.quest = quest;
    this.task = task;
  }

  @Override
  public void initPanel() {
    super.initPanel();

    boolean isComplete = task.isComplete(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player));
    final int questID = quest.getID();
    final int taskID = quest.getValue().getTasks().getID(task);

    PanelButton btnCheck = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -16, -16, 32, 32, 0), -1, "") {
      @Override
      public void onButtonClick() {
        setIcon(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00), 4);
        setActive(false);

        NetTaskCheckbox.requestClick(questID, taskID);
      }
    };
    btnCheck.setIcon(isComplete ? PresetIcon.ICON_TICK.getTexture() : PresetIcon.ICON_CROSS.getTexture(),
                     new GuiColorStatic(isComplete ? 0xFF00FF00 : 0xFFFF0000), 4);
    btnCheck.setActive(!isComplete);
    this.addPanel(btnCheck);
  }
}
