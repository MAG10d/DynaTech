package net.guizhanss.minecraft.dynatech.utils;

import javax.annotation.Nonnull;

public class FluidUtils {
    private FluidUtils() {
    }

    @Nonnull
    public static String getFluidType(@Nonnull String fluidName){
        return switch (fluidName) {
            case "WATER" -> "水";
            case "LAVA" -> "岩漿";
            case "NO_FLUID" -> "沒有液體";
            default -> fluidName;
        };
    }
}
