package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class KomodoSorcerer extends AbstractSorcerer
{

    public KomodoSorcerer(EntityType<? extends AbstractSorcerer> entityType, Level level) {
        super(entityType, level);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new KomodoSorcerer.EvokerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidEntityGoal(this, Player.class, 8.0F, 0.6, 1.0));
        this.goalSelector.addGoal(4, new KomodoSorcerer.EvokerSummonSpellGoal());
        this.goalSelector.addGoal(5, new KomodoSorcerer.EvokerAttackSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[]{Raider.class})).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, (new NearestAttackableTargetGoal(this, Player.class, true)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal(this, AbstractVillager.class, false)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false));
    }
    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    public boolean isAlliedTo(Entity entity) {
        if (entity == null) {
            return false;
        } else if (entity == this) {
            return true;
        } else if (super.isAlliedTo(entity)) {
            return true;
        } else if (entity instanceof Vex) {
            return this.isAlliedTo(((Vex)entity).getOwner());
        } else if (entity instanceof LivingEntity && ((LivingEntity)entity)instanceof AbstractKomodo) {
            return this.getTeam() == null && entity.getTeam() == null;
        } else {
            return false;
        }
    }

    private class EvokerCastingSpellGoal extends AbstractSorcerer.SpellcasterCastingSpellGoal {
        EvokerCastingSpellGoal() {
            super();
        }

        public void tick() {
            if (KomodoSorcerer.this.getTarget() != null) {
                KomodoSorcerer.this.getLookControl().setLookAt(KomodoSorcerer.this.getTarget(), (float)KomodoSorcerer.this.getMaxHeadYRot(), (float)KomodoSorcerer.this.getMaxHeadXRot());
            }

        }
    }

    private class EvokerSummonSpellGoal extends AbstractSorcerer.SpellcasterUseSpellGoal {
        private final TargetingConditions vexCountTargeting = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();

        EvokerSummonSpellGoal() {
            super();
        }

        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else {
                int i = KomodoSorcerer.this.level().getNearbyEntities(Vex.class, this.vexCountTargeting, KomodoSorcerer.this, KomodoSorcerer.this.getBoundingBox().inflate(16.0)).size();
                return KomodoSorcerer.this.random.nextInt(8) + 1 > i;
            }
        }

        protected int getCastingTime() {
            return 100;
        }

        protected int getCastingInterval() {
            return 340;
        }

        @Override
        protected @Nullable SoundEvent getSpellPrepareSound() {
            return null;
        }

        protected void performSpellCasting() {
            ServerLevel serverLevel = (ServerLevel)KomodoSorcerer.this.level();

            for(int i = 0; i < 3; ++i) {
                BlockPos blockPos = KomodoSorcerer.this.blockPosition().offset(-2 + KomodoSorcerer.this.random.nextInt(5), 1, -2 + KomodoSorcerer.this.random.nextInt(5));
                Vex vex = (Vex)EntityType.VEX.create(KomodoSorcerer.this.level());
                if (vex != null) {
                    vex.moveTo(blockPos, 0.0F, 0.0F);
                    vex.finalizeSpawn(serverLevel, KomodoSorcerer.this.level().getCurrentDifficultyAt(blockPos), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
                    vex.setOwner(KomodoSorcerer.this);
                    vex.setBoundOrigin(blockPos);
                    vex.setLimitedLife(20 * (30 + KomodoSorcerer.this.random.nextInt(90)));
                    serverLevel.addFreshEntityWithPassengers(vex);
                }
            }

        }


        protected AbstractSorcerer.IllagerSpell getSpell() {
            return AbstractSorcerer.IllagerSpell.SUMMON_VEX;
        }
    }

    private class EvokerAttackSpellGoal extends AbstractSorcerer.SpellcasterUseSpellGoal {
        EvokerAttackSpellGoal() {
            super();
        }

        protected int getCastingTime() {
            return 40;
        }

        protected int getCastingInterval() {
            return 100;
        }

        protected void performSpellCasting() {
            LivingEntity livingEntity = KomodoSorcerer.this.getTarget();
            double d = Math.min(livingEntity.getY(), KomodoSorcerer.this.getY());
            double e = Math.max(livingEntity.getY(), KomodoSorcerer.this.getY()) + 1.0;
            float f = (float) Mth.atan2(livingEntity.getZ() - KomodoSorcerer.this.getZ(), livingEntity.getX() - KomodoSorcerer.this.getX());
            int i;
            if (KomodoSorcerer.this.distanceToSqr(livingEntity) < 9.0) {
                float g;
                for(i = 0; i < 5; ++i) {
                    g = f + (float)i * 3.1415927F * 0.4F;
                    this.createSpellEntity(KomodoSorcerer.this.getX() + (double)Mth.cos(g) * 1.5, KomodoSorcerer.this.getZ() + (double)Mth.sin(g) * 1.5, d, e, g, 0);
                }

                for(i = 0; i < 8; ++i) {
                    g = f + (float)i * 3.1415927F * 2.0F / 8.0F + 1.2566371F;
                    this.createSpellEntity(KomodoSorcerer.this.getX() + (double)Mth.cos(g) * 2.5, KomodoSorcerer.this.getZ() + (double)Mth.sin(g) * 2.5, d, e, g, 3);
                }
            } else {
                for(i = 0; i < 16; ++i) {
                    double h = 1.25 * (double)(i + 1);
                    int j = 1 * i;
                    this.createSpellEntity(KomodoSorcerer.this.getX() + (double)Mth.cos(f) * h, KomodoSorcerer.this.getZ() + (double)Mth.sin(f) * h, d, e, f, j);
                }
            }

        }

        private void createSpellEntity(double x, double z, double minY, double maxY, float yRot, int warmupDelay) {
            BlockPos blockPos = BlockPos.containing(x, maxY, z);
            boolean bl = false;
            double d = 0.0;

            do {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState = KomodoSorcerer.this.level().getBlockState(blockPos2);
                if (blockState.isFaceSturdy(KomodoSorcerer.this.level(), blockPos2, Direction.UP)) {
                    if (!KomodoSorcerer.this.level().isEmptyBlock(blockPos)) {
                        BlockState blockState2 = KomodoSorcerer.this.level().getBlockState(blockPos);
                        VoxelShape voxelShape = blockState2.getCollisionShape(KomodoSorcerer.this.level(), blockPos);
                        if (!voxelShape.isEmpty()) {
                            d = voxelShape.max(Direction.Axis.Y);
                        }
                    }

                    bl = true;
                    break;
                }

                blockPos = blockPos.below();
            } while(blockPos.getY() >= Mth.floor(minY) - 1);

            if (bl) {
                KomodoSorcerer.this.level().addFreshEntity(new EvokerFangs(KomodoSorcerer.this.level(), x, (double)blockPos.getY() + d, z, yRot, warmupDelay, KomodoSorcerer.this));
            }

        }

        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_ATTACK;
        }

        protected AbstractSorcerer.IllagerSpell getSpell() {
            return AbstractSorcerer.IllagerSpell.FANGS;
        }
    }
}
