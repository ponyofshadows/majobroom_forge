package com.innky.majobroom.events;

import com.innky.majobroom.item.EyeRingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

@Mod.EventBusSubscriber(modid = "majobroom")
public class RingEquipUnequipHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack from = event.getFrom(); 
        ItemStack to   = event.getTo(); 

        // equip
        if (from.isEmpty() && to.getItem() instanceof EyeRingItem) {
            CompoundTag tag = to.getOrCreateTag();
            tag.putLong(EyeRingItem.TAG_PREV_MANA, 0L);
            tag.putLong(EyeRingItem.TAG_CURR_MANA, 0L);
            LOGGER.info("[EyeRing] Equipped: initialized mana tags");
            return;
        }

        // unequip
        if (from.getItem() instanceof EyeRingItem && to.isEmpty()) {
            MobEffectInstance hb = player.getEffect(MobEffects.HEALTH_BOOST);
            if (hb != null) {
                int amp = hb.getAmplifier();
                player.removeEffect(MobEffects.HEALTH_BOOST);
                if (amp > 0) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.HEALTH_BOOST,
                        hb.getDuration(),
                        amp - 1,
                        false,
                        false
                    ));
                }
            }
            LazyOptional<IItemHandlerModifiable> curiosOpt =
                CuriosApi.getCuriosHelper().getEquippedCurios(player);
            IItemHandlerModifiable handler = curiosOpt.orElse(null);
            boolean hasRing = false;
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack s2 = handler.getStackInSlot(i);
                    if (s2.getItem() instanceof EyeRingItem) {
                        hasRing = true;
                        break;
                    }
                }
            }
            if (!hasRing && player.getEffect(MobEffects.NIGHT_VISION) != null) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }

            CompoundTag fromTag = from.getOrCreateTag();
            long curr = fromTag.getLong(EyeRingItem.TAG_CURR_MANA);
            EyeRingItem.adjustPlayerMana(player, -curr);
        }
    }
}
