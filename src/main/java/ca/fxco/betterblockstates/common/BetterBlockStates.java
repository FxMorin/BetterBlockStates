package ca.fxco.betterblockstates.common;

import ca.fxco.betterblockstates.common.classes.StateSystem;
import ca.fxco.betterblockstates.common.config.Config;
import net.fabricmc.api.ModInitializer;

public class BetterBlockStates implements ModInitializer {
    public static Config CONFIG;

    @Override
    public void onInitialize() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
        StateSystem.onInitialize();
    }
}
