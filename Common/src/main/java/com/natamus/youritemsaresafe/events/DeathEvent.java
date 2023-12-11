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
		Level level = player.level;

		int chestcount = 1;
		String playername = player.getName().getString();
		
		List<ItemStack> itemstacks = new ArrayList<>(player.getInventory().items);
		
		int totalitemcount = 0;
		for (ItemStack itemstack : itemstacks) {
			if (!itemstack.isEmpty()) {
				totalitemcount += 1;
			}
		}
		
		if (!ConfigHandler.createArmorStand) {
			for (EquipmentSlot slottype : slotTypes) {
				if (!player.getItemBySlot(slottype).isEmpty()) {
					totalitemcount += 1;
				}
			}
			
			if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				totalitemcount += 1;
			}
		}
		
		if (totalitemcount == 0) {
			return;
		}

		if (Constants.inventoryTotemLoaded) {
			for (ItemStack inventoryStack : itemstacks) {
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
					totalitemcount += 1;
				}

				int stoneleft = 1; // 1 armor stand
				int planksleft = 0; // 1 chest, 1 armor stand

				if (ConfigHandler.needChestMaterials) {
					planksleft += 8;
					if (totalitemcount > 27) {
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

				planksleft = Util.processLogCheck(itemstacks, planksleft);

				if (planksleft > 0) {
					planksleft = Util.processPlankCheck(itemstacks, planksleft);
				}
				if (planksleft > 0) {
					planksleft = Util.processChestCheck(itemstacks, planksleft);
				}

				if (planksleft > 0) {
					Util.failureMessage(player, planksleft, stoneleft, planksneeded, stoneneeded);
					return;
				}

				if (stoneleft > 0) {
					stoneleft = Util.processStoneCheck(itemstacks, stoneleft);
				}
				if (stoneleft > 0) {
					stoneleft = Util.processSlabCheck(itemstacks, stoneleft);
				}

				if (stoneleft > 0) {
					Util.failureMessage(player, planksleft, stoneleft, planksneeded, stoneneeded);
					return;
				}
			}
		}
		
		BlockPos deathpos = player.blockPosition().immutable();
		if (CompareBlockFunctions.isAirOrOverwritableBlock(level.getBlockState(deathpos.below()).getBlock())) {
			deathpos = deathpos.below().immutable();
		}
	
		ArmorStand armorstand = null;

		List<EquipmentSlot> localSlotTypes = new ArrayList<EquipmentSlot>(slotTypes);
		if (ConfigHandler.createArmorStand) {
			ItemStack helmetStack = null;
			armorstand = new ArmorStand(EntityType.ARMOR_STAND, level);

			if (ConfigHandler.addPlayerHeadToArmorStand) {
				ItemStack headstack = HeadFunctions.getPlayerHead(playername, 1);

				if (headstack != null) {
					if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
						helmetStack = player.getItemBySlot(EquipmentSlot.HEAD).copy();
						player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					}

					armorstand.setItemSlot(EquipmentSlot.HEAD, headstack);
					localSlotTypes.remove(EquipmentSlot.HEAD);
				}
			}
	
			for (EquipmentSlot slottype : localSlotTypes) {
				armorstand.setItemSlot(slottype, player.getItemBySlot(slottype).copy());
				player.setItemSlot(slottype, ItemStack.EMPTY);
			}

			if (helmetStack != null) {
				armorstand.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD).copy());
				player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
			}

			itemstacks = new ArrayList<>(player.getInventory().items);
		}
		else {
			for (EquipmentSlot slottype : localSlotTypes) {
				if (slottype.equals(EquipmentSlot.MAINHAND)) {
					continue;
				}

				itemstacks.add(player.getItemBySlot(slottype).copy());
				player.setItemSlot(slottype, ItemStack.EMPTY);
			}
			
			itemstacks.add(player.getItemBySlot(EquipmentSlot.HEAD).copy());
			player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
		}
		
		BlockState cheststate = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestBlockEntity chestentity = new ChestBlockEntity(deathpos, cheststate);
		level.setBlock(deathpos, cheststate, 3);
		level.setBlockEntity(chestentity);
		
		BlockPos deathposup = new BlockPos(deathpos.getX(), deathpos.getY()+1, deathpos.getZ());
		ChestBlockEntity chestentitytwo = new ChestBlockEntity(deathposup, cheststate);
		
		int i = 0;
		for (ItemStack itemstack : itemstacks) {
			if (itemstack.isEmpty()) {
				continue;
			}
			
			if (i < 27) {
				chestentity.setItem(i, itemstack.copy());
				itemstack.setCount(0);
			}
			else if (i >= 27) {
				if (chestcount == 1) {
					chestcount+=1;
					level.setBlock(deathposup, cheststate, 3);
					level.setBlockEntity(chestentitytwo);
				}

				if (i-27 > 26) {
					break;
				}
				
				chestentitytwo.setItem(i-27, itemstack.copy());
				itemstack.setCount(0);
			}
			
			i+=1;
		}
		
		if (armorstand != null) {
			armorstand.setPos(deathpos.getX()+0.5, deathpos.getY()+chestcount, deathpos.getZ()+0.5);
			armorstand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, DataFunctions.setBit(armorstand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS), 4, true));
			level.addFreshEntity(armorstand);
		}
		
		Util.successMessage(player);
		
		if (ConfigHandler.createSignWithPlayerName) {
			BlockPos signpos = deathpos.south().immutable();
			level.setBlockAndUpdate(signpos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH));
			
			BlockEntity te = level.getBlockEntity(signpos);
			if (!(te instanceof SignBlockEntity)) {
				return;
			}
			
			SignBlockEntity signentity = (SignBlockEntity)te;
			signentity.setMessage(1, Component.literal(playername));
			TileEntityFunctions.updateTileEntity(level, signpos, signentity);
		}
	}
}
