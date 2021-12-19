package ca.fxco.betterblockstates.mixin.blockstate_tickers;

import ca.fxco.betterblockstates.common.classes.StateSystem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class World_stateTickerMixin {

    @Shadow public abstract boolean shouldTickBlocksInChunk(long chunkPos);

    private final World self = (World)(Object)this;

    @Inject(
            method = "tickBlockEntities()V",
            at = @At(
                value = "HEAD",
                shift = At.Shift.AFTER
            )
    )
    protected void tickBlockTickers(CallbackInfo ci) {
        StateSystem.onTick(self);
    }
}
