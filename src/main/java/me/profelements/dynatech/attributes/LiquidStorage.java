package me.profelements.dynatech.attributes;

import java.util.Objects;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.bakedlibs.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.core.attributes.ItemAttribute;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.profelements.dynatech.DynaTech;

/**
 * This interface, when attached to a class that inherits {@link SlimefunItem}, marks 
 * the item as an liquid containter.
 * This lets the item interact with registered liquids.
 *
 */
public interface LiquidStorage extends ItemAttribute {
    
    int getLiquidCapacity();

    default boolean isFillable() {
        return getLiquidCapacity() > 0;
    }

    default int getLiquidAmount(@Nonnull BlockPosition l) {
        if (!isFillable()) {
            return 0;
        }

        return getLiquidAmount(l, StorageCacheUtils.getBlock(l.toLocation()));
    }

    //TODO: Move from CSCorelib Config to Dough Config
    default int getLiquidAmount(@Nonnull BlockPosition l, SlimefunBlockData data) {
        Preconditions.checkNotNull(l, "Location was null");
        Preconditions.checkNotNull(data, "Config was null");

        if (!isFillable()) {
            return 0;
        }

        String fluidAmount = data.getData("fluid-amount");

        if (fluidAmount != null) {
            return Integer.parseInt(fluidAmount);
        } else {
            return 0;
        }
    }

    default String getLiquid(@Nonnull BlockPosition l) {
        return getLiquid(l, StorageCacheUtils.getBlock(l.toLocation()));
    }

    default String getLiquid(@Nonnull BlockPosition l, @Nonnull SlimefunBlockData data) {
        Preconditions.checkNotNull(l, "Location was null");
        Preconditions.checkNotNull(data, "Config was null");

        String fluidName = data.getData("fluid-name");

        return Objects.requireNonNullElse(fluidName, "NO_LIQUID");
    }

    default void setLiquidAmount(@Nonnull BlockPosition l, int liquidAmount) {
        Preconditions.checkNotNull("Location was null");
        Preconditions.checkArgument(liquidAmount >= 0, "The fluid amount must be set to 0 or more");

        try {
            int liquidCapacity = getLiquidCapacity();

            if (liquidCapacity > 0) {
                liquidAmount = NumberUtils.clamp(0, liquidAmount, liquidCapacity);

                if (liquidAmount != getLiquidAmount(l)) {
                    StorageCacheUtils.setData(l.toLocation(), "fluid-amount", String.valueOf(liquidAmount));
                }
            }
        } catch (Exception | LinkageError x) {
            DynaTech.getInstance().getLogger().log(Level.SEVERE, x,() -> "Exception while trying to set the fluid-amount for \"" + getId() + "\" at " + l);
        }
    }
    
    default void addLiquidAmount(@Nonnull BlockPosition l, int liquidAmount) {
        Preconditions.checkNotNull("Location was null");
        Preconditions.checkArgument(liquidAmount > 0, "The fluid amount must be greater then 0");

        try {
            int liquidCapacity = getLiquidCapacity();

            if (liquidCapacity > 0) {
                int currentLiquidAmount = getLiquidAmount(l);

                if (currentLiquidAmount < liquidCapacity) {
                    int newLiquidAmount = Math.min(liquidCapacity, currentLiquidAmount + liquidAmount);
                    StorageCacheUtils.setData(l.toLocation(), "fluid-amount", String.valueOf(newLiquidAmount));
                }
            }
        } catch (Exception | LinkageError x) {
            DynaTech.getInstance().getLogger().log(Level.SEVERE, x,() -> "Exception while trying to add an fluid-amount for \"" + getId() + "\" at " + l);
        }
    }

    default void removeLiquidAmount(@Nonnull BlockPosition l, int liquidAmount) {
        Preconditions.checkNotNull("Location was null");
        Preconditions.checkArgument(liquidAmount > 0, "The fluid amount must be greater then 0");
        
        try {
            int liquidCapacity = getLiquidCapacity();
            if (liquidCapacity > 0) {
                int currentLiquidAmount = getLiquidAmount(l);

                if (currentLiquidAmount > 0) {
                    int newLiquidAmount = Math.max(0, currentLiquidAmount - liquidAmount);
                    StorageCacheUtils.setData(l.toLocation(), "fluid-amount", String.valueOf(newLiquidAmount));
                }
            }

        } catch (Exception | LinkageError x) {
            DynaTech.getInstance().getLogger().log(Level.SEVERE, x,() -> "Exception while trying to add an fluid-amount for \"" + getId() + "\" at " + l);
        }

    }

    default void setLiquid(@Nonnull BlockPosition l, @Nullable String fluidName) {
        Preconditions.checkNotNull(l, "Location was null");

        try {
            int liquidCapacity = getLiquidCapacity();
            
            //changing fluids must happen when no other fluid is in the block
            if (liquidCapacity == 0) {
                if (fluidName == null) {
                    StorageCacheUtils.setData(l.toLocation(), "fluid-name", "NO_LIQUID");
                } else {
                    StorageCacheUtils.setData(l.toLocation(), "fluid-name", fluidName);
                }
            }
        } catch (Exception | LinkageError x) {
            DynaTech.getInstance().getLogger().log(Level.SEVERE, x,() -> "Exception while trying to set the fluid-name for \"" + getId() + "\" at " + l);
        }
    }
}
