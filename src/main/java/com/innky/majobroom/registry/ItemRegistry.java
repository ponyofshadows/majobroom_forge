package com.innky.majobroom.registry;

import com.innky.majobroom.MajoBroom;
import com.innky.majobroom.armors.MajoWearableItem;
import com.innky.majobroom.armors.Modelinit;
import com.innky.majobroom.item.BroomItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.innky.majobroom.item.EyeRingItem;

public class ItemRegistry {
    public static  DeferredRegister<Item> ITEMS ;
    public static RegistryObject<Item> broomItem;
    public static  RegistryObject<Item> majoHat ;
    public static RegistryObject<Item> eyeRing;
    public static Map<String ,RegistryObject<Item>> itemMap = new HashMap<>();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "majobroom");
    public static RegistryObject<CreativeModeTab> MOD_TAB;
    
    public static void registry()  {
        ITEMS= DeferredRegister.create(ForgeRegistries.ITEMS, "majobroom");
        broomItem = ITEMS.register("broom_item", () ->{
            return new BroomItem(new Item.Properties());
        });
        itemMap.put("majobroom",broomItem);
        
        eyeRing = ITEMS.register("eye_ring", () ->
          new EyeRingItem(new Item.Properties())
);
        itemMap.put("eye_ring", eyeRing);

        boolean isRemote = true;
        try {
            System.out.println(HumanoidModel.class);
        }catch (Exception e){
            isRemote = false;
        }
        try {
            InputStream in = MajoBroom.class.getClassLoader().getResourceAsStream("/assets/majobroom/jsonmodels/model_list.txt");
            if (in!=null) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(in));
                String temp = "";
                while ((temp = bf.readLine())!= null) {
                    String[] results = temp.split(",");
                    switch (results[1]){
                        case "3":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem.Helmet()));
                            break;
                        case "2":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem.Chestplate()));
                            break;
                        case "1":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem.Leggings()));
                            break;
                        case "0":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem.Boots()));
                            break;
                    }
                    Modelinit.reg(results[0],isRemote);

                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        MOD_TAB = CREATIVE_MODE_TABS.register("majo_group", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.majo_group")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistry.broomItem.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (RegistryObject<Item> holder : itemMap.values()) {
                    output.accept(holder.get());
                }
            }).build());
        CREATIVE_MODE_TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
