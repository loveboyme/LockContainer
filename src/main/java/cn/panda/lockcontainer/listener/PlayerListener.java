package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;

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
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && block != null && player.isSneaking()) {
            if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                Location containerLoc = findNearbyContainer(block.getLocation());
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
        if (plugin.getDataManager().addTrustedPlayer(loc, targetUUID)) {
            player.sendMessage("§a已添加玩家: " + displayName);
        } else {
            player.sendMessage("§c该玩家已在信任列表中");
        }
    }

    private Location findNearbyContainer(Location signLoc) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block block = signLoc.getBlock().getRelative(face);
            if (plugin.getContainerManager().isSupportedContainer(block)) {
                return block.getLocation();
            }
        }
        return null;
    }
}