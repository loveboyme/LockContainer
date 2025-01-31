package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ContainerMergeListener implements Listener {

    private final LockContainer plugin;
    private final BlockFace[] checkFaces = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST
    };

    public ContainerMergeListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onContainerPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Material placedType = placedBlock.getType();

        // 仅处理容器类方块
        if (!plugin.getContainerManager().isSupportedContainer(placedBlock)) {
            return;
        }

        Player player = event.getPlayer();

        // 检查四周是否有容器
        for (BlockFace face : checkFaces) {
            Block adjacentBlock = placedBlock.getRelative(face);
            if (adjacentBlock.getType() == placedType) {
                Location adjacentLoc = adjacentBlock.getLocation();

                // 如果相邻容器已被锁定
                if (plugin.getDataManager().isContainerLocked(adjacentLoc)) {
                    DataManager.ContainerData data = plugin.getDataManager().getContainerData(adjacentLoc);

                    // 检查权限 (保持原有逻辑，阻止非信任玩家合并)
                    if (data != null &&
                            !data.owner.equals(player.getUniqueId()) &&
                            !data.trustedPlayers.contains(player.getUniqueId())) {

                        event.setCancelled(true);
                        player.sendMessage("§c无法合并：相邻容器已被其他玩家锁定");
                        return;
                    }
                }
            }
        }
    }
}