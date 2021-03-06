package me.daddychurchill.CityWorld;

import java.util.Random;

import me.daddychurchill.CityWorld.Context.ContextFarm;
import me.daddychurchill.CityWorld.Context.ContextNature;
import me.daddychurchill.CityWorld.Context.ContextNeighborhood;
import me.daddychurchill.CityWorld.Context.ContextCityCenter;
import me.daddychurchill.CityWorld.Context.ContextHighrise;
import me.daddychurchill.CityWorld.Context.ContextLowrise;
import me.daddychurchill.CityWorld.Context.ContextMall;
import me.daddychurchill.CityWorld.Context.ContextMidrise;
import me.daddychurchill.CityWorld.Context.ContextUnconstruction;
import me.daddychurchill.CityWorld.Context.ContextData;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Plats.PlatLot.LotStyle;
import me.daddychurchill.CityWorld.Plats.PlatNature;
import me.daddychurchill.CityWorld.Plats.PlatRoad;
import me.daddychurchill.CityWorld.Plats.PlatStatue;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.HeightInfo;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SupportChunk;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class PlatMap {
	
	// Class Constants
	static final public int Width = 10;
	
	// Instance data
	public World world;
	public WorldGenerator generator;
	public int originX;
	public int originZ;
	public ContextData context;
	private PlatLot[][] platLots;
	private int naturalPlats;

	public PlatMap(WorldGenerator aGenerator, SupportChunk typicalChunk, int aOriginX, int aOriginZ) {
		super();
		
		// populate the instance data
		world = typicalChunk.world;
		generator = aGenerator;
		originX = aOriginX;
		originZ = aOriginZ;

		// make room for plat data
		platLots = new PlatLot[Width][Width];
		naturalPlats = 0;
		
		// assume everything is natural
		context = new ContextNature(generator, this);
		context.populateMap(generator, this);
		
		// place and validate the roads
		if (generator.settings.includeBuildings) {
			populateRoads(typicalChunk);
			validateRoads(typicalChunk);

			// recalculate the context based on the "natural-ness" of the platmap
			context = getContext();
			context.populateMap(generator, this);
		}
	}

	private ContextData getContext() {
		
		// how natural is this platmap?
		if (naturalPlats == 0) {
//			if (typicalChunk.random.nextDouble() > oddsOfCentralPark)
//				return new ContextCentralPark(generator, this);
//			else
				return new ContextHighrise(generator, this);
		} else if (naturalPlats < 15)
			return new ContextUnconstruction(generator, this);
		else if (naturalPlats < 25)
			return new ContextMidrise(generator, this);
		else if (naturalPlats < 37)
			return new ContextCityCenter(generator, this);
		else if (naturalPlats < 50)
			return new ContextMall(generator, this);
		else if (naturalPlats < 65)
			return new ContextLowrise(generator, this);
		else if (naturalPlats < 80)
			return new ContextNeighborhood(generator, this);
		else 
		if (naturalPlats < 90)
			return new ContextFarm(generator, this);
		else if (naturalPlats < 100)
			return new ContextNeighborhood(generator, this);
		
		// otherwise just keep what we have
		else
			return context;
	}

	public Random getRandomGenerator() {
		return generator.getMacroRandomGeneratorAt(originX, originZ);
	}
	
	public Random getChunkRandomGenerator(SupportChunk chunk) {
		return generator.getMicroRandomGeneratorAt(chunk.chunkX, chunk.chunkZ);
	}
	
	public Random getChunkRandomGenerator(int chunkX, int chunkZ) {
		return generator.getMicroRandomGeneratorAt(chunkX, chunkZ);
	}
	
	public void generateChunk(ByteChunk chunk, BiomeGrid biomes) {

		// depending on the platchunk's type render a layer
		int platX = chunk.chunkX - originX;
		int platZ = chunk.chunkZ - originZ;
		
		PlatLot platlot = platLots[platX][platZ];
		if (platlot != null) {

			// do what we came here for
			platlot.generateChunk(generator, this, chunk, biomes, context, platX, platZ);
		}
	}
	
	public void generateBlocks(RealChunk chunk) {

		// depending on the platchunk's type render a layer
		int platX = chunk.chunkX - originX;
		int platZ = chunk.chunkZ - originZ;
		PlatLot platlot = platLots[platX][platZ];
		if (platlot != null) {

			// do what we came here for
			platlot.generateBlocks(generator, this, chunk, context, platX, platZ);
		}
	}
	
	public int getNumberOfRoads() {
		int result = 0;
		for (int x = 0; x < Width; x++) {
			for (int z = 0; z < Width; z++) {
				if (platLots[x][z] != null && platLots[x][z].style == LotStyle.ROAD)
					result++;
			}
		}
		return result;
	}
	
	public PlatLot getLot(int x, int z) {
		return platLots[x][z];
	}
	
	public boolean isEmptyLot(int x, int z) {
		if (x >= 0 && x < Width && z >= 0 && z < Width)
			return platLots[x][z] == null;
		else
			return true;
	}
	
	public void recycleLot(int x, int z) {

		// if it is not natural, make it so
		PlatLot current = platLots[x][z];
		if (current == null || current.style != LotStyle.NATURE) {
		
			// place nature
			platLots[x][z] = new PlatNature(this, originX + x, originZ + z);
			naturalPlats++;
		}
	}
	
	public void paveLot(int x, int z) {
		
		// clear it please
		emptyLot(x, z);
		
		// place the road
		platLots[x][z] = new PlatRoad(this, originX + x, originZ + z, generator.connectedKeyForPavedRoads);
	}
	
	public void setLot(int x, int z, PlatLot lot) {

		// clear it please
		emptyLot(x, z);
		
		// place the road
		platLots[x][z] = lot;
	}
	
	public void emptyLot(int x, int z) {
		
		// keep track of the nature count
		PlatLot current = platLots[x][z];
		if (current != null && current.style == LotStyle.NATURE)
			naturalPlats--;
		
		// empty this one out
		platLots[x][z] = null;
	}
	
	private void populateRoads(SupportChunk typicalChunk) {
		
		// place the big four
		placeIntersection(typicalChunk, PlatRoad.PlatMapRoadInset - 1, PlatRoad.PlatMapRoadInset - 1);
		placeIntersection(typicalChunk, PlatRoad.PlatMapRoadInset - 1, Width - PlatRoad.PlatMapRoadInset);
		placeIntersection(typicalChunk, Width - PlatRoad.PlatMapRoadInset, PlatRoad.PlatMapRoadInset - 1);
		placeIntersection(typicalChunk, Width - PlatRoad.PlatMapRoadInset, Width - PlatRoad.PlatMapRoadInset);
	}
	
	private void placeIntersection(SupportChunk typicalChunk, int x, int z) {
		boolean roadToNorth = false, roadToSouth = false, 
				roadToEast = false, roadToWest = false, 
				roadHere = false;
		
		// is there a road here?
		if (isEmptyLot(x, z)) {
		
			// are there roads from here?
			roadToNorth = isRoadTowards(typicalChunk, x, z, 0, -5);
			roadToSouth = isRoadTowards(typicalChunk, x, z, 0, 5);
			roadToEast = isRoadTowards(typicalChunk, x, z, 5, 0);
			roadToWest = isRoadTowards(typicalChunk, x, z, -5, 0);
			
			// is there a need for this intersection?
			if (roadToNorth || roadToSouth || roadToEast || roadToWest) {
			
				// are the odds in favor of a roundabout? AND..
				// are all the surrounding chunks empty (connecting roads shouldn't be there yet)
				if (generator.isRoundaboutAt(originX + x, originZ + z) &&
					isEmptyLot(x - 1, z - 1) && isEmptyLot(x - 1, z) &&	isEmptyLot(x - 1, z + 1) &&
					isEmptyLot(x, z - 1) &&	isEmptyLot(x, z + 1) &&
					isEmptyLot(x + 1, z - 1) &&	isEmptyLot(x + 1, z) &&	isEmptyLot(x + 1, z + 1)) {
					
					paveLot(x - 1, z - 1);
					paveLot(x - 1, z    );
					paveLot(x - 1, z + 1);
					
					paveLot(x    , z - 1);
					platLots[x][z] = new PlatStatue(this, originX + x, originZ + z);
					paveLot(x    , z + 1);
			
					paveLot(x + 1, z - 1);
					paveLot(x + 1, z    );
					paveLot(x + 1, z + 1);
				
				// place the intersection then
				} else {
					roadHere = true;
				}
			}
		
		// now figure out if we are within a bridge/tunnel
		} else {
			
			// are there roads from here?
			if (isBridgeTowardsNorth(typicalChunk, x, z) &&
				isBridgeTowardsSouth(typicalChunk, x, z)) {
				roadToNorth = true;
				roadToSouth = true;
				roadHere = true;
				
			} else if (isBridgeTowardsEast(typicalChunk, x, z) && 
					   isBridgeTowardsWest(typicalChunk, x, z)) {
				roadToEast = true;
				roadToWest = true;
				roadHere = true;
			}
		}
		
		// now place any remaining roads we need
		if (roadHere)
			paveLot(x, z);
		if (roadToNorth) {
			paveLot(x, z - 1);
			paveLot(x, z - 2);
		}
		if (roadToSouth) {
			paveLot(x, z + 1);
			paveLot(x, z + 2);
		}
		if (roadToEast) {
			paveLot(x + 1, z);
			paveLot(x + 2, z);
		}
		if (roadToWest) {
			paveLot(x - 1, z);
			paveLot(x - 2, z);
		}
	}
	
	private boolean isRoadTowards(SupportChunk typicalChunk, int x, int z, int deltaX, int deltaZ) {
		
		// is this a "real" spot?
		boolean result = HeightInfo.isBuildableAt(generator, (originX + x + deltaX) * typicalChunk.width,
									   			 			 (originZ + z + deltaZ) * typicalChunk.width);
		
		// if this isn't a buildable spot, is there a bridge or tunnel that gets us there?
		if (!result)
			result = isBridgeTowards(typicalChunk, x, z, deltaX, deltaZ);
		
		// report back
		return result;
	}
	
	public boolean isBridgeTowardsNorth(SupportChunk typicalChunk, int x, int z) {
		return isBridgeTowards(typicalChunk, x, z, 0, -5);
	}
	
	public boolean isBridgeTowardsSouth(SupportChunk typicalChunk, int x, int z) {
		return isBridgeTowards(typicalChunk, x, z, 0, 5);
	}
	
	public boolean isBridgeTowardsWest(SupportChunk typicalChunk, int x, int z) {
		return isBridgeTowards(typicalChunk, x, z, -5, 0);
	}
	
	public boolean isBridgeTowardsEast(SupportChunk typicalChunk, int x, int z) {
		return isBridgeTowards(typicalChunk, x, z, 5, 0);
	}
	
	private boolean isBridgeTowards(SupportChunk typicalChunk, int x, int z, int deltaX, int deltaZ) {
		
		// how far do we go?
		int offsetX = deltaX * typicalChunk.width;
		int offsetZ = deltaZ * typicalChunk.width;
		
		// where do we test?
		int chunkX = (originX + x) * typicalChunk.width;
		int chunkZ = (originZ + z) * typicalChunk.width;
		
		// what is the polarity of this spot
		boolean originPolarity = generator.getBridgePolarityAt(chunkX, chunkZ);
		boolean currentPolarity = originPolarity;
		
		// short cut things a bit by looking for impossible things
		if (originPolarity) {
			if (deltaX != 0)
				return false;
		} else {
			if (deltaZ != 0)
				return false;
		}
		
		// keep searching in the delta direction until polarity shifts
		while (originPolarity == currentPolarity) {
			
			// move it along a bit
			chunkX += offsetX;
			chunkZ += offsetZ;
			
			// keep going as long it is the same polarity
			currentPolarity = generator.getBridgePolarityAt(chunkX, chunkZ);
			
			// did we found a "real" spot and the polarity is still the same
			if (currentPolarity == originPolarity && HeightInfo.isBuildableAt(generator, chunkX, chunkZ))
				return true;
		};
		
		// we have failed to find a real bridge/tunnel
		return false;
	}
	
	private void validateRoads(SupportChunk typicalChunk) {
		
		// any roads leading out?
		if (!(isRoad(0, PlatRoad.PlatMapRoadInset - 1) ||
			  isRoad(0, Width - PlatRoad.PlatMapRoadInset) ||
			  isRoad(Width - 1, PlatRoad.PlatMapRoadInset - 1) ||
			  isRoad(Width - 1, Width - PlatRoad.PlatMapRoadInset) ||
			  isRoad(PlatRoad.PlatMapRoadInset - 1, 0) ||
			  isRoad(Width - PlatRoad.PlatMapRoadInset, 0) ||
			  isRoad(PlatRoad.PlatMapRoadInset - 1, Width - 1) ||
			  isRoad(Width - PlatRoad.PlatMapRoadInset, Width - 1))) {
			
			// reclaim all of the silly roads
			for (int x = 0; x < Width; x++) {
				for (int z = 0; z < Width; z++) {
					this.recycleLot(x, z);
				}
			}
			
		} else {
			
			//TODO any other validation?
		}
	}
	
	private boolean isRoad(int x, int z) {
		PlatLot current = platLots[x][z];
		return current != null && (current.style == LotStyle.ROAD || current.style == LotStyle.ROUNDABOUT);
	}
	
	public boolean isExistingRoad(int x, int z) {
		if (x >= 0 && x < Width && z >= 0 && z < Width)
			return isRoad(x, z);
		else
			return false;
	}
}
