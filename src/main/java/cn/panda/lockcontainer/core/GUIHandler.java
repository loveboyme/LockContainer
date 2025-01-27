package cn.panda.lockcontainer.core;

import cn.panda.lockcontainer.LockContainer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIHandler {
    private final LockContainer plugin;
    private final Map<UUID, Integer> currentPages = new HashMap<>();

    public GUIHandler(LockContainer plugin) {
        this.plugin = plugin;
    }

    // 新增公共访问方法
    public int getCurrentPage(UUID playerId) {
        return currentPages.getOrDefault(playerId, 0);
    }

    public void setCurrentPage(UUID playerId, int page) {
        currentPages.put(playerId, page);
    }

    public void openMainMenu(Player player, Location containerLoc) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
        if (data == null || !data.owner.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "错误：你不是该容器的所有者！");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLACK + "容器安全设置 " + ChatColor.GRAY + "(主菜单)");

        // 信息展示项
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "容器信息");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "所有者: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(data.owner).getName(),
                ChatColor.GRAY + "锁定容器数量: " + ChatColor.YELLOW + plugin.getContainerManager().getConnectedContainers(containerLoc).size(),
                ChatColor.GRAY + "信任成员数量: " + ChatColor.YELLOW + data.trustedPlayers.size()
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // 功能按钮
        ItemStack memberItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta memberMeta = memberItem.getItemMeta();
        memberMeta.setDisplayName(ChatColor.AQUA + "信任成员管理");
        memberMeta.setLore(Collections.singletonList(ChatColor.GRAY + "点击查看/管理信任成员"));
        memberItem.setItemMeta(memberMeta);

        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "添加信任玩家");
        addMeta.setLore(Collections.singletonList(ChatColor.GRAY + "点击输入玩家名称"));
        addItem.setItemMeta(addMeta);

        ItemStack unlockItem = new ItemStack(Material.REDSTONE_TORCH_ON);
        ItemMeta unlockMeta = unlockItem.getItemMeta();
        unlockMeta.setDisplayName(ChatColor.RED + "解锁容器");
        unlockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "点击解除所有锁定",
                ChatColor.DARK_RED + "警告：这会移除所有锁牌!"
        ));
        unlockItem.setItemMeta(unlockMeta);

        // 背景填充
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        int[] fillSlots = {0,1,2,3,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : fillSlots) {
            gui.setItem(slot, glass);
        }

        // 装饰分隔
        ItemStack infoBorder = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        for (int slot : new int[]{19,21,23,25}) {
            gui.setItem(slot, infoBorder);
        }

        // 功能按钮位置
        gui.setItem(20, memberItem);
        gui.setItem(22, addItem);
        gui.setItem(24, unlockItem);

        // 位置标记
        ItemStack locMarker = createLocationMarker(containerLoc);
        gui.setItem(0, locMarker);

        player.openInventory(gui);
    }

    public void openMemberMenu(Player player, Location containerLoc, int page) {
        DataManager.ContainerData data = plugin.getDataManager().getContainerData(containerLoc);
        if (data == null) return;

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLACK + "信任成员管理 " + ChatColor.GRAY + "(第 " + (page+1) + " 页)");
        setCurrentPage(player.getUniqueId(), page); // 使用公共方法设置页码

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.WHITE + "返回主菜单");
        backItem.setItemMeta(backMeta);
        gui.setItem(45, backItem);

        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.PAPER);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.GREEN + "上一页");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(48, prevPage);
        }

        if (data.trustedPlayers.size() > (page + 1) * 45) {
            ItemStack nextPage = new ItemStack(Material.PAPER);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GREEN + "下一页");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(50, nextPage);
        }

        int start = page * 45;
        int end = Math.min(start + 45, data.trustedPlayers.size());
        int slot = 1;

        for (int i = start; i < end; i++) {
            UUID memberId = data.trustedPlayers.get(i);
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(member);
            meta.setDisplayName(ChatColor.YELLOW + member.getName());
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "UUID: " + memberId,
                    ChatColor.DARK_GRAY + "右键点击移除"
            ));
            head.setItemMeta(meta);
            gui.setItem(slot, head);
            slot++;
        }

        ItemStack locMarker = createLocationMarker(containerLoc);
        gui.setItem(0, locMarker);
        player.openInventory(gui);
    }

    private ItemStack createLocationMarker(Location loc) {
        ItemStack marker = new ItemStack(Material.PAPER);
        ItemMeta meta = marker.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "LOCK_LOC:" + locToString(loc));
        marker.setItemMeta(meta);
        return marker;
    }

    public Location parseLocation(String locString) {
        try {
            String[] parts = locString.split(",");
            if (parts.length != 4) return null;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            return new Location(
                    world,
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();
    }

    public void promptAddPlayer(Player player, Location location) {
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "请在聊天栏输入要添加的玩家名称：");
        plugin.getContainerManager().setAddingPlayer(player, location);
    }
}