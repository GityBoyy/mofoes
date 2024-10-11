package org.chubby.github.mofoes.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StalkerEnderman extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(StalkerEnderman.class,
            EntityDataSerializers.BOOLEAN);
    public static final int MIN_BLOCK_SEARCH_DISTANCE = 100;
    public static final int MAX_BLOCK_SEARCH_DISTANCE = 250;
    private int teleportCooldown = 0;  // To prevent rapid teleporting
    private int targetChangeTime;

    public StalkerEnderman(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CREEPY, false);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null) {
            this.targetChangeTime = this.tickCount;
            this.setCreepy(false);  // Reset creepy state when acquiring a new target
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player && !((Player) target).isCreative()) {
            attackPlayerInRange();
            return true;
        }
        return super.canAttack(target);
    }

    /**
     * Check if the player is within a certain range and teleport towards them.
     */
    protected void attackPlayerInRange() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double distanceToTarget = this.distanceTo(target);
            if (distanceToTarget > MIN_BLOCK_SEARCH_DISTANCE && distanceToTarget < MAX_BLOCK_SEARCH_DISTANCE) {
                this.setCreepy(true);  // Mark as creepy when within range but not too close
                if (this.teleportCooldown == 0) {
                    this.teleportTowards(target);  // Teleport closer if within range
                    this.teleportCooldown = 100;  // 5 seconds cooldown to prevent rapid teleporting
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // Cooldown management
        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }

        // Daylight check and teleport away if necessary
        if (this.level().isDay() && this.tickCount >= this.targetChangeTime + 600) {
            float lightLevel = this.getLightLevelDependentMagicValue();
            if (lightLevel > 0.5F && this.level().canSeeSky(this.blockPosition())) {
                this.setTarget(null);
                this.teleport();
            }
        }

        // Call the custom attack logic
        attackPlayerInRange();
    }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
            double e = this.getY() + (this.random.nextInt(64) - 32);
            double f = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
            return this.teleport(d, e, f);
        }
        return false;
    }

    boolean teleportTowards(Entity target) {
        Vec3 directionVec = new Vec3(this.getX() - target.getX(), this.getY(0.5) - target.getEyeY(), this.getZ() - target.getZ()).normalize();
        double d = 16.0;
        double teleportX = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - directionVec.x * d;
        double teleportY = this.getY() + (this.random.nextInt(16) - 8) - directionVec.y * d;
        double teleportZ = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - directionVec.z * d;
        return this.teleport(teleportX, teleportY, teleportZ);
    }

    private boolean teleport(double x, double y, double z) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x, y, z);
        while (blockPos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(blockPos).blocksMotion()) {
            blockPos.move(Direction.DOWN);
        }

        BlockState blockState = this.level().getBlockState(blockPos);
        boolean canTeleport = blockState.blocksMotion() && !blockState.getFluidState().is(FluidTags.WATER);

        if (canTeleport) {
            Vec3 oldPosition = this.position();
            boolean success = this.randomTeleport(x, y, z, true);
            if (success) {
                this.level().gameEvent(GameEvent.TELEPORT, oldPosition, GameEvent.Context.of(this));
                this.playTeleportSound();
            }
            return success;
        }
        return false;
    }

    private void playTeleportSound() {
        if (!this.isSilent()) {
            this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public boolean isCreepy() {
        return this.entityData.get(DATA_CREEPY);
    }

    public void setCreepy(boolean creepy) {
        this.entityData.set(DATA_CREEPY, creepy);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }
}
