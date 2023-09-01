package bq_standard.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskTame;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskTame implements IFactoryData<ITask, NBTTagCompound> {
  public static final FactoryTaskTame INSTANCE = new FactoryTaskTame();

  private final ResourceLocation REG_ID = new ResourceLocation(BQ_Standard.MODID, "tame");

  @Override
  public ResourceLocation getRegistryName() {
    return REG_ID;
  }

  @Override
  public TaskTame createNew() {
    return new TaskTame();
  }

  @Override
  public TaskTame loadFromData(NBTTagCompound nbt) {
    TaskTame task = new TaskTame();
    task.readFromNBT(nbt);
    return task;
  }
}
