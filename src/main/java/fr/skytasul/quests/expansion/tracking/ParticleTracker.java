package fr.skytasul.quests.expansion.tracking;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable.NearbyFetcher;
import fr.skytasul.quests.expansion.api.tracking.Tracker;
import fr.skytasul.quests.expansion.utils.ShapesAnalysis;
import fr.skytasul.quests.gui.particles.ParticleEffectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.nms.NMS;

public class ParticleTracker extends AbstractTaskFetcherTracker {
	
	private static final ParticleEffect DEFAULT_EFFECT = new ParticleEffect(Particle.FLAME, ParticleShape.POINT, null);
	
	private double maxDistance = 20;
	private int maxAmount = 10;
	
	private ParticleEffect particles;
	
	public ParticleTracker() {
		this(DEFAULT_EFFECT);
	}
	
	public ParticleTracker(ParticleEffect particles) {
		super(20);
		this.particles = particles;
	}
	
	@Override
	public Tracker clone() {
		return new ParticleTracker(particles);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation()) return;
		new ParticleEffectGUI(newEffect -> {
			if (newEffect != null) particles = newEffect;
			event.reopenGUI();
		}, particles).create(event.getPlayer());
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(particles.toString()), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	protected void display(Located located) {
		if (located instanceof Locatable.Located.LocatedEntity) {
			Entity entity = ((Locatable.Located.LocatedEntity) located).getEntity();
			if (entity != null) particles.send(entity, shown);
			return;
		}
		
		Location bottom;
		double height;
		if (located instanceof Locatable.Located.LocatedBlock) {
			Block block = ((Locatable.Located.LocatedBlock) located).getBlockNullable();
			if (block == null)
				return;
			if (NMS.getMCVersion() >= 17) {
				bottom = ShapesAnalysis.getBlockBottom(block);
				height = ShapesAnalysis.getBlockHeight(block);
			}else {
				bottom = block.getLocation().add(0.5, 0, 0.5);
				height = 1;
			}
		}else {
			bottom = located.getLocation();
			height = 0;
		}
		particles.send(bottom, height, shown);
	}
	
	@Override
	protected NearbyFetcher constructFetcher(Player player) {
		return NearbyFetcher.create(player.getLocation(), maxDistance);
	}
	
	@Override
	protected int getAmount(Player player) {
		return maxAmount;
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
