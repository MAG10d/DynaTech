package net.guizhanss.minecraft.dynatech.utils;

import javax.annotation.Nonnull;

public class ItemBandUtils {
    private ItemBandUtils() {
    }

    @Nonnull
    public static String getBandaid(@Nonnull String effect){
        switch(effect){
            case "FAST_DIGGING":
                return "急迫";
            case "HEALTH_BOOST":
                return "生命提升";
            default:
                return effect;
        }
    }
}
