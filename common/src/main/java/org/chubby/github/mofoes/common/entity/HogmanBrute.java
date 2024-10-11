package org.chubby.github.mofoes.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.chubby.github.mofoes.common.entity.ai.HogmanBruteAi;
import org.chubby.github.mofoes.common.entity.ai.memory.MemoryModules;
import org.chubby.github.mofoes.common.entity.ai.sensor.ModSensors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HogmanBrute extends Monster
{
    protected static final ImmutableList<SensorType<? extends Sensor<? super HogmanBrute>>> SENSOR_TYPES;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;
    public HogmanBrute(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Brain.Provider<HogmanBrute> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        return HogmanBruteAi.makeBrain(this,this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public @NotNull Brain<HogmanBrute> getBrain() {
        return (Brain<HogmanBrute>) super.getBrain();
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        HogmanBruteAi.initMemories(this);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.3499999940395355).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    protected void customServerAiStep() {
        this.level().getProfiler().push("hogmanBruteBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        HogmanBruteAi.updateActivity(this);
        super.customServerAiStep();
    }
    public PiglinArmPose getArmPose() {
        return this.isAggressive() && this.isHoldingMeleeWeapon() ? PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON : PiglinArmPose.DEFAULT;
    }

    private boolean isHoldingMeleeWeapon()
    {
        return this.getMainHandItem().getItem() instanceof TieredItem;
    }

    static {
        SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS,
                 SensorType.HURT_BY, ModSensors.HOGMEN_BRUTE_SENSOR.get());

        MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.DOORS_TO_CLOSE,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
                MemoryModules.NEAREST_ADULT_HOGMEN.get(),
                MemoryModules.NEARBY_HOGMEN.get(),
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER,
                MemoryModuleType.AVOID_TARGET
               , MemoryModuleType.CELEBRATE_LOCATION,
                MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
                MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET,
                MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
                MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
                MemoryModules.NEAREST_PLAYER_NOT_WEARING_CHAIN.get(),
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
                MemoryModuleType.ATE_RECENTLY);
    }
    public static boolean checkHogmanSpawnRule(EntityType<HogmanBrute> type,
                                               ServerLevelAccessor level, MobSpawnType spawnType,
                                               BlockPos pos, RandomSource random)

    {
        return level.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(type, level, spawnType, pos, random);
    }
}
