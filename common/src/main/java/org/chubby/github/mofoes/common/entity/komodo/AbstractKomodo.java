package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mofoes.common.entity.EntityBison;
import org.chubby.github.mofoes.common.entity.EntityHogmen;

public abstract class AbstractKomodo extends Monster {

    public AbstractKomodo(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.3499999940395355).add(Attributes.MAX_HEALTH, 24.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        } else if (entity instanceof LivingEntity && ((LivingEntity)entity) instanceof AbstractKomodo) {
            return this.getTeam() == null && entity.getTeam() == null;
        } else {
            return false;
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target instanceof Wolf || target instanceof AbstractIllager || target instanceof EntityHogmen ||
                target instanceof AbstractVillager || target instanceof EntityBison || (target instanceof Player playr &&
                !playr.isCreative() && !playr.isSpectator());
    }


    public KomodoArmPoses getArmPose()
    {
        return KomodoArmPoses.CROSSED;
    }

    public static enum KomodoArmPoses
    {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        MELEE_POSE,
        CELEBRATING,
        NEUTRAL;


    }


}
