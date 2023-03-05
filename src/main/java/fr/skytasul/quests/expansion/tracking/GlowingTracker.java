package fr.skytasul.quests.expansion.tracking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.skytasul.glowingentities.GlowingEntities;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.Located.LocatedEntity;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.api.stages.types.Locatable.PreciseLocatable;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.EnumParser;
import fr.skytasul.quests.expansion.BeautyQuestsExpansion;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.utils.Lang;

public class GlowingTracker extends AbstractTaskTracker {
	
	private static final ChatColor DEFAULT_COLOR = ChatColor.GREEN;
	private static final long UPDATE_RATE = 50L;

	private static GlowingEntities GLOWING_API;
	
	private ChatColor color;
	private double maxDistance = 40;
	private int maxAmount = 10;
	
	private Map<Player, Set<Glowing>> shown;
	
	public GlowingTracker() {
		this(DEFAULT_COLOR);
	}

	public GlowingTracker(ChatColor color) {
		super(UPDATE_RATE);
		if (GLOWING_API == null) GLOWING_API = new GlowingEntities(BeautyQuestsExpansion.getInstance());
		
		this.color = color;
	}
	
	@Override
	public void start(Locatable locatable) {
		super.start(locatable);
		if (locatable.canBeFetchedAsynchronously()) {
			shown = new ConcurrentHashMap<>();
		}else {
			shown = new HashMap<>();
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		if (shown != null && !shown.isEmpty()) shown.forEach((__, set) -> set.forEach(Glowing::remove));
	}
	
	@Override
	public synchronized void run() {
		shown.values().forEach(set -> set.forEach(glowing -> glowing.found = false));
		
		if (locatable instanceof PreciseLocatable) {
			PreciseLocatable precise = (PreciseLocatable) locatable;
			Located located = precise.getLocated();
			if (!isRunning()) return;
			
			if (located instanceof LocatedEntity) {
				Entity entity = ((LocatedEntity) located).getEntity();
				if (entity != null) {
					shown.forEach((player, set) -> {
						foundLocatedEntity(player, set, entity);
					});
				}
			}
		}
		
		if (locatable instanceof MultipleLocatable) {
			MultipleLocatable multiple = (MultipleLocatable) locatable;
			shown.forEach((player, set) -> {
				Spliterator<Located> locateds = multiple.getNearbyLocated(NearbyFetcher.create(player.getLocation(), maxDistance, LocatedType.ENTITY));
				for (int i = 0; i < maxAmount; i++) {
					if (!locateds
							.tryAdvance(located -> foundLocatedEntity(player, set, ((LocatedEntity) located).getEntity())))
						break;
				}
			});
		}
		
		shown.values().forEach(set -> {
			for (Iterator<Glowing> iterator = set.iterator(); iterator.hasNext();) {
				Glowing glowing = iterator.next();
				if (!glowing.found) {
					glowing.remove();
					iterator.remove();
				}
			}
		});
	}
	
	private void foundLocatedEntity(Player player, Set<Glowing> playerSet, Entity located) {
		Optional<Glowing> glowingOpt = playerSet.stream().filter(glowing -> glowing.entity.equals(located)).findAny();
		if (glowingOpt.isPresent()) {
			glowingOpt.get().found = true;
		}else {
			Glowing glowing = new Glowing(player, located);
			playerSet.add(glowing);
			glowing.display();
		}
	}
	
	@Override
	public Tracker clone() {
		return new GlowingTracker(color);
	}
	
	@Override
	public void show(Player player) {
		shown.put(player, new HashSet<>());
	}
	
	@Override
	public void hide(Player player) {
		Set<Glowing> glowing = shown.remove(player);
		if (glowing != null && !glowing.isEmpty()) glowing.forEach(Glowing::remove);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(color.name().toLowerCase().replace('_', ' ')), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation()) return;
		Lang.COLOR_NAMED_EDITOR.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopenGUI, newColor -> {
			this.color = newColor;
			event.reopenGUI();
		}, new EnumParser<>(ChatColor.class, ChatColor::isColor)).enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		if (color != DEFAULT_COLOR) section.set("color", color.name());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		if (section.contains("color")) color = ChatColor.valueOf(section.getString("color"));
	}
	
	class Glowing {
		private final Player player;
		private final Entity entity;
		private boolean found = true;
		
		public Glowing(Player player, Entity entity) {
			this.player = player;
			this.entity = entity;
		}
		
		public void display() {
			try {
				GLOWING_API.setGlowing(entity, player, color);
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		
		public void remove() {
			try {
				GLOWING_API.unsetGlowing(entity, player);
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public int hashCode() {
			return entity.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Glowing) {
				return ((Glowing) obj).entity.equals(entity);
			}
			return false;
		}
		
	}
	
}
