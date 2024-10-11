package org.chubby.github.mofoes.common.entity.ai.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.ai.HogmenAi;

public class StartAdmiringItemIfSeen
{
    public StartAdmiringItemIfSeen() {
    }

    public static BehaviorControl<EntityHogmen> create(int admireDuration) {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), instance.absent(MemoryModuleType.ADMIRING_ITEM), instance.absent(MemoryModuleType.ADMIRING_DISABLED), instance.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> {
                return (serverLevel, livingEntity, l) -> {
                    ItemEntity itemEntity = (ItemEntity)instance.get(memoryAccessor);
                    if (!HogmenAi.isLovedItem(itemEntity.getItem())) {
                        return false;
                    } else {
                        memoryAccessor2.setWithExpiry(true, (long)admireDuration);
                        return true;
                    }
                };
            });
        });
    }
}
