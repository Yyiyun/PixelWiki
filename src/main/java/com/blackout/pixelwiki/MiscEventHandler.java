package com.blackout.pixelwiki;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.ForceEndBattleEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;

public class MiscEventHandler {
	static int pokeDollarsLostOnDeathPercent = 10;
	static double pokeDollarsLostOnDeathPercentAsDecimal = pokeDollarsLostOnDeathPercent / 100.0;
	static int pokeDollarsLostOnWhiteoutPercent = 50;
	static double pokeDollarsLostOnWhiteoutPercentAsDecimal = pokeDollarsLostOnWhiteoutPercent / 100.0;
	private static final DecimalFormat df = new DecimalFormat("0.00");

	@SubscribeEvent
	public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
		new WikiCommand(commandDispatcher);
	}

	@SubscribeEvent
	public static void onPlayerDeathEvent(LivingDeathEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if (livingEntity instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = ((ServerPlayerEntity) livingEntity).connection.player;
			BankAccount userAccount = BankAccountProxy.getBankAccount(player).orElseThrow(() -> new NullPointerException("bank account"));
			double balance = userAccount.getBalance().doubleValue();
			double removeBalance = balance * pokeDollarsLostOnDeathPercentAsDecimal;
			userAccount.take(removeBalance);
			player.sendMessage(new TranslationTextComponent("pixelwiki.pokedollars.lost.died").withStyle(TextFormatting.DARK_GREEN).append(new StringTextComponent(df.format(removeBalance)).withStyle(TextFormatting.GREEN).append(new TranslationTextComponent("pixelwiki.pokedollars.lost.message_end").withStyle(TextFormatting.DARK_GREEN))), Util.NIL_UUID);
		}
	}

	@SubscribeEvent
	public static void onEndBattleEvent(BattleEndEvent event) {
		PixelWiki.LOGGER.debug("Hello: 1");
		boolean pvpBattle = (event.getPlayers().size() > 1);
		for (ServerPlayerEntity player : event.getPlayers()) {
			PixelWiki.LOGGER.debug("Hello: 2");
			boolean lostBattle = true;
			for (Pokemon pokemon : StorageProxy.getParty(player.getUUID()).getAll()) {
				PixelWiki.LOGGER.debug("Hello: 3");
				if (pokemon != null && pokemon.getHealth() > 0) {
					PixelWiki.LOGGER.debug("Hello: 4");
					lostBattle = false;
					break;
				}
			}
			PixelWiki.LOGGER.debug("Hello: 5");
			if (lostBattle && !pvpBattle) {
				PixelWiki.LOGGER.debug("Hello: 6");
				playerWhitedOut(player);
			}
		}
	}

	@SubscribeEvent
	public static void onForceEndBattleEvent(ForceEndBattleEvent event) {
		PixelWiki.LOGGER.debug("Hello: 1");
		boolean pvpBattle = (event.getPlayers().size() > 1);
		for (ServerPlayerEntity player : event.getPlayers()) {
			PixelWiki.LOGGER.debug("Hello: 2");
			boolean lostBattle = true;
			for (Pokemon pokemon : StorageProxy.getParty(player.getUUID()).getAll()) {
				PixelWiki.LOGGER.debug("Hello: 3");
				if (pokemon != null && pokemon.getHealth() > 0) {
					PixelWiki.LOGGER.debug("Hello: 4");
					lostBattle = false;
					break;
				}
			}
			PixelWiki.LOGGER.debug("Hello: 5");
			if (lostBattle && !pvpBattle) {
				PixelWiki.LOGGER.debug("Hello: 6");
				playerWhitedOut(player);
			}
		}
	}

	private static void playerWhitedOut(ServerPlayerEntity player) {
		PixelWiki.LOGGER.debug("Hello: 7");
		BankAccount userAccount = BankAccountProxy.getBankAccount(player).orElseThrow(() -> new NullPointerException("bank account"));
		double balance = userAccount.getBalance().doubleValue();
		double removeBalance = balance * pokeDollarsLostOnWhiteoutPercentAsDecimal;
		PixelWiki.LOGGER.debug("Hello: 8");
		userAccount.take(removeBalance);
		player.sendMessage(new TranslationTextComponent("pixelwiki.pokedollars.lost.whiteout").withStyle(TextFormatting.DARK_GREEN).append(new StringTextComponent(df.format(removeBalance)).withStyle(TextFormatting.GREEN).append(new TranslationTextComponent("pixelwiki.pokedollars.lost.message_end").withStyle(TextFormatting.DARK_GREEN))), Util.NIL_UUID);
	}
}
