package cn.panda.lockcontainer.utils;

import cn.panda.lockcontainer.LockContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Sign;

public class ContainerUtils {

    public static Location findAttachedContainer(Location location, LockContainer plugin) {
        Block signBlock = location.getBlock();
        if (signBlock.getType() != Material.WALL_SIGN) return null;

        // 获取木牌附着方向
        Sign signData = (Sign) signBlock.getState().getData();
        BlockFace attachedFace = signData.getAttachedFace();
        Block containerBlock = signBlock.getRelative(attachedFace);

        // 关键修改：只检查配置支持的容器
        if (plugin.getContainerManager().isSupportedContainer(containerBlock)) {
            return containerBlock.getLocation();
        }
        return null;
    }
}