package bq_standard.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.tasks.TaskCrafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskCraft {
  public ITask[] convertTask(JsonObject json) {
    TaskCrafting task = new TaskCrafting();

    for (JsonElement element : JsonHelper.GetArray(json, "items")) {
      if (!(element instanceof JsonObject)) { continue; }
      task.requiredItems.add(HQMUtilities.HQMStackT2(element.getAsJsonObject()));
    }

    return new ITask[] { task };
  }
}
