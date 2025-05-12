package com.innky.majobroom.events;

import com.innky.majobroom.armors.MajoWearableItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorEquipListener {

    /**
     * Fired whenever any living entity’s equipment changes.
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // Get the entity whose equipment changed
        LivingEntity entity = event.getEntity();

        // We only care about players
        if (!(entity instanceof Player player)) {
            return;
        }

        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        // --- When the player PUTS ON one of our MajoWearableItem pieces --- //
        if (!(from.getItem() instanceof MajoWearableItem)
            && (to.getItem() instanceof MajoWearableItem)) {

            // 1) Give the slot-based “infinite” buff
            MobEffectInstance effect = switch (slot) {
                case HEAD  -> new MobEffectInstance(MobEffects.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false);
                case CHEST -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE,  Integer.MAX_VALUE, 0, false, false);
                case LEGS  -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, false, false);
                case FEET  -> new MobEffectInstance(MobEffects.DIG_SPEED,        Integer.MAX_VALUE, 0, false, false);
                default    -> null;
            };
            if (effect != null) {
                player.addEffect(effect);
            }
        }

        // --- When the player TAKES OFF one of our MajoWearableItem pieces --- //
        if ((from.getItem() instanceof MajoWearableItem)
            && !(to.getItem() instanceof MajoWearableItem)) {

            // 1) Remove the infinite buff and reapply 8-second version
            MobEffect effectType = switch (slot) {
                case HEAD  -> MobEffects.WATER_BREATHING;
                case CHEST -> MobEffects.FIRE_RESISTANCE;
                case LEGS  -> MobEffects.DAMAGE_RESISTANCE;
                case FEET  -> MobEffects.DIG_SPEED;
                default    -> null;
            };

            if (effectType != null) {
                // Strip off the infinite-duration effect
                player.removeEffect(effectType);
                // Reapply 8-second buff at same level
                MobEffectInstance old = player.getEffect(effectType);
                int amp = old != null ? old.getAmplifier() : 0;
                player.addEffect(new MobEffectInstance(effectType, 8 * 20, amp, false, false));
            }
        }
    }
}
