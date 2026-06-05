package net.khofo.encumbered.data;

import net.khofo.encumbered.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

// This defines the actual Data Map mapping registry items from the "Item" registry to a weight entry.
public final class WeightsDataMap {
    public static final DataMapType<Item, WeightEntry> WeightsDataMap = DataMapType.builder(
            Identifier.fromNamespaceAndPath("encumbered", "item_weights"),
            Registries.ITEM,
            WeightEntry.CODEC
    ).synced(WeightEntry.CODEC,true).build();

    // Getter for registering the WeightsDataMap on the modEventBus.
    public static void registerWeightsDataMap(RegisterDataMapTypesEvent event) {
        event.register(WeightsDataMap);
    }

    // Helper method to get the weight of an item from an ItemStack.
    public static float getWeight(ItemStack stack){
        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
        WeightEntry data = holder.getData(WeightsDataMap);
        return data != null ? data.weight() : Config.DEFAULT_ITEM_WEIGHT.get().floatValue();
    }
}
