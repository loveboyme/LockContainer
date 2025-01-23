package cn.panda.lockcontainer.core;

import cn.panda.lockcontainer.LockContainer;
import cn.panda.lockcontainer.api.LockContainerAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class LockContainerAPIImpl implements LockContainerAPI {

    private final LockContainer plugin;

    public LockContainerAPIImpl(LockContainer plugin) {
        this.plugin = plugin;
    }

    // ---------- 基础功能实现 ----------
    @Override
    public boolean isLocked(Location location) {
        return plugin.getDataManager().isContainerLocked(location);
    }

    @Override
    public UUID getOwner(Location location) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(location);
        return (data != null) ? data.owner : null;
    }

    @Override
    public List<UUID> getTrustedPlayers(Location location) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(location);
        return (data != null) ? data.trustedPlayers : null;
    }

    @Override
    public boolean addTrustedPlayer(Location location, Player player) {
        return plugin.getDataManager().addTrustedPlayer(location, player.getUniqueId());
    }

    @Override
    public boolean removeTrustedPlayer(Location location, Player player) {
        return plugin.getDataManager().removeTrustedPlayer(location, player.getUniqueId());
    }

    @Override
    public boolean lockContainer(Location location, Player owner) {
        return plugin.getDataManager().lockContainer(location, owner.getUniqueId());
    }

    @Override
    public boolean unlockContainer(Location location) {
        return plugin.getDataManager().unlockContainer(location);
    }

    // ---------- 容器注册管理实现 ----------
    @Override
    public void registerCustomContainer(Material containerType) {
        plugin.getContainerManager().registerCustomContainer(containerType);
    }

    @Override
    public void unregisterCustomContainer(Material containerType) {
        plugin.getContainerManager().unregisterCustomContainer(containerType);
    }

    // ---------- 新增木牌管理功能实现 (v2.0) ----------
    @Override
    public boolean isLockedSign(Location signLocation) {
        return plugin.getDataManager().isSignLocked(signLocation);
    }

    @Override
    public boolean removeSigns(Location containerLoc, Player requester) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
        if (data == null) return false;

        // 权限验证：只有所有者可以操作
        if (!data.owner.equals(requester.getUniqueId())) {
            requester.sendMessage("§c只有容器所有者可以执行此操作");
            return false;
        }

        // 调用核心解锁逻辑（包含木牌删除）
        return plugin.getDataManager().unlockContainer(containerLoc);
    }
}