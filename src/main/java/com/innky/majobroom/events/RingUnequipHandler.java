package com.innky.majobroom.events;

import com.innky.majobroom.item.EyeRingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
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
public class RingUnequipHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        // 只处理玩家实体
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack from = event.getFrom();  // 被摘下的戒指
        ItemStack to   = event.getTo();    // 放入的新物品

        // 仅当 from 是 EyeRing，且 to 为空（摘下操作）时继续
        if (from.getItem() instanceof EyeRingItem && to.isEmpty()) {
            // —— 保留原有的效果处理（Health Boost / Night Vision） —— 
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
            // 检查 Curios 槽中是否还存在其他 EyeRing
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

            // —— 仅保留这一条调试：把被摘下戒指的 NBT 发给玩家及日志 —— 
            CompoundTag fromTag = from.getTag();
            String fromStr = fromTag != null ? fromTag.toString() : "{}";
            LOGGER.info("[EyeRing] Unequip from-slot NBT: " + fromStr);
            // 发送到玩家聊天栏
            player.sendSystemMessage(Component.literal("DEBUG: unequipped NBT = " + fromStr));
        }
    }
}
