package cn.panda.lockcontainer.core;

import org.bukkit.Material;
import org.bukkit.block.Block;
import cn.panda.lockcontainer.LockContainer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;

public class ContainerManager {

    private final LockContainer plugin;
    // 存储支持的容器类型
    private final Set<Material> supportedContainers = new HashSet<>(Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.HOPPER,
            Material.DISPENSER,
            Material.DROPPER,
            Material.BREWING_STAND,
            // 添加1.12.2版本中原有的支持容器
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.SILVER_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX
    ));

    // 存储自定义的容器类型
    private final Set<Material> customContainers = new HashSet<>();

    // 存储正在添加信任玩家的玩家和容器位置
    private final Map<Player, Location> addingPlayers = new HashMap<>();

    public ContainerManager(LockContainer plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查方块是否为受支持的容器。
     *
     * @param block 要检查的方块
     * @return 如果方块是受支持的容器，则返回 true，否则返回 false
     */
    public boolean isSupportedContainer(Block block) {
        return supportedContainers.contains(block.getType()) || customContainers.contains(block.getType());
    }

    /**
     * 注册自定义容器
     *
     * @param containerType 方块类型
     */
    public void registerCustomContainer(Material containerType) {
        customContainers.add(containerType);
        plugin.getLogger().info("已注册自定义容器: " + containerType.name());
    }

    /**
     * 取消注册自定义容器
     *
     * @param containerType 方块类型
     */
    public void unregisterCustomContainer(Material containerType) {
        customContainers.remove(containerType);
        plugin.getLogger().info("已取消注册自定义容器: " + containerType.name());
    }

    /**
     * 将玩家添加到正在添加信任玩家的列表中。
     *
     * @param player   玩家
     * @param location 容器位置
     */
    public void setAddingPlayer(Player player, Location location) {
        addingPlayers.put(player, location);
    }

    /**
     * 获取正在添加信任玩家的玩家对应的容器位置。
     *
     * @param player 玩家
     * @return 容器位置，如果玩家不在添加信任玩家的列表中，则返回 null
     */
    public Location getAddingPlayer(Player player) {
        return addingPlayers.get(player);
    }

    /**
     * 将玩家从正在添加信任玩家的列表中移除。
     *
     * @param player 玩家
     */
    public void removeAddingPlayer(Player player) {
        addingPlayers.remove(player);
    }


    
}