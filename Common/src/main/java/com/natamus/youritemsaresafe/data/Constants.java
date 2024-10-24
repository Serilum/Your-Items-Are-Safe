package com.natamus.youritemsaresafe.data;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final List<EquipmentSlot> slotTypes = new ArrayList<EquipmentSlot>(Arrays.asList(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    public static boolean inventoryTotemModIsLoaded = false;
}
