package ca.fxco.betterblockstates.common.patches;

import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;

public interface TickableGameEventListener extends GameEventListener {

    boolean canTick();

    void tick(World world);
}
