package com.serilum.youritemsaresafe.data;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Variables {
	public static final HashMap<UUID, List<ItemStack>> pendingChestStacks = new HashMap<>();
}
