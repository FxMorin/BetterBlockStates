package ca.fxco.betterblockstates.common.patches;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.event.listener.GameEventListener;

public interface WorldListeners {

    public void addListener(GameEventListener listener, ChunkPos chunkPos, BlockPos pos);

    public void addListener(GameEventListener listener, BlockPos pos);

    public void addTickingListener(GameEventListener listener);

    public GameEventListener removeListener(ChunkPos chunkPos, BlockPos pos);

    public GameEventListener removeListener(BlockPos pos);

    public void removeTickingListener(GameEventListener listener);

    public void tickListeners();
}
