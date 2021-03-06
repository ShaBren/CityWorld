package me.daddychurchill.CityWorld.Plats;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

import me.daddychurchill.CityWorld.PlatMap;
import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.ContextData;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.HouseFactory;
import me.daddychurchill.CityWorld.Support.RealChunk;

public class PlatHouse extends PlatIsolated {

	public PlatHouse(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);
		
		style = LotStyle.STRUCTURE;
	}

	@Override
	protected int getTopStrataY(WorldGenerator generator, int blockX, int blockZ) {
		return generator.sidewalkLevel - 1;
	}

	@Override
	protected void generateActualChunk(WorldGenerator generator, PlatMap platmap, ByteChunk chunk, BiomeGrid biomes, ContextData context, int platX, int platZ) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void generateActualBlocks(WorldGenerator generator, PlatMap platmap, RealChunk chunk, ContextData context, int platX, int platZ) {

		// ground please
		chunk.setLayer(generator.sidewalkLevel, Material.GRASS);
		
		// now make a house
		HouseFactory.generateHouse(chunk, context, chunkRandom, generator.sidewalkLevel + 1, 2);
	}

}
