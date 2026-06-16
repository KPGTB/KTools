package com.github.kpgtb.ktools.manager.gui.listener;

import com.github.kpgtb.ktools.manager.gui.KGui;
import com.github.kpgtb.ktools.manager.gui.action.ClickLocation;
import com.github.kpgtb.ktools.manager.gui.container.GuiContainer;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.gui.item.GuiItemLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MenuListener implements Listener {
	private static volatile MenuListener INSTANCE;

	private final JavaPlugin plugin;
	private final ConcurrentMap<UUID, KGui> openedMenus;

	private MenuListener(JavaPlugin plugin) {
		this.plugin = plugin;
		this.openedMenus = new ConcurrentHashMap<>();

		plugin.getServer()
			.getPluginManager()
			.registerEvents(this, plugin);
	}

	public static MenuListener getInstance(JavaPlugin plugin) {
		if (INSTANCE == null) {
			synchronized (MenuListener.class) {
				if (INSTANCE == null) INSTANCE = new MenuListener(plugin);
			}
		}
		return INSTANCE;
	}

	public void addViewer(Player player, KGui menu) {
		this.openedMenus.put(player.getUniqueId(), menu);
	}

	public void removeViewer(Player player, KGui menu) {
		this.openedMenus.remove(player.getUniqueId(), menu);
	}

	public void removeViewer(Player player) {
		this.openedMenus.remove(player.getUniqueId());
	}

	public KGui getMenu(Player player) {
		return this.openedMenus.get(player.getUniqueId());
	}

	@EventHandler
	public void onGlobalClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		KGui menu = this.getMenu(player);
		if (menu == null) return;

		Inventory inv = event.getInventory();
		if(!inv.equals(menu.getBukkitInventory())) return;

		Inventory clickedInv = event.getClickedInventory();

		if(menu.getGlobalClickAction() != null) {
			ClickLocation clickLocation = clickedInv == null ? ClickLocation.OUTSIDE : clickedInv.equals(menu.getBukkitInventory()) ? ClickLocation.TOP : ClickLocation.BOTTOM;
			menu.getGlobalClickAction().run(event,clickLocation);
		}

		if(clickedInv != menu.getBukkitInventory()) return;

		int slot = event.getSlot();
		GuiContainer container = menu.getContainerAt(slot);
		if(container == null) return;

		GuiItem item = container.getItem(container.getContainerLocFromGuiLoc(slot));
		if(item == null) return;

		if(item.getClickAction() != null) {
			item.getClickAction().run(event, ClickLocation.TOP);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUpdate(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		KGui menu = this.getMenu(player);
		if (menu == null) return;

		if(!menu.isUpdateItems()) return;

		Inventory inv = event.getInventory();
		if(!inv.equals(menu.getBukkitInventory())) return;

		new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = 0; i < inv.getContents().length; i++) {
					ItemStack realIS = inv.getItem(i);
					GuiContainer container = menu.getContainerAt(i);
					if(container == null) {
						continue;
					}
					GuiItemLocation loc = container.getContainerLocFromGuiLoc(i);
					GuiItem guiItem = container.getItem(loc);

					if(guiItem == null) {
						if(realIS != null && !realIS.getType().equals(Material.AIR)) {
							container.setItem(loc.getX(), loc.getY(), new GuiItem(realIS));
						}
						continue;
					}

					if(realIS == null || realIS.getType().equals(Material.AIR)) {
						container.removeItem(loc.getX(),loc.getY());
						continue;
					}

					if(guiItem.getItemStack().isSimilar(realIS)) {
						continue;
					}

					guiItem.setItemStack(realIS);
				}
			}
		}.runTaskLater(plugin,3);
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
		KGui menu = this.getMenu(player);
		if (menu == null) return;

		Inventory inv = event.getInventory();
		if(inv != menu.getBukkitInventory()) return;

		if(menu.getGlobalDragAction() != null) {
			menu.getGlobalDragAction().run(event);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		KGui menu = this.getMenu(player);
		if (menu == null) return;

		Inventory inv = event.getInventory();
		if(inv != menu.getBukkitInventory()) return;

		this.removeViewer(player, menu);
		if(menu.getCloseAction() != null) {
			menu.getCloseAction().run(event);
		}
	}
}
