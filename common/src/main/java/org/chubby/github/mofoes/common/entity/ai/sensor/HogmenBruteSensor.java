package org.chubby.github.mofoes.common.entity.ai.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.chubby.github.mofoes.common.entity.EntityBison;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.ai.HogmenAi;
import org.chubby.github.mofoes.common.entity.ai.memory.MemoryModules;

import java.util.*;

public class HogmenBruteSensor extends Sensor<LivingEntity>
{
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<Mob> optional = Optional.empty();
        Optional<EntityBison> optional2 = Optional.empty();
        Optional<EntityBison> optional3 = Optional.empty();
        Optional<EntityHogmen> optional4 = Optional.empty();
        Optional<LivingEntity> optional5 = Optional.empty();
        Optional<Player> optional6 = Optional.empty();
        Optional<Player> optional7 = Optional.empty();
        int i = 0;
        List<EntityHogmen> list = Lists.newArrayList();
        List<EntityHogmen> list2 = Lists.newArrayList();
        NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities) brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        Iterator var15 = nearestVisibleLivingEntities.findAll((livingEntityx) -> {
            return true;
        }).iterator();

        while (true) {
            while (true) {
                while (var15.hasNext()) {
                    LivingEntity livingEntity = (LivingEntity) var15.next();
                    if (livingEntity instanceof EntityBison hoglin) {
                        if (hoglin.isBaby() && optional3.isEmpty()) {
                            optional3 = Optional.of(hoglin);
                        } else if (hoglin.isAdult()) {
                            ++i;
                            if (optional2.isEmpty() && hoglin.canBeHunted()) {
                                optional2 = Optional.of(hoglin);
                            }
                        }
                    }  else if (livingEntity instanceof EntityHogmen piglin) {
                        if (piglin.isBaby() && optional4.isEmpty()) {
                            optional4 = Optional.of(piglin);
                        } else if (piglin.isAdult()) {
                            list.add(piglin);
                        }
                    } else if (livingEntity instanceof Player player) {
                        if (optional6.isEmpty() && !HogmenAi.isWearingGold(player) && entity.canAttack(livingEntity)) {
                            optional6 = Optional.of(player);
                        }

                        if (optional7.isEmpty() && !player.isSpectator() && HogmenAi.isPlayerHoldingLovedItem(player)) {
                            optional7 = Optional.of(player);
                        }
                    } else if (optional.isEmpty() && (livingEntity instanceof WitherSkeleton || livingEntity instanceof WitherBoss)) {
                        optional = Optional.of((Mob) livingEntity);
                    } else if (optional5.isEmpty() && PiglinAi.isZombified(livingEntity.getType())) {
                        optional5 = Optional.of(livingEntity);
                    }
                }

                List<LivingEntity> list3 = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of());
                Iterator<LivingEntity> var22 = list3.iterator();

                while (var22.hasNext()) {
                    LivingEntity livingEntity2 = (LivingEntity) var22.next();
                    if (livingEntity2 instanceof EntityHogmen abstractPiglin) {
                        if (abstractPiglin.isAdult()) {
                            list2.add(abstractPiglin);
                        }
                    }
                }

                // brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
                //brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
                brain.setMemory(MemoryModules.NEAREST_PLAYER_NOT_WEARING_CHAIN.get(), optional6);
                brain.setMemory(MemoryModules.NEARBY_HOGMEN.get(), list2);
                brain.setMemory(MemoryModules.NEAREST_ADULT_HOGMEN.get(), list);
                brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
                brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
                return;
            }
        }
    }
}
