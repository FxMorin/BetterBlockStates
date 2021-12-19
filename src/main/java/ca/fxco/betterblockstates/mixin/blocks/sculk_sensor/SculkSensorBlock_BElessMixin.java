package ca.fxco.betterblockstates.mixin.blocks.sculk_sensor;

import ca.fxco.betterblockstates.common.classes.StateSystem;
import ca.fxco.betterblockstates.common.patches.BlockListener;
import ca.fxco.betterblockstates.common.patches.WorldListeners;
import ca.fxco.betterblockstates.mixin.blockstate_listeners.WorldChunkGameEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static net.minecraft.block.SculkSensorBlock.*;
import static net.minecraft.block.entity.SculkSensorBlockEntity.getPower;

@Mixin(SculkSensorBlock.class)
public abstract class SculkSensorBlock_BElessMixin extends Block implements SculkSensorListener.Callback, BlockListener {

    @Shadow @Final private int range;

    private static final IntProperty LAST_FREQUENCY = IntProperty.of("last_frequency", 0, 15);

    public SculkSensorBlock_BElessMixin(Settings settings) {super(settings);}

    static {
        StateSystem.blockListenersOnLoad.add(() -> Blocks.SCULK_SENSOR);
    }


    @Inject(
            method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V",
                    shift = At.Shift.AFTER
            )
    )
    public void onBlockAddedCreateListener(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        WorldChunk chunk = world.getWorldChunk(pos);
        createListener(world, chunk, chunk.getPos(), pos);
    }


    @Inject(
            method = "onStateReplaced(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockWithEntity;onStateReplaced(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
                    shift = At.Shift.BEFORE
            )
    )
    public void onStateReplacedRemoveListener(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (!world.isClient()) {
            removeListener(world, world.getWorldChunk(pos), pos);
        }
    }


    @Redirect(
            method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/SculkSensorBlock;setDefaultState(Lnet/minecraft/block/BlockState;)V"
            )
    )
    protected final void setDefaultStateWithFrequency(SculkSensorBlock instance, BlockState blockState) {
        this.setDefaultState(blockState.with(LAST_FREQUENCY, 0));
    }


    @Inject(
            method = "createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void createBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(null);
    }


    @Inject(
            method = "getComparatorOutput(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getComparatorOutputNoEntity(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(getPhase(state) == SculkSensorPhase.ACTIVE ? state.get(LAST_FREQUENCY) : 0);
    }


    @Inject(
            method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void appendPropertiesPlusFrequency(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(new Property[]{SCULK_SENSOR_PHASE, POWER, WATERLOGGED, LAST_FREQUENCY});
        ci.cancel();
    }


    public boolean accepts(World world, GameEventListener listener, BlockPos pos, GameEvent event, Entity entity) {
        Optional<BlockPos> selfPos = listener.getPositionSource().getPos(world);
        if (selfPos.isEmpty()) return false;
        boolean bl = event == GameEvent.BLOCK_DESTROY && pos.equals(selfPos.get());
        boolean bl2 = event == GameEvent.BLOCK_PLACE && pos.equals(selfPos.get());
        return !bl && !bl2 && SculkSensorBlock.isInactive(world.getBlockState(selfPos.get()));
    }


    public void accept(World world, GameEventListener listener, GameEvent event, int distance) {
        Optional<BlockPos> pos = listener.getPositionSource().getPos(world);
        if (pos.isPresent()) {
            BlockState blockState = world.getBlockState(pos.get());
            if (!world.isClient() && SculkSensorBlock.isInactive(blockState)) {
                SculkSensorBlock.setActive(world, pos.get(), blockState.with(LAST_FREQUENCY,SculkSensorBlock.FREQUENCIES.getInt(event)), getPower(distance, listener.getRange()));
            }
        }
    }


    @Override
    public void createListener(World world, Chunk chunk, ChunkPos chunkPos, BlockPos pos) {
        GameEventDispatcher gameEventDispatcher = chunk.getGameEventDispatcher(ChunkSectionPos.getSectionCoord(pos.getY()));
        SculkSensorListener listener = new SculkSensorListener(new BlockPositionSource(pos), this.range, this);
        gameEventDispatcher.addListener(listener);
        ((WorldListeners)world).addListener(listener,chunkPos,pos);
    }


    @Override
    public void removeListener(World world, Chunk chunk, BlockPos pos) {
        int i = ChunkSectionPos.getSectionCoord(pos.getY());
        GameEventDispatcher gameEventDispatcher = chunk.getGameEventDispatcher(i);
        gameEventDispatcher.removeListener(((WorldListeners)world).removeListener(pos));
        if (gameEventDispatcher.isEmpty()) {
            ((WorldChunkGameEvents)chunk).getGameEventDispatchers().remove(i);
        }
    }
}
