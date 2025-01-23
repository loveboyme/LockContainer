# 🔌 LockContainer API 文档

---

## 📚 目录
1. [基础功能](#-基础功能)
2. [木牌管理](#-木牌管理)
3. [容器管理](#-容器管理)
4. [代码示例](#-代码示例)
5. [注意事项](#-注意事项)

---

## 🛠️ 基础功能

| 方法签名 | 参数 | 返回值 | 描述 | 
|---------|------|--------|-----|
| `boolean isLocked(Location location)` | `location`: 容器位置 | `boolean` | 检查容器是否被锁定 🔒 |  
| `UUID getOwner(Location location)` | `location`: 容器位置 | `UUID` | 获取容器所有者ID 👑 |  
| `List<UUID> getTrustedPlayers(Location location)` | `location`: 容器位置 | `List<UUID>` | 获取信任玩家列表 🤝 |  
| `boolean addTrustedPlayer(Location loc, Player player)` | <ul><li>`loc`: 容器位置</li><li>`player`: 要添加的玩家</li></ul> | `boolean` | 添加信任玩家 ➕ |  
| `boolean removeTrustedPlayer(Location loc, Player player)` | <ul><li>`loc`: 容器位置</li><li>`player`: 要移除的玩家</li></ul> | `boolean` | 移除信任玩家 ➖ |

[⬆️ 返回目录](#-目录)

---

## 🪧 木牌管理

| 方法签名 | 参数 | 返回值 | 描述 |
|---------|------|--------|-----|
| `boolean isLockedSign(Location signLocation)` | `signLocation`: 木牌位置 | `boolean` | 检查是否为锁定容器的关联木牌 🚫 |  
| `boolean removeSigns(Location containerLoc, Player requester)` | <ul><li>`containerLoc`: 容器位置</li><li>`requester`: 操作玩家</li></ul> | `boolean` | 强制移除所有关联木牌 💥 |

[⬆️ 返回目录](#-目录)

---

## 📦 容器管理

| 方法签名 | 参数 | 返回值 | 描述 |
|---------|------|--------|-----|
| `void registerCustomContainer(Material containerType)` | `containerType`: 方块类型 | `void` | 注册新型容器 📥 |  
| `void unregisterCustomContainer(Material containerType)` | `containerType`: 方块类型 | `void` | 取消容器注册 📤 |  
| `boolean unlockContainer(Location location)` | `location`: 容器位置 | `boolean` | 解锁容器并删除木牌 🔓 |

[⬆️ 返回目录](#-目录)

---

## 💻 代码示例

### 基础功能使用
```java
LockContainerAPI api = Bukkit.getServicesManager().getRegistration(LockContainerAPI.class).getProvider();

// 锁定容器
if (api.lockContainer(chestLoc, player)) {
    player.sendMessage("🔒 容器已锁定");
}

// 获取信任列表
List<UUID> trusted = api.getTrustedPlayers(chestLoc);

```
⚠️ 注意事项
线程安全

java
复制
// ❌ 错误方式（异步线程直接操作方块）
new Thread(() -> api.unlockContainer(loc)).start();

// ✅ 正确方式（使用Bukkit调度器）
Bukkit.getScheduler().runTask(plugin, () -> api.unlockContainer(loc));
权限验证

java
复制
// 推荐在调用 removeSigns() 前进行权限检查
if (player.hasPermission("lockcontainer.admin")) {
api.removeSigns(loc, player);
}
位置有效性

java
复制
// 调用前务必检查位置有效性
if (location.getWorld() != null) {
boolean locked = api.isLocked(location);
}