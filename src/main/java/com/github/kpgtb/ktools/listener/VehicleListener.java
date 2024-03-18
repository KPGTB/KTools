package com.github.kpgtb.ktools.listener;

import com.github.kpgtb.ktools.manager.listener.KListener;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleListener extends KListener {
    private final BarManager barManager;
    private final JavaPlugin plugin;

    public VehicleListener(ToolsObjectWrapper wrapper) {
        super(wrapper);
        this.barManager = wrapper.getBarManager();
        this.plugin = wrapper.getPlugin();
    }

    @EventHandler
    public void onEnter(VehicleEnterEvent event) {
        if(!(event.getEntered() instanceof Player)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                barManager.updateBars((Player) event.getEntered());
            }
        }.runTaskLater(plugin,3L);
    }

    @EventHandler
    public void onExit(VehicleExitEvent event) {
        if(!(event.getExited() instanceof Player)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                barManager.updateBars((Player) event.getExited());
            }
        }.runTaskLater(plugin,3L);
    }
}
