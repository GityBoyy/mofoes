package org.chubby.github.mofoes.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.chubby.github.mofoes.common.entity.ai.HogmenAi;
import org.chubby.github.mofoes.common.entity.ai.memory.MemoryModules;
import org.chubby.github.mofoes.common.entity.ai.sensor.ModSensors;
import org.chubby.github.mofoes.common.init.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityHogmen extends Monster implements InventoryCarrier, CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(EntityHogmen.class,
            EntityDataSerializers.BOOLEAN);

    private static final float RANGED_WEAPON_SPAWN_CHANCE = 0.25F;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt;
    protected static final ImmutableList<SensorType<? extends Sensor<? super EntityHogmen>>> SENSOR_TYPES;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;
    public EntityHogmen(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING_CROSSBOW,false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (this.cannotHunt) {
            compound.putBoolean("CannotHunt", true);
        }

        this.writeInventoryToTag(compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.setCannotHunt(compound.getBoolean("CannotHunt"));
        this.readInventoryFromTag(compound);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.3499999940395355).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    private void setCannotHunt(boolean bl) {
        this.cannotHunt = bl;
    }

    protected Brain.@NotNull Provider<EntityHogmen> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return HogmenAi.makeBrain(this,this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<EntityHogmen> getBrain() {
        return (Brain<EntityHogmen>) super.getBrain();
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public void holdInMainHand(ItemStack itemStack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemStack);
    }

    public void holdInOffHand(ItemStack itemStack) {
        if (itemStack.is(HogmenAi.TRADE_ITEM)) {
            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, itemStack);
        }

    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean flag = super.hurt(source, amount);
        if (this.level().isClientSide) {
            return false;
        } else {
            if (flag && source.getEntity() instanceof LivingEntity) {
                HogmenAi.wasHurtBy(this, (LivingEntity)source.getEntity());
            }

            return flag;
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        RandomSource randomSource = level.getRandom();
        if (reason != MobSpawnType.STRUCTURE) {
            if (randomSource.nextFloat() < 0.2F) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }

        HogmenAi.initMemories(this, level.getRandom());
        this.populateDefaultEquipmentSlots(randomSource, difficulty);
        this.populateDefaultEquipmentEnchantments(randomSource, difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
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

    private boolean isChargingCrossbow() {
        return (Boolean)this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, chargingCrossbow);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbowStack, Projectile projectile, float projectileAngle) {
        if(HogmenAi.hasCrossbow(this))
        {
            this.shootCrossbowProjectile(this,target,projectile,projectileAngle,1);
        }

    }

    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public ItemStack addToInventory(ItemStack itemStack)
    {
        return this.inventory.addItem(itemStack);
    }

    public boolean canHunt() {
        return !this.cannotHunt;
    }

    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult interactionResult = super.mobInteract(player, hand);
        if (interactionResult.consumesAction()) {
            return interactionResult;
        } else if (!this.level().isClientSide) {
            return HogmenAi.mobInteract(this, player, hand);
        } else {
            boolean bl = HogmenAi.canAdmire(this, player.getItemInHand(hand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
            return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    public PiglinArmPose getArmPose() {
        if (HogmenAi.isLovedItem(this.getOffhandItem())) {
            return PiglinArmPose.ADMIRING_ITEM;
        } else if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        } else if (this.isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        } else {
            return this.isAggressive() && this.isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
        }
    }

    protected boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().getItem() instanceof TieredItem;
    }

    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (this.isAdult()) {
            //this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET), random);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE), random);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS), random);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS), random);
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

    protected void customServerAiStep() {
        this.level().getProfiler().push("hogmenBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        HogmenAi.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public SimpleContainer getInventory() {
        return inventory;
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        this.onItemPickup(itemEntity);
        HogmenAi.pickUpItem(this, itemEntity);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && HogmenAi.wantsToPickup(this, stack);
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        if (this.isBaby() && vehicle.getType() == ModEntities.BISON.get()) {
            vehicle = this.getTopPassenger(vehicle, 3);
        }

        return super.startRiding(vehicle, force);
    }

    private Entity getTopPassenger(Entity vehicle, int maxPosition) {
        List<Entity> list = vehicle.getPassengers();
        return maxPosition != 1 && !list.isEmpty() ? this.getTopPassenger((Entity)list.get(0), maxPosition - 1) : vehicle;
    }

    static {
        SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS,
                SensorType.NEAREST_ITEMS, SensorType.HURT_BY, ModSensors.HOGMEN_SENSOR.get());

        MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.DOORS_TO_CLOSE,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
                MemoryModules.NEAREST_ADULT_HOGMEN.get(),
                MemoryModules.NEARBY_HOGMEN.get(),
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER,
                MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM,
                MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION,
                MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
                MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET,
                MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
                MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
                MemoryModules.NEAREST_PLAYER_NOT_WEARING_CHAIN.get(),
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
                MemoryModuleType.ATE_RECENTLY);
    }


    public boolean canReplaceCurrentItem(ItemStack candidate) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(candidate);
        ItemStack itemStack = this.getItemBySlot(equipmentSlot);
        return this.canReplaceCurrentItem(candidate, itemStack);
    }

    protected boolean canReplaceCurrentItem(ItemStack candidate, ItemStack existing) {
        if (EnchantmentHelper.hasBindingCurse(existing)) {
            return false;
        } else {
            boolean bl = HogmenAi.isLovedItem(candidate) || candidate.is(Items.CROSSBOW);
            boolean bl2 = HogmenAi.isLovedItem(existing) || existing.is(Items.CROSSBOW);
            if (bl && !bl2) {
                return true;
            } else if (!bl && bl2) {
                return false;
            } else {
                return (!this.isAdult() || candidate.is(Items.CROSSBOW) || !existing.is(Items.CROSSBOW)) && super.canReplaceCurrentItem(candidate, existing);
            }
        }
    }

    public static boolean checkHogmanSpawnRule(EntityType<EntityHogmen> type,
                                               ServerLevelAccessor level, MobSpawnType spawnType,
                                               BlockPos pos, RandomSource random)

    {
        return level.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(type, level, spawnType, pos, random)
                &&level.getBlockState(pos)!= Blocks.OAK_LOG.defaultBlockState()
                &&level.getBlockState(pos)!= Blocks.AIR.defaultBlockState();
    }
}
