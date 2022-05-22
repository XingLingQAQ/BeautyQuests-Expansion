package fr.skytasul.quests.expansion.tracking;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.expansion.utils.ShapesAnalysis;
import fr.skytasul.quests.gui.misc.ParticleEffectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.nms.NMS;

public class ParticleTracker extends AbstractTaskTracker {
	
	private static final ParticleEffect DEFAULT_EFFECT = new ParticleEffect(Particle.FLAME, ParticleShape.POINT, null);
	
	private double maxDistance = 20;
	private int maxAmount = 10;
	
	private ParticleEffect particles;
	
	public ParticleTracker() {
		this(DEFAULT_EFFECT);
	}
	
	public ParticleTracker(ParticleEffect particles) {
		this.particles = particles;
	}
	
	@Override
	public Tracker clone() {
		return new ParticleTracker(particles);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ParticleEffectGUI(newEffect -> {
			particles = newEffect;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, particles).create(event.getPlayer());
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(particles.toString()), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	protected void displayLocation(Location location) {
		particles.send(location, 0, shown);
	}
	
	@Override
	protected void displayEntity(Entity entity) {
		particles.send(entity, shown);
	}
	
	@Override
	protected void displayBlock(Block block) {
		if (NMS.getMCVersion() >= 17) {
			particles.send(ShapesAnalysis.getBlockBottom(block), ShapesAnalysis.getBlockHeight(block), shown);
		}else {
			particles.send(block.getLocation().add(0.5, 0, 0.5), 1, shown);
		}
	}
	
	@Override
	protected NearbyFetcher constructFetcher(Player player) {
		return NearbyFetcher.create(player.getLocation(), maxDistance, maxAmount);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		if (particles != DEFAULT_EFFECT) particles.serialize(section.createSection("effect"));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		if (section.contains("effect")) particles = ParticleEffect.deserialize(section.getConfigurationSection("effect"));
	}
	
}
