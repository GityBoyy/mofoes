package org.chubby.github.mofoes.common.entity.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.chubby.github.mofoes.common.entity.EntityHogmen;

public class StayWithEntityGoal extends Goal {
    private final TamableAnimal mob;
    private EntityHogmen owner;
    private final float speedModifier;
    private final boolean followIfNotSeen;
    private final double maxDistanceSquared = 100;

    public StayWithEntityGoal(TamableAnimal mob, Class<EntityHogmen> owner, float speedModifier, boolean followIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followIfNotSeen = followIfNotSeen;
    }

    @Override
    public boolean canUse() {
        if(this.owner==null) return false;
        return this.mob.isAlive() && !this.mob.isAggressive() && !this.mob.isTame()
                && this.mob.distanceToSqr(this.owner) > maxDistanceSquared;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null) {
            return false;
        }

        return this.mob.isAlive() && this.mob.distanceToSqr(this.owner) > maxDistanceSquared
                && (!this.mob.getNavigation().isDone() || followIfNotSeen);
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
    }

    @Override
    public void tick() {
        if (this.owner == null) return;

        if (this.mob.distanceToSqr(this.owner) > maxDistanceSquared) {
            this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }
}