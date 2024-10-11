package org.chubby.github.mofoes.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.entity.MobCategory;

import java.util.Optional;
import java.util.function.Supplier;

public class CheckSpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
//        dispatcher.register(Commands.literal("checkspawn")
//                .then(Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
//                        .then(Commands.argument("entity", StringArgumentType.string())
//                                .executes(context -> checkSpawnChance(context)))));
    }

    //private static int checkSpawnChance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        ResourceLocation structureId = ResourceOrTagKeyArgument.getResourceOrTagKey(context,"structure",Registries.STRUCTURE
//        ,context.getSource().getEntityOrException()).;
//        String entityId = StringArgumentType.getString(context, "entity");
//        CommandSourceStack source = context.getSource();
//        ServerLevel level = source.getLevel();
//        StructureManager structureManager = level.structureManager();
//
//        // Fetch the structure from the registry
//        Optional<? extends Structure> structureOptional = structureManager.registryAccess()
//                .registryOrThrow(Registries.STRUCTURE)
//                .getOptional(structureId);
//
//        if (!structureOptional.isPresent()) {
//            source.sendFailure(Component.literal("Structure not found: " + structureId));
//            return 0;
//        }
//
//        Structure structure = structureOptional.get();
//        MobSpawnSettings.SpawnerData spawnData = getEntitySpawnDataForStructure(structure, entityId);
//
//        if (spawnData != null) {
//            int weight = spawnData.getWeight().asInt();
//            int minCount = spawnData.minCount;
//            int maxCount = spawnData.maxCount;
//
//            source.sendSuccess((Supplier<Component>) Component.literal("Entity: " + entityId + " | Spawn Weight: " + weight + " | Min Count: " + minCount + " | Max Count: " + maxCount), false);
//        } else {
//            source.sendFailure(Component.literal("Entity not found or does not spawn in structure: " + entityId));
//        }
//        return 1;
//    }
//
//    private static MobSpawnSettings.SpawnerData getEntitySpawnDataForStructure(Structure structure, String entityId) {
//        for (MobCategory category : structure.spawnOverrides().keySet()) {
//            for (MobSpawnSettings.SpawnerData spawnerData : structure.spawnOverrides().get(category).spawns().unwrap()) {
//                if (spawnerData.type.toShortString().equals(entityId)) {
//                    return spawnerData;
//                }
//            }
//        }
//        return null;
//    }
}
