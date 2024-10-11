package org.chubby.github.mofoes.common.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.HogmanBrute;
import org.chubby.github.mofoes.common.init.ModEntities;

import java.util.Optional;

public class HogmanBruteAi 
{
    private static final int ANGER_DURATION = 600;
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final double ACTIVITY_SOUND_LIKELIHOOD_PER_TICK = 0.0125;
    private static final int MAX_LOOK_DIST = 8;
    private static final int INTERACTION_RANGE = 8;
    private static final double TARGETING_RANGE = 12.0;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;
    private static final int HOME_CLOSE_ENOUGH_DISTANCE = 2;
    private static final int HOME_TOO_FAR_DISTANCE = 100;
    private static final int HOME_STROLL_AROUND_DISTANCE = 5;

    public HogmanBruteAi() {
    }

    public static Brain<?> makeBrain(HogmanBrute piglinBrute, Brain<HogmanBrute> brain) {
        initCoreActivity(piglinBrute, brain);
        initIdleActivity(piglinBrute, brain);
        initFightActivity(piglinBrute, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void initMemories(HogmanBrute piglinBrute) {
        GlobalPos globalPos = GlobalPos.of(piglinBrute.level().dimension(), piglinBrute.blockPosition());
        piglinBrute.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
    }

    private static void initCoreActivity(HogmanBrute piglinBrute, Brain<HogmanBrute> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), StopBeingAngryIfTargetDead.create()));
    }

    private static void initIdleActivity(HogmanBrute piglinBrute, Brain<HogmanBrute> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(StartAttacking.create(HogmanBruteAi::findNearestValidAttackTarget),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(HogmanBrute piglinBrute, Brain<HogmanBrute> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(StopAttackingIfTargetInvalid.create((arg2) -> {
            return !isNearestValidAttackTarget(piglinBrute, arg2);
        }), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), MeleeAttack.create(20)), MemoryModuleType.ATTACK_TARGET);
    }

    private static RunOne<HogmanBrute> createIdleLookBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN_BRUTE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1), Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<HogmanBrute> createIdleMovementBehaviors() {
        return new RunOne(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(ModEntities.HOGMEN.get()
                , 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                Pair.of(InteractWith.of(ModEntities.HOGMEN_BRUTE.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    public static void updateActivity(HogmanBrute piglinBrute) {
        Brain<HogmanBrute> brain = piglinBrute.getBrain();
        Activity activity = (Activity)brain.getActiveNonCoreActivity().orElse((Activity)null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        Activity activity2 = (Activity)brain.getActiveNonCoreActivity().orElse((Activity)null);
        if (activity != activity2) {
            playActivitySound(piglinBrute);
        }

        piglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    private static boolean isNearestValidAttackTarget(HogmanBrute piglinBrute, LivingEntity target) {
        return findNearestValidAttackTarget(piglinBrute).filter((arg2) -> {
            return arg2 == target;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(HogmanBrute piglinBrute) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglinBrute, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglinBrute, (LivingEntity)optional.get())) {
            return optional;
        } else {
            Optional<? extends LivingEntity> optional2 = getTargetIfWithinRange(piglinBrute, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
            return optional2.isPresent() ? optional2 : piglinBrute.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        }
    }

    private static Optional<? extends LivingEntity> getTargetIfWithinRange(HogmanBrute piglinBrute, MemoryModuleType<? extends LivingEntity> memoryType) {
        return piglinBrute.getBrain().getMemory(memoryType).filter((arg2) -> {
            return arg2.closerThan(piglinBrute, 12.0);
        });
    }

    protected static void wasHurtBy(HogmanBrute piglinBrute, LivingEntity target) {
        if (!(target instanceof EntityHogmen)) {
            //HogmenAi.maybeRetaliate(piglinBrute, target);
        }
    }

    protected static void setAngerTarget(HogmanBrute piglinBrute, LivingEntity angerTarget) {
        piglinBrute.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        piglinBrute.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, angerTarget.getUUID(), 600L);
    }

    protected static void maybePlayActivitySound(HogmanBrute piglinBrute) {
        if ((double)piglinBrute.level().random.nextFloat() < 0.0125) {
            playActivitySound(piglinBrute);
        }

    }

    private static void playActivitySound(HogmanBrute piglinBrute) {
        piglinBrute.getBrain().getActiveNonCoreActivity().ifPresent((arg2) -> {
            if (arg2 == Activity.FIGHT) {
               // piglinBrute.playAngrySound();
            }

        });
    }
}
