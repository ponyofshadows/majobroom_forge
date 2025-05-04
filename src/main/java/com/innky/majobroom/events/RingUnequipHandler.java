package com.innky.majobroom.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioChangeEvent;



@Mod.EventBusSubscriber(modid = "majobroom")
public class RingUnequipHandler {

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        // only care about players
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack from = event.getFrom();  // the stack being removed from the slot
        ItemStack to   = event.getTo();    // the stack placed into the slot

        // detect “our” ring being moved out into an empty slot:
        if (from.getItem() instanceof com.innky.majobroom.item.EyeRingItem 
            && to.isEmpty()) 
        {
            // 1. lower HEALTH_BOOST by one amplifier, or remove if amp was 0
            MobEffectInstance hb = player.getEffect(MobEffects.HEALTH_BOOST);
            if (hb != null) {
                int amp = hb.getAmplifier();
                if (amp > 0) {
                    player.removeEffect(MobEffects.HEALTH_BOOST);
                    player.addEffect(new MobEffectInstance(
                        MobEffects.HEALTH_BOOST,
                        hb.getDuration(),
                        amp - 1,
                        false,
                        false
                    ));
                } else {
                    player.removeEffect(MobEffects.HEALTH_BOOST);
                }
            }
        }
    }
}
