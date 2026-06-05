package net.khofo.server;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeModifiers {

    // Attribute Modifier that negates the +30% sprint speed by applying -30%
    public static final Identifier stopSprintingModifierID = Identifier.fromNamespaceAndPath("encumbered", "stop_sprinting");
    public static final AttributeModifier stopSprintingModifier = new AttributeModifier(
            stopSprintingModifierID,-0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
}
