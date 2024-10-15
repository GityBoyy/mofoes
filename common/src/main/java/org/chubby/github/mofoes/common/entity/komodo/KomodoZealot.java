package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mofoes.common.init.ModEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KomodoZealot extends AbstractSorcerer implements CrossbowAttackMob {
    public static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(KomodoZealot.class,
            EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.literal("💀 Komodo Zealot 💀"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_12);
    private static final int NUM_ILLUSIONS = 4;
    private static final int ILLUSION_TRANSITION_TICKS = 3;
    private static final int ILLUSION_SPREAD = 3;
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;

    public KomodoZealot(EntityType<? extends AbstractSorcerer> entityType, Level level) {
        super(entityType, level);
        this.clientSideIllusionOffsets = new Vec3[2][4];

        for(int i = 0; i < 4; ++i) {
            this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this,1.3D,false));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        this.goalSelector.addGoal(4, new KomodoZealot.IllusionerMirrorSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[]{Raider.class})).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.666666686D)
                .add(Attributes.MAX_HEALTH, 500D)
                .add(Attributes.ARMOR_TOUGHNESS, 5D)
                .add(Attributes.ARMOR, 2.35445673D)
                .add(Attributes.FOLLOW_RANGE, 128D)
                .add(Attributes.ATTACK_DAMAGE,10.0D)
                .add(Attributes.ATTACK_KNOCKBACK,2.4D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 32D);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        RandomSource randomSource = level.getRandom();
        this.setItemInHand(InteractionHand.MAIN_HAND,getDefaultStack());
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public static boolean isTargetValid(LivingEntity entity) {
        return !(entity instanceof KomodoZealot); // Do not target other KomodoZealots
    }

    public static boolean isAdult(Mob mob) {
        return !mob.isBaby();
    }

    public boolean hasCrossbow() {
        return this.getMainHandItem().getItem() instanceof CrossbowItem;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public boolean isChargingCrossbow() {
        return (Boolean) this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.entityData.set(IS_CHARGING_CROSSBOW, chargingCrossbow);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbowStack, Projectile projectile, float projectileAngle) {
        this.shootCrossbowProjectile(this,target,projectile,projectileAngle,1.6F);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distanceToTarget = this.distanceTo(target);

            // Teleport away if target is too close
            if (distanceToTarget < 5.0D && this.random.nextFloat() < 0.3F) {
                this.teleportAwayFrom(target);
            }

            // Switch weapon and attack based on distance
            if (distanceToTarget > 10.0D) {
                // Switch to crossbow
                this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
                this.performRangedAttack(target, 1.0F);
                if(this.random.nextInt() > 0.02F)
                {
                    this.throwPotionAtTarget(target);
                }
            } else {
                // Switch to melee
                this.setItemInHand(InteractionHand.MAIN_HAND, getDefaultStack());
                this.performMeleeAttack(target);
            }

            // As health decreases, increase teleportation frequency
            if (this.getHealth() < this.getMaxHealth() / 2) {
                if (this.random.nextFloat() < 0.04F) {
                    this.throwPotionAtTarget(this.getTarget());
                }
                if (this.random.nextFloat() < 0.5F) {
                    this.teleportCloseTo(this.getTarget());
                }
            }
        }
    }

    private void performMeleeAttack(LivingEntity target)
    {
        this.goalSelector.addGoal(3,new MeleeAttackGoal(this,1.2D,false));
    }

    private void teleportAwayFrom(LivingEntity target) {
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        Vec3 teleportPos = new Vec3(this.getX() + dx * 2, this.getY(), this.getZ() + dz * 2);
        this.doTeleport(teleportPos);
    }

    private void teleportCloseTo(LivingEntity target) {
        Vec3 teleportPos = target.position().add(this.random.nextDouble() * 3 - 1.5, 0, this.random.nextDouble() * 3 - 1.5);
        this.doTeleport(teleportPos);
    }

    private void doTeleport(Vec3 pos) {
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        this.teleportTo(pos.x, pos.y, pos.z);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
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
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(this.getTarget() == null) return;

//        if(this.distanceTo(this.getTarget()) > )
//        {
//
//        }
        if(!this.level().isClientSide())
        {
            this.changeWeaponType();
        }

        if (this.level().isClientSide && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }

            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    this.clientSideIllusionTicks = 3;

                    for(int k = 0; k < 4; ++k) {
                        this.clientSideIllusionOffsets[0][k] = this.clientSideIllusionOffsets[1][k];
                        this.clientSideIllusionOffsets[1][k] = new Vec3(0.0, 0.0, 0.0);
                    }
                }
            } else {
                this.clientSideIllusionTicks = 3;
                float f = -6.0F;

                int j;
                for(j = 0; j < 4; ++j) {
                    this.clientSideIllusionOffsets[0][j] = this.clientSideIllusionOffsets[1][j];
                    this.clientSideIllusionOffsets[1][j] = new Vec3((double)(-6.0F + (float)this.random.nextInt(13)) * 0.5, (double)Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5);
                }

                for(j = 0; j < 16; ++j) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
                }

                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            }
        }
    }

    public Vec3[] getIllusionOffsets(float partialTick) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        } else {
            double d = ((float)this.clientSideIllusionTicks - partialTick) / 3.0F;
            d = Math.pow(d, 0.25);
            Vec3[] vec3s = new Vec3[4];

            for(int i = 0; i < 4; ++i) {
                vec3s[i] = this.clientSideIllusionOffsets[1][i].scale(1.0 - d).add(this.clientSideIllusionOffsets[0][i].scale(d));
            }

            return vec3s;
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.performCrossbowAttack(this,1.6F);
    }

    private void throwPotionAtTarget(LivingEntity target) {
        Vec3 vec3 = target.getDeltaMovement();
        double d = target.getX() + vec3.x - this.getX();
        double e = target.getEyeY() - 1.1 - this.getY();
        double f = target.getZ() + vec3.z - this.getZ();
        double g = Math.sqrt(d * d + f * f);

        Potion potion = determinePotionForTarget(target, g);

        ThrownPotion thrownPotion = new ThrownPotion(this.level(), this);
        thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        thrownPotion.shoot(d, e + g * 0.2, f, 0.75F, 8.0F);

        this.level().addFreshEntity(thrownPotion);
    }

    private Potion determinePotionForTarget(LivingEntity target, double distance) {
        if (distance >= 8.0 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            return Potions.SLOWNESS;
        } else if (target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) {
            return Potions.POISON;
        } else if (distance <= 3.0 && this.random.nextFloat() < 0.25F) {
            return Potions.WEAKNESS;
        }

        return Potions.HARMING;
    }

    public AbstractKomodo.KomodoArmPoses getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractKomodo.KomodoArmPoses.SPELLCASTING;
        }
        else if (this.isChargingCrossbow()) {
            return AbstractKomodo.KomodoArmPoses.CROSSBOW_CHARGE;
        } else if (this.isHolding(Items.CROSSBOW)) {
            return AbstractKomodo.KomodoArmPoses.CROSSBOW_HOLD;
        } else if (this.isMelee()) {
            return KomodoArmPoses.MELEE_POSE;
        } else {
            return this.isAggressive() ? AbstractKomodo.KomodoArmPoses.ATTACKING : AbstractKomodo.KomodoArmPoses.NEUTRAL;
        }
    }

    public boolean isMelee() {
        return this.getMainHandItem().getItem() instanceof TieredItem;
    }

    public void changeWeaponType()
    {
        if(this.random.nextFloat() > 0.4F)
        {
            this.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.CROSSBOW));
        }
        else{
            this.setItemInHand(InteractionHand.MAIN_HAND, getDefaultStack());
        }
    }

    private ItemStack getDefaultStack()
    {
        return Items.GOLDEN_SWORD.getDefaultInstance();
    }

    private class IllusionerMirrorSpellGoal extends AbstractSorcerer.SpellcasterUseSpellGoal {
        IllusionerMirrorSpellGoal() {
            super();
        }

        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else {
                return !KomodoZealot.this.hasEffect(MobEffects.INVISIBILITY);
            }
        }

        protected int getCastingTime() {
            return 20;
        }

        protected int getCastingInterval() {
            return 340;
        }

        protected void performSpellCasting() {
            KomodoZealot.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
        }

        @Nullable
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        protected AbstractSorcerer.IllagerSpell getSpell() {
            return AbstractSorcerer.IllagerSpell.DISAPPEAR;
        }
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}
