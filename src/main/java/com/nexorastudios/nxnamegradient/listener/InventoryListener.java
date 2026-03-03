package com.nexorastudios.nxnamegradient.listener;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import com.nexorastudios.nxnamegradient.gui.GradientGui;
import com.nexorastudios.nxnamegradient.service.BlacklistService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class InventoryListener implements Listener {

    private final NxNameGradientPlugin plugin;

    public InventoryListener(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof GradientGui gui)) return;

        e.setCancelled(true);

        var item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        String action = pdc.get(GradientGui.KEY_ACTION, PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "prev" -> {
                new GradientGui(plugin, player, Math.max(0, gui.getPage() - 1)).open();
                return;
            }
            case "next" -> {
                new GradientGui(plugin, player, gui.getPage() + 1).open();
                return;
            }
            case "reset" -> { // ✅ NEW RESET BUTTON SUPPORT
                plugin.getAnimationManager().stop(player);
                plugin.getNameService().clear(player);
                plugin.getDataManager().saveAsync(player.getUniqueId(), null);
                plugin.msg().send(player, "unequipped");
                player.closeInventory();
                return;
            }
        }

        if (!action.equals("equip")) return;

        String id = pdc.get(GradientGui.KEY_ID, PersistentDataType.STRING);
        if (id == null) return;

        Gradient g = plugin.getGradientManager().getById(id);
        if (g == null) {
            plugin.msg().send(player, "invalid_id", Map.of("<id>", id));
            return;
        }

        // Blacklist enforcement
        BlacklistService.BlockReason reason = plugin.getBlacklistService().check(player);
        if (reason != BlacklistService.BlockReason.NONE) {

            switch (reason) {
                case WORLD -> plugin.msg().send(player, "blacklisted_world");
                case REGION -> plugin.msg().send(player, "blacklisted_region");
                case PLAYER -> plugin.msg().send(player, "blacklisted_player");
                case PERMISSION -> plugin.msg().send(player, "blacklisted_permission");
                default -> plugin.msg().send(player, "blacklisted_permission");
            }

            plugin.getAnimationManager().stop(player);
            plugin.getNameService().clear(player);
            plugin.getDataManager().saveAsync(player.getUniqueId(), null);
            player.closeInventory();
            return;
        }

        // Permission check
        if (!player.hasPermission(g.getPermission())) {
            plugin.msg().send(player, "no_permission_gradient",
                    Map.of("<gradient>", g.getName(), "<id>", g.getId()));
            return;
        }

        // Apply + save
        plugin.getAnimationManager().apply(player, g);
        plugin.getDataManager().saveAsync(player.getUniqueId(), g.getId());
        plugin.msg().send(player, "equipped", Map.of("<gradient>", g.getName(), "<id>", g.getId()));
        player.closeInventory();
    }
}