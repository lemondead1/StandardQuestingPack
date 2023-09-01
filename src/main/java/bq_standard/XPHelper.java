package bq_standard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSetExperience;

public class XPHelper {
  public static void addXP(EntityPlayer player, long xp) {
    addXP(player, xp, true);
  }

  public static void addXP(EntityPlayer player, long xp, boolean sync) {
    long experience = getPlayerXP(player) + xp;
    player.experienceTotal = experience >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) experience;
    player.experienceLevel = getXPLevel(experience);
    long expForLevel = getLevelXP(player.experienceLevel);
    player.experience = (float) ((double) (experience - expForLevel) / (double) xpBarCap(player));
    player.experience = Math.max(0F, player.experience); // Sanity check

    if (sync && player instanceof EntityPlayerMP) { syncXP((EntityPlayerMP) player); }
  }

  public static void syncXP(EntityPlayerMP player) {
    // Make sure the client isn't being stupid about syncing the experience bars which routinely fail
    player.connection.sendPacket(
        new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
  }

  public static long getPlayerXP(EntityPlayer player) {
    // Math.max is used here because for some reason the player.experience float value can sometimes be negitive in error
    return getLevelXP(player.experienceLevel) + (long) (xpBarCap(player) * Math.max(0D, player.experience));
  }

  public static long xpBarCap(EntityPlayer player) {
    if (player.experienceLevel < 16) {
      return (long) (2D * player.experienceLevel + 7L);
    } else if (player.experienceLevel < 31) {
      return (long) (5D * player.experienceLevel - 38L);
    } else {
      return (long) (9D * player.experienceLevel - 158L);
    }
  }

  public static int getXPLevel(long xp) {
    if (xp <= 0) {
      return 0;
    }
    if (xp <= 352) {
      return (int) (Math.sqrt(xp + 9) - 3);
    }
    if (xp <= 1507) {
      return (int) (Math.sqrt(0.4 * xp - 78.39) + 8.1);
    }
    return (int) (Math.sqrt(0.2222222222222222 * xp - 167.33024691358025) + 18.055555555555556);
  }

  public static long getLevelXP(long level) {
    if (level <= 0) {
      return 0;
    }
    if (level <= 16) {
      return level * level + 6 * level;
    }
    if (level <= 31) {
      return (5 * level * level - 81 * level + 720) / 2;
    }
    return (9 * level * level - 325 * level + 2220) / 2;
  }
}
