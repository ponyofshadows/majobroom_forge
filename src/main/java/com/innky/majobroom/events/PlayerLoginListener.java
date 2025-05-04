package com.innky.majobroom.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Resets the Iron’s Spellbooks max-mana attribute back to 100
 * whenever a player logs in or reconnects.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoginListener {

    // The ResourceLocation of the custom max_mana attribute from irons_spellbooks
    private static final ResourceLocation MAX_MANA_ID =
        new ResourceLocation("irons_spellbooks", "max_mana");

    /**
     * Fired when a player logs in (or rejoins) the world.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        // Grab the Player instance from the event
        Player player = (Player) event.getEntity();

        // Look up the attribute by its ResourceLocation
        Attribute maxManaAttr = ForgeRegistries.ATTRIBUTES.getValue(MAX_MANA_ID);
        if (maxManaAttr == null) {
            return;
        }

        // Get the player's AttributeInstance for max_mana
        AttributeInstance inst = player.getAttribute(maxManaAttr);
        if (inst == null) {
            return;
        }

        // Reset the base value to exactly 100
        inst.setBaseValue(100.0);
    }
}
