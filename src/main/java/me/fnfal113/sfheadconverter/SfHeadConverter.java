package me.fnfal113.sfheadconverter;

import io.github.bakedlibs.dough.updater.BlobBuildUpdater;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.fnfal113.sfheadconverter.commands.ScanChunk;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SfHeadConverter extends JavaPlugin implements SlimefunAddon {

    private static SfHeadConverter instance;

    @Override
    public void onEnable() {
        setInstance(this);
        
        new Metrics(this, 24102);

        getLogger().info("*******************************************************");
        getLogger().info("*        SfHeadConverter - Created by FN_FAL113       *");
        getLogger().info("*                 Addon for Slimefun                  *");
        getLogger().info("* Convert player heads to normal blocks for fps boost *");
        getLogger().info("*******************************************************");


        Objects.requireNonNull(getCommand("sfheadconverter")).setExecutor(new ScanChunk());

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        if (getConfig().getBoolean("auto-update", true) && getDescription().getVersion().startsWith("Dev - ")) {
            new BlobBuildUpdater(this, getFile(), "SfHeadConverter").start();
        }
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "";
    }

    private static void setInstance(SfHeadConverter ins) {
        instance = ins;
    }

    public static SfHeadConverter getInstance() {
        return instance;
    }

}
