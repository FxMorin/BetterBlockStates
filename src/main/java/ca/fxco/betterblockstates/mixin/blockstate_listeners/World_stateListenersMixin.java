package ca.fxco.betterblockstates.mixin.blockstate_listeners;

import ca.fxco.betterblockstates.common.patches.TickableGameEventListener;
import ca.fxco.betterblockstates.common.patches.WorldListeners;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(World.class)
public abstract class World_stateListenersMixin implements WorldListeners {

    @Shadow public abstract boolean shouldTickBlocksInChunk(long chunkPos);

    private final World self = (World)(Object)this;

    private final HashMap<ChunkPos,HashMap<BlockPos,GameEventListener>> blockListeners = new HashMap<>();
    private final HashSet<GameEventListener> tickingBlockListeners = new HashSet<>();
    private final List<GameEventListener> pendingBlockListeners = new ArrayList<>();
    private boolean iteratingTickingBlockListeners;

    @Inject(
            method = "tickBlockEntities()V",
            at = @At("HEAD")
    )
    protected void tickBlockEntities(CallbackInfo ci) {
        this.tickListeners();
    }

    @Override
    public void addListener(GameEventListener listener, ChunkPos chunkPos, BlockPos pos) {
        blockListeners.computeIfAbsent(chunkPos, cp -> new HashMap<>()).put(pos, listener);
    }

    @Override
    public void addListener(GameEventListener listener, BlockPos pos) {
        this.addListener(listener,new ChunkPos(pos),pos);
    }

    @Override
    public void addTickingListener(GameEventListener listener) {
        if (iteratingTickingBlockListeners) {
            tickingBlockListeners.add(listener);
        } else {
            pendingBlockListeners.add(listener);
        }
    }

    @Override
    public GameEventListener removeListener(ChunkPos chunkPos, BlockPos pos) {
        HashMap<BlockPos, GameEventListener> map = blockListeners.get(chunkPos);
        if (map != null) {
            return map.remove(pos);
        }
        return null;
    }

    @Override
    public void removeListenersChunk(ChunkPos chunkPos) {
        blockListeners.remove(chunkPos);
    }

    @Override
    public GameEventListener removeListener(BlockPos pos) {
        return this.removeListener(new ChunkPos(pos),pos);
    }

    @Override
    public void removeTickingListener(GameEventListener listener) {
        tickingBlockListeners.remove(listener);
        pendingBlockListeners.remove(listener);
    }

    @Override
    public void tickListeners() {
        this.iteratingTickingBlockListeners = true;
        if (!this.pendingBlockListeners.isEmpty()) {
            this.tickingBlockListeners.addAll(this.pendingBlockListeners);
            this.pendingBlockListeners.clear();
        }
        Iterator iterator = this.tickingBlockListeners.iterator();
        while(iterator.hasNext()) {
            TickableGameEventListener eventListener = (TickableGameEventListener)iterator.next();
            if (eventListener.canTick()) {
                Optional<BlockPos> pos = eventListener.getPositionSource().getPos(self);
                if (pos.isEmpty()) {
                    iterator.remove();
                } else {
                    if (this.shouldTickBlocksInChunk(ChunkPos.toLong(pos.get()))) {
                        eventListener.tick(self);
                    }
                }
            } else {
                iterator.remove();
            }
        }
        this.iteratingTickingBlockListeners = false;
        //tickingBlockListeners.forEach((chunkPos, value) -> value.forEach((blockPos, listener) -> ((TickableGameEventListener)listener).tick(self)));
    }
}
