package com.blocky13.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Issue #19: a mob directly beneath a honey block clings to it instead of falling.
 *
 * Honey is sticky, so when a honey block sits right above a mob's head and the mob
 * would otherwise fall, its downward motion is cancelled each tick — the mob hangs
 * from the honey rather than dropping into the air below.
 *
 * The check runs at the TAIL of {@code aiStep()}, after {@code travel()} has already
 * moved the entity and applied gravity for the next tick. Zeroing the (now negative)
 * Y velocity here means next tick's {@code move()} sees no downward motion, producing
 * a clean hang. Only mobs are affected (not players), and only downward motion is
 * cancelled so the mob can still be knocked or jump upward.
 */
@Mixin(LivingEntity.class)
public abstract class HoneyClingMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void blocky13$clingToHoneyAbove(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Mob)) return;

        Vec3 motion = self.getDeltaMovement();
        if (motion.y >= 0.0) return; // only arrest falling; leave jumps/knockback alone

        BlockPos above = BlockPos.containing(self.getX(), self.getBoundingBox().maxY + 0.01, self.getZ());
        if (self.level().getBlockState(above).is(Blocks.HONEY_BLOCK)) {
            self.setDeltaMovement(motion.x, 0.0, motion.z);
            self.resetFallDistance();
        }
    }
}
