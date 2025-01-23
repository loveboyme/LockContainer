package cn.panda.lockcontainer.listener;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import cn.panda.lockcontainer.LockContainer;
import org.bukkit.Location;
import cn.panda.lockcontainer.core.DataManager;

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
        Location containerLoc = findNearbyContainer(signBlock.getLocation());

        if (containerLoc == null || !plugin.getContainerManager().isSupportedContainer(containerLoc.getBlock())) {
            return;
        }

        if (lines[0].isEmpty() || lines[0].equalsIgnoreCase("[lock]")) {
            if (plugin.getDataManager().lockContainer(containerLoc, player.getUniqueId())) {
                DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
                if (data != null) {
                    data.addSignLocation(signBlock.getLocation()); // 记录木牌位置
                }
                event.setLine(0, "§c[已锁定]");
                event.setLine(1, "§7所有者: " + player.getName());
                player.sendMessage("§a容器锁定成功!");
            } else {
                player.sendMessage("§c该容器已被锁定!");
            }
        }
    }

    private Location findNearbyContainer(Location signLoc) {
        BlockFace[] checkFaces = {
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : checkFaces) {
            Block block = signLoc.getBlock().getRelative(face);
            if (plugin.getContainerManager().isSupportedContainer(block)) {
                return block.getLocation();
            }
        }
        return null;
    }
}