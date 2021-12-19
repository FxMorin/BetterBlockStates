package ca.fxco.betterblockstates.common.classes;

import ca.fxco.betterblockstates.common.patches.BlockListener;
import ca.fxco.betterblockstates.common.patches.BlockTicker;
import ca.fxco.betterblockstates.common.patches.WorldListeners;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class StateSystem {

    protected static final Set<BlockState> COMBINED_STATES_ON_LOAD = new ObjectOpenHashSet<>();

    protected static final Set<BlockState> STATE_LISTENERS_ON_LOAD = new ObjectOpenHashSet<>();

    protected static final Map<Function<World,Boolean>,Set<BlockState>> STATE_TICKERS_ON_LOAD = new Object2ObjectOpenHashMap<>();
    protected static final Set<BlockState> STATE_TICKERS_ALL_ON_LOAD = new ObjectOpenHashSet<>();
    protected static final Map<World,Map<Function<World,Boolean>,Set<BlockPos>>> STATE_TICKERS = new Object2ObjectOpenHashMap<>();

    private static Map<Function<World, Boolean>, Set<BlockPos>> runningTickers;
    private static World currentWorld = null;

    public static Set<Supplier<Block>> blockListenersOnLoad = new ObjectOpenHashSet<>();
    public static Set<Supplier<Map<Function<World,Boolean>,Set<BlockState>>>> blockTickersOnLoad = new ObjectOpenHashSet<>();

    public static Set<BlockState> getAllBlockStateListeners() {
        return StateSystem.STATE_LISTENERS_ON_LOAD;
    }

    public static Map<Function<World,Boolean>,Set<BlockState>> getAllBlockStateTickers() {
        return StateSystem.STATE_TICKERS_ON_LOAD;
    }

    public static void addBlockListener(Block block) {
        Set<BlockState> blockStates = new HashSet<>(block.getStateManager().getStates());
        COMBINED_STATES_ON_LOAD.addAll(blockStates);
        StateSystem.STATE_LISTENERS_ON_LOAD.addAll(blockStates);
    }

    public static void addBlockTicker(Function<World,Boolean> canTick, Block block) {
        Set<BlockState> blockStates = new HashSet<>(block.getStateManager().getStates());
        COMBINED_STATES_ON_LOAD.addAll(blockStates);
        StateSystem.STATE_TICKERS_ON_LOAD.put(canTick,blockStates);
        StateSystem.STATE_TICKERS_ALL_ON_LOAD.addAll(blockStates);
    }

    public static void addBlockListeners(Set<Block> blocks) {
        blocks.forEach(StateSystem::addBlockListener);
    }

    public static void addBlockTickers(Function<World,Boolean> canTick, Set<Block> blocks) {
        Set<BlockState> blockStates = new ObjectOpenHashSet<>();
        blocks.forEach(block -> blockStates.addAll(block.getStateManager().getStates()));
        COMBINED_STATES_ON_LOAD.addAll(blockStates);
        StateSystem.STATE_TICKERS_ON_LOAD.put(canTick,blockStates);
        StateSystem.STATE_TICKERS_ALL_ON_LOAD.addAll(blockStates);
    }


    public static void addBlockStateTickers(Function<World,Boolean> canTick, Set<BlockState> states) {
        COMBINED_STATES_ON_LOAD.addAll(states);
        StateSystem.STATE_TICKERS_ON_LOAD.put(canTick, states);
        StateSystem.STATE_TICKERS_ALL_ON_LOAD.addAll(states);
    }

    public static void removeTicker(World world, BlockPos pos) {
        if (STATE_TICKERS.containsKey(world)) {
            STATE_TICKERS.get(world).values().forEach(locations -> locations.remove(pos));
            if (currentWorld == world) {
                runningTickers.values().forEach(locations -> locations.remove(pos));
            }
        }
    }

    public static void onInitialize() {
        StateSystem.blockListenersOnLoad.forEach(block -> StateSystem.addBlockListener(block.get()));
        StateSystem.blockTickersOnLoad.forEach(piece -> piece.get().forEach(StateSystem::addBlockStateTickers));
    }

    public static void onChunkLoad(World world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        for (ChunkSection section : chunk.getSectionArray()) {
            if (!section.isEmpty() && section.hasAny(COMBINED_STATES_ON_LOAD::contains)) {
                ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, ChunkSectionPos.getSectionCoord(section.getYOffset()));
                chunkSectionPos.streamBlocks().forEach((pos) -> {
                    BlockState blockState = section.getBlockState(ChunkSectionPos.getLocalCoord(pos.getX()), ChunkSectionPos.getLocalCoord(pos.getY()), ChunkSectionPos.getLocalCoord(pos.getZ()));
                    if (STATE_LISTENERS_ON_LOAD.contains(blockState)) {
                        ((BlockListener) blockState.getBlock()).createListener(world, chunk, chunkPos, pos.mutableCopy());
                    }
                    if (STATE_TICKERS_ALL_ON_LOAD.contains(blockState)) {
                        STATE_TICKERS_ON_LOAD.forEach((canTick,states) -> {
                            if (states.contains(blockState)) {
                                STATE_TICKERS.computeIfAbsent(world,(key) -> new HashMap<>()).computeIfAbsent(canTick,(key) -> new ObjectOpenHashSet<>()).add(pos);
                            }
                        });
                    }
                });
            }
        }
    }

    public static void onChunkUnload(World world, Chunk chunk) {
        ((WorldListeners)world).removeListenersChunk(chunk.getPos());
    }

    public static void onTick(World world) {
        if (STATE_TICKERS.containsKey(world)) {
            currentWorld = world;
            runningTickers = new HashMap<>(STATE_TICKERS.get(world));
            runningTickers.forEach((canTick,locations) -> {
                if (canTick.apply(world)) {
                    locations.forEach(pos -> {
                        if (world.shouldTickBlocksInChunk(ChunkPos.toLong(pos))) {
                            BlockState blockState = world.getBlockState(pos);
                            ((BlockTicker)blockState.getBlock()).tick(world, blockState, pos);
                        } else if (!world.isChunkLoaded(pos)) {
                            removeTicker(world,pos);
                        }
                    });
                }
            });
            currentWorld = null;
        }
    }
}