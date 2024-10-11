package org.chubby.github.mofoes.common.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.ai.behaviour.StartAdmiringItemIfSeen;
import org.chubby.github.mofoes.common.entity.ai.behaviour.StopHoldingItemIfNoLongerAdmiring;
import org.chubby.github.mofoes.common.entity.ai.memory.MemoryModules;
import org.chubby.github.mofoes.common.init.ModEntities;
import org.chubby.github.mofoes.tags.ModTags;

import java.util.*;

public class HogmenAi
{
    public static final Item TRADE_ITEM;
    public static final int PLAYER_ANGER_RADIUS = 12;
    private static final int ANGER_DURATION = 600;
    private static final int ADMIRE_DURATION = 120;
    private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
    private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
    private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
    private static final int CELEBRATION_TIME = 300;
    protected static final UniformInt TIME_BETWEEN_HUNTS;
    private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
    private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
    private static final UniformInt RIDE_START_INTERVAL;
    private static final UniformInt RIDE_DURATION;
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final int EAT_COOLDOWN = 200;
    private static final int MAX_LOOK_DIST = 8;
    private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
    private static final int INTERACTION_RANGE = 8;
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

    public static Brain<?> makeBrain(EntityHogmen piglin, Brain<EntityHogmen> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initAdmireItemActivity(brain);
        initFightActivity(piglin, brain);
        initRetreatActivity(brain);
        initRideHoglinActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void initMemories(EntityHogmen hogmen, RandomSource source) {
        int i = TIME_BETWEEN_HUNTS.sample(source);
        hogmen.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, false, (long) i);
    }

    // Core behaviors like looking around and moving
    private static void initCoreActivity(Brain<EntityHogmen> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                StopHoldingItemIfNoLongerAdmiring.create(),
                StartAdmiringItemIfSeen.create(ADMIRE_DURATION)
        ));
    }

    // Idle behaviors
    private static void initIdleActivity(Brain<EntityHogmen> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                SetEntityLookTarget.create(HogmenAi::isPlayerHoldingLovedItem, 14.0F),
                StartAttacking.create(EntityHogmen::isAdult, HogmenAi::findNearestValidAttackTarget),
                babySometimesRideBabyHoglin(),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                SetLookAndInteract.create(EntityType.PLAYER, 4)
        ));
    }

    // Fight behaviors for combat scenarios
    private static void initFightActivity(EntityHogmen piglin, Brain<EntityHogmen> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10,ImmutableList.of(StopAttackingIfTargetInvalid.create((p_34981_) -> {
            return !isNearestValidAttackTarget(piglin, p_34981_);
        }), BehaviorBuilder.triggerIf(HogmenAi::hasCrossbow,
                BackUpIfTooClose.create(5, 0.75F)),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                MeleeAttack.create(20)), (MemoryModuleType<?>) MemoryModuleType.ATTACK_TARGET);
    }

    // Admire item behavior
    private static void initAdmireItemActivity(Brain<EntityHogmen> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(
                GoToWantedItem.create(HogmenAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9),
                StopAdmiringIfItemTooFarAway.create(9),
                StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)
        ), MemoryModuleType.ADMIRING_ITEM);
    }

    // Retreat when the entity wants to avoid threats
    private static void initRetreatActivity(Brain<EntityHogmen> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true),
                createIdleLookBehaviors(),
                createIdleMovementBehaviors(),
                EraseMemoryIf.create(HogmenAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
        ), MemoryModuleType.AVOID_TARGET);
    }

    // Ride behavior for riding entities like Hoglins
    private static void initRideHoglinActivity(Brain<EntityHogmen> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(
                Mount.create(0.8F),
                SetEntityLookTarget.create(HogmenAi::isPlayerHoldingLovedItem, 8.0F),
                BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(
                        ImmutableList.of(
                                Pair.of(BehaviorBuilder.triggerIf((hogmen) -> true), 1)
                        ))),
                DismountOrSkipMounting.create(8, HogmenAi::wantsToStopRiding)
        ), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(ModEntities.HOGMEN.get(), 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1));
    }

    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne(ImmutableList.builder().addAll(createLookBehaviors()).add(Pair.of(new DoNothing(30, 60), 1)).build());
    }

    private static RunOne<EntityHogmen> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(ModEntities.HOGMEN.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(HogmenAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
    }

    public static void pickUpItem(EntityHogmen piglin, ItemEntity itemEntity) {
        stopWalking(piglin);
        ItemStack itemStack;
        if (itemEntity.getItem().is(Items.GOLD_NUGGET)) {
            piglin.take(itemEntity, itemEntity.getItem().getCount());
            itemStack = itemEntity.getItem();
            itemEntity.discard();
        } else {
            piglin.take(itemEntity, 1);
            itemStack = removeOneItemFromItemEntity(itemEntity);
        }

        if (isLovedItem(itemStack)) {
            piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(piglin, itemStack);
            admireGoldItem(piglin);
        } else if (isFood(itemStack) && !hasEatenRecently(piglin)) {
            eat(piglin);
        } else {
            boolean bl = !piglin.equipItemIfPossible(itemStack).equals(ItemStack.EMPTY);
            if (!bl) {
                putInInventory(piglin, itemStack);
            }
        }
    }

    private static void holdInOffhand(EntityHogmen piglin, ItemStack itemStack) {
        if (isHoldingItemInOffHand(piglin)) {
            piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));
        }

        piglin.holdInOffHand(itemStack);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = itemStack.split(1);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack);
        }

        return itemStack2;
    }

    public static void stopHoldingOffHandItem(EntityHogmen piglin, boolean bl) {
        ItemStack itemStack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        boolean bl2;
        if (piglin.isAdult()) {
            bl2 = isBarterCurrency(itemStack);
            if (bl && bl2) {
                throwItems(piglin, getBarterResponseItems(piglin));
            } else if (!bl2) {
                boolean bl3 = !piglin.equipItemIfPossible(itemStack).isEmpty();
                if (!bl3) {
                    putInInventory(piglin, itemStack);
                }
            }
        } else {
            bl2 = !piglin.equipItemIfPossible(itemStack).isEmpty();
            if (!bl2) {
                ItemStack itemStack2 = piglin.getMainHandItem();
                if (isLovedItem(itemStack2)) {
                    putInInventory(piglin, itemStack2);
                } else {
                    throwItems(piglin, Collections.singletonList(itemStack2));
                }

                piglin.holdInMainHand(itemStack);
            }
        }

    }

    protected static void cancelAdmiring(EntityHogmen piglin) {
        if (isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
            piglin.spawnAtLocation(piglin.getOffhandItem());
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }

    }

    private static void putInInventory(EntityHogmen piglin, ItemStack itemStack) {
        ItemStack itemStack2 = piglin.addToInventory(itemStack);
        throwItemsTowardRandomPos(piglin, Collections.singletonList(itemStack2));
    }

    private static void throwItems(EntityHogmen piglin, List<ItemStack> list) {
        Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            throwItemsTowardPlayer(piglin, (Player)optional.get(), list);
        } else {
            throwItemsTowardRandomPos(piglin, list);
        }

    }

    public static void updateActivity(EntityHogmen piglin) {
        Brain<EntityHogmen> brain = piglin.getBrain();
        Activity activity = (Activity)brain.getActiveNonCoreActivity().orElse((Activity) null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity activity2 = (Activity)brain.getActiveNonCoreActivity().orElse((Activity) null);
        if (activity != activity2) {
            Optional var10000 = getSoundForCurrentActivity(piglin);
            Objects.requireNonNull(piglin);
            //var10000.ifPresent(piglin::playSoundEvent);
        }

        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET)) {
            piglin.stopRiding();
        }

        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        }

    }

    private static void throwItemsTowardRandomPos(EntityHogmen piglin, List<ItemStack> list) {
        throwItemsTowardPos(piglin, list, getRandomNearbyPos(piglin));
    }

    private static void throwItemsTowardPlayer(EntityHogmen piglin, Player player, List<ItemStack> list) {
        throwItemsTowardPos(piglin, list, player.position());
    }

    private static void throwItemsTowardPos(EntityHogmen piglin, List<ItemStack> list, Vec3 vec3) {
        if (!list.isEmpty()) {
            piglin.swing(InteractionHand.OFF_HAND);
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                ItemStack itemStack = (ItemStack)var3.next();
                BehaviorUtils.throwItem(piglin, itemStack, vec3.add(0.0, 1.0, 0.0));
            }
        }

    }

    private static List<ItemStack> getBarterResponseItems(EntityHogmen piglin) {
        LootTable lootTable = piglin.level().getServer().getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> list = lootTable.getRandomItems((new LootParams.Builder((ServerLevel)piglin.level())).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }

    private static boolean wantsToDance(LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity2.getType() != EntityType.HOGLIN) {
            return false;
        } else {
            return RandomSource.create(livingEntity.level().getGameTime()).nextFloat() < 0.1F;
        }
    }

    public static boolean wantsToPickup(EntityHogmen piglin, ItemStack itemStack) {
        if (piglin.isBaby() && itemStack.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        } else if (itemStack.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        } else if (isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (isBarterCurrency(itemStack)) {
            return isNotHoldingLovedItemInOffHand(piglin);
        } else {
             if (isFood(itemStack)) {
                return !hasEatenRecently(piglin);
            } else if (!isLovedItem(itemStack)) {
                return piglin.canReplaceCurrentItem(itemStack);
            } else {
                return isNotHoldingLovedItemInOffHand(piglin) ;
            }
        }
    }

    public static boolean isLovedItem(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.HOGMEN_LIKED);
    }


    private static boolean wantsToStopRiding(EntityHogmen piglin, Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        } else {
            return !mob.isBaby() || !mob.isAlive() || wasHurtRecently(piglin) || wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
        }
    }

    private static boolean isNearestValidAttackTarget(EntityHogmen piglin, LivingEntity livingEntity) {
        return findNearestValidAttackTarget(piglin).filter((livingEntity2) -> {
            return livingEntity2 == livingEntity;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(EntityHogmen piglin) {
        Brain<EntityHogmen> brain = piglin.getBrain();
         {
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglin, (LivingEntity)optional.get())) {
                return optional;
            } else {
                Optional optional2;
                if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                    if (optional2.isPresent()) {
                        return optional2;
                    }
                }

                optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
                if (optional2.isPresent()) {
                    return optional2;
                } else {
                    Optional<Player> optional3 = brain.getMemory(MemoryModules.NEAREST_PLAYER_NOT_WEARING_CHAIN.get());
                    return optional3.isPresent() && Sensor.isEntityAttackable(piglin, (LivingEntity)optional3.get()) ? optional3 : Optional.empty();
                }
            }
        }
    }

    public static void angerNearbyPiglins(Player player, boolean bl) {
        List<EntityHogmen> list = player.level().getEntitiesOfClass(EntityHogmen.class, player.getBoundingBox().inflate(16.0));
        list.stream().filter(HogmenAi::isIdle).filter((piglin) -> {
            return !bl || BehaviorUtils.canSee(piglin, player);
        }).forEach((piglin) -> {
            if (piglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                setAngerTargetToNearestTargetablePlayerIfFound(piglin, player);
            } else {
                setAngerTarget(piglin, player);
            }

        });
    }

    public static InteractionResult mobInteract(EntityHogmen piglin, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (canAdmire(piglin, itemStack)) {
            ItemStack itemStack2 = itemStack.split(1);
            holdInOffhand(piglin, itemStack2);
            admireGoldItem(piglin);
            stopWalking(piglin);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static boolean canAdmire(EntityHogmen piglin, ItemStack itemStack) {
        return !isAdmiringDisabled(piglin) && !isAdmiringItem(piglin) && piglin.isAdult() && isBarterCurrency(itemStack);
    }

    public static void wasHurtBy(EntityHogmen piglin, LivingEntity livingEntity) {
        if (!(livingEntity instanceof EntityHogmen)) {
            if (isHoldingItemInOffHand(piglin)) {
                stopHoldingOffHandItem(piglin, false);
            }

            Brain<EntityHogmen> brain = piglin.getBrain();
            brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
            brain.eraseMemory(MemoryModuleType.DANCING);
            brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            if (livingEntity instanceof Player) {
                brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
            }

            getAvoidTarget(piglin).ifPresent((livingEntity2) -> {
                if (livingEntity2.getType() != livingEntity.getType()) {
                    brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
                }

            });
            if (piglin.isBaby()) {
                brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, 100L);
                if (Sensor.isEntityAttackableIgnoringLineOfSight(piglin, livingEntity)) {
                    broadcastAngerTarget(piglin, livingEntity);
                }

            } else if (livingEntity.getType() == ModEntities.BISON.get() && hoglinsOutnumberPiglins(piglin)) {
                setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity);
            } else {
                maybeRetaliate(piglin, livingEntity);
            }
        }
    }

    protected static void maybeRetaliate(EntityHogmen abstractPiglin, LivingEntity livingEntity) {
        if (!abstractPiglin.getBrain().isActive(Activity.AVOID)) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractPiglin, livingEntity)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(abstractPiglin, livingEntity, 4.0)) {
                    if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                        setAngerTargetToNearestTargetablePlayerIfFound(abstractPiglin, livingEntity);
                        broadcastUniversalAnger(abstractPiglin);
                    } else {
                        setAngerTarget(abstractPiglin, livingEntity);
                        broadcastAngerTarget(abstractPiglin, livingEntity);
                    }

                }
            }
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(EntityHogmen piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map((activity) -> {
            return getSoundForActivity(piglin, activity);
        });
    }

    private static SoundEvent getSoundForActivity(EntityHogmen piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        }  else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        } else if (activity == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        } else if (activity == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        } else if (seesPlayerHoldingLovedItem(piglin)) {
            return SoundEvents.PIGLIN_JEALOUS;
        } else {
            return isNearRepellent(piglin) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
        }
    }

    private static boolean isNearAvoidTarget(EntityHogmen piglin) {
        Brain<EntityHogmen> brain = piglin.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) && ((LivingEntity) brain.getMemory(MemoryModuleType.AVOID_TARGET).get()).closerThan(piglin, 12.0);
    }

    protected static List<EntityHogmen> getVisibleAdultPiglins(EntityHogmen piglin) {
        return (List)piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<EntityHogmen> getAdultPiglins(EntityHogmen abstractPiglin) {
        return (List)abstractPiglin.getBrain().getMemory(MemoryModules.NEAREST_ADULT_HOGMEN.get()).orElse(ImmutableList.of());
    }

    public static boolean isWearingGold(LivingEntity livingEntity) {
        Iterable<ItemStack> iterable = livingEntity.getArmorSlots();
        Iterator<ItemStack> var2 = iterable.iterator();

        Item item;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            ItemStack itemStack = (ItemStack)var2.next();
            item = itemStack.getItem();
        } while(!(item instanceof ArmorItem) || ((ArmorItem)item).getMaterial() != ArmorMaterials.CHAIN);

        return true;
    }

    private static void stopWalking(EntityHogmen piglin) {
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker ticker = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create((livingEntity) -> {
            return livingEntity.isBaby() && ticker.tickDownAndCheck(livingEntity.level().random);
        }, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
    }

    protected static void broadcastAngerTarget(EntityHogmen abstractPiglin, LivingEntity livingEntity) {
        getAdultPiglins(abstractPiglin).forEach((abstractPiglinx) -> {
            if (livingEntity.getType() != EntityType.HOGLIN || abstractPiglinx.canHunt() && ((Hoglin)livingEntity).canBeHunted()) {
                setAngerTargetIfCloserThanCurrent(abstractPiglinx, livingEntity);
            }
        });
    }

    protected static void broadcastUniversalAnger(EntityHogmen abstractPiglin) {
        getAdultPiglins(abstractPiglin).forEach((abstractPiglinx) -> {
            getNearestVisibleTargetablePlayer(abstractPiglinx).ifPresent((player) -> {
                setAngerTarget(abstractPiglinx, player);
            });
        });
    }

    protected static void setAngerTarget(EntityHogmen abstractPiglin, LivingEntity livingEntity) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractPiglin, livingEntity)) {
            abstractPiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingEntity.getUUID(), 600L);
            if (livingEntity.getType() == EntityType.HOGLIN && abstractPiglin.canHunt()) {
                dontKillAnyMoreHoglinsForAWhile(abstractPiglin);
            }

            if (livingEntity.getType() == EntityType.PLAYER && abstractPiglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
            }

        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(EntityHogmen abstractPiglin, LivingEntity livingEntity) {
        Optional<Player> optional = getNearestVisibleTargetablePlayer(abstractPiglin);
        if (optional.isPresent()) {
            setAngerTarget(abstractPiglin, (LivingEntity)optional.get());
        } else {
            setAngerTarget(abstractPiglin, livingEntity);
        }

    }

    private static void setAngerTargetIfCloserThanCurrent(EntityHogmen abstractPiglin, LivingEntity livingEntity) {
        Optional<LivingEntity> optional = getAngerTarget(abstractPiglin);
        LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(abstractPiglin, optional, livingEntity);
        if (!optional.isPresent() || optional.get() != livingEntity2) {
            setAngerTarget(abstractPiglin, livingEntity2);
        }
    }

    private static Optional<LivingEntity> getAngerTarget(EntityHogmen abstractPiglin) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(EntityHogmen piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(EntityHogmen abstractPiglin) {
        return abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
    }

    private static boolean wantsToStopFleeing(EntityHogmen piglin) {
        Brain<EntityHogmen> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> entityType = livingEntity.getType();
            if (entityType == EntityType.HOGLIN) {
                return piglinsEqualOrOutnumberHoglins(piglin);
            } else {
                return false;
            }
        }
    }

    private static boolean piglinsEqualOrOutnumberHoglins(EntityHogmen piglin) {
        return !hoglinsOutnumberPiglins(piglin);
    }

    private static boolean hoglinsOutnumberPiglins(EntityHogmen piglin) {
        int i = (Integer)piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = (Integer)piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return j > i;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(EntityHogmen piglin, LivingEntity livingEntity) {
        piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        dontKillAnyMoreHoglinsForAWhile(piglin);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(EntityHogmen abstractPiglin) {
        abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.sample(abstractPiglin.level().random));
    }

    private static void eat(EntityHogmen piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(EntityHogmen piglin) {
        Vec3 vec3 = LandRandomPos.getPos(piglin, 4, 2);
        return vec3 == null ? piglin.position() : vec3;
    }

    private static boolean hasEatenRecently(EntityHogmen piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(EntityHogmen abstractPiglin) {
        return abstractPiglin.getBrain().isActive(Activity.IDLE);
    }

    public static boolean hasCrossbow(LivingEntity livingEntity) {
        return livingEntity.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean isAdmiringItem(EntityHogmen piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(ItemStack itemStack) {
        return itemStack.is(TRADE_ITEM);
    }

    private static boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.PIGLIN_FOOD);
    }

    private static boolean isNearRepellent(EntityHogmen piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return !seesPlayerHoldingLovedItem(livingEntity);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getType() == EntityType.PLAYER && livingEntity.isHolding(HogmenAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(EntityHogmen piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(EntityHogmen piglin) {
        return !piglin.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(EntityHogmen piglin) {
        return piglin.getOffhandItem().isEmpty() || !isLovedItem(piglin.getOffhandItem());
    }

    static {
        TRADE_ITEM = Items.EMERALD;
        TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
        RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
        RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    }


}
