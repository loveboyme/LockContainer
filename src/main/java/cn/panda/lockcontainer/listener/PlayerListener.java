package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.utils.ContainerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;

import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final LockContainer plugin;

    public PlayerListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        if ((block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) && player.isSneaking()) {
            Location containerLoc = ContainerUtils.findAttachedContainer(block.getLocation(), plugin);
            if (containerLoc != null && plugin.getDataManager().isContainerLocked(containerLoc)) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null) {
                    boolean isOwner = player.getUniqueId().equals(data.owner);
                    boolean isTrusted = data.trustedPlayers.contains(player.getUniqueId());

                    if (isOwner) {
                        plugin.getGUIHandler().openMainMenu(player, containerLoc);
                        event.setCancelled(true);
                    } else if (isTrusted) {
                        player.sendMessage("§a你已被信任，但无法修改容器设置");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Location loc = plugin.getContainerManager().getAddingPlayer(player);

        if (loc != null) {
            event.setCancelled(true);

            DataManager.ContainerData data = plugin.getDataManager().getContainerData(loc);
            if (data == null || !data.owner.equals(player.getUniqueId())) {
                player.sendMessage("§c权限错误：只有容器所有者可以添加信任！");
                plugin.getContainerManager().removeAddingPlayer(player);
                return;
            }

            String playerName = event.getMessage().trim();
            Player onlineTarget = Bukkit.getPlayerExact(playerName);

            if (onlineTarget != null) {
                handleAddPlayer(player, loc, onlineTarget.getUniqueId(), onlineTarget.getName());
            } else {
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(playerName);
                if (offlineTarget.hasPlayedBefore()) {
                    handleAddPlayer(player, loc, offlineTarget.getUniqueId(), playerName);
                } else {
                    player.sendMessage("§c玩家不存在!");
                }
            }

            plugin.getContainerManager().removeAddingPlayer(player);
            plugin.getGUIHandler().openMainMenu(player, loc);
        }
    }

    private void handleAddPlayer(Player player, Location loc, UUID targetUUID, String displayName) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(loc);
        if (data == null) return;

        // 检查是否尝试添加自己
        if (targetUUID.equals(player.getUniqueId())) {
            player.sendMessage("§c您已经是容器的所有者，无需将自己添加到信任列表。");
            return; // 直接返回，不执行添加操作
        }

        List<Location> connectedContainers = plugin.getContainerManager().getConnectedContainers(loc);
        int addedCount = 0;

        for (Location containerLoc : connectedContainers) {
            if (plugin.getDataManager().addTrustedPlayer(containerLoc, targetUUID)) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            player.sendMessage("§a已将玩家 " + displayName + " 添加到容器组的 " + addedCount + " 个部件");
        } else {
            player.sendMessage("§c该玩家已在所有关联容器的信任列表中");
        }
    }
}