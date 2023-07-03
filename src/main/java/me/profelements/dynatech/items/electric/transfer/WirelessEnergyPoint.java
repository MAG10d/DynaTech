package me.profelements.dynatech.items.electric.transfer;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.DynaTechItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WirelessEnergyPoint extends SlimefunItem implements EnergyNetProvider {

    private static final NamespacedKey WIRELESS_LOCATION_KEY = new NamespacedKey(DynaTech.getInstance(), "wireless-location");
    private final int capacity;
    private final int energyRate;

    @ParametersAreNonnullByDefault
    public WirelessEnergyPoint(ItemGroup itemGroup, int capacity, int energyRate, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        this.capacity = capacity;
        this.energyRate = energyRate;

        addItemHandler(onRightClick(), onBlockPlace());
    }

    @Override
    public int getGeneratedOutput(Location l, SlimefunBlockData data) {
        String wirelessBankLocation = data.getData("wireless-location");

        int chargedNeeded = getCapacity() - getCharge(l);

        if(chargedNeeded != 0 && wirelessBankLocation != null) {
            Location wirelessEnergyBank = StringToLocation(wirelessBankLocation);

            // Note: You should probably also see if the Future from getChunkAtAsync is finished here.
            // you don't really want to possibly trigger the chunk to load in another thread twice.
            if (!wirelessEnergyBank.getWorld().isChunkLoaded(wirelessEnergyBank.getBlockX() >> 4, wirelessEnergyBank.getBlockZ() >> 4)) {
                CompletableFuture<Chunk> chunkLoad = PaperLib.getChunkAtAsync(wirelessEnergyBank);
                if (!chunkLoad.isDone()) {
                    return 0;
                }
            }

            var bank = StorageCacheUtils.getSfItem(wirelessEnergyBank);
            if (bank != null && bank.getId().equals(DynaTechItems.WIRELESS_ENERGY_BANK.getItemId())) {
                int BankCharge = getCharge(wirelessEnergyBank);

                if (BankCharge > chargedNeeded) {
                    if (chargedNeeded > getEnergyRate()) {
                        removeCharge(wirelessEnergyBank, getEnergyRate());
                        return getEnergyRate();
                    }
                    removeCharge(wirelessEnergyBank, chargedNeeded);
                    return chargedNeeded;
                }

            }

        }
        return 0;
    }

    private ItemHandler onRightClick() {
        return new ItemUseHandler() {

            @Override
            public void onRightClick(PlayerRightClickEvent event) {

                Optional<Block> blockClicked = event.getClickedBlock();
                Optional<SlimefunItem> sfBlockClicked = event.getSlimefunBlock();
                if (blockClicked.isPresent() && sfBlockClicked.isPresent()) {
                    Location blockLoc = blockClicked.get().getLocation();
                    SlimefunItem sfBlock = sfBlockClicked.get();
                    ItemStack item = event.getItem();


                    if (sfBlock != null && Slimefun.getProtectionManager().hasPermission(event.getPlayer(), blockLoc, Interaction.INTERACT_BLOCK) && sfBlock.getId().equals(DynaTechItems.WIRELESS_ENERGY_BANK.getItemId()) && blockLoc != null) {
                        event.cancel();
                        ItemMeta im = item.getItemMeta();
                        String locationString = LocationToString(blockLoc);

                        PersistentDataAPI.setString(im, WIRELESS_LOCATION_KEY, locationString);
                        item.setItemMeta(im);
                        setItemLore(item, blockLoc);
                    }
                }
            }
        };
    }

    private ItemHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent event) {


                Location blockLoc = event.getBlockPlaced().getLocation();
                ItemStack item = event.getItemInHand();
                String locationString = PersistentDataAPI.getString(item.getItemMeta(), WIRELESS_LOCATION_KEY);

                if (item.getType() == DynaTechItems.WIRELESS_ENERGY_POINT.getType() && item.hasItemMeta() && locationString != null) {
                    StorageCacheUtils.setData(blockLoc, "wireless-location", locationString);

                }
            }

        };
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public int getEnergyRate() {
        return energyRate;
    }

    private void setItemLore(ItemStack item, Location l) {
        ItemMeta im = item.getItemMeta();
        List<String> lore = im.getLore();
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains("綁定位置: ")) {
                lore.remove(i);
            }
        }

        lore.add(ChatColor.WHITE + "綁定位置: " + l.getWorld().getName() + " " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ());

        im.setLore(lore);
        item.setItemMeta(im);

    }


    private String LocationToString(Location l) {
        return l.getWorld().getName()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ();
    }

    private Location StringToLocation (String str) {
            String[] locComponents = str.split(":");
            return new Location(Bukkit.getWorld(locComponents[0]), Double.parseDouble(locComponents[1]), Double.parseDouble(locComponents[2]), Double.parseDouble(locComponents[3]));
    }

}
