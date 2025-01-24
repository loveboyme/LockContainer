package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.utils.ContainerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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

        // 条件过滤
        if (signBlock.getType() != Material.WALL_SIGN) return;
        Location containerLoc = ContainerUtils.findAttachedContainer(signBlock.getLocation(), plugin);
        if (containerLoc == null) return;
        if (!lines[0].isEmpty() && !lines[0].equalsIgnoreCase("[lock]")) return;

        event.setCancelled(true); // 阻止原版生成

        // 获取连接的容器组
        List<Location> connectedContainers = plugin.getContainerManager().getConnectedContainers(containerLoc);

        // 锁定检查
        if (connectedContainers.stream().anyMatch(loc -> {
            DataManager.ContainerData data = plugin.getDataManager().getContainerData(loc);
            return data != null && !data.owner.equals(player.getUniqueId());
        })) {
            player.sendMessage(ChatColor.RED + "该容器组已被其他玩家锁定！");
            return;
        }

        // 执行锁定
        boolean success = true;
        for (Location loc : connectedContainers) {
            if (!plugin.getDataManager().lockContainer(loc, player.getUniqueId())) {
                success = false;
                break;
            }
        }

        if (success) {
            // 手动更新木牌状态
            Sign signState = (Sign) signBlock.getState();
            signState.setLine(0, ChatColor.DARK_RED + "[已锁定]");
            signState.setLine(1, ChatColor.GRAY + "所有者: " + player.getName());
            signState.update(true, false); // 重要：强制物理更新

            // 保存数据
            DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
            if (data != null) {
                data.addSignLocation(signBlock.getLocation());
            }
            player.sendMessage(ChatColor.GREEN + "容器组锁定成功!");
        } else {
            player.sendMessage(ChatColor.RED + "部分容器锁定失败，请检查容器状态！");
        }
    }
}