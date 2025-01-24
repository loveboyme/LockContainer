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

    // BlockListener.java 修改后的代码片段
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        // 处理锁牌破坏
        if (block.getType() == Material.WALL_SIGN && plugin.getDataManager().isSignLocked(blockLoc)) {
            Location containerLoc = ContainerUtils.findAttachedContainer(blockLoc, plugin);
            if (containerLoc != null) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null) {
                    // 移除锁牌位置并检查是否需要解锁
                    data.signLocations.remove(blockLoc);
                    plugin.getDataManager().removeLockedSign(blockLoc);

                    // 如果没有其他锁牌，则完全解锁
                    if (data.signLocations.isEmpty()) {
                        plugin.getDataManager().unlockContainer(containerLoc);
                    }

                    plugin.getDataManager().saveAllData();
                }
            }
        }
    }
}