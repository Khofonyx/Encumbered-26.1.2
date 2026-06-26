package net.khofo.encumbered.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class BoostItemMap {
    private BoostItemMap() {
    }

    public static final DataMapType<Item, BoostItem> BOOST_ITEMS =
            DataMapType.builder(
                    Identifier.fromNamespaceAndPath("encumbered", "boost_items"),
                    Registries.ITEM,
                    BoostItem.CODEC
            ).synced(BoostItem.CODEC, true).build();

    public static void registerBoostItemMap(RegisterDataMapTypesEvent event) {
        event.register(BOOST_ITEMS);
    }

    public static BoostItem get(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
        return holder.getData(BOOST_ITEMS);
    }
}
