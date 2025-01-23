package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.utils.ContainerUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import java.util.List;

public class SignListener implements Listener {

    private final LockContainer plugin;

    public SignListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block signBlock = event.getBlock();
        String[] lines = event.getLines();
        Location containerLoc = ContainerUtils.findAttachedContainer(signBlock.getLocation(), plugin);

        if (containerLoc == null || !plugin.getContainerManager().isSupportedContainer(containerLoc.getBlock())) {
            return;
        }

        // 获取所有连接的容器
        List<Location> connectedContainers = plugin.getContainerManager().getConnectedContainers(containerLoc);

        // 检查整个容器组是否已被锁定
        boolean hasExistingLock = false;
        for (Location loc : connectedContainers) {
            if (plugin.getDataManager().isContainerLocked(loc)) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(loc);
                if (data != null && !data.owner.equals(player.getUniqueId())) {
                    hasExistingLock = true;
                    break;
                }
            }
        }

        if (hasExistingLock) {
            event.setCancelled(true);
            player.sendMessage("§c该容器组已被其他玩家锁定！");
            return;
        }

        if (lines[0].isEmpty() || lines[0].equalsIgnoreCase("[lock]")) {
            // 锁定整个容器组
            boolean success = true;
            for (Location loc : connectedContainers) {
                if (!plugin.getDataManager().lockContainer(loc, player.getUniqueId())) {
                    success = false;
                    break;
                }
            }

            if (success) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null) {
                    data.addSignLocation(signBlock.getLocation());
                }
                event.setLine(0, "§c[已锁定]");
                event.setLine(1, "§7所有者: " + player.getName());
                player.sendMessage("§a容器组锁定成功!");
            } else {
                event.setCancelled(true);
                player.sendMessage("§c部分容器锁定失败，请检查容器状态！");
            }
        }
    }
}