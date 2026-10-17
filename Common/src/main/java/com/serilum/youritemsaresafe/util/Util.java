package com.serilum.youritemsaresafe.util;

import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.CompareItemFunctions;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.MessageFunctions;
import com.serilum.youritemsaresafe.config.ConfigHandler;
import com.serilum.youritemsaresafe.data.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
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

	public static boolean isWearingGear(Player player) {
		for (EquipmentSlot slotType : Constants.slotTypes) {
			if (slotType.equals(EquipmentSlot.MAINHAND)) {
				continue;
			}

			ItemStack slotStack = player.getItemBySlot(slotType);
			if (!slotStack.isEmpty() && !hasCurseOfVanishing(slotStack)) {
				return true;
			}
		}

		return false;
	}

	public static List<ItemStack> takeItemStacks(List<ItemStack> itemStacks) {
		List<ItemStack> takenStacks = new ArrayList<>();
		for (ItemStack itemStack : itemStacks) {
			if (itemStack.isEmpty()) {
				continue;
			}

			if (takenStacks.size() == 54) {
				break;
			}

			takenStacks.add(itemStack.copy());
			itemStack.setCount(0);
		}

		return takenStacks;
	}

	public static int getChestCount(List<ItemStack> itemStacks) {
		if (itemStacks.isEmpty()) {
			return 0;
		}

		if (itemStacks.size() > 27) {
			return 2;
		}

		return 1;
	}

	public static boolean isFreeBlockPos(Level level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		return blockState.canBeReplaced() || CompareBlockFunctions.isAirOrOverwritableBlock(blockState.getBlock());
	}

	public static boolean isFreeChestPos(Level level, BlockPos pos, int chestCount) {
		for (int i = 0; i < chestCount; i++) {
			BlockPos chestPos = pos.above(i);
			if (level.isOutsideBuildHeight(chestPos) || !isFreeBlockPos(level, chestPos)) {
				return false;
			}
		}

		return true;
	}

	public static BlockPos getFreeChestPos(Level level, BlockPos deathPos, int chestCount) {
		if (isFreeChestPos(level, deathPos, chestCount)) {
			return deathPos;
		}

		BlockPos closestPos = null;
		for (BlockPos possiblePos : BlockPos.betweenClosed(deathPos.offset(-3, -3, -3), deathPos.offset(3, 3, 3))) {
			if (closestPos != null && possiblePos.distSqr(deathPos) >= closestPos.distSqr(deathPos)) {
				continue;
			}

			if (isFreeChestPos(level, possiblePos, chestCount)) {
				closestPos = possiblePos.immutable();
			}
		}

		if (closestPos == null) {
			return deathPos;
		}

		return closestPos;
	}

	public static boolean isInConfigList(String configList, String value) {
		for (String entry : configList.split(",")) {
			if (entry.strip().equalsIgnoreCase(value)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isIgnoredDimension(Level level) {
		return ConfigFunctions.isInIdList(ConfigHandler.dimensionsToIgnore, level.dimension().location().toString());
	}

	public static boolean isIgnoredPlayer(Player player) {
		return isInConfigList(ConfigHandler.playerNamesToIgnore, player.getName().getString());
	}

	public static void nameChest(ChestBlockEntity chestEntity, String playerName) {
		ItemStack namedChestStack = new ItemStack(Items.CHEST);
		namedChestStack.set(DataComponents.CUSTOM_NAME, Component.literal(playerName));
		chestEntity.applyComponentsFromItemStack(namedChestStack);
	}

	public static int placeChestsWithItems(Level level, BlockPos chestPos, List<ItemStack> itemStacks, String playerName) {
		BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);

		int chestCount = 0;
		while (!itemStacks.isEmpty() && chestCount < 2) {
			BlockPos pos = chestPos.above(chestCount);
			if (level.isOutsideBuildHeight(pos)) {
				break;
			}

			if (!isFreeBlockPos(level, pos)) {
				level.destroyBlock(pos, true);
			}

			level.setBlock(pos, chestState, 3);

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof ChestBlockEntity)) {
				break;
			}

			ChestBlockEntity chestEntity = (ChestBlockEntity)blockEntity;
			if (ConfigHandler.nameChestWithPlayerName) {
				nameChest(chestEntity, playerName);
			}

			for (int slot = 0; slot < chestEntity.getContainerSize() && !itemStacks.isEmpty(); slot++) {
				chestEntity.setItem(slot, itemStacks.remove(0));
			}

			chestCount += 1;
		}

		return chestCount;
	}

	public static void dropItemStacks(Level level, BlockPos pos, List<ItemStack> itemStacks) {
		for (ItemStack itemStack : itemStacks) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
		}

		itemStacks.clear();
	}

	public static Direction getFreeSignDirection(Level level, BlockPos chestPos) {
		for (Direction direction : Arrays.asList(Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.NORTH)) {
			if (isFreeBlockPos(level, chestPos.relative(direction))) {
				return direction;
			}
		}

		return null;
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
