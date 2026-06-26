package net.khofo.encumbered.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatapackPathGenerator {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        Path datapacks = server.getWorldPath(LevelResource.DATAPACK_DIR);
        Path pack = datapacks.resolve("encumbered_pack");
        Path itemMaps = pack.resolve("data/encumbered/data_maps/item");

        try {
            Files.createDirectories(itemMaps);

            Path packMcmeta = pack.resolve("pack.mcmeta");
            if (Files.notExists(packMcmeta)) {
                Files.writeString(packMcmeta, """
                {
                  "pack": {
                    "pack_format": 61,
                    "description": "Encumbered generated datapack"
                  }
                }
                """);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Encumbered datapack folder", e);
        }
    }
}
