package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.utils.ContainerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    private final LockContainer plugin;

    public BlockListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location blockLoc = block.getLocation();

        // 处理容器破坏
        if (plugin.getContainerManager().isSupportedContainer(block)) {
            if (plugin.getDataManager().isContainerLocked(blockLoc)) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(blockLoc);
                if (data != null && !player.getUniqueId().equals(data.owner)) {
                    event.setCancelled(true);
                    player.sendMessage("§c无法破坏被锁定的容器！");
                }
            }
            return;
        }

        // 处理锁牌破坏
        if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
            if (plugin.getDataManager().isSignLocked(blockLoc)) {
                // 查找关联的容器
                Location containerLoc = ContainerUtils.findAttachedContainer(blockLoc, plugin);
                if (containerLoc != null) {
                    DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                    if (data != null) {
                        // 验证权限
                        if (!data.owner.equals(player.getUniqueId())) {
                            event.setCancelled(true);
                            player.sendMessage("§c只有所有者可以拆除锁牌！");
                            return;
                        }
                        // 清理数据
                        data.signLocations.remove(blockLoc);
                        plugin.getDataManager().removeLockedSign(blockLoc);
                        plugin.getDataManager().saveAllData();
                        player.sendMessage("§a锁牌已移除");
                    }
                }
            }
        }
    }
}