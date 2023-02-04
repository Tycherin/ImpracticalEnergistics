package com.tycherin.impen.util;

import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

@UtilityClass
public class MobUtil {

    public static boolean canBeCaptured(final Mob mob) {
        if (mob.isBaby() || mob instanceof WitherBoss || mob instanceof EnderDragon) {
            // TODO Forge 1.19 adds a tag to see if a mob is a boss or not, we should denylist those instead of
            // hardcoding vanilla mob classes
            return false;
        }
        else if (mob.isPassenger()) {
            return false;
        }
        else if (mob.getControllingPassenger() != null && mob.getControllingPassenger() instanceof Player) {
            return false;
        }
        else if (mob.hasCustomName()) {
            return false;
        }
        else {
            return true;
        }
    }
}
