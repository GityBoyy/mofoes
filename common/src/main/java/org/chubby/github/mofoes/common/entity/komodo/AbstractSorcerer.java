package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.IntFunction;

public abstract class AbstractSorcerer extends AbstractKomodo
{
    private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID;
    protected int spellCastingTickCount;
    private AbstractSorcerer.IllagerSpell currentSpell;

    protected AbstractSorcerer(EntityType<? extends AbstractSorcerer> entityType, Level level) {
        super(entityType, level);
        this.currentSpell = AbstractSorcerer.IllagerSpell.NONE;
    }



    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL_CASTING_ID, (byte)0);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.spellCastingTickCount = compound.getInt("SpellTicks");
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SpellTicks", this.spellCastingTickCount);
    }

    public AbstractKomodo.KomodoArmPoses getArmPose() {
        if (this.isCastingSpell()) {
            return KomodoArmPoses.SPELLCASTING;
        } else {
            return KomodoArmPoses.CROSSED;
        }
    }

    public boolean isCastingSpell() {
        if (this.level().isClientSide) {
            return (Byte)this.entityData.get(DATA_SPELL_CASTING_ID) > 0;
        } else {
            return this.spellCastingTickCount > 0;
        }
    }

    public void setIsCastingSpell(AbstractSorcerer.IllagerSpell currentSpell) {
        this.currentSpell = currentSpell;
        this.entityData.set(DATA_SPELL_CASTING_ID, (byte)currentSpell.id);
    }

    protected AbstractSorcerer.IllagerSpell getCurrentSpell() {
        return !this.level().isClientSide ? this.currentSpell : AbstractSorcerer.IllagerSpell.byId((Byte)this.entityData.get(DATA_SPELL_CASTING_ID));
    }

    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }

    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.isCastingSpell()) {
            AbstractSorcerer.IllagerSpell illagerSpell = this.getCurrentSpell();
            double d = illagerSpell.spellColor[0];
            double e = illagerSpell.spellColor[1];
            double f = illagerSpell.spellColor[2];
            float g = this.yBodyRot * 0.017453292F + Mth.cos((float)this.tickCount * 0.6662F) * 0.25F;
            float h = Mth.cos(g);
            float i = Mth.sin(g);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double)h * 0.6, this.getY() + 1.8, this.getZ() + (double)i * 0.6, d, e, f);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double)h * 0.6, this.getY() + 1.8, this.getZ() - (double)i * 0.6, d, e, f);
        }

    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    protected abstract SoundEvent getCastingSoundEvent();

    static {
        DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(AbstractSorcerer.class, EntityDataSerializers.BYTE);
    }

    protected static enum IllagerSpell {
        NONE(0, 0.0, 0.0, 0.0),
        SUMMON_VEX(1, 0.7, 0.7, 0.8),
        FANGS(2, 0.4, 0.3, 0.35),
        WOLOLO(3, 0.7, 0.5, 0.2),
        DISAPPEAR(4, 0.3, 0.3, 0.8),
        BLINDNESS(5, 0.1, 0.1, 0.2);

        private static final IntFunction<AbstractSorcerer.IllagerSpell> BY_ID = ByIdMap.continuous((arg) -> {
            return arg.id;
        }, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        final int id;
        final double[] spellColor;

        private IllagerSpell(int id, double red, double green, double blue) {
            this.id = id;
            this.spellColor = new double[]{red, green, blue};
        }

        public static AbstractSorcerer.IllagerSpell byId(int id) {
            return (AbstractSorcerer.IllagerSpell)BY_ID.apply(id);
        }
    }

    protected abstract class SpellcasterUseSpellGoal extends Goal {
        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        protected SpellcasterUseSpellGoal() {
        }

        public boolean canUse() {
            LivingEntity livingEntity = AbstractSorcerer.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive()) {
                if (AbstractSorcerer.this.isCastingSpell()) {
                    return false;
                } else {
                    return AbstractSorcerer.this.tickCount >= this.nextAttackTickCount;
                }
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingEntity = AbstractSorcerer.this.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.attackWarmupDelay > 0;
        }

        public void start() {
            this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
            AbstractSorcerer.this.spellCastingTickCount = this.getCastingTime();
            this.nextAttackTickCount = AbstractSorcerer.this.tickCount + this.getCastingInterval();
            SoundEvent soundEvent = this.getSpellPrepareSound();
            if (soundEvent != null) {
                AbstractSorcerer.this.playSound(soundEvent, 1.0F, 1.0F);
            }

            AbstractSorcerer.this.setIsCastingSpell(this.getSpell());
        }

        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
                AbstractSorcerer.this.playSound(AbstractSorcerer.this.getCastingSoundEvent(), 1.0F, 1.0F);
            }

        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract AbstractSorcerer.IllagerSpell getSpell();
    }

    protected class SpellcasterCastingSpellGoal extends Goal {
        public SpellcasterCastingSpellGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return AbstractSorcerer.this.getSpellCastingTime() > 0;
        }

        public void start() {
            super.start();
            AbstractSorcerer.this.navigation.stop();
        }

        public void stop() {
            super.stop();
            AbstractSorcerer.this.setIsCastingSpell(AbstractSorcerer.IllagerSpell.NONE);
        }

        public void tick() {
            if (AbstractSorcerer.this.getTarget() != null) {
                AbstractSorcerer.this.getLookControl().setLookAt(AbstractSorcerer.this.getTarget(), (float)AbstractSorcerer.this.getMaxHeadYRot(), (float)AbstractSorcerer.this.getMaxHeadXRot());
            }

        }
    }
}
