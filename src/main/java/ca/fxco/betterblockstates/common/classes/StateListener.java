package ca.fxco.betterblockstates.common.classes;

import ca.fxco.betterblockstates.common.patches.BlockListener;
import ca.fxco.betterblockstates.common.patches.WorldListeners;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.*;
import java.util.function.Supplier;

public class StateListener {

    protected static final Set<BlockState> TICK_STATES_ON_LOAD = new ObjectOpenHashSet<>();

    public static Set<Supplier<Block>> blocksOnLoad = new HashSet<>();

    public static Set<BlockState> getAllBlockStates() {
        return StateListener.TICK_STATES_ON_LOAD;
    }

    public static void addBlock(Block block) {
        StateListener.TICK_STATES_ON_LOAD.addAll(new HashSet<>(block.getStateManager().getStates()));
    }

    public static void addBlocks(Set<Block> blocks) {
        blocks.forEach(StateListener::addBlock);
    }

    public static void addBlockState(BlockState state) {
        StateListener.addBlock(state.getBlock());
    }

    public static void addBlockStates(Set<BlockState> states) {
        states.forEach(state -> StateListener.addBlock(state.getBlock()));
    }

    public static void onInitialize() {
        StateListener.blocksOnLoad.forEach(block -> StateListener.addBlock(block.get()));
    }

    public static void onChunkLoad(World world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        for (ChunkSection section : chunk.getSectionArray()) {
            if (!section.isEmpty() && section.hasAny(TICK_STATES_ON_LOAD::contains)) {
                ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, ChunkSectionPos.getSectionCoord(section.getYOffset()));
                chunkSectionPos.streamBlocks().forEach((pos) -> {
                    BlockState blockState = section.getBlockState(ChunkSectionPos.getLocalCoord(pos.getX()), ChunkSectionPos.getLocalCoord(pos.getY()), ChunkSectionPos.getLocalCoord(pos.getZ()));
                    if (TICK_STATES_ON_LOAD.contains(blockState))((BlockListener)blockState.getBlock()).createListener(world,chunk,chunkPos,pos.mutableCopy());
                });
            }
        }
    }

    public static void onChunkUnload(World world, Chunk chunk) {
        ((WorldListeners)world).removeListenersChunk(chunk.getPos());
    }
}