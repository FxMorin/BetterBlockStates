package ca.fxco.betterblockstates.common.patches;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface BlockListener {
    void createListener(World world, Chunk chunk, ChunkPos chunkPos, BlockPos pos);
    void removeListener(World world, Chunk chunk, BlockPos pos);
}
