package com.SolanaDevMinecraft;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ElytraItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SolanaForge.MODID);

    public static final RegistryObject<Item> NETHER_RELIC = ITEMS.register("nether_relic",
            () -> new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.HEAD, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> THOR_AXE = ITEMS.register("thor_axe",
            () -> new Item(new Item.Properties().stacksTo(1).durability(2031)));

    public static final RegistryObject<Item> BOOT_RELIC = ITEMS.register("boot_relic",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.FEET, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> WING_RELIC = ITEMS.register("wing_relic",
            () -> new ElytraItem(new Item.Properties().stacksTo(1).durability(432).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> LEG_RELIC = ITEMS.register("leg_relic",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
}
