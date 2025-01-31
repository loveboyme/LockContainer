package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.utils.ContainerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class BlockListener implements Listener {

    private final LockContainer plugin;

    public BlockListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();
        Player player = event.getPlayer();

        // **处理容器破坏**
        if (plugin.getContainerManager().isSupportedContainer(block)) {
            // 获取被破坏方块所属的完整容器组
            List<Location> connectedContainers = plugin.getContainerManager().getConnectedContainers(blockLoc);
            boolean isAnyLocked = false;
            DataManager.ContainerData dataForGroup = null; // 用于存储容器组的 Data

            for (Location containerLoc : connectedContainers) {
                if (plugin.getDataManager().isContainerLocked(containerLoc)) {
                    isAnyLocked = true;
                    dataForGroup = plugin.getDataManager().getContainerData(plugin.getContainerManager().getMainContainerLocation(containerLoc)); // 获取主容器Data
                    break; // 找到一个锁定的就跳出循环
                }
            }

            if (isAnyLocked) { // 如果容器组中任何一个部件被锁定
                if (dataForGroup != null) {
                    if (!dataForGroup.owner.equals(player.getUniqueId())) { // **如果破坏者不是所有者，直接阻止**
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "该容器组已被锁定，无法破坏！");
                        return;
                    } else { // **如果是所有者，检查锁牌**
                        if (!dataForGroup.signLocations.isEmpty()) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.YELLOW + "请先移除容器上的所有锁牌才能破坏容器！");
                            return;
                        }
                        // 如果没有锁牌，所有者可以破坏 (后续逻辑)
                    }
                }
            }
            // 如果容器组没有被锁定，则允许破坏 (后续逻辑)
        }

        // 处理锁牌破坏 (Sign breaking logic 保持不变)
        if (block.getType() == Material.WALL_SIGN && plugin.getDataManager().isSignLocked(blockLoc)) {
            Location containerLoc = ContainerUtils.findAttachedContainer(blockLoc, plugin);
            if (containerLoc != null) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null) {
                    if (data.owner.equals(player.getUniqueId())) {
                        data.signLocations.remove(blockLoc);
                        plugin.getDataManager().removeLockedSign(blockLoc);

                        if (data.signLocations.isEmpty()) {
                            plugin.getDataManager().unlockContainer(containerLoc);
                        }
                        plugin.getDataManager().saveAllData();
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "只有容器所有者才能移除锁牌!");
                    }
                }
            }
        }
    }
}