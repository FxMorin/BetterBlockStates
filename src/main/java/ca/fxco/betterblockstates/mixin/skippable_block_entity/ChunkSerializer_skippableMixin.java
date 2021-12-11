package ca.fxco.betterblockstates.mixin.skippable_block_entity;

import ca.fxco.betterblockstates.common.patches.blockEntitySkipable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializer_skippableMixin {


    @Redirect(
            method = "method_39797(Lnet/minecraft/nbt/NbtList;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/nbt/NbtList;Lnet/minecraft/world/chunk/WorldChunk;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V"
            )
    )
    private static void getEntityLoadingCallback(WorldChunk instance, BlockEntity blockEntity) {
        if (!((blockEntitySkipable)(blockEntity)).shouldSkip()) {
            instance.setBlockEntity(blockEntity);
        }
    }
}
