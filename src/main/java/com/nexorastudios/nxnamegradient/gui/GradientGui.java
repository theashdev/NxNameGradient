package com.nexorastudios.nxnamegradient.gui;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class GradientGui implements InventoryHolder {

    public static final NamespacedKey KEY_ACTION = new NamespacedKey("nxnamegradient", "action");
    public static final NamespacedKey KEY_ID = new NamespacedKey("nxnamegradient", "id");

    private static final List<Integer> CONTENT_SLOTS = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    );

    private final NxNameGradientPlugin plugin;
    private final Player player;
    private final int page;
    private final Inventory inv;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GradientGui(NxNameGradientPlugin plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = Math.max(0, page);

        int size = plugin.getConfigManager().gui().getInt("menu.size", 45);
        String baseTitle = plugin.getConfigManager().gui().getString("menu.title", "<gray>Gradients</gray>");

        this.inv = plugin.getServer().createInventory(
                this,
                size,
                mm.deserialize("<italic:false>" + baseTitle + " <dark_gray>(Page " + (this.page + 1) + ")</dark_gray>")
        );

        render();
    }

    public void open() {
        player.openInventory(inv);
    }

    public int getPage() {
        return page;
    }

    private void render() {
        var gui = plugin.getConfigManager().gui();

        // Border
        var border = gui.getConfigurationSection("menu.border");
        if (border != null && border.getBoolean("enabled", true)) {
            Material mat = Material.matchMaterial(border.getString("material", "BARRIER"));
            if (mat == null) mat = Material.BARRIER;

            ItemStack borderItem = new ItemStack(mat);
            ItemMeta m = borderItem.getItemMeta();
            m.displayName(mm.deserialize("<italic:false>" + border.getString("name", " ")));
            borderItem.setItemMeta(m);

            for (int slot : border.getIntegerList("slots")) {
                if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, borderItem);
            }
        }

        // Optional panes
        var panes = gui.getConfigurationSection("menu.panes");
        if (panes != null && panes.getBoolean("enabled", false)) {
            Material mat = Material.matchMaterial(panes.getString("material", "GRAY_STAINED_GLASS_PANE"));
            if (mat == null) mat = Material.GRAY_STAINED_GLASS_PANE;

            ItemStack pane = new ItemStack(mat);
            ItemMeta meta = pane.getItemMeta();
            meta.displayName(mm.deserialize("<italic:false>" + panes.getString("name", " ")));
            pane.setItemMeta(meta);

            for (int slot : panes.getIntegerList("slots")) {
                if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, pane);
            }
        }

        int perPage = Math.min(CONTENT_SLOTS.size(), Math.max(1, gui.getInt("menu.per_page", 21)));
        List<Gradient> all = plugin.getGradientManager().list();

        int start = page * perPage;
        int end = Math.min(start + perPage, all.size());

        String equipped = plugin.getDataManager().getCached(player.getUniqueId());

        int idx = 0;
        for (int i = start; i < end; i++) {
            Gradient g = all.get(i);
            boolean hasPerm = player.hasPermission(g.getPermission());
            boolean isEquipped = equipped != null && equipped.equalsIgnoreCase(g.getId());
            inv.setItem(CONTENT_SLOTS.get(idx++), buildGradientItem(g, hasPerm, isEquipped));
        }

        setNavItem("menu.page_back_item", "prev", page > 0);
        setNavItem("menu.page_forward_item", "next", end < all.size());
        setResetItem(); // reset/clear button
    }

    private void setNavItem(String path, String action, boolean show) {
        var sec = plugin.getConfigManager().gui().getConfigurationSection(path);
        if (sec == null) return;

        int slot = sec.getInt("slot", -1);
        if (slot < 0 || slot >= inv.getSize()) return;

        if (!show) {
            inv.setItem(slot, null);
            return;
        }

        Material mat = Material.matchMaterial(sec.getString("material", "ARROW"));
        if (mat == null) mat = Material.ARROW;

        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(mm.deserialize("<italic:false>" + sec.getString("name", " ")));
        meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
        it.setItemMeta(meta);

        inv.setItem(slot, it);
    }

    private void setResetItem() {
        var gui = plugin.getConfigManager().gui();

        // supports both keys (old + new)
        var sec = gui.getConfigurationSection("menu.reset_gradient_item");
        if (sec == null) sec = gui.getConfigurationSection("menu.clear_gradient_item");
        if (sec == null) return;

        int slot = sec.getInt("slot", 40);
        if (slot < 0 || slot >= inv.getSize()) return;

        Material mat = Material.matchMaterial(sec.getString("material", "BARRIER"));
        if (mat == null) mat = Material.BARRIER;

        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();

        meta.displayName(mm.deserialize("<italic:false>" + sec.getString("name", "<red>Reset Gradient</red>")));

        List<Component> lore = new ArrayList<>();
        for (String line : sec.getStringList("lore")) {
            lore.add(mm.deserialize("<italic:false>" + line));
        }
        if (!lore.isEmpty()) meta.lore(lore);

        meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "reset");
        it.setItemMeta(meta);

        inv.setItem(slot, it);
    }

    private ItemStack buildGradientItem(Gradient g, boolean hasPerm, boolean equipped) {
        var sec = plugin.getConfigManager().gui().getConfigurationSection("menu.gradient_item");

        Material mat = Material.NAME_TAG;
        if (sec != null) {
            Material cfg = Material.matchMaterial(sec.getString("material", "NAME_TAG"));
            if (cfg != null) mat = cfg;
        }

        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();

        boolean showTag = plugin.getConfig().getBoolean("gui.showgradientid", true);

        String start = g.getStartHex();
        String end = g.getEndHex();

        // ✅ Toggle "(id)" tag by config
        String idPart = showTag ? " <dark_gray>(" + g.getId() + ")</dark_gray>" : "";

        String display = "<italic:false><gradient:" + start + ":" + end + ">"
                + g.getName()
                + "</gradient>"
                + idPart;

        meta.displayName(mm.deserialize(display));

        List<Component> lore = new ArrayList<>();
        if (sec != null) {
            List<String> baseLore = hasPerm ? sec.getStringList("lore_permission") : sec.getStringList("lore_no_permission");
            for (String line : baseLore) lore.add(mm.deserialize("<italic:false>" + line));
            if (equipped) for (String line : sec.getStringList("lore_equipped")) lore.add(mm.deserialize("<italic:false>" + line));
        }
        meta.lore(lore);

        meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "equip");
        meta.getPersistentDataContainer().set(KEY_ID, PersistentDataType.STRING, g.getId());
        it.setItemMeta(meta);

        return it;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
