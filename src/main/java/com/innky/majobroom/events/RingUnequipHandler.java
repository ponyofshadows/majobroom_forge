package com.innky.majobroom.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosApi;
import javax.annotation.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;

@Mod.EventBusSubscriber(modid = "majobroom")
public class RingUnequipHandler {

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        // 只处理玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack from = event.getFrom();  // 被移出的物品
        ItemStack to   = event.getTo();    // 放入的物品

        // 仅当我们的 Eye Ring 被摘下（from 是戒指，to 为空）时触发
        if (from.getItem() instanceof com.innky.majobroom.item.EyeRingItem && to.isEmpty()) {
            // 1. 降低 HEALTH_BOOST 效果一级，或直接移除
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

            // 2. 检查玩家身上是否还留有其他 Eye Ring，如果没有则移除夜视效果
            LazyOptional<IItemHandlerModifiable> curiosOpt =
                CuriosApi.getCuriosHelper().getEquippedCurios(player);
            IItemHandlerModifiable handler = curiosOpt.orElse(null);
            boolean hasRing = false;
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack s2 = handler.getStackInSlot(i);
                    if (s2.getItem() instanceof com.innky.majobroom.item.EyeRingItem) {
                        hasRing = true;
                        break;
                    }
                }
            }

            // 如果已经没有任何 Eye Ring 则移除夜视
            if (!hasRing) {
                MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
                if (nv != null) {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }
        }
    }
}
