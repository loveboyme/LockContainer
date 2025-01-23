package cn.panda.lockcontainer.core;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import cn.panda.lockcontainer.LockContainer;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataManager {

    private final LockContainer plugin;
    private final File dataFile;
    private final Gson gson;
    private final Map<String, ContainerData> containerDataCache = new HashMap<>();
    private final Set<Location> lockedSigns = new HashSet<>();

    private static final JsonSerializer<Location> LOCATION_SERIALIZER = (src, type, context) -> {
        JsonObject obj = new JsonObject();
        obj.addProperty("world", src.getWorld().getName());
        obj.addProperty("x", src.getBlockX());
        obj.addProperty("y", src.getBlockY());
        obj.addProperty("z", src.getBlockZ());
        return obj;
    };

    private static final JsonDeserializer<Location> LOCATION_DESERIALIZER = (json, type, context) -> {
        JsonObject obj = json.getAsJsonObject();
        World world = Bukkit.getWorld(obj.get("world").getAsString());
        if (world == null) return null;
        return new Location(
                world,
                obj.get("x").getAsInt(),
                obj.get("y").getAsInt(),
                obj.get("z").getAsInt()
        );
    };

    public DataManager(LockContainer plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, LOCATION_SERIALIZER)
                .registerTypeAdapter(Location.class, LOCATION_DESERIALIZER)
                .setPrettyPrinting()
                .create();
        loadAllData();
    }

    public void loadAllData() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                return;
            }

            try (FileReader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<Map<String, ContainerData>>(){}.getType();
                Map<String, ContainerData> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    containerDataCache.putAll(loaded);
                    // 初始化锁牌数据
                    loaded.values().forEach(data -> {
                        data.signLocations.forEach(loc -> {
                            if (loc.getWorld() != null) {
                                lockedSigns.add(loc);
                            }
                        });
                    });
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("数据加载失败: " + e.getMessage());
        }
    }

    public void saveAllData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(containerDataCache, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("数据保存失败: " + e.getMessage());
        }
    }

    private String getContainerKey(Location loc) {
        return String.format("%s-%d-%d-%d",
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    public boolean isContainerLocked(Location loc) {
        return loc != null && containerDataCache.containsKey(getContainerKey(loc));
    }

    public ContainerData getContainerData(Location loc) {
        return (loc != null) ? containerDataCache.get(getContainerKey(loc)) : null;
    }

    public boolean lockContainer(Location loc, UUID owner) {
        if (loc == null || loc.getWorld() == null) return false;
        String key = getContainerKey(loc);
        if (containerDataCache.containsKey(key)) return false;

        ContainerData data = new ContainerData(owner);
        containerDataCache.put(key, data);
        CompletableFuture.runAsync(this::saveAllData);
        return true;
    }

    public boolean unlockContainer(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        String key = getContainerKey(loc);
        ContainerData data = containerDataCache.remove(key);
        if (data == null) return false;

        // 删除物理木牌
        Bukkit.getScheduler().runTask(plugin, () -> {
            data.signLocations.forEach(signLoc -> {
                if (signLoc.getWorld() == null) return;
                Block block = signLoc.getBlock();
                if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                    block.setType(Material.AIR);
                    plugin.getLogger().info("已移除木牌: " + signLoc);
                }
            });
        });

        // 清理数据
        data.signLocations.forEach(this::removeLockedSign);
        CompletableFuture.runAsync(this::saveAllData);
        return true;
    }

    public boolean addTrustedPlayer(Location loc, UUID player) {
        ContainerData data = getContainerData(loc);
        if (data == null) return false;
        if (data.trustedPlayers.add(player)) {
            CompletableFuture.runAsync(this::saveAllData);
            return true;
        }
        return false;
    }

    public boolean removeTrustedPlayer(Location loc, UUID player) {
        ContainerData data = getContainerData(loc);
        if (data == null) return false;
        if (data.trustedPlayers.remove(player)) {
            CompletableFuture.runAsync(this::saveAllData);
            return true;
        }
        return false;
    }

    public boolean isSignLocked(Location loc) {
        return lockedSigns.contains(loc);
    }

    public void addLockedSign(Location signLoc) {
        if (signLoc != null && signLoc.getWorld() != null) {
            lockedSigns.add(signLoc);
        }
    }

    public void removeLockedSign(Location signLoc) {
        lockedSigns.remove(signLoc);
    }

    public static class ContainerData {
        public final UUID owner;
        public final List<UUID> trustedPlayers = new ArrayList<>();
        public final List<Location> signLocations = new ArrayList<>();

        public ContainerData(UUID owner) {
            this.owner = owner;
        }

        public void addSignLocation(Location location) {
            if (location != null && location.getWorld() != null) {
                signLocations.add(location);
                LockContainer.getInstance().getDataManager().addLockedSign(location);
            }
        }
    }
}