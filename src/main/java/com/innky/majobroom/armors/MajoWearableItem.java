package com.innky.majobroom.armors;

import com.innky.majobroom.armors.ModArmorMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MajoWearableItem extends ArmorItem implements DyeableLeatherItem {
    public MajoWearableItem(ArmorMaterial materialIn, ArmorItem.Type slot, Properties builderIn) {
        super(materialIn, slot, builderIn.fireResistant().rarity(Rarity.EPIC));
    }


    @Override
    public int getColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 0xdda3c7;
    }

    @Override
    public @NotNull Object getRenderPropertiesInternal() {

        return new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return Modelinit.modelMap.get(itemStack.getDescriptionId().substring(15));
            }
        };
    }


    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "majobroom:jsonmodels/textures/"+stack.getDescriptionId().substring(15)+".png";

    }


    public static class Helmet extends MajoWearableItem {
        public Helmet() {
            super(ModArmorMaterial.CLOTH, ArmorItem.Type.HELMET, (new Item.Properties()));
        }


        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 4, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, 4, false, false));
            }
        }
    }

    public static class Chestplate extends MajoWearableItem {
        public Chestplate() {
            super(ModArmorMaterial.CLOTH, ArmorItem.Type.CHESTPLATE, (new Item.Properties()));
        }


        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 3, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 4, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 4, false, false));
            }
        }
    }

    public static class Leggings extends MajoWearableItem {
        public Leggings() {
            super(ModArmorMaterial.CLOTH, ArmorItem.Type.LEGGINGS, (new Item.Properties()));
        }


        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 2, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 4, false, false));
            }
        }
    }

    public static class Boots extends MajoWearableItem {
        public Boots() {
            super(ArmorItem.Type.BOOTS, new Item.Properties());
        }


        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 2, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.LUCK, 240, 2, false, false));
            }
        }
    }
}
