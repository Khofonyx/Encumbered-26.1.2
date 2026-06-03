package net.khofo.encumbered;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import static net.minecraft.commands.Commands.literal;

public final class EncumberedCommands {
    private EncumberedCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("encumbered")
                        .then(
                                literal("weight")
                                        .executes(context -> {
                                            var source = context.getSource();

                                            if (!source.isPlayer()) {
                                                source.sendFailure(Component.literal("This command can only be used by a player."));
                                                return 0;
                                            }

                                            var player = source.getPlayer();
                                            double weight = WeightCalculator.getPlayerWeight(player);

                                            source.sendSuccess(
                                                    () -> Component.literal("Your current weight is " + weight),
                                                    false
                                            );

                                            return 1;
                                        })
                        )
        );
    }
}