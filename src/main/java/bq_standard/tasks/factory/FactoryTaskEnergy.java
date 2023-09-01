package bq_standard.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskEnergy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskEnergy implements IFactoryData<ITask, NBTTagCompound> {
  public static final FactoryTaskEnergy INSTANCE = new FactoryTaskEnergy();

  private final ResourceLocation regId = new ResourceLocation(BQ_Standard.MODID, "energy");

  @Override
  public ITask loadFromData(NBTTagCompound nbtTagCompound) {
    TaskEnergy task = new TaskEnergy();
    task.readFromNBT(nbtTagCompound);
    return task;
  }

  @Override
  public ResourceLocation getRegistryName() {
    return regId;
  }

  @Override
  public ITask createNew() {
    return new TaskEnergy();
  }
}
