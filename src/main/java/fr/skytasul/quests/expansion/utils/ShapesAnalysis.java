package fr.skytasul.quests.expansion.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class ShapesAnalysis {
	
	private static final List<AnalyzedEdges> ANALYZED_EDGES = new ArrayList<>();
	
	public static double getBlockHeight(Block block) {
		double minY = 0;
		double maxY = 0;
		for (BoundingBox box : block.getCollisionShape().getBoundingBoxes()) {
			if (box.getMinY() < minY) minY = box.getMinY();
			if (box.getMaxY() > maxY) maxY = box.getMaxY();
		}
		return maxY - minY;
	}
	
	public static Location getBlockBottom(Block block) {
		Collection<BoundingBox> boxes = block.getCollisionShape().getBoundingBoxes();
		if (boxes.isEmpty()) return block.getLocation().add(0.5, 0, 0.5);
		Iterator<BoundingBox> iterator = boxes.iterator();
		
		BoundingBox box = iterator.next();
		double minX = box.getMinX();
		double maxX = box.getMaxX();
		double minZ = box.getMinZ();
		double maxZ = box.getMaxZ();
		double minY = box.getMinY();
		for (; iterator.hasNext(); box = iterator.next()) {
			if (box.getMinX() < minX) minX = box.getMinX();
			if (box.getMaxX() > maxX) maxX = box.getMaxX();
			if (box.getMinZ() < minZ) minZ = box.getMinZ();
			if (box.getMaxZ() > maxZ) maxZ = box.getMaxZ();
			if (box.getMinY() < minY) minY = box.getMinY();
		}
		return block.getLocation().add((maxX - minX) / 2D + minX, minY, (maxZ - minZ) / 2D + minZ);
	}
	
	public static List<Location> getEdgePoints(Block block, double resolution) {
		BlockData blockData  = block.getBlockData();
		
		Optional<AnalyzedEdges> precomputed = ANALYZED_EDGES.stream().filter(edges -> edges.match(blockData, resolution)).findAny();
		if (precomputed.isPresent()) return precomputed.get().apply(block.getLocation());
	
		AnalyzedEdges edges = new AnalyzedEdges(blockData, resolution, getEdgePoints(block.getCollisionShape().getBoundingBoxes(), resolution));
		ANALYZED_EDGES.add(edges);
		return edges.apply(block.getLocation());
	}
	
	public static List<Vector> getEdgePoints(Collection<BoundingBox> boxes, double resolution) {
		List<Vector> list = new ArrayList<>((int) (1D / resolution * 12D * boxes.size()));
		
		for (BoundingBox box : boxes) {
			CoordinateConsumer consumer = (x, y, z) -> {
				if (boxes
						.stream()
						.filter(otherBox -> otherBox != box)
						.noneMatch(otherBox -> liesInside(otherBox, x, y, z)))
					list.add(new Vector(x, y, z));
			};
			// lol probably not optimized at all
			iterateOverX(box, box.getMinY(), box.getMinZ(), resolution, consumer);
			iterateOverX(box, box.getMinY(), box.getMaxZ(), resolution, consumer);
			iterateOverX(box, box.getMaxY(), box.getMinZ(), resolution, consumer);
			iterateOverX(box, box.getMaxY(), box.getMaxZ(), resolution, consumer);
			iterateOverY(box, box.getMinX(), box.getMinZ(), resolution, consumer);
			iterateOverY(box, box.getMinX(), box.getMaxZ(), resolution, consumer);
			iterateOverY(box, box.getMaxX(), box.getMinZ(), resolution, consumer);
			iterateOverY(box, box.getMaxX(), box.getMaxZ(), resolution, consumer);
			iterateOverZ(box, box.getMinX(), box.getMinY(), resolution, consumer);
			iterateOverZ(box, box.getMinX(), box.getMaxY(), resolution, consumer);
			iterateOverZ(box, box.getMaxX(), box.getMinY(), resolution, consumer);
			iterateOverZ(box, box.getMaxX(), box.getMaxY(), resolution, consumer);
		}
		
		return list;
	}
	
	private static void iterateOverX(BoundingBox box, double y, double z, double resolution, CoordinateConsumer consumer) {
		for (double x = box.getMinX(); x <= box.getMaxX(); x += resolution) consumer.accept(x, y, z);
	}
	
	private static void iterateOverY(BoundingBox box, double x, double z, double resolution, CoordinateConsumer consumer) {
		for (double y = box.getMinY(); y <= box.getMaxY(); y += resolution) consumer.accept(x, y, z);
	}
	
	private static void iterateOverZ(BoundingBox box, double x, double y, double resolution, CoordinateConsumer consumer) {
		for (double z = box.getMinZ(); z <= box.getMaxZ(); z += resolution) consumer.accept(x, y, z);
	}
	
	public static boolean liesInside(BoundingBox box, double x, double y, double z) {
		return x >= box.getMinX() && x <= box.getMaxX()
				&& y >= box.getMinY() && y <= box.getMaxY()
				&& z >= box.getMinZ() && z <= box.getMaxZ();
	}
	
	@FunctionalInterface
	private interface CoordinateConsumer {
		void accept(double x, double y, double z);
	}
	
	// Code from net.minecraft.world.phys.shapes.DiscreteVoxelShape
	/*private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer operation, AxisCycle $$1, boolean $$2) {
	    AxisCycle cycle = $$1.inverse();
	    int xSize = this.getSize(cycle.cycle(Direction.Axis.X));
	    int ySize = this.getSize(cycle.cycle(Direction.Axis.Y));
	    int zSize = this.getSize(cycle.cycle(Direction.Axis.Z));
	    for (int x = 0; x <= xSize; ++x) {
	        for (int y = 0; y <= ySize; ++y) {
	            int $$9 = -1;
	            for (int z = 0; z <= zSize; ++z) {
	                int $$11 = 0;
	                int $$12 = 0;
	                for (int i = 0; i <= 1; ++i) {
	                    for (int j = 0; j <= 1; ++j) {
	                        if (!this.isFullWide(cycle, x + i - 1, y + j - 1, z)) continue;
	                        ++$$11;
	                        $$12 ^= i ^ j;
	                    }
	                }
	                if ($$11 == 1 || $$11 == 3 || $$11 == 2 && !($$12 & true)) {
	                    if ($$2) {
	                        if ($$9 != -1) continue;
	                        $$9 = z;
	                        continue;
	                    }
	                    operation.consume(cycle.cycle(x, y, z, Direction.Axis.X), cycle.cycle(x, y, z, Direction.Axis.Y), cycle.cycle(x, y, z, Direction.Axis.Z), cycle.cycle(x, y, z + 1, Direction.Axis.X), cycle.cycle(x, y, z + 1, Direction.Axis.Y), cycle.cycle(x, y, z + 1, Direction.Axis.Z));
	                    continue;
	                }
	                if ($$9 == -1) continue;
	                operation.consume(cycle.cycle(x, y, $$9, Direction.Axis.X), cycle.cycle(x, y, $$9, Direction.Axis.Y), cycle.cycle(x, y, $$9, Direction.Axis.Z), cycle.cycle(x, y, z, Direction.Axis.X), cycle.cycle(x, y, z, Direction.Axis.Y), cycle.cycle(x, y, z, Direction.Axis.Z));
	                $$9 = -1;
	            }
	        }
	    }
	}*/
	
	private static class AnalyzedEdges {
		private final BlockData blockData;
		private final double resolution;
		private final List<Vector> points;
		
		private AnalyzedEdges(BlockData blockData, double resolution, List<Vector> points) {
			this.blockData = blockData;
			this.resolution = resolution;
			this.points = points;
		}
		
		public boolean match(BlockData blockData, double resolution) {
			return this.blockData.equals(blockData) && this.resolution == resolution;
		}
		
		public List<Location> apply(Location location) {
			return points.stream().map(point -> location.clone().add(point)).collect(Collectors.toList());
		}
		
	}
	
}
