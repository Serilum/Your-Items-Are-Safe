package com.natamus.youritemsaresafe.util;

import com.natamus.collective.functions.CompareItemFunctions;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.youritemsaresafe.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Util {
	public static int processCheck(List<ItemStack> itemStacks, int itemsLeft, String compare, int decrease) {
		for (ItemStack itemStack : itemStacks) {
			if (itemsLeft <= 0) {
				break;
			}
			
			int count = itemStack.getCount();
			if (comparePassed(compare, itemStack)) {
				while (count > 0 && itemsLeft > 0) {
					itemsLeft -= decrease;
					count -= 1;
					itemStack.setCount(count);
				}
			}
		}
		
		return itemsLeft;
	}
	
	public static boolean comparePassed(String compare, ItemStack itemStack) {
		return switch (compare) {
			case "log" -> CompareItemFunctions.isLog(itemStack);
			case "plank" -> CompareItemFunctions.isPlank(itemStack);
			case "chest" -> CompareItemFunctions.isChest(itemStack);
			case "stone" -> CompareItemFunctions.isStone(itemStack);
			case "slab" -> CompareItemFunctions.isSlab(itemStack);
			default -> false;
		};
	}
	
	public static int processLogCheck(List<ItemStack> itemStacks, int planksLeft) {
		return processCheck(itemStacks, planksLeft, "log", 4);
	}
	
	public static int processPlankCheck(List<ItemStack> itemStacks, int planksLeft) {
		return processCheck(itemStacks, planksLeft, "plank", 1);
	}
	
	public static int processChestCheck(List<ItemStack> itemStacks, int planksLeft) {
		return processCheck(itemStacks, planksLeft, "chest", 8);
	}
	
	public static int processStoneCheck(List<ItemStack> itemStacks, int stoneLeft) {
		return processCheck(itemStacks, stoneLeft, "stone", 1);
	}
	
	public static int processSlabCheck(List<ItemStack> itemStacks, int stoneLeft) {
		return processCheck(itemStacks, stoneLeft, "slab", 1);
	}

	public static boolean hasCurseOfVanishing(ItemStack itemStack) {
		return EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
	}

	public static List<ItemStack> getInventoryItems(Player player) {
		List<ItemStack> itemStacks = new ArrayList<>(player.getInventory().items);

		itemStacks.removeIf(Util::hasCurseOfVanishing);

		return itemStacks;
	}
	
	public static void failureMessage(Player player, int planksLeft, int stoneLeft, int planksNeeded, int stoneNeeded) {
		if (ConfigHandler.sendMessageOnCreationFailure) {
			String failureString = ConfigHandler.creationFailureMessage;
			failureString = failureString.replaceAll("%plankamount%", planksLeft + "").replaceAll("%stoneamount%", stoneLeft + "");
			
			MessageFunctions.sendMessage(player, failureString, ChatFormatting.RED, true);
		}
		
		Level level = player.level();
		Vec3 vec = player.position();
		
		if (planksLeft != planksNeeded) {
			ItemEntity planks = new ItemEntity(level, vec.x, vec.y+1, vec.z, new ItemStack(Items.OAK_PLANKS, planksNeeded-planksLeft));
			level.addFreshEntity(planks);
		}
		
		if (stoneLeft != stoneNeeded) {
			ItemEntity stones = new ItemEntity(level, vec.x, vec.y+1, vec.z, new ItemStack(Items.STONE, stoneNeeded-stoneLeft));
			level.addFreshEntity(stones);
		}

		deathCoordinatesMessage(player);
	}
	public static void successMessage(Player player) {
		if (ConfigHandler.sendMessageOnCreationSuccess) {
			MessageFunctions.sendMessage(player, ConfigHandler.creationSuccessMessage, ChatFormatting.DARK_GREEN, true);
		}

		deathCoordinatesMessage(player);
	}

	public static void deathCoordinatesMessage(Player player) {
		if (ConfigHandler.sendDeathCoordinatesInChat) {
			BlockPos pPos = player.blockPosition();
			String deathLocationString = " Death Coordinates; x: " + pPos.getX() + ", y: " + pPos.getY() + ", z: " + pPos.getZ() + ".";
			MessageFunctions.sendMessage(player, deathLocationString, ChatFormatting.GRAY);
		}
	}
}
