package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.BlockState;
import java.util.Collections;
import java.util.List;

public class InventoryListener implements Listener {

    private final LockContainer plugin;

    public InventoryListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;

        Player player = (Player) event.getPlayer();
        List<Location> connectedContainers = plugin.getContainerManager().getConnectedContainers(loc);

        boolean anyLocked = connectedContainers.stream()
                .anyMatch(containerLoc -> plugin.getDataManager().isContainerLocked(containerLoc));

        if (!anyLocked) return;

        boolean isOwner = connectedContainers.stream()
                .anyMatch(containerLoc -> {
                    DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                    return data != null && data.owner.equals(player.getUniqueId());
                });

        boolean isTrusted = connectedContainers.stream()
                .anyMatch(containerLoc -> {
                    DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                    return data != null && data.trustedPlayers.contains(player.getUniqueId());
                });

        if (!isOwner && !isTrusted) {
            event.setCancelled(true);
            player.sendMessage("§c该容器组已被锁定，无访问权限！");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals("§0容器安全设置")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            ItemStack locationMarker = event.getInventory().getItem(0);
            if (locationMarker == null || !locationMarker.hasItemMeta() ||
                    !locationMarker.getItemMeta().getDisplayName().startsWith("§bLOCK_LOC:")) {
                player.sendMessage("§c系统错误：容器位置数据丢失");
                return;
            }

            String locString = locationMarker.getItemMeta().getDisplayName().substring(11);
            Location containerLoc = parseLocation(locString);
            if (containerLoc == null || containerLoc.getWorld() == null) {
                player.sendMessage("§c错误：容器位置无效或世界未加载");
                return;
            }

            if (clicked.getType() == Material.SKULL_ITEM) {
                plugin.getGUIHandler().promptAddPlayer(player, containerLoc);
            } else if (clicked.getType() == Material.REDSTONE_TORCH_ON) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null && data.owner.equals(player.getUniqueId())) {
                    boolean success = plugin.getDataManager().unlockContainer(containerLoc);
                    player.sendMessage(success ? "§a容器组已成功解锁！" : "§c解锁失败：容器组未被锁定");
                } else {
                    player.sendMessage("§c只有所有者可以解锁容器组！");
                }
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        InventoryHolder holder = event.getSource().getHolder();
        if (holder instanceof BlockState) {
            BlockState state = (BlockState) holder;
            Location fromLoc = state.getBlock().getLocation();
            if (plugin.getDataManager().isContainerLocked(fromLoc)) {
                event.setCancelled(true);
            }
        }
    }

    private Location parseLocation(String locString) {
        try {
            String[] parts = locString.split(",");
            if (parts.length != 4) {
                plugin.getLogger().warning("位置格式错误: " + locString + " (需要 world,x,y,z)");
                return null;
            }

            String worldName = parts[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("世界不存在: '" + worldName + "'");
                return null;
            }

            return new Location(
                    world,
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("坐标解析失败: " + locString + " | 错误: " + e.getMessage());
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("位置解析异常: " + locString + " | 错误: " + e.getMessage());
            return null;
        }
    }
}