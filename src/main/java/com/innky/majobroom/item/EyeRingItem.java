package com.innky.majobroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Random;

public class EyeRingItem extends Item implements ICurioItem {
    public static final String TAG_TIMER = "ring_timer";
    public static final String TAG_PREV_MANA = "previous_mana";
    public static final String TAG_CURR_MANA = "current_mana";

    private static final int TICKS_PER_MINUTE = 20 * 60;
    private static final Random RANDOM = new Random();

    public EyeRingItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(Component.translatable("item.majobroom.eye_ring.tooltip"));
        tooltip.add(Component.translatable("item.majobroom.eye_ring.tooltip.2"));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int timer = tag.getInt(TAG_TIMER);

        // 每分钟打印调试信息
        if (player.level().getGameTime() % TICKS_PER_MINUTE == 0) {
            player.sendSystemMessage(
                Component.literal("[EyeRing DEBUG] ring_timer = " + timer),
                false
            );
        }

        if (timer == 0) {
            ServerLevel serverWorld = (ServerLevel) player.level();

            // 原有效果
            applyEffect(player, "spore", "marker", 5 * TICKS_PER_MINUTE, 0);
            applyEffect(player, "spore", "uneasy", 5 * TICKS_PER_MINUTE, 0);

            MobEffectInstance currentBoost = player.getEffect(MobEffects.HEALTH_BOOST);
            int newAmp = (currentBoost != null) ? currentBoost.getAmplifier() + 1 : 0;
            if (newAmp > 1) newAmp = 1;
            player.addEffect(new MobEffectInstance(
                MobEffects.HEALTH_BOOST,
                2 * TICKS_PER_MINUTE,
                newAmp,
                false,
                false
            ));
            player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                2 * TICKS_PER_MINUTE,
                0,
                false,
                false
            ));

            if (RANDOM.nextInt(10) == 0) {
                BlockPos belowPos = player.blockPosition().below();
                Level world = player.level();
                if (world.getBlockState(belowPos).isFaceSturdy(world, belowPos, Direction.UP)) {
                    spawnEntity(serverWorld, player, "spore", "vigil", player.blockPosition());
                }
            }

            // 多戒指疯狂效果
            LazyOptional<IItemHandlerModifiable> curiosOpt =
                CuriosApi.getCuriosHelper().getEquippedCurios(player);
            IItemHandlerModifiable handler = curiosOpt.orElse(null);
            int ringCount = 0;
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack s2 = handler.getStackInSlot(i);
                    if (s2.getItem() instanceof EyeRingItem) {
                        ringCount++;
                    }
                }
            }
            if (ringCount > 1 && RANDOM.nextInt(6) == 0) {
                ResourceLocation madnessId = new ResourceLocation("spore", "madness");
                var madnessEffect = ForgeRegistries.MOB_EFFECTS.getValue(madnessId);
                if (madnessEffect != null) {
                    MobEffectInstance existing = player.getEffect(madnessEffect);
                    int amp = (existing != null) ? existing.getAmplifier() + 1 : 0;
                    player.addEffect(new MobEffectInstance(
                        madnessEffect,
                        10 * TICKS_PER_MINUTE,
                        amp,
                        false,
                        false
                    ));
                }
            }

            // 新增：每分钟计算 spore: 装备数量并调整 max_mana
            Inventory inv = player.getInventory();
            int numSporeArmor = 0;
            for (ItemStack armor : inv.armor) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(armor.getItem());
                if (id != null && "spore".equals(id.getNamespace())) {
                    numSporeArmor++;
                }
            }

            // 更新 NBT
            double prev = tag.getDouble(TAG_CURR_MANA);
            double curr = 50.0 * numSporeArmor;
            tag.putDouble(TAG_PREV_MANA, prev);
            tag.putDouble(TAG_CURR_MANA, curr);

            // 调整玩家属性：irons_spellbooks:max_mana
            adjustPlayerMana(player, curr - prev);
        }

        // 更新 timer 并保存
        timer = (timer + 1) % TICKS_PER_MINUTE;
        tag.putInt(TAG_TIMER, timer);
    }

    /** 调整玩家的 max_mana 属性 **/
    public static void adjustPlayerMana(Player player, double amount) {
        ResourceLocation manaId = new ResourceLocation("irons_spellbooks", "max_mana");
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(manaId);
        if (attr != null) {
            AttributeInstance inst = player.getAttribute(attr);
            if (inst != null) {
                inst.setBaseValue(inst.getBaseValue() + amount);
            }
        }
    }

    private void applyEffect(ServerPlayer player, String modid, String effectName, int duration, int amplifier) {
        ResourceLocation id = new ResourceLocation(modid, effectName);
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
    }

    private void spawnEntity(ServerLevel serverWorld, ServerPlayer player, String modid, String entityName, BlockPos pos) {
        ResourceLocation id = new ResourceLocation(modid, entityName);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type != null) {
            type.spawn(
                serverWorld,
                ItemStack.EMPTY,
                player,
                pos,
                MobSpawnType.TRIGGERED,
                true,
                false
            );
        }
    }
}
