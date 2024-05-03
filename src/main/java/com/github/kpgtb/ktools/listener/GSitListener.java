package com.github.kpgtb.ktools.listener;

import com.github.kpgtb.ktools.manager.listener.KListener;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import dev.geco.gsit.api.event.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GSitListener extends KListener {
    private final BarManager barManager;
    private final JavaPlugin plugin;

    public GSitListener(ToolsObjectWrapper wrapper) {
        super(wrapper);
        this.barManager = wrapper.getBarManager();
        this.plugin = wrapper.getPlugin();
    }

    @EventHandler
    public void on(EntitySitEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        handle((Player) event.getEntity());
    }

    @EventHandler
    public void on(EntityGetUpSitEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        handle((Player) event.getEntity());
    }

    @EventHandler
    public void on(PlayerPoseEvent event) {
        handle(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerGetUpPoseEvent event) {
        handle(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerPlayerSitEvent event) {
        handle(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerGetUpPlayerSitEvent event) {
        handle(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerCrawlEvent event) {
        handle(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerGetUpCrawlEvent event) {
        handle(event.getPlayer());
    }

    public void handle(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                barManager.updateBars(player);
            }
        }.runTaskLater(plugin,5L);
    }

}
