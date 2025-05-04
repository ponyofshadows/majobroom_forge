package com.innky.majobroom.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.SlotContext;

public class EyeRingItem extends Item implements ICurioItem {
    public EyeRingItem(Properties props) {
        super(props.stacksTo(1)); // Curios 装备通常只允许叠堆为 1
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 每 tick 执行一次（如果不需要效果可以留空）
    }
}
