package ca.fxco.betterblockstates.mixin.blockstate_listeners;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.listener.GameEventDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldChunk.class)
public interface WorldChunkGameEvents {
    @Accessor("gameEventDispatchers")
    public Int2ObjectMap<GameEventDispatcher> getGameEventDispatchers();
}
