package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BasicKomodo extends AbstractKomodo implements CrossbowAttackMob
{
    public static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(BasicKomodo.class,
            EntityDataSerializers.BOOLEAN);
    private static final float CROSSBOW_POWER = 1.6F;

    public BasicKomodo(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this,1.3F,true));

        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[]{Raider.class})).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        return projectileWeapon == Items.CROSSBOW;
    }

    public boolean isChargingCrossbow() {
        return (Boolean)this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.entityData.set(IS_CHARGING_CROSSBOW, chargingCrossbow);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbowStack, Projectile projectile, float projectileAngle) {
        this.shootCrossbowProjectile(this,target,projectile,projectileAngle,CROSSBOW_POWER);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag)
    {
        RandomSource randomSource = this.level().getRandom();
        this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
        this.populateDefaultEquipmentSlots(randomSource, difficulty);
        this.populateDefaultEquipmentEnchantments(randomSource, difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (!this.isBaby()) {
            //this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET), random);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE), random);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS), random);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS), random);
        }

    }

    private void maybeWearArmor(EquipmentSlot slot, ItemStack stack, RandomSource random) {
        if (random.nextFloat() < 0.2F) {
            this.setItemSlot(slot, stack);
        }

    }
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.performCrossbowAttack(this, 1.6F);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    private ItemStack createSpawnWeapon() {
        ItemStack[] possibleWeapons = new ItemStack[]{
                new ItemStack(Items.STONE_SWORD),
                new ItemStack(Items.WOODEN_SWORD),
                new ItemStack(Items.STONE_AXE),
                new ItemStack(Items.WOODEN_AXE)
        };
        return (double)this.random.nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : possibleWeapons[this.random.nextInt(possibleWeapons.length)];
    }

    protected boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().getItem() instanceof TieredItem;
    }


    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    public AbstractKomodo.KomodoArmPoses getArmPose() {
        if (this.isChargingCrossbow()) {
            return AbstractKomodo.KomodoArmPoses.CROSSBOW_CHARGE;
        } else if (this.isHolding(Items.CROSSBOW)) {
            return AbstractKomodo.KomodoArmPoses.CROSSBOW_HOLD;
        }
        else if(this.isHoldingMeleeWeapon())
        {
            return KomodoArmPoses.MELEE_POSE;
        }
        else {
            return this.isAggressive() ? AbstractKomodo.KomodoArmPoses.ATTACKING : AbstractKomodo.KomodoArmPoses.NEUTRAL;
        }
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setCanPickUpLoot(true);
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    public int getMaxSpawnClusterSize() {
        return 1;
    }
}
