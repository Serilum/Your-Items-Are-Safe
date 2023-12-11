package com.natamus.youritemsaresafe.events;

import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.HeadFunctions;
import com.natamus.collective.functions.TileEntityFunctions;
import com.natamus.youritemsaresafe.config.ConfigHandler;
import com.natamus.youritemsaresafe.data.Constants;
import com.natamus.youritemsaresafe.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathEvent {
	private static final List<EquipmentSlot> slotTypes = new ArrayList<EquipmentSlot>(Arrays.asList(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
	
	public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource, float damageAmount) {
		Level level = player.level();

		int chestCount = 1;
		String playerName = player.getName().getString();
		
		List<ItemStack> itemStacks = new ArrayList<>(player.getInventory().items);
		
		int totalItemCount = 0;
		for (ItemStack itemStack : itemStacks) {
			if (!itemStack.isEmpty()) {
				totalItemCount += 1;
			}
		}
		
		if (!ConfigHandler.createArmorStand) {
			for (EquipmentSlot slotType : slotTypes) {
				if (!player.getItemBySlot(slotType).isEmpty()) {
					totalItemCount += 1;
				}
			}
			
			if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				totalItemCount += 1;
			}
		}
		
		if (totalItemCount == 0) {
			return;
		}

		if (Constants.inventoryTotemLoaded) {
			for (ItemStack inventoryStack : itemStacks) {
				if (inventoryStack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
					return;
				}
			}
		}

		if (player.getMainHandItem().getItem().equals(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().getItem().equals(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		if (ConfigHandler.mustHaveItemsInInventoryForCreation) {
			if (ConfigHandler.needChestMaterials || ConfigHandler.needArmorStandMaterials || ConfigHandler.needSignMaterials) {
				if (ConfigHandler.createArmorStand && ConfigHandler.addPlayerHeadToArmorStand && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
					totalItemCount += 1;
				}

				int stoneleft = 1; // 1 armor stand
				int planksleft = 0; // 1 chest, 1 armor stand

				if (ConfigHandler.needChestMaterials) {
					planksleft += 8;
					if (totalItemCount > 27) {
						planksleft += 8;
					}
				}

				if (ConfigHandler.createArmorStand && ConfigHandler.needArmorStandMaterials) {
					planksleft += 3;
				}

				if (ConfigHandler.createSignWithPlayerName && ConfigHandler.needSignMaterials) {
					planksleft += 7;
				}

				if (ConfigHandler.ignoreStoneMaterialNeed) {
					stoneleft = 0;
				}

				int planksneeded = planksleft;
				int stoneneeded = stoneleft;

				planksleft = Util.processLogCheck(itemStacks, planksleft);

				if (planksleft > 0) {
					planksleft = Util.processPlankCheck(itemStacks, planksleft);
				}
				if (planksleft > 0) {
					planksleft = Util.processChestCheck(itemStacks, planksleft);
				}

				if (planksleft > 0) {
					Util.failureMessage(player, planksleft, stoneleft, planksneeded, stoneneeded);
					return;
				}

				if (stoneleft > 0) {
					stoneleft = Util.processStoneCheck(itemStacks, stoneleft);
				}
				if (stoneleft > 0) {
					stoneleft = Util.processSlabCheck(itemStacks, stoneleft);
				}

				if (stoneleft > 0) {
					Util.failureMessage(player, planksleft, stoneleft, planksneeded, stoneneeded);
					return;
				}
			}
		}
		
		BlockPos deathPos = player.blockPosition().atY((int)Math.ceil(player.position().y)).immutable();
		if (CompareBlockFunctions.isAirOrOverwritableBlock(level.getBlockState(deathPos.below()).getBlock())) {
			deathPos = deathPos.below().immutable();
		}
	
		ArmorStand armourStand = null;

		List<EquipmentSlot> localSlotTypes = new ArrayList<EquipmentSlot>(slotTypes);
		if (ConfigHandler.createArmorStand) {
			ItemStack helmetStack = null;
			armourStand = new ArmorStand(EntityType.ARMOR_STAND, level);

			if (ConfigHandler.addPlayerHeadToArmorStand) {
				ItemStack headStack = HeadFunctions.getPlayerHead(playerName, 1);

				if (headStack != null) {
					if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
						helmetStack = player.getItemBySlot(EquipmentSlot.HEAD).copy();
						player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					}

					armourStand.setItemSlot(EquipmentSlot.HEAD, headStack);
					localSlotTypes.remove(EquipmentSlot.HEAD);
				}
			}
	
			for (EquipmentSlot slotType : localSlotTypes) {
				ItemStack slotStack = player.getItemBySlot(slotType).copy();
				if (!slotStack.isEmpty()) {
					armourStand.setItemSlot(slotType, slotStack);
					player.setItemSlot(slotType, ItemStack.EMPTY);
				}
			}

			itemStacks = new ArrayList<>(player.getInventory().items);

			if (helmetStack != null) {
				itemStacks.add(helmetStack);
			}
		}
		else {
			for (EquipmentSlot slotType : localSlotTypes) {
				if (slotType.equals(EquipmentSlot.MAINHAND)) {
					continue;
				}

				ItemStack slotStack = player.getItemBySlot(slotType).copy();
				if (!slotStack.isEmpty()) {
					itemStacks.add(slotStack);
					player.setItemSlot(slotType, ItemStack.EMPTY);
				}
			}
		}
		
		BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestBlockEntity chestEntity = new ChestBlockEntity(deathPos, chestState);
		level.setBlock(deathPos, chestState, 3);
		level.setBlockEntity(chestEntity);
		
		BlockPos deathPosUp = new BlockPos(deathPos.getX(), deathPos.getY()+1, deathPos.getZ());
		ChestBlockEntity chestEntityTwo = new ChestBlockEntity(deathPosUp, chestState);
		
		int i = 0;
		for (ItemStack itemStack : itemStacks) {
			if (itemStack.isEmpty()) {
				continue;
			}
			
			if (i < 27) {
				chestEntity.setItem(i, itemStack.copy());
				itemStack.setCount(0);
			}
			else if (i >= 27) {
				if (chestCount == 1) {
					chestCount+=1;
					level.setBlock(deathPosUp, chestState, 3);
					level.setBlockEntity(chestEntityTwo);
				}

				if (i-27 > 26) {
					break;
				}
				
				chestEntityTwo.setItem(i-27, itemStack.copy());
				itemStack.setCount(0);
			}
			
			i+=1;
		}
		
		if (armourStand != null) {
			armourStand.setPos(deathPos.getX()+0.5, deathPos.getY()+chestCount, deathPos.getZ()+0.5);
			armourStand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, DataFunctions.setBit(armourStand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS), 4, true));
			level.addFreshEntity(armourStand);
		}
		
		Util.successMessage(player);
		
		if (ConfigHandler.createSignWithPlayerName) {
			BlockPos signPos = deathPos.south().immutable();
			level.setBlockAndUpdate(signPos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH));
			
			BlockEntity te = level.getBlockEntity(signPos);
			if (!(te instanceof SignBlockEntity)) {
				return;
			}
			
			SignBlockEntity signEntity = (SignBlockEntity)te;
			signEntity.setText(signEntity.getFrontText().setMessage(1, Component.literal(playerName)), true);
			TileEntityFunctions.updateTileEntity(level, signPos, signEntity);
		}
	}
}
