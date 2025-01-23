package cn.panda.lockcontainer.utils;

import cn.panda.lockcontainer.LockContainer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import java.util.Arrays;
import java.util.List;

public class ContainerUtils {
    private static final List<BlockFace> CONTAINER_CHECK_FACES = Arrays.asList(
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    );

    public static Location findAttachedContainer(Location location, LockContainer plugin) {
        Block block = location.getBlock();
        for (BlockFace face : CONTAINER_CHECK_FACES) {
            Block relative = block.getRelative(face);
            if (plugin.getContainerManager().isSupportedContainer(relative)) {
                return relative.getLocation();
            }
        }
        return null;
    }
}