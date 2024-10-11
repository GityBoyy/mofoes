package org.chubby.github.mofoes.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.chubby.github.mofoes.Mofoes;

public class ModTags
{
    public static class Items {

        public static final TagKey<Item> HOGMEN_LIKED = tag("hogmen_liked");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM,new ResourceLocation(Mofoes.MOD_ID, name));
        }

        private static TagKey<Item> forgeTag(String name) {
            return TagKey.create(Registries.ITEM,new ResourceLocation("forge", name));
        }
    }
}
