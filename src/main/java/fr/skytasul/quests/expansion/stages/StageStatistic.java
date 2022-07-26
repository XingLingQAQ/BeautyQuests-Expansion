package fr.skytasul.quests.expansion.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.utils.LangExpansion;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageStatistic extends AbstractStage {
	
	private final Statistic statistic;
	private final Material offsetMaterial;
	private final EntityType offsetEntity;
	
	private final int limit;
	private final ComparisonMethod comparison;
	private final boolean relative;
	
	private BukkitTask task;
	private List<Player> players;
	
	public StageStatistic(QuestBranch branch, Statistic statistic, int limit, ComparisonMethod comparison, boolean relative) {
		super(branch);
		
		this.statistic = statistic;
		this.offsetMaterial = null;
		this.offsetEntity = null;
		
		this.limit = limit;
		this.comparison = comparison;
		this.relative = relative;
	}
	
	public StageStatistic(QuestBranch branch, Statistic statistic, Material offsetMaterial, int limit, ComparisonMethod comparison, boolean relative) {
		super(branch);
		
		this.statistic = statistic;
		this.offsetMaterial = offsetMaterial;
		this.offsetEntity = null;
		
		this.limit = limit;
		this.comparison = comparison;
		this.relative = relative;
	}
	
	public StageStatistic(QuestBranch branch, Statistic statistic, EntityType offsetEntity, int limit, ComparisonMethod comparison, boolean relative) {
		super(branch);
		
		this.statistic = statistic;
		this.offsetMaterial = null;
		this.offsetEntity = offsetEntity;
		
		this.limit = limit;
		this.comparison = comparison;
		this.relative = relative;
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return LangExpansion.Stage_Statistic_Advancement.format(descriptionFormat(acc, source));
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		String offsetName = getOffsetName();
		return new Object[] {
				statistic.name() + (offsetName == null ? "" : "(" + offsetName + ")"),
				(Supplier<Integer>) () -> acc.isCurrent() ? limit - getPlayerTarget(acc.getPlayer()) : -1,
				statistic.name(),
				offsetName
		};
	}

	private String getOffsetName() {
		return offsetMaterial != null ? offsetMaterial.name() : (offsetEntity != null ? offsetEntity.name() : null);
	}
	
	private int getPlayerTarget(Player player) {
		int stat = getStatistic(player);
		
		if (relative) {
			Number initial = getData(PlayersManager.getPlayerAccount(player), "initial");
			if (initial != null) stat -= initial.intValue();
		}
		
		return stat;
	}
	
	private int getStatistic(Player player) {
		int stat;
		if (offsetMaterial != null) {
			stat = player.getStatistic(statistic, offsetMaterial);
		}else if (offsetEntity != null) {
			stat = player.getStatistic(statistic, offsetEntity);
		}else {
			stat = player.getStatistic(statistic);
		}
		return stat;
	}
	
	protected void refresh() {
		players.forEach(player -> {
			if (!canUpdate(player)) return;
			
			if (comparison.test(getPlayerTarget(player) - limit)) finishStage(player);
		});
	}
	
	@Override
	public void load() {
		super.load();
		players = new ArrayList<>();
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(BeautyQuestsExpansion.getInstance(), this::refresh, 20, 20);
	}
	
	@Override
	public void unload() {
		super.unload();
		players = null;
		if (task != null) task.cancel();
	}
	
	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		if (relative) {
			int stat;
			if (acc.isCurrent()) {
				stat = getStatistic(acc.getPlayer());
			}else {
				stat = 0;
				BeautyQuestsExpansion.logger.warning("Trying to set initial statistic " + statistic.name() + " for account " + acc.debugName() + " but the player is offline, in " + toString());
			}
			datas.put("initial", stat);
		}
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		players.add(p);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) players.add(acc.getPlayer());
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		players.remove(p);
	}
	
	@Override
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) players.remove(acc.getPlayer());
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("statistic", statistic.name());
		if (offsetMaterial != null) {
			section.set("material", offsetMaterial.name());
		}else if (offsetEntity != null) {
			section.set("entity", offsetEntity.name());
		}
		section.set("limit", limit);
		if (relative) section.set("relative", true);
		if (comparison != ComparisonMethod.GREATER_OR_EQUAL) section.set("comparison", comparison.name());
	}
	
	public static StageStatistic deserialize(ConfigurationSection section, QuestBranch branch) {
		Statistic statistic = Statistic.valueOf(section.getString("statistic"));
		int limit = section.getInt("limit");
		boolean relative = section.getBoolean("relative", false);
		ComparisonMethod comparison = section.contains("comparison") ? ComparisonMethod.valueOf(section.getString("comparison")) : ComparisonMethod.GREATER_OR_EQUAL;
		
		if (section.contains("material")) {
			return new StageStatistic(branch, statistic, Material.valueOf(section.getString("material")), limit, comparison, relative);
		}else if (section.contains("entity")) {
			return new StageStatistic(branch, statistic, EntityType.valueOf(section.getString("entity")), limit, comparison, relative);
		}else {
			return new StageStatistic(branch, statistic, limit, comparison, relative);
		}
	}
	
	public static class Creator extends StageCreation<StageStatistic> {
		
		static class StatisticListGUI extends PagedGUI<Statistic> {
			
			private final Consumer<Statistic> end;
			
			public StatisticListGUI(Consumer<Statistic> end) {
				super(LangExpansion.Stage_Statistic_StatList_Gui_Name.toString(), DyeColor.LIGHT_BLUE, Arrays.asList(Statistic.values()), null, Statistic::name);
				this.end = end;
			}
			
			@Override
			public ItemStack getItemStack(Statistic object) {
				XMaterial material;
				String lore = null;
				switch (object.getType()) {
				case BLOCK:
					material = XMaterial.GRASS_BLOCK;
					lore = LangExpansion.Stage_Statistic_StatList_Gui_Block.toString();
					break;
				case ENTITY:
					material = XMaterial.BLAZE_SPAWN_EGG;
					lore = LangExpansion.Stage_Statistic_StatList_Gui_Entity.toString();
					break;
				case ITEM:
					material = XMaterial.STONE_HOE;
					lore = LangExpansion.Stage_Statistic_StatList_Gui_Item.toString();
					break;
				default:
					material = XMaterial.FEATHER;
					break;
				}
				return ItemUtils.item(material, "§e" + object.name(), QuestOption.formatDescription(lore));
			}
			
			@Override
			public void click(Statistic existing, ItemStack item, ClickType clickType) {
				end.accept(existing);
			}
			
			@Override
			public CloseBehavior onClose(Player p, Inventory inv) {
				Utils.runSync(() -> end.accept(null));
				return CloseBehavior.REMOVE;
			}
			
		}
		
		private static final int SLOT_STAT = 5;
		private static final int SLOT_LIMIT = 6;
		private static final int SLOT_RELATIVE = 7;
		
		private Statistic statistic;
		private Material offsetMaterial;
		private EntityType offsetEntity;
		
		private int limit;
		private ComparisonMethod comparison = ComparisonMethod.GREATER_OR_EQUAL;
		private boolean relative = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(SLOT_STAT, ItemUtils.item(XMaterial.FEATHER, LangExpansion.Stage_Statistic_Item_Stat.toString()), (p, item) -> {
				openStatisticGUI(p, () -> reopenGUI(p, true), false);
			});
			line.setItem(SLOT_LIMIT, ItemUtils.item(XMaterial.REDSTONE, LangExpansion.Stage_Statistic_Item_Limit.toString()), (p, item) -> {
				openLimitEditor(p, () -> reopenGUI(p, true), () -> reopenGUI(p, true));
			});
			line.setItem(SLOT_RELATIVE, ItemUtils.itemSwitch(LangExpansion.Stage_Statistic_Item_Relative.toString(), relative, QuestOption.formatDescription(LangExpansion.Stage_Statistic_Item_Relative_Description.toString())), (p, item) -> {
				relative = ItemUtils.toggle(item);
			});
		}
		
		public void setStatistic(Statistic statistic) {
			this.statistic = statistic;
			
			String name;
			if (offsetMaterial != null) {
				name = offsetMaterial.name();
			}else if (offsetEntity != null) {
				name = offsetEntity.name();
			}else {
				name = null;
			}
			line.editItem(SLOT_STAT, ItemUtils.lore(line.getItem(SLOT_STAT), Lang.optionValue.format(statistic.name() + (name == null ? "" : " (" + name + ")"))));
		}
		
		public void setLimit(int limit) {
			this.limit = limit;
			line.editItem(SLOT_LIMIT, ItemUtils.lore(line.getItem(SLOT_LIMIT), Lang.optionValue.format(limit)));
		}
		
		public void setRelative(boolean relative) {
			this.relative = relative;
			line.editItem(SLOT_RELATIVE, ItemUtils.set(line.getItem(SLOT_RELATIVE), relative));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			openStatisticGUI(p, removeAndReopen(p, true), true);
		}

		private void openStatisticGUI(Player p, Runnable cancel, boolean askLimit) {
			new StatisticListGUI(stat -> {
				if (stat == null) {
					cancel.run();
				}else {
					switch (stat.getType()) {
					case BLOCK:
					case ITEM:
						new TextEditor<>(p, cancel, offset -> {
							Runnable end = () -> {
								offsetMaterial = offset.parseMaterial();
								setStatistic(stat);
								reopenGUI(p, true);
							};
							if (askLimit) {
								openLimitEditor(p, cancel, end);
							}else end.run();
						}, stat.getType() == Type.ITEM ? MaterialParser.ITEM_PARSER : MaterialParser.BLOCK_PARSER).enter();
						break;
					case ENTITY:
						new EntityTypeGUI(offset -> {
							Runnable end = () -> {
								offsetEntity = offset;
								setStatistic(stat);
								reopenGUI(p, true);
							};
							if (askLimit) {
								openLimitEditor(p, cancel, end);
							}else end.run();
						}, null).create(p);
						break;
					default:
						Runnable end = () -> {
							setStatistic(stat);
							reopenGUI(p, true);
						};
						if (askLimit) {
							openLimitEditor(p, cancel, end);
						}else end.run();
						break;
					}
				}
			}).sortValues(Statistic::name).create(p);
		}
		
		private void openLimitEditor(Player p, Runnable cancel, Runnable end) {
			LangExpansion.Stage_Statistic_EDITOR_LIMIT.send(p);
			new TextEditor<>(p, cancel, newLimit -> {
				// add comparison editor
				
				setLimit(newLimit);
				end.run();
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
		}
		
		@Override
		public void edit(StageStatistic stage) {
			super.edit(stage);
			if (stage.offsetEntity != null) {
				this.offsetEntity = stage.offsetEntity;
			}else if (stage.offsetMaterial != null) {
				this.offsetMaterial = stage.offsetMaterial;
			}
			setStatistic(stage.statistic);
			setLimit(stage.limit);
			setRelative(stage.relative);
		}
		
		@Override
		protected StageStatistic finishStage(QuestBranch branch) {
			if (offsetMaterial != null) {
				return new StageStatistic(branch, statistic, offsetMaterial, limit, comparison, relative);
			}else if (offsetEntity != null) {
				return new StageStatistic(branch, statistic, offsetEntity, limit, comparison, relative);
			}else {
				return new StageStatistic(branch, statistic, limit, comparison, relative);
			}
		}
		
	}
	
}
