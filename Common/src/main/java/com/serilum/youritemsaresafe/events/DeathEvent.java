package com.serilum.youritemsaresafe.events;

import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.TaskFunctions;
import com.natamus.collective.functions.TileEntityFunctions;
import com.serilum.youritemsaresafe.config.ConfigHandler;
import com.serilum.youritemsaresafe.data.Constants;
import com.serilum.youritemsaresafe.data.Variables;
import com.serilum.youritemsaresafe.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DeathEvent {
	public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource, float damageAmount) {
		Level level = player.level();
		if (level.isClientSide()) {
			return;
		}

		if (player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}

		if (Util.isIgnoredDimension(level) || Util.isIgnoredPlayer(player)) {
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

		if (totalItemCount == 0 && !Util.isWearingGear(player)) {
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

		int highestChestY = level.getMaxY() - 1;
		if (deathPos.getY() > highestChestY) {
			deathPos = deathPos.atY(highestChestY);
		}
		else if (deathPos.getY() < level.getMinY()) {
			deathPos = deathPos.atY(level.getMinY());
		}


		if (ConfigHandler.mustHaveItemsInInventoryForCreation) {
			if (ConfigHandler.needChestMaterials || ConfigHandler.needArmorStandMaterials || ConfigHandler.needSignMaterials) {
				if (ConfigHandler.createArmorStand && ConfigHandler.addPlayerHeadToArmorStand && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
					totalItemCount += 1;
				}

				int stoneleft = 1; // 1 armor stand
				int planksleft = 0; // 1 chest, 1 armor stand

				if (ConfigHandler.needChestMaterials && totalItemCount > 0) {
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
			armourStand = new ArmorStand(EntityTypes.ARMOR_STAND, level);

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

		List<ItemStack> chestStacks = Util.takeItemStacks(itemStacks);
		Variables.pendingChestStacks.put(player.getUUID(), chestStacks);

		BlockPos finalDeathPos = deathPos;
		TaskFunctions.enqueueCollectiveServerTask(level.getServer(), () -> {
			Variables.pendingChestStacks.remove(player.getUUID());

			BlockPos chestPos = Util.getFreeChestPos(level, finalDeathPos, Util.getChestCount(chestStacks));
			int chestCount = Util.placeChestsWithItems(level, chestPos, chestStacks, playerName);

			if (armourStand != null) {
				armourStand.setPos(chestPos.getX()+0.5, chestPos.getY()+chestCount, chestPos.getZ()+0.5);
				armourStand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, DataFunctions.setBit(armourStand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS), 4, true));
				level.addFreshEntity(armourStand);
			}

			if (!chestStacks.isEmpty()) {
				Util.dropItemStacks(level, chestPos, chestStacks);
				Util.deathCoordinatesMessage(player);
				return;
			}

			Util.successMessage(player);

			if (ConfigHandler.createSignWithPlayerName && chestCount > 0) {
				Direction signDirection = Util.getFreeSignDirection(level, chestPos);
				if (signDirection == null) {
					return;
				}

				BlockPos signPos = chestPos.relative(signDirection).immutable();
				level.setBlockAndUpdate(signPos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, signDirection));

				BlockEntity blockEntity = level.getBlockEntity(signPos);
				if (!(blockEntity instanceof SignBlockEntity)) {
					return;
				}

				SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
				signBlockEntity.setText(signBlockEntity.getText(SignTextSlot.FRONT).asMutable().setLine(1, Component.literal(playerName)).asImmutable(), SignTextSlot.FRONT);
				TileEntityFunctions.updateTileEntity(level, signPos, signBlockEntity);
			}
		}, 1);
	}

	public static void onPlayerDrops(ServerPlayer player, Collection<ItemEntity> drops) {
		List<ItemStack> chestStacks = Variables.pendingChestStacks.get(player.getUUID());
		if (chestStacks == null) {
			return;
		}

		Iterator<ItemEntity> dropIterator = drops.iterator();
		while (dropIterator.hasNext() && chestStacks.size() < 54) {
			ItemEntity itemEntity = dropIterator.next();
			ItemStack itemStack = itemEntity.getItem();
			if (itemStack.isEmpty() || Util.hasCurseOfVanishing(itemStack)) {
				continue;
			}

			chestStacks.add(itemStack.copy());
			itemEntity.discard();
			dropIterator.remove();
		}
	}
}
