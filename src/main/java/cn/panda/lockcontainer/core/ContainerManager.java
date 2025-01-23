package cn.panda.lockcontainer.core;

import cn.panda.lockcontainer.LockContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import java.util.*;

public class ContainerManager {

    private final LockContainer plugin;
    private final Set<Material> supportedContainers = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE,
            Material.HOPPER, Material.DISPENSER, Material.DROPPER,
            Material.BREWING_STAND, Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.SILVER_SHULKER_BOX, Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX
    ));
    private final Set<Material> customContainers = new HashSet<>();
    private final Map<Player, Location> addingPlayers = new HashMap<>();

    public ContainerManager(LockContainer plugin) {
        this.plugin = plugin;
    }

    public boolean isSupportedContainer(Block block) {
        return supportedContainers.contains(block.getType()) || customContainers.contains(block.getType());
    }

    public void registerCustomContainer(Material containerType) {
        customContainers.add(containerType);
        plugin.getLogger().info("已注册自定义容器: " + containerType.name());
    }

    public void unregisterCustomContainer(Material containerType) {
        customContainers.remove(containerType);
        plugin.getLogger().info("已取消注册自定义容器: " + containerType.name());
    }

    public void setAddingPlayer(Player player, Location location) {
        addingPlayers.put(player, location);
    }

    public Location getAddingPlayer(Player player) {
        return addingPlayers.get(player);
    }

    public void removeAddingPlayer(Player player) {
        addingPlayers.remove(player);
    }

    public List<Location> getConnectedContainers(Location loc) {
        List<Location> connected = new ArrayList<>();
        if (loc == null || loc.getWorld() == null) return connected;

        Block startBlock = loc.getBlock();
        Material baseType = startBlock.getType();
        Queue<Location> queue = new LinkedList<>();
        Set<Location> checked = new HashSet<>();

        queue.add(loc);
        checked.add(loc);

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            connected.add(current);

            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                Block relative = current.getBlock().getRelative(face);
                Location relativeLoc = relative.getLocation();

                if (!checked.contains(relativeLoc) &&
                        isSupportedContainer(relative) &&
                        relative.getType() == baseType) {

                    checked.add(relativeLoc);
                    queue.add(relativeLoc);
                }
            }
        }
        return connected;
    }

    public Location getMainContainerLocation(Location loc) {
        List<Location> connected = getConnectedContainers(loc);
        if (connected.isEmpty()) return loc;

        return connected.stream()
                .min((a, b) -> {
                    if (a.getX() != b.getX()) return Double.compare(a.getX(), b.getX());
                    if (a.getZ() != b.getZ()) return Double.compare(a.getZ(), b.getZ());
                    return Double.compare(a.getY(), b.getY());
                })
                .orElse(loc);
    }
}