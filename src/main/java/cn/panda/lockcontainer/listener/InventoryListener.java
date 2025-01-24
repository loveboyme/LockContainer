package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.core.GUIHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.block.BlockState;
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
            player.sendMessage(ChatColor.RED + "该容器组已被锁定，无访问权限！");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        if (title.startsWith(ChatColor.BLACK + "容器安全设置 " + ChatColor.GRAY + "(主菜单)")) {
            event.setCancelled(true);
            handleMainMenuClick(event, player, inv);
        } else if (title.startsWith(ChatColor.BLACK + "信任成员管理 " + ChatColor.GRAY + "(第 ")) {
            event.setCancelled(true);
            handleMemberMenuClick(event, player, inv);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player, Inventory inv) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Location containerLoc = parseLocationMarker(inv.getItem(0));
        if (containerLoc == null) return;

        if (clicked.getType() == Material.SKULL_ITEM && clicked.getDurability() == 3) {
            plugin.getGUIHandler().openMemberMenu(player, containerLoc, 0);
        } else if (clicked.getType() == Material.EMERALD) {
            plugin.getGUIHandler().promptAddPlayer(player, containerLoc);
        } else if (clicked.getType() == Material.REDSTONE_TORCH_ON) {
            DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
            if (data != null && data.owner.equals(player.getUniqueId())) {
                boolean success = plugin.getDataManager().unlockContainer(containerLoc);
                player.sendMessage(success ? ChatColor.GREEN + "容器组已成功解锁！" : ChatColor.RED + "解锁失败：容器组未被锁定");
            }
            player.closeInventory();
        }
    }

    private void handleMemberMenuClick(InventoryClickEvent event, Player player, Inventory inv) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Location containerLoc = parseLocationMarker(inv.getItem(0));
        if (containerLoc == null) return;

        GUIHandler guiHandler = plugin.getGUIHandler();
        int currentPage = guiHandler.getCurrentPage(player.getUniqueId()); // 使用公共方法获取页码

        if (clicked.getType() == Material.PAPER) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName.equals(ChatColor.GREEN + "上一页")) {
                    guiHandler.openMemberMenu(player, containerLoc, currentPage - 1);
                } else if (displayName.equals(ChatColor.GREEN + "下一页")) {
                    guiHandler.openMemberMenu(player, containerLoc, currentPage + 1);
                }
            }
        } else if (clicked.getType() == Material.ARROW) {
            guiHandler.openMainMenu(player, containerLoc);
        } else if (clicked.getType() == Material.SKULL_ITEM && event.isRightClick()) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            OfflinePlayer target = meta.getOwningPlayer();

            if (target != null) {
                int removed = 0;
                List<Location> connected = plugin.getContainerManager().getConnectedContainers(containerLoc);
                for (Location loc : connected) {
                    if (plugin.getDataManager().removeTrustedPlayer(loc, target.getUniqueId())) {
                        removed++;
                    }
                }

                if (removed > 0) {
                    player.sendMessage(ChatColor.GREEN + "已从 " + removed + " 个容器中移除 " + target.getName());
                    guiHandler.openMemberMenu(player, containerLoc, currentPage);
                } else {
                    player.sendMessage(ChatColor.RED + "该玩家不在信任列表中");
                }
            }
        }
    }

    private Location parseLocationMarker(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.startsWith(ChatColor.AQUA + "LOCK_LOC:")) return null;
        return plugin.getGUIHandler().parseLocation(displayName.substring(11));
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
}