package ca.fxco.betterblockstates.common.patches;

public interface blockEntitySkipable {

    default boolean shouldSkip() {
        return false;
    }
}
