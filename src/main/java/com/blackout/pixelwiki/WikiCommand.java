package com.blackout.pixelwiki;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.*;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.InteractEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.TradeEvolution;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.api.world.WorldTime;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.comm.CommandChatHandler;
import com.pixelmonmod.pixelmon.command.PixelCommand;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class WikiCommand extends PixelCommand {
	private static final List<String> ALIASES = Lists.newArrayList("pinfo", "pokeinfo");
	private static final List<String> info = Lists.newArrayList("moves", "levelupmoves", "eggmoves", "tmtrhmmoves", "tutormoves", "genderratio", "evyield", "evdrop",
			"evolution", "evo", "abilities", "hiddenability", "ha", "spawn", "basestats", "stats", "egggroup", "catchrate", "catch", "rarity", "biome", "time", "weather",
			"grass", "caverock", "headbutt", "rocksmash", "sweetscent", "fishing", "moonphase", "yheight", "ylevel", "y", "lightlevel", "light", "preevolution", "preevo",
			"nationalpokedex", "nationaldex", "pokedex", "dex", "drops");

	public WikiCommand(CommandDispatcher<CommandSource> dispatcher) {
		super(dispatcher, "wiki", "/wiki <pokemon> [form] <info> - displays the <info> of <pokemon>", 0);
	}

	@Override
	public List<String> getAliases() {
		return ALIASES;
	}

	@Override
	public void execute(CommandSource commandSource, String[] args) throws CommandException, CommandSyntaxException {
		if (args.length == 0) {
			commandSource.sendSuccess(PixelmonCommandUtils.format(TextFormatting.RED, "pixelmon.command.general.invalid"), false);
			PixelmonCommandUtils.endCommand(this.getUsage(commandSource));
		}

		Optional<Species> species = PixelmonSpecies.fromNameOrDex(args[0]);
		if (!species.isPresent()) {
			PixelmonCommandUtils.endCommand("pixelmon.command.movelist.pokemonnotfound", args[0]);
		}

		if (args.length == 1) {
			commandSource.sendSuccess(PixelmonCommandUtils.format(TextFormatting.RED, "pixelmon.command.general.invalid"), false);
			PixelmonCommandUtils.endCommand(this.getUsage(commandSource));
		}

		Pokemon pokemon = PokemonFactory.create(species.get());
		if (args[1].contains("form:")) {
			pokemon.setForm(args[1].replaceAll("form:", ""));
		} else {
			pokemon.setForm(pokemon.getForm().getDefaultBaseForm());
		}

		sendInfo(pokemon, commandSource, args);
	}

	public List<String> getTabCompletions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) throws CommandSyntaxException {
		if (args.length == 1) {
			return PixelmonCommandUtils.tabCompletePokemon();
		}
		if (args.length == 2) {
			return info.subList(0, info.size());
		}
		if (args.length == 3) {
			return info.subList(0, info.size());
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	private void sendInfo(Pokemon pokemon, CommandSource commandSource, String[] args) throws CommandSyntaxException {
		String s = args[1].replaceAll("form:", "").replaceAll("palette:", "");
		String s1 = String.valueOf(s.charAt(0)).toUpperCase();
		String s2 = s.substring(1);
		String s3 = s1 + s2;
		if (args[1].equals("moves") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("moves"))) {
			List<String> moveNames = pokemon.getForm().getMoves().getAllMoves().stream().map(ImmutableAttack::getAttackName).collect(Collectors.toList());
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Move List").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(StringUtils.join(moveNames, ", ")).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("levelupmoves") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("levelupmoves"))) {
			List<String> levelUpMoveNames = pokemon.getForm().getMoves().getAllLevelUpMoves().stream().map(ImmutableAttack::getAttackName).collect(Collectors.toList());
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Level Up Move List").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(StringUtils.join(levelUpMoveNames, ", ")).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("eggmoves") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("eggmoves"))) {
			List<String> eggMoveNames = pokemon.getForm().getMoves().getEggMoves().stream().map(ImmutableAttack::getAttackName).collect(Collectors.toList());
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Egg Move List").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(StringUtils.join(eggMoveNames, ", ")).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("tmtrhmmoves") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("tmtrhmmoves"))) {
			List<String> tmtrhmMoveNames = pokemon.getForm().getMoves().getAllTMTRHMMoves().stream().map(ImmutableAttack::getAttackName).collect(Collectors.toList());
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s TM/TR/HM Move List").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(StringUtils.join(tmtrhmMoveNames, ", ")).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("tutormoves") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("tutormoves"))) {
			List<String> tutorMoveNames = pokemon.getForm().getMoves().getTutorMoves().stream().map(ImmutableAttack::getAttackName).collect(Collectors.toList());
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Tutor Move List").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(StringUtils.join(tutorMoveNames, ", ")).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("genderratio") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("genderratio"))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Gender Ratio", pokemon.getDisplayName()).withStyle(TextFormatting.DARK_GREEN)), false);
			if (pokemon.getForm().isMaleOnly()) {
				commandSource.sendSuccess(new TranslationTextComponent("Male: ").withStyle(TextFormatting.DARK_GREEN).append(new StringTextComponent(String.valueOf(pokemon.getForm().getMalePercentage())).withStyle(TextFormatting.GREEN)), false);
			} else if (pokemon.getForm().isFemaleOnly()) {
				commandSource.sendSuccess(new TranslationTextComponent("Female: ").withStyle(TextFormatting.DARK_GREEN).append(new StringTextComponent(String.valueOf((100 - pokemon.getForm().getMalePercentage()))).withStyle(TextFormatting.GREEN)), false);
			} else {
				commandSource.sendSuccess(new TranslationTextComponent("Male: ").withStyle(TextFormatting.DARK_GREEN).append(new StringTextComponent(String.valueOf(pokemon.getForm().getMalePercentage())).withStyle(TextFormatting.GREEN)).append(new StringTextComponent(", Female: ").withStyle(TextFormatting.DARK_GREEN)).append(new StringTextComponent(String.valueOf(100 - pokemon.getForm().getMalePercentage())).withStyle(TextFormatting.GREEN)), false);
			}
		} else if ((args[1].equals("preevolution") || args[1].equals("preevo")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("preevolution") || args[2].equals("preevo")))) {
			if (!pokemon.getForm().getPreEvolutions().isEmpty()) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Preevolution", pokemon.getDisplayName()).withStyle(TextFormatting.DARK_GREEN)), false);
				commandSource.sendSuccess(new StringTextComponent(String.valueOf(pokemon.getForm().getPreEvolutions())).withStyle(TextFormatting.GREEN), false);
			} else {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new StringTextComponent(" does not have a preevolution.").withStyle(TextFormatting.DARK_GREEN)), false);
			}
		} else if ((args[1].equals("nationalpokedex") || args[1].equals("nationaldex") || args[1].equals("pokedex") || args[1].equals("dex")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("nationalpokedex") || args[2].equals("nationaldex") || args[2].equals("pokedex") || args[2].equals("dex")))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s National Pokedex Number", pokemon.getDisplayName()).withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(pokemon.getSpecies().getDex())).withStyle(TextFormatting.GREEN), false);
		} else if ((args[1].equals("evyield") || args[1].equals("evdrop")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("evyield") || args[2].equals("evdrop")))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s EV Yields").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("HP: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.HP))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Attack: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.ATTACK))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Defense: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.DEFENSE))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Sp. Attack: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.SPECIAL_ATTACK))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Sp. Defense: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.SPECIAL_DEFENSE))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Speed: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getEVYields().getYield(BattleStatsType.SPEED))).withStyle(TextFormatting.GREEN)), false);
		} else if ((args[1].equals("evo") || args[1].equals("evolution")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("evo") || args[2].equals("evolution")))) {
			List<Evolution> evolutions = (pokemon.getForm()).getEvolutions();
			String t = args[1].replaceAll("form:", "").replaceAll("palette:", "");
			String t1 = String.valueOf(t.charAt(0)).toUpperCase();
			String t2 = t.substring(1);
			String t3 = t1 + t2;
			String t4 = (args[1].contains("form:") || args[1].contains("palette:")) ? t3 + " " + pokemon.getDisplayName() : pokemon.getDisplayName();
			if (evolutions == null || evolutions.size() == 0) {
				CommandChatHandler.sendFormattedChat(commandSource, TextFormatting.WHITE, "%s " + TextFormatting.DARK_GREEN + "does not evolve.", t4);
				return;
			}
			CommandChatHandler.sendFormattedChat(commandSource, TextFormatting.WHITE, "%s " + TextFormatting.DARK_GREEN + "evolutions: ", t4);
			for (Evolution evolution : evolutions) {
				if (evolution == null)
					continue;
				String evoTo = evolution.to.toString();
				String[] evoToForm = evoTo.split(":");
				String evoToForm1 = Arrays.stream(evoToForm).skip(evoToForm.length - 1).findFirst().get();
				String evoToForm2 = String.valueOf(evoToForm1.charAt(0)).toUpperCase();
				String evoToForm3 = evoToForm1.substring(1);
				String evoToForm4 = evoToForm2 + evoToForm3;
				String[] evoToPokemonName = evoTo.split(" ");
				String evoToPokemonName1 = Arrays.stream(evoToPokemonName).findFirst().get();
				String evoTo2 = (evoTo.contains("form:") || evoTo.contains("palette:")) ? evoToForm4 + " " + evoToPokemonName1 : evoTo;
				String evoTo3 = evoTo2.contains("Base") ? evoToPokemonName1 : evoTo2;
				StringBuilder baseMsg = new StringBuilder(TextFormatting.WHITE + "  " + evoTo3 + ": " + TextFormatting.GREEN + "Leveling up ");
				if (evolution instanceof LevelingEvolution) {
					LevelingEvolution levelingEvolution = (LevelingEvolution) evolution;
					if (levelingEvolution.level != null && levelingEvolution.level > 1)
						baseMsg.append("to level ").append(levelingEvolution.level);
				} else if (evolution instanceof InteractEvolution) {
					if (((InteractEvolution) evolution).item != null) {
						baseMsg = new StringBuilder(TextFormatting.WHITE + "  " + evoTo3 + ": " + TextFormatting.GREEN + "When exposed to " + TextFormatting.DARK_GREEN + ((InteractEvolution) evolution).item.getItemStack().getHoverName().getString());
					}
				} else if (evolution instanceof TradeEvolution) {
					TradeEvolution tradeEvo = (TradeEvolution) evolution;
					if (tradeEvo.with != null) {
						baseMsg = new StringBuilder(TextFormatting.WHITE + "  " + evoTo3 + ": " + TextFormatting.GREEN + "Trading with " + TextFormatting.DARK_GREEN + tradeEvo.with);
					} else {
						baseMsg = new StringBuilder(TextFormatting.WHITE + "  " + evoTo3 + ": " + TextFormatting.GREEN + "Trading");
					}
				}
				CommandChatHandler.sendChat(commandSource, baseMsg.toString());
				if (evolution.conditions != null && !evolution.conditions.isEmpty()) {
					TextFormatting headingColour = TextFormatting.DARK_GREEN;
					TextFormatting valueColour = TextFormatting.GREEN;
					CommandChatHandler.sendChat(commandSource, TextFormatting.DARK_GREEN + "    " + TextFormatting.UNDERLINE + "Conditions:");
					for (EvoCondition condition : evolution.conditions) {
						if (condition instanceof BiomeCondition) {
							BiomeCondition biomeCondition = (BiomeCondition) condition;
							StringBuilder biomes = new StringBuilder(headingColour + "Biomes: " + valueColour);
							for (int i = 0; i < biomeCondition.biomes.size(); i++) {
								ResourceLocation biome = new ResourceLocation(biomeCondition.biomes.get(i));
								String biomeName = biome.toString();
								if (i == 0) {
									biomes.append(biomeName);
								} else {
									biomes.append(headingColour).append(", ").append(valueColour).append(biomeName);
								}
							}
							CommandChatHandler.sendChat(commandSource, "      " + biomes);
							continue;
						}
						if (condition instanceof ChanceCondition) {
							ChanceCondition chanceCondition = (ChanceCondition) condition;
							CommandChatHandler.sendChat(commandSource, "      " + valueColour + (chanceCondition.chance * 100.0F) + " percent chance");
							continue;
						}
						if (condition instanceof EvoRockCondition) {
							EvoRockCondition evoRockCond = (EvoRockCondition) condition;
							long value = Math.round(Math.sqrt(evoRockCond.maxRangeSquared));
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "Within " + valueColour + value + " " + headingColour + "blocks of a " + valueColour + evoRockCond.evolutionRock);
							continue;
						}
						if (condition instanceof FriendshipCondition) {
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "Friendship: " + valueColour + ((FriendshipCondition) condition).friendship);
							continue;
						}
						if (condition instanceof GenderCondition) {
							GenderCondition genderCondition = (GenderCondition) condition;
							StringBuilder genders = new StringBuilder(headingColour + "Genders: " + valueColour + (genderCondition.genders.get(0)).name());
							for (int i = 1; i < genderCondition.genders.size(); i++)
								genders.append(headingColour).append(", ").append(valueColour).append((genderCondition.genders.get(i)).name());
							CommandChatHandler.sendChat(commandSource, "      " + genders);
							continue;
						}
						if (condition instanceof HeldItemCondition) {
							HeldItemCondition heldItemCondition = (HeldItemCondition) condition;
							ItemStack stack = heldItemCondition.item.getItemStack();
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "Held item: " + valueColour + ((stack == null) ? heldItemCondition.item.getItemStack().getHoverName().getString() : stack.getHoverName().getString()));
							continue;
						}
						if (condition instanceof HighAltitudeCondition) {
							HighAltitudeCondition altitudeCondition = (HighAltitudeCondition) condition;
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "Above altitude: " + valueColour + (int) altitudeCondition.minAltitude);
							continue;
						}
						if (condition instanceof LevelCondition) {
							CommandChatHandler.sendChat(commandSource, "    " + headingColour + "Starting at level: " + ((LevelCondition) condition).level);
							continue;
						}
						if (condition instanceof MoveCondition) {
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "Knowing move: " + valueColour + (AttackRegistry.getAttackBase(((MoveCondition) condition).attackName).get()).getAttackName());
							continue;
						}
						if (condition instanceof MoveTypeCondition) {
							CommandChatHandler.sendChat(commandSource, "      " + headingColour + "With a move of type: " + valueColour + ((MoveTypeCondition) condition).type);
							continue;
						}
						if (condition instanceof PartyCondition) {
							ArrayList<PokemonSpecification> withPokemon = new ArrayList<>();
							ArrayList<Element> withTypes = new ArrayList<>();
							ArrayList<String> withForms = new ArrayList<>();
							PartyCondition partyCond = (PartyCondition) condition;
							if (partyCond.withPokemon != null)
								withPokemon = partyCond.withPokemon;
							if (partyCond.withTypes != null)
								withTypes = partyCond.withTypes;
							if (partyCond.withForms != null)
								withForms = partyCond.withForms;
							if (!withPokemon.isEmpty()) {
								StringBuilder pokemonWith = new StringBuilder(headingColour + "      With these Pokemon in party: " + valueColour + (withPokemon.get(0)).toString());
								for (int i = 1; i < withPokemon.size(); i++)
									pokemonWith.append(headingColour).append(", ").append(valueColour).append((withPokemon.get(i)).toString());
								CommandChatHandler.sendChat(commandSource, pokemonWith.toString());
							}
							if (!withTypes.isEmpty()) {
								StringBuilder typesWith = new StringBuilder(headingColour + "      With Pokemon of these types in party: " + valueColour + (withTypes.get(0)));
								for (int i = 1; i < withTypes.size(); i++)
									typesWith.append(headingColour).append(", ").append(valueColour).append((withTypes.get(i)));
								CommandChatHandler.sendChat(commandSource, typesWith.toString());
							}
							if (!withForms.isEmpty()) {
								StringBuilder formsWith = new StringBuilder(headingColour + "      With Pokemon of these forms in party: " + valueColour + withForms.get(0));
								for (int i = 1; i < withForms.size(); i++)
									formsWith.append(headingColour).append(", ").append(valueColour).append(withForms.get(i));
								CommandChatHandler.sendChat(commandSource, formsWith.toString());
							}
							continue;
						}
						if (condition instanceof StatRatioCondition) {
							StatRatioCondition statCond = (StatRatioCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With a stat ratio of " + valueColour + statCond.ratio + headingColour + " between " + valueColour + statCond.stat1 + headingColour + " and " + valueColour + statCond.stat2);
							continue;
						}
						if (condition instanceof TimeCondition) {
							CommandChatHandler.sendChat(commandSource, headingColour + "      During: " + valueColour + ((TimeCondition) condition).time);
							continue;
						}
						if (condition instanceof WeatherCondition) {
							CommandChatHandler.sendChat(commandSource, headingColour + "      With weather: " + valueColour + "Rain");
							continue;
						}
						if (condition instanceof EvoScrollCondition) {
							EvoScrollCondition evoScrollCondition = (EvoScrollCondition) condition;
							long value = Math.round(Math.sqrt(evoScrollCondition.maxRangeSquared));
							CommandChatHandler.sendChat(commandSource, headingColour + "      With Scroll: " + evoScrollCondition.evolutionScroll.getName() + " at range of " + value + " blocks");
							continue;
						}
						if (condition instanceof BattleCriticalCondition) {
							BattleCriticalCondition battleCriticalCondition = (BattleCriticalCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With critical: " + battleCriticalCondition.critical);
							continue;
						}
						if (condition instanceof AbsenceOfHealthCondition) {
							AbsenceOfHealthCondition absenceOfHealthCondition = (AbsenceOfHealthCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With Health absence: " + absenceOfHealthCondition.getHealth());
							continue;
						}
						if (condition instanceof StatusPersistCondition) {
							StatusPersistCondition statusPersistCondition = (StatusPersistCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With status: " + statusPersistCondition.getType().getLocalizedName());
							continue;
						}
						if (condition instanceof WithinStructureCondition) {
							WithinStructureCondition withinStructureCondition = (WithinStructureCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      Within Structure: " + withinStructureCondition.getStructure());
							continue;
						}
						if (condition instanceof NatureCondition) {
							NatureCondition withNatureCondition = (NatureCondition) condition;
							List<String> nName = Lists.newArrayList();
							withNatureCondition.getNatures().forEach(enumNature -> nName.add(enumNature.getLocalizedName()));
							CommandChatHandler.sendChat(commandSource, headingColour + "      With natures: " + String.join(",", nName));
							continue;
						}
						if (condition instanceof OreCondition) {
							OreCondition oreCondition = (OreCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With ore smelted: " + oreCondition.ores);
							continue;
						}
						if (condition instanceof PotionEffectCondition) {
							PotionEffectCondition potionEffectCondition = (PotionEffectCondition) condition;
							CommandChatHandler.sendChat(commandSource, headingColour + "      With potion effect: " + String.join(",", potionEffectCondition.getPotions()));
						}
					}
				}
			}
		} else if (args[1].equals("abilities") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("abilities"))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Abilities").withStyle(TextFormatting.DARK_GREEN)), false);
			Stats stats = pokemon.getForm();
			for (Ability ability : stats.getAbilities().getAbilities()) {
				if (ability != null) {
					commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(ability.getLocalizedName())).withStyle(TextFormatting.GREEN), false);
				}
			}

		} else if ((args[1].equals("ha") || args[1].equals("hiddenability")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("ha") || args[2].equals("hiddenability")))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Hidden Ability").withStyle(TextFormatting.DARK_GREEN)), false);
			Stats stats = pokemon.getForm();
			for (Ability hidden : stats.getAbilities().getHiddenAbilities()) {
				if (hidden != null) {
					commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(hidden.getLocalizedName())).withStyle(TextFormatting.GREEN), false);
				}
			}
		} else if (args[1].equals("spawn") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("spawn"))) {
			int i = 1;
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.standard);
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			List<StringTextComponent> infos = Lists.newArrayList();
			for (SpawnSet set : setinfos) {
				for (SpawnInfo info : set.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						SpawnInfoPokemon spawnInfoPokemon = (SpawnInfoPokemon) info;
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.contentEquals(pokemon.getSpecies().getName())
								|| (noLevel.contentEquals(pokemon.getSpecies().getName() + (args[1].contains("form:") ? " " + args[1] : "")))
								|| (noLevel.contentEquals(pokemon.getSpecies().getName() + (args[1].contains("palette:") ? " " + args[1] : "")))) {
							infos.add(createPokeDetails(spawnInfoPokemon));
						}
					}
				}
			}
			for (StringTextComponent iTextComponents : infos) {
				ITextComponent txt = (new StringTextComponent(TextFormatting.GOLD + String.valueOf(i) + "-")).append(iTextComponents);
				i++;
				commandSource.sendSuccess(txt, false);
			}
		} else if (args[1].equals("biome") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("biome"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" can spawn in:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++) {
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
				}
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("grass") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("grass"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.grassSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" can be found in Grass block in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++)
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("caverock") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("caverock"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.caveRockSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" can be found in Cave Rock block in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++)
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("headbutt") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("headbutt"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.headbuttSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns using the (Headbutt) external move in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++)
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("rocksmash") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("rocksmash"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.rocksmashSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns using the (Rocksmash) external move in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++)
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("sweetscent") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("sweetscent"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.sweetscentSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns using the (Sweetscent) external move in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++) {
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
				}
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("fishing") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("fishing"))) {
			ArrayList<Biome> biomes = new ArrayList<>();
			for (SpawnSet spawnSet : PixelmonSpawning.fishingSpawner.spawnSets) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							for (Biome biome : info.condition.biomes) {
								if (!biomes.contains(biome))
									biomes.add(biome);
							}
						}
					}
				}
			}
			ArrayList<String> biomeNames = new ArrayList<>();
			for (Biome biome : biomes)
				biomeNames.add(String.valueOf(biome.getRegistryName()));
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" can be fished in these biomes:").withStyle(TextFormatting.DARK_GREEN)), false);
			StringBuilder message = new StringBuilder(TextFormatting.RED + "None.");
			if (!biomeNames.isEmpty()) {
				Collections.sort(biomeNames);
				message = new StringBuilder(TextFormatting.GREEN + biomeNames.get(0));
				for (int i = 1; i < biomeNames.size(); i++)
					message.append(TextFormatting.WHITE + ", " + TextFormatting.GREEN).append(biomeNames.get(i));
			}
			CommandChatHandler.sendChat(commandSource, message.toString());
		} else if (args[1].equals("time") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("time"))) {
			ArrayList<WorldTime> times = new ArrayList<>();
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							times = info.condition.times;
						}
					}
				}
			}
			if (times == null || times.isEmpty()) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at any time.").withStyle(TextFormatting.DARK_GREEN)), false);
				return;
			}
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at these times:").withStyle(TextFormatting.DARK_GREEN)), false);
			for (WorldTime time : times)
				commandSource.sendSuccess(new TranslationTextComponent(time.name()).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("weather") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("weather"))) {
			ArrayList<WeatherType> weathers = new ArrayList<>();
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							weathers = info.condition.weathers;
						}
					}
				}
			}
			if (weathers == null || weathers.isEmpty()) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns during any weather.").withStyle(TextFormatting.DARK_GREEN)), false);
				return;
			}
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns during these weathers:").withStyle(TextFormatting.DARK_GREEN)), false);
			for (WeatherType weatherType : weathers)
				commandSource.sendSuccess(new TranslationTextComponent(weatherType.name()).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("moonphase") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("moonphase"))) {
			Integer moons = null;
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							moons = info.condition.moonPhase;
						}
					}
				}
			}
			if (moons == null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns during any moon phase.").withStyle(TextFormatting.DARK_GREEN)), false);
				return;
			}
			String moonPhaseName =  "";
			switch (moons) {
				case 0:
					moonPhaseName = "Full Moon";
					break;
				case 1:
					moonPhaseName = "Waning Gibbous";
					break;
				case 2:
					moonPhaseName = "Last Quarter";
					break;
				case 3:
					moonPhaseName = "Waning Crescent";
					break;
				case 4:
					moonPhaseName = "New Moon";
					break;
				case 5:
					moonPhaseName = "Waxing Crescent";
					break;
				case 6:
					moonPhaseName = "First Quarter";
					break;
				case 7:
					moonPhaseName = "Waxing Gibbous";
			}
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns during these moon phases:").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(moonPhaseName).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("yheight") || (args[1].equals("ylevel") || args[1].equals("y")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("yheight") || args[2].equals("ylevel") || args[2].equals("y")))) {
			Integer minylevel = null;
			Integer maxylevel = null;
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							minylevel = info.condition.minY;
							maxylevel = info.condition.maxY;
						}
					}
				}
			}
			if (minylevel != null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns above this y level:").withStyle(TextFormatting.DARK_GREEN)), false);
				commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(minylevel)).withStyle(TextFormatting.GREEN), false);
			}
			if (maxylevel != null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns below this y level:").withStyle(TextFormatting.DARK_GREEN)), false);
				commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(maxylevel)).withStyle(TextFormatting.GREEN), false);
			}
			if (minylevel == null && maxylevel == null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at any y level:").withStyle(TextFormatting.DARK_GREEN)), false);
			}
		} else if ((args[1].equals("light") || args[1].equals("lightlevel")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("light") || args[2].equals("lightlevel")))) {
			Integer minLightLevel = null;
			Integer maxLightLevel = null;
			ArrayList<SpawnSet> setinfos = Lists.newArrayList();
			setinfos.addAll(PixelmonSpawning.legendarySpawner.spawnSets);
			setinfos.addAll(PixelmonSpawning.standard);
			for (SpawnSet spawnSet : setinfos) {
				for (SpawnInfo info : spawnSet.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						String infoSpec = (((SpawnInfoPokemon) info).getPokemonSpec()).toString();
						String noSpecies = infoSpec.replaceAll("species:", "");
						String noLevel = noSpecies.substring(0, noSpecies.contains("level:") ? noSpecies.length() - 2 : noSpecies.length()).replaceAll(noSpecies.contains("level:") ? " level:" : "", "");
						if (noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : "")) || noLevel.equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("palette:") ? " " + args[1] : ""))) {
							minLightLevel = info.condition.minLightLevel;
							maxLightLevel = info.condition.maxLightLevel;
						}
					}
				}
			}
			if (minLightLevel != null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at or above this light level:").withStyle(TextFormatting.DARK_GREEN)), false);
				commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(minLightLevel)).withStyle(TextFormatting.GREEN), false);
			}
			if (maxLightLevel != null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at or below this light level:").withStyle(TextFormatting.DARK_GREEN)), false);
				commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(maxLightLevel)).withStyle(TextFormatting.GREEN), false);
			}
			if (minLightLevel == null && maxLightLevel == null) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" spawns at any light level.").withStyle(TextFormatting.DARK_GREEN)), false);
			}
		} else if (args[1].equals("egggroup") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("egggroup"))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Egg Group").withStyle(TextFormatting.DARK_GREEN)), false);
			CommandChatHandler.sendFormattedChat(commandSource, TextFormatting.GREEN, StringUtils.join(pokemon.getForm().getEggGroups(), ", "));
		} else if ((args[1].equals("stats") || args[1].equals("basestats")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("stats") || args[2].equals("basestats")))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Stats").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("HP: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.HP))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Attack: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.ATTACK))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Defense: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.DEFENSE))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Sp. Attack: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.SPECIAL_ATTACK))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Sp. Defense: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.SPECIAL_DEFENSE))).withStyle(TextFormatting.GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent("Speed: ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(String.valueOf(pokemon.getForm().getBattleStats().getStat(BattleStatsType.SPEED))).withStyle(TextFormatting.GREEN)), false);
		} else if ((args[1].equals("catch") || args[1].equals("catchrate")) || ((args[1].contains("form:") || args[1].contains("palette:")) && (args[2].equals("catch") || args[2].equals("catchrate")))) {
			commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent("'s Catch Rate").withStyle(TextFormatting.DARK_GREEN)), false);
			commandSource.sendSuccess(new TranslationTextComponent(String.valueOf(pokemon.getForm().getCatchRate())).withStyle(TextFormatting.GREEN), false);
		} else if (args[1].equals("rarity") || ((args[1].contains("form:") || args[1].contains("palette:")) && args[2].equals("rarity"))) {
			float rarity = -1.0F;
			for (SpawnSet standard : PixelmonSpawning.standard) {
				for (SpawnInfo info : standard.spawnInfos) {
					if (info instanceof SpawnInfoPokemon) {
						if ((((SpawnInfoPokemon) info).getPokemonSpec()).toString().replaceAll("species:", "").equalsIgnoreCase((pokemon.getSpecies()).getName() + (args[1].contains("form:") ? " " + args[1] : ""))) {
							rarity = info.rarity;
							break;
						}
					}
				}
			}
			if (pokemon.isLegendary()) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" is a legendary").withStyle(TextFormatting.DARK_GREEN)), false);
			} else if (rarity > 0.0F) {
				commandSource.sendSuccess(new TranslationTextComponent("The rarity of ").withStyle(TextFormatting.DARK_GREEN).append(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" is ").withStyle(TextFormatting.DARK_GREEN)).append(new TranslationTextComponent(String.valueOf(rarity)).withStyle(TextFormatting.GREEN)), false);
			} else if (rarity <= 0.0F) {
				commandSource.sendSuccess(new TranslationTextComponent(args[1].contains("form:") || args[1].contains("palette:") ? s3 + " " : "").withStyle(TextFormatting.WHITE).append(new TranslationTextComponent(pokemon.getDisplayName()).withStyle(TextFormatting.WHITE)).append(new TranslationTextComponent(" does not spawn").withStyle(TextFormatting.DARK_GREEN)), false);
			}
		} else {
			CommandChatHandler.sendFormattedChat(commandSource, TextFormatting.RED, "Invalid option");
		}
	}

	public static StringTextComponent createPokeDetails(SpawnInfoPokemon spawnInfo) {
		StringTextComponent txt = new StringTextComponent("spawn info:\n");
		Pokemon pokemon = PokemonFactory.create(spawnInfo.getPokemonSpec());
		String form = "";
		txt.append("Type of spawn location: " + String.join(", ", spawnInfo.stringLocationTypes) + "\n");
		txt.append("Minimum level: " + spawnInfo.minLevel + "\n");
		txt.append("Maximum level: " + spawnInfo.maxLevel + "\n");
		if (spawnInfo.heldItems != null) {
			List<String> itemName = Lists.newArrayList();
			spawnInfo.heldItems.forEach(jsonItemStack -> itemName.add(TextFormatting.DARK_AQUA + String.valueOf(jsonItemStack.getItemStack().getDisplayName())));
			txt.append("HeldItems: " + String.join(TextFormatting.YELLOW + ", ", itemName) + "\n");
		}
		txt.append("Biomes: " + getBiomeSpawns(spawnInfo) + "\n");
		txt.append("Rarity: " + spawnInfo.rarity);
		StringTextComponent pokeHover = new StringTextComponent(TextFormatting.DARK_AQUA + pokemon.getLocalizedName() + form);
		pokeHover.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, txt));
		return pokeHover;
	}

	public static String getBiomeSpawns(SpawnInfoPokemon info) {
		ArrayList<Biome> allBiomes = new ArrayList<>();
		for (Biome biome : GameRegistry.findRegistry(Biome.class))
			allBiomes.add(biome);
		if (info.condition != null && info.condition.biomes != null && !info.condition.biomes.isEmpty())
			allBiomes.removeIf(biome -> !info.condition.biomes.contains(biome));
		if (info.anticondition != null && info.anticondition.biomes != null && !info.anticondition.biomes.isEmpty())
			allBiomes.removeIf(biome -> info.anticondition.biomes.contains(biome));
		if (info.compositeCondition != null) {
			if (info.compositeCondition.conditions != null)
				info.compositeCondition.conditions.forEach(condition -> {
					if (condition.biomes != null && !condition.biomes.isEmpty())
						allBiomes.removeIf((Objects::isNull));
				});
			if (info.compositeCondition.anticonditions != null)
				info.compositeCondition.anticonditions.forEach(anticondition -> {
					if (anticondition.biomes != null && anticondition.biomes.isEmpty())
						allBiomes.removeIf((Objects::isNull));
				});
		}
		Set<Biome> avail = new HashSet<>(allBiomes);
		ArrayList<String> biomeNames = new ArrayList<>();
		for (Biome biome : avail)
			biomeNames.add(TextFormatting.DARK_AQUA + String.valueOf(biome.getRegistryName()));
		return String.join(TextFormatting.YELLOW + ", ", biomeNames);
	}
}
