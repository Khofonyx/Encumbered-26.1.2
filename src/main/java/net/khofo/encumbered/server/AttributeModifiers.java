package net.khofo.encumbered.server;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/*
This class adds an AttributeModifier to slow the player down. This is used in the 2nd threshold to make them "heavy"
This also matches the speed of crouching.
 */
public class AttributeModifiers {
    public static final Identifier playerSpeedID = Identifier.fromNamespaceAndPath("encumbered", "player_speed");
    public static final AttributeModifier playerSpeedModifier = new AttributeModifier(
            playerSpeedID,-0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public static final Identifier playerJumpID = Identifier.fromNamespaceAndPath("encumbered", "player_jump");
    public static final AttributeModifier playerJumpModifier = new AttributeModifier(
            playerJumpID,-1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
}
