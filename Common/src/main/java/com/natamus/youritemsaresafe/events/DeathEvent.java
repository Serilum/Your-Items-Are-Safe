package com.natamus.youritemsaresafe.events;

import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.TaskFunctions;
import com.natamus.collective.functions.TileEntityFunctions;
import com.natamus.youritemsaresafe.config.ConfigHandler;
import com.natamus.youritemsaresafe.data.Constants;
import com.natamus.youritemsaresafe.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import java.util.List;

public class DeathEvent {
	public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource, float damageAmount) {
		Level level = player.level();
		if (level.isClientSide) {
			return;
		}

		String playerName = player.getName().getString();
		
		List<ItemStack> itemStacks = Util.getInventoryItems(player);
		
		int totalItemCount = 0;
		for (ItemStack itemStack : itemStacks) {
			if (!itemStack.isEmpty()) {
				totalItemCount += 1;
			}
		}
		
		if (!ConfigHandler.createArmorStand) {
			for (EquipmentSlot slotType : Constants.slotTypes) {
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


		BlockPos deathPos = player.blockPosition().atY((int)Math.ceil(player.position().y)).immutable();
		if (deathPos.below().getY() >= level.getMinY() && CompareBlockFunctions.isAirOrOverwritableBlock(level.getBlockState(deathPos.below()).getBlock())) {
			deathPos = deathPos.below().immutable();
		}

		boolean isVoidDeath = deathPos.getY() < (level.getMinY() - 32);


		if (!isVoidDeath) {
			if (Constants.inventoryTotemModIsLoaded) {
				for (ItemStack inventoryStack : itemStacks) {
					if (inventoryStack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
						return;
					}
				}
			}

			if (player.getMainHandItem().getItem().equals(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().getItem().equals(Items.TOTEM_OF_UNDYING)) {
				return;
			}
		}
		else { // Void Death
			if (!ConfigHandler.createChestAboveVoid) {
				return;
			}

			int y;
			for (y = level.getMinY(); y < level.getMaxY(); y++) {
				BlockPos possiblePos = new BlockPos(deathPos.getX(), y, deathPos.getZ());
				if (CompareBlockFunctions.isAirOrOverwritableBlock(level.getBlockState(possiblePos).getBlock())) {
					deathPos = possiblePos.immutable();
					break;
				}
			}

			if (ConfigHandler.createVoidPlatform) {
				deathPos = deathPos.above().immutable();

				for (BlockPos platformPos : BlockPos.betweenClosed(deathPos.getX()-1, y, deathPos.getZ()-1, deathPos.getX()+1, y, deathPos.getZ()+1)) {
					if (CompareBlockFunctions.isAirOrOverwritableBlock(level.getBlockState(platformPos).getBlock())) {
						level.setBlock(platformPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
					}
				}
			}
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

					planksleft = Util.processChestCheck(itemStacks, planksleft);
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

				if (planksleft > 0) {
					planksleft = Util.processLogCheck(itemStacks, planksleft);
				}
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
	
		ArmorStand armourStand;

		List<EquipmentSlot> localSlotTypes = new ArrayList<EquipmentSlot>(Constants.slotTypes);
		if (ConfigHandler.createArmorStand) {
			ItemStack helmetStack = null;
			armourStand = new ArmorStand(EntityType.ARMOR_STAND, level);

			if (ConfigHandler.addPlayerHeadToArmorStand) {
				ItemStack headStack = PlayerHeadCacheFeature.getPlayerHeadStackFromCache(player);
				if (headStack == null) {
					headStack = PlayerHeadCacheFeature.getPlayerHeadStackFromCache((ServerLevel)level, player.getName().getString());
				}

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
					if (Util.hasCurseOfVanishing(slotStack)) {
						continue;
					}

					armourStand.setItemSlot(slotType, slotStack);
					player.setItemSlot(slotType, ItemStack.EMPTY);
				}
			}

			itemStacks = Util.getInventoryItems(player);

			if (helmetStack != null) {
				if (!Util.hasCurseOfVanishing(helmetStack)) {
					itemStacks.add(helmetStack);
				}
			}
		}
		else {
			armourStand = null;
			for (EquipmentSlot slotType : localSlotTypes) {
				if (slotType.equals(EquipmentSlot.MAINHAND)) {
					continue;
				}

				ItemStack slotStack = player.getItemBySlot(slotType).copy();
				if (!slotStack.isEmpty()) {
					if (Util.hasCurseOfVanishing(slotStack)) {
						continue;
					}

					itemStacks.add(slotStack);
					player.setItemSlot(slotType, ItemStack.EMPTY);
				}
			}
		}

		BlockPos finalDeathPos = deathPos;
		List<ItemStack> finalItemStacks = itemStacks;
		TaskFunctions.enqueueCollectiveTask(level.getServer(), () -> {
			int chestCount = 1;

			BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
			ChestBlockEntity chestEntity = new ChestBlockEntity(finalDeathPos, chestState);
			level.setBlock(finalDeathPos, chestState, 3);
			level.setBlockEntity(chestEntity);

			BlockPos deathPosUp = new BlockPos(finalDeathPos.getX(), finalDeathPos.getY()+1, finalDeathPos.getZ());
			ChestBlockEntity chestEntityTwo = new ChestBlockEntity(deathPosUp, chestState);

			int i = 0;
			for (ItemStack itemStack : finalItemStacks) {
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
				armourStand.setPos(finalDeathPos.getX()+0.5, finalDeathPos.getY()+chestCount, finalDeathPos.getZ()+0.5);
				armourStand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, DataFunctions.setBit(armourStand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS), 4, true));
				level.addFreshEntity(armourStand);
			}

			Util.successMessage(player);

			if (ConfigHandler.createSignWithPlayerName) {
				BlockPos signPos = finalDeathPos.south().immutable();
				level.setBlockAndUpdate(signPos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH));

				BlockEntity blockEntity = level.getBlockEntity(signPos);
				if (!(blockEntity instanceof SignBlockEntity)) {
					return;
				}

				SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
				signBlockEntity.setText(signBlockEntity.getFrontText().setMessage(1, Component.literal(playerName)), true);
				TileEntityFunctions.updateTileEntity(level, signPos, signBlockEntity);
			}
		}, 1);
	}
}
