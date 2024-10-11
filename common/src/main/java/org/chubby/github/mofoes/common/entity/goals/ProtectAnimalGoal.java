package org.chubby.github.mofoes.common.entity.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ProtectAnimalGoal extends TargetGoal {
    private final Class<? extends LivingEntity> targetClass; // The class of the animal to protect
    private LivingEntity targetAnimal; // The animal being protected

    public ProtectAnimalGoal(Mob mob, Class<? extends LivingEntity> targetClass, boolean checkSight) {
        super(mob, checkSight);
        this.targetClass = targetClass;
    }

    @Override
    public boolean canUse() {
        // Check for nearby animals to protect
        List<? extends LivingEntity> nearbyAnimals = this.mob.level().getEntitiesOfClass(targetClass, this.mob.getBoundingBox().inflate(16.0D)); // Change the range as needed
        for (LivingEntity animal : nearbyAnimals) {
            // If an animal is found and it's not tamed, set it as the targetAnimal
            if (animal instanceof TamableAnimal && !((TamableAnimal) animal).isTame()) {
                this.targetAnimal = animal;
                return true;
            }
        }
        return false; // No animal found to protect
    }

    @Override
    public void start() {
        // Set the target to be the attacker of the protected animal
        if (targetAnimal != null) {
            LivingEntity attacker = targetAnimal.getLastHurtByMob(); // Get the last entity that attacked the target animal
            if (attacker != null && attacker instanceof Player) { // Check if the attacker is a player
                this.mob.setTarget(attacker); // Set the mob's target to the attacker
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        // The goal can continue if the protected animal is still alive and there's a valid target
        return targetAnimal != null && targetAnimal.isAlive() && this.mob.getTarget() != null;
    }

    @Override
    public void stop() {
        super.stop();
        targetAnimal = null; // Reset the target animal when the goal stops
    }
}
