package net.khofo.encumbered;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.minecraft.resources.Identifier;
/*
This creates a data map called encumbered:item_weights
because this is attached to the minecraft:item registry, the generated datapack file path will be: data/encumbered/data_maps/item/item_weights.json
 */
public class EncumberedDataMaps {
    private EncumberedDataMaps() {
    }

    public static final DataMapType<Item, ItemWeight> ITEM_WEIGHTS =
            DataMapType.builder(
                    Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "item_weights"),
                    Registries.ITEM,
                    ItemWeight.CODEC
            ).build();
}
