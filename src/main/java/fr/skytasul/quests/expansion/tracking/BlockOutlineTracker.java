package fr.skytasul.quests.expansion.tracking;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.ColorParser;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.expansion.utils.ShapesAnalysis;
import fr.skytasul.quests.gui.particles.ParticleListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;

public class BlockOutlineTracker extends AbstractTaskFetcherTracker {
	
	private static final ParticleEffect DEFAULT_EFFECT = new ParticleEffect(Particle.FLAME, null, null);
	
	private double maxDistance = 20;
	private int maxAmount = 10;
	private double resolution = 0.125;
	
	private ParticleEffect particles = DEFAULT_EFFECT;
	
	public BlockOutlineTracker() {
		this(DEFAULT_EFFECT);
	}
	
	public BlockOutlineTracker(ParticleEffect particles) {
		super(20);
		this.particles = particles;
	}
	
	@Override
	protected void display(Located located) {
		if (located instanceof Located.LocatedBlock) {
			Block block = ((Located.LocatedBlock) located).getBlock();
			ShapesAnalysis.getEdgePoints(block, resolution).forEach(point -> particles.sendParticle(point, shown, 0, 0, 0, 1));
		}
	}
	
	@Override
	protected NearbyFetcher constructFetcher(Player player) {
		return NearbyFetcher.create(player.getLocation(), maxDistance, maxAmount, Locatable.Located.LocatedBlock.class);
	}
	
	@Override
	public Tracker clone() {
		return new BlockOutlineTracker(particles);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation()) return;
		new ParticleListGUI(newParticle -> {
			if (newParticle != null) {
				if (ParticleEffect.canHaveColor(newParticle)) {
					Lang.COLOR_EDITOR.send(event.getPlayer());
					new TextEditor<>(event.getPlayer(), event::reopenGUI, color -> {
						particles = new ParticleEffect(newParticle, null, color);
						event.updateItemLore(getLore());
						event.reopenGUI();
					}, ColorParser.PARSER).enter();
					return;
				}
				particles = new ParticleEffect(newParticle, null, null);
				event.updateItemLore(getLore());
			}
			event.reopenGUI();
		}).create(event.getPlayer());
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(particles.toString()), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void save(ConfigurationSection section) {
		if (particles != DEFAULT_EFFECT) {
			section.set("particleEffect", particles.getParticle().name());
			if (particles.getColor() != null) section.set("particleColor", particles.getColor().serialize());
		}
	}
	
	@Override
	public void load(ConfigurationSection section) {
		if (section.contains("particleEffect")) {
			Particle particle = Particle.valueOf(section.getString("particleEffect").toUpperCase());
			Color color = section.contains("particleColor") ? Color.deserialize(section.getConfigurationSection("particleColor").getValues(false)) : null;
			particles = new ParticleEffect(particle, null, color);
		}
	}
	
}
