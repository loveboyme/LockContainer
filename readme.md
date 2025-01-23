# 🔒 LockContainer 锁箱插件

欢迎使用 **LockContainer** 插件！这款插件为你的 Minecraft 服务器增加强大的容器保护功能，让玩家可以安全地保护他们的宝贵物品，防止未经授权的访问。🛡️

## ✨ 主要功能特性

*   **🔒 容器锁定：** 玩家可以使用简单的操作锁定箱子、熔炉、漏斗、发射器等多种容器，保护其内部物品。
*   **🤝 信任系统：** 容器所有者可以添加信任玩家，允许他们访问锁定的容器，方便团队合作。
*   **🏷️ 木牌锁定：** 使用木牌轻松锁定容器，直观方便。
*   **⚙️ 图形界面管理：** 通过友好的图形界面管理容器安全设置，操作简单。
*   **💥 防爆保护：** 锁定的容器和关联木牌将受到爆炸保护，更加安全。
*   **📦 自定义容器支持：** 管理员可以自定义插件支持锁定的容器类型，扩展性强。
*   **🔌 完善的 API：** 提供强大的 API 接口，方便其他插件集成和扩展容器保护功能。
*   **🛡️ 防止容器合并漏洞：**  阻止玩家通过放置容器合并来绕过锁定保护。

## 🛠️ 使用指南

### 玩家操作

1.  **锁定容器：**
   *   将木牌放置在想要锁定的容器旁边（任何面都可以）。
   *   在木牌的第一行输入 `[lock]` (不区分大小写) 或留空。
   *   完成！容器将被锁定，您将成为容器的所有者。
   *   木牌上会自动显示 `[已锁定]` 和您的名字。
2.  **访问锁定容器：**
   *   只有容器的所有者和被信任的玩家才能打开和操作锁定的容器。
   *   如果您不是所有者或信任玩家，尝试打开容器会收到提示信息。
3.  **管理容器安全设置（GUI）：**
   *   潜行 (按住 Shift) 并 **右键点击** 容器旁边的 **锁牌**。
   *   将打开一个图形界面，您可以进行以下操作：
      *   **添加信任玩家：** 点击“添加信任玩家”按钮，然后在聊天栏输入玩家名称，即可将该玩家添加到信任列表。
      *   **解锁容器：** 点击“解锁容器”按钮，容器将被完全解锁，移除所有保护和关联木牌。（只有所有者可以解锁）
4.  **移除锁牌：**
   *   只有容器的所有者可以破坏锁牌。
   *   破坏锁牌后，锁牌数据将被移除。
5.  **容器合并限制：**
   *   当放置容器时，如果旁边有已锁定的同类型容器，且您不是所有者或信任玩家，则无法放置，以防止绕过保护。

### 管理员操作

1.  **自定义容器类型：**
   *   通过插件 API，管理员可以注册或取消注册自定义的容器方块类型，以扩展插件的锁定支持范围。
   *   这允许插件支持更多类型的容器，例如模组添加的容器。

## 👨‍💻 API 开发接口

**LockContainer** 插件提供了强大的 API 接口，供其他插件开发者使用，以便集成容器锁定功能或进行更高级的扩展。

### API 方法概览

*   `boolean isLocked(Location location)`: 检查指定位置的容器是否被锁定。
*   `UUID getOwner(Location location)`: 获取指定位置锁定容器的所有者 UUID，未锁定时返回 null。
*   `List<UUID> getTrustedPlayers(Location location)`: 获取指定位置锁定容器的信任玩家 UUID 列表，未锁定时返回 null。
*   `boolean addTrustedPlayer(Location location, Player player)`: 将玩家添加到指定位置锁定容器的信任列表。
*   `boolean removeTrustedPlayer(Location location, Player player)`: 从指定位置锁定容器的信任列表移除玩家。
*   `boolean lockContainer(Location location, Player owner)`: 锁定指定位置的容器，并设置所有者。
*   `boolean unlockContainer(Location location)`: 解锁指定位置的容器，移除所有锁定和限制。
*   `void registerCustomContainer(Material containerType)`: 注册自定义容器类型。
*   `void unregisterCustomContainer(Material containerType)`: 取消注册自定义容器类型。
*   `boolean isLockedSign(Location signLocation)`: 检查木牌是否为锁定容器的关联木牌。
*   `boolean removeSigns(Location containerLoc, Player requester)`: 强制移除指定容器的所有关联木牌（仅限所有者）。

### 使用示例 (Java)

```java
// 获取 LockContainerAPI 实例
LockContainerAPI api = (LockContainerAPI) Bukkit.getServer().getServicesManager().getRegistration(LockContainerAPI.class).getProvider();

if (api != null) {
    Location chestLocation = block.getLocation(); // 假设 block 是一个箱子方块

    if (api.isLocked(chestLocation)) {
        // 容器已被锁定
        UUID ownerId = api.getOwner(chestLocation);
        // ...
    } else {
        // 容器未被锁定
        // ...
    }
} else {
    Bukkit.getLogger().warning("LockContainerAPI 未找到!");
}