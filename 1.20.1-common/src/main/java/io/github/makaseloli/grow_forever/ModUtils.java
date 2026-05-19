package io.github.makaseloli.grow_forever;

import net.minecraft.resources.ResourceLocation;

public final class ModUtils {
    private ModUtils() {}

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(Constants.MODID, path);
    }
}
