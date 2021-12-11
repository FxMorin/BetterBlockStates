package ca.fxco.betterblockstates.mixin.blocks.sculksensor;

import ca.fxco.betterblockstates.common.patches.TickableGameEventListener;
import ca.fxco.betterblockstates.common.patches.WorldListeners;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SculkSensorListener.class)
public abstract class SculkSensorEventListener_tickableMixin implements TickableGameEventListener {

    @Shadow protected Optional<GameEvent> event;
    @Shadow protected int delay;
    @Shadow @Final protected SculkSensorListener.Callback callback;
    @Shadow protected int distance;
    private final SculkSensorListener self = (SculkSensorListener)(Object)this;

    @Inject(
            method = "listen(Lnet/minecraft/world/World;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At("HEAD")
    )
    private void listen(World world, GameEvent event, BlockPos pos, BlockPos sourcePos, CallbackInfo ci) {
        ((WorldListeners)world).addTickingListener(self);
    }

    @Override
    public boolean canTick() {
        return this.event.isPresent();
    }

    @Override
    public void tick(World world) {
        --this.delay;
        if (this.delay <= 0) {
            this.delay = 0;
            this.callback.accept(world, this, this.event.get(), this.distance);
            this.event = Optional.empty();
        }
    }
}
