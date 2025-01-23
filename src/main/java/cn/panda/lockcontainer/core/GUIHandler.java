package cn.panda.lockcontainer.core;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.Collections;

public class GUIHandler {

    private final LockContainer plugin;

    public GUIHandler(LockContainer plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, Location containerLoc) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
        if (data == null || !data.owner.equals(player.getUniqueId())) {
            player.sendMessage("§c错误：你不是该容器的所有者！");
            return;
        }

        if (containerLoc == null || containerLoc.getWorld() == null) {
            player.sendMessage("§c错误：无效的容器位置");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, "§0容器安全设置");
        ItemStack locationMarker = createLocationMarker(containerLoc);
        gui.setItem(0, locationMarker);

        ItemStack addPlayerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta addMeta = addPlayerItem.getItemMeta();
        addMeta.setDisplayName("§a添加信任玩家");
        addPlayerItem.setItemMeta(addMeta);
        gui.setItem(11, addPlayerItem);

        ItemStack unlockItem = new ItemStack(Material.REDSTONE_TORCH_ON);
        ItemMeta unlockMeta = unlockItem.getItemMeta();
        unlockMeta.setDisplayName("§c解锁容器");
        unlockMeta.setLore(Collections.singletonList("§7点击后解除锁定"));
        unlockItem.setItemMeta(unlockMeta);
        gui.setItem(15, unlockItem);

        player.openInventory(gui);
    }

    private ItemStack createLocationMarker(Location loc) {
        ItemStack marker = new ItemStack(Material.PAPER);
        ItemMeta meta = marker.getItemMeta();
        meta.setDisplayName("§bLOCK_LOC:" + locToString(loc));
        marker.setItemMeta(meta);
        return marker;
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();
    }

    public void promptAddPlayer(Player player, Location location) {
        player.closeInventory();
        player.sendMessage("§a请在聊天栏输入要添加的玩家名称：");
        plugin.getContainerManager().setAddingPlayer(player, location);
    }
}