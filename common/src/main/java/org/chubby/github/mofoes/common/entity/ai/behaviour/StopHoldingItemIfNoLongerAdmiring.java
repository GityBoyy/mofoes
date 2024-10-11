package org.chubby.github.mofoes.common.entity.ai.behaviour;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Items;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.ai.HogmenAi;

public class StopHoldingItemIfNoLongerAdmiring
{
    public StopHoldingItemIfNoLongerAdmiring() {
    }

    public static BehaviorControl<EntityHogmen> create() {
        return BehaviorBuilder.create((instance) -> {
            return instance.group(instance.absent(MemoryModuleType.ADMIRING_ITEM)).apply(instance, (memoryAccessor) -> {
                return (serverLevel, hogmen, l) -> {
                    if (!hogmen.getOffhandItem().isEmpty() && !hogmen.getOffhandItem().is(Items.SHIELD)) {
                        HogmenAi.stopHoldingOffHandItem(hogmen, true);
                        return true;
                    } else {
                        return false;
                    }
                };
            });
        });
    }
}
