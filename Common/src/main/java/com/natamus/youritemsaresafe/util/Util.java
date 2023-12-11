package com.natamus.youritemsaresafe.util;

import com.natamus.collective.functions.CompareItemFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.youritemsaresafe.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Util {
	public static int processCheck(List<ItemStack> itemstacks, int itemsleft, String compare, int decrease) {
		for (ItemStack itemstack : itemstacks) {
			if (itemsleft <= 0) {
				break;
			}
			
			int count = itemstack.getCount();
			if (comparePassed(compare, itemstack)) {
				while (count > 0 && itemsleft > 0) {
					itemsleft -= decrease;
					count -= 1;
					itemstack.setCount(count);
				}
			}
		}
		
		return itemsleft;		
	}
	
	public static boolean comparePassed(String compare, ItemStack itemstack) {
		return switch (compare) {
			case "log" -> CompareItemFunctions.isLog(itemstack);
			case "plank" -> CompareItemFunctions.isPlank(itemstack);
			case "chest" -> CompareItemFunctions.isChest(itemstack);
			case "stone" -> CompareItemFunctions.isStone(itemstack);
			case "slab" -> CompareItemFunctions.isSlab(itemstack);
			default -> false;
		};
	}
	
	public static int processLogCheck(List<ItemStack> itemstacks, int planksleft) {
		return processCheck(itemstacks, planksleft, "log", 4);
	}
	
	public static int processPlankCheck(List<ItemStack> itemstacks, int planksleft) {
		return processCheck(itemstacks, planksleft, "plank", 1);
	}
	
	public static int processChestCheck(List<ItemStack> itemstacks, int planksleft) {
		return processCheck(itemstacks, planksleft, "chest", 8);
	}
	
	public static int processStoneCheck(List<ItemStack> itemstacks, int stoneleft) {
		return processCheck(itemstacks, stoneleft, "stone", 1);
	}
	
	public static int processSlabCheck(List<ItemStack> itemstacks, int stoneleft) {
		return processCheck(itemstacks, stoneleft, "slab", 1);
	}
	
	public static void failureMessage(Player player, int planksleft, int stoneleft, int planksneeded, int stoneneeded) {
		if (ConfigHandler.sendMessageOnCreationFailure) {
			String failurestring = ConfigHandler.creationFailureMessage;
			failurestring = failurestring.replaceAll("%plankamount%", planksleft + "").replaceAll("%stoneamount%", stoneleft + "");
			
			StringFunctions.sendMessage(player, failurestring, ChatFormatting.RED);
		}
		
		Level world = player.getCommandSenderWorld();
		Vec3 vec = player.position();
		
		if (planksleft != planksneeded) {
			ItemEntity planks = new ItemEntity(world, vec.x, vec.y+1, vec.z, new ItemStack(Items.OAK_PLANKS, planksneeded-planksleft));
			world.addFreshEntity(planks);
		}
		
		if (stoneleft != stoneneeded) {
			ItemEntity stones = new ItemEntity(world, vec.x, vec.y+1, vec.z, new ItemStack(Items.STONE, stoneneeded-stoneleft));
			world.addFreshEntity(stones);
		}
	}
	public static void successMessage(Player player) {
		if (ConfigHandler.sendMessageOnCreationSuccess) {
			StringFunctions.sendMessage(player, ConfigHandler.creationSuccessMessage, ChatFormatting.DARK_GREEN);
		}		
	}
}
