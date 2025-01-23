package cn.panda.lockcontainer.listener;

import cn.panda.lockcontainer.LockContainer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import java.util.Iterator;

public class ExplosionListener implements Listener {

    private final LockContainer plugin;

    public ExplosionListener(LockContainer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location loc = block.getLocation();

            // 保护容器和锁牌
            if (plugin.getDataManager().isContainerLocked(loc) ||
                    plugin.getDataManager().isSignLocked(loc)) {
                iterator.remove();
                plugin.getLogger().info("已阻止爆炸破坏锁牌/容器: " + loc);
            }
        }
    }
}