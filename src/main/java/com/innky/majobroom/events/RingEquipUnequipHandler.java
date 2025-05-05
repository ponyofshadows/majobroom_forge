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

        ItemStack from = event.getFrom();  // 之前的物品
        ItemStack to   = event.getTo();    // 新的物品

        // 装备 EyeRing 时：初始化 NBT 变量
        if (from.isEmpty() && to.getItem() instanceof EyeRingItem) {
            CompoundTag tag = to.getOrCreateTag();
            tag.putLong(EyeRingItem.TAG_PREV_MANA, 0L);
            tag.putLong(EyeRingItem.TAG_CURR_MANA, 0L);
            LOGGER.info("[EyeRing] Equipped: initialized mana tags");
            return;
        }

        // 脱下 EyeRing 时：处理效果和属性扣除
        if (from.getItem() instanceof EyeRingItem && to.isEmpty()) {
            // 1) 原有效果处理（Health Boost / Night Vision）
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

            // 2) 从戒指读取 current_mana 并调整玩家属性
            CompoundTag fromTag = from.getOrCreateTag();
            long curr = fromTag.getLong(EyeRingItem.TAG_CURR_MANA);
            EyeRingItem.adjustPlayerMana(player, -curr);

            // 可选调试信息
            String tagStr = fromTag.toString();
            LOGGER.info("[EyeRing] Unequipped NBT: " + tagStr);
            player.sendSystemMessage(Component.literal("DEBUG: unequipped NBT = " + tagStr));
        }
    }
}
