package cn.panda.lockcontainer.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import java.util.List;
import java.util.UUID;

/**
 * LockContainer 插件的 API 接口。
 */
public interface LockContainerAPI {

    /**
     * 检查容器是否被锁定。
     *
     * @param location 容器的位置
     * @return 如果容器被锁定，则返回 true，否则返回 false
     */
    boolean isLocked(Location location);

    /**
     * 获取容器的所有者。
     *
     * @param location 容器的位置
     * @return 容器所有者的 UUID，如果容器未被锁定，则返回 null
     */
    UUID getOwner(Location location);

    /**
     * 获取容器的信任列表。
     *
     * @param location 容器的位置
     * @return 容器的信任列表，如果容器未被锁定，则返回 null
     */
    List<UUID> getTrustedPlayers(Location location);

    /**
     * 将玩家添加到容器的信任列表中。
     *
     * @param location 容器的位置
     * @param player   要添加的玩家
     * @return 如果添加成功，则返回 true，否则返回 false
     */
    boolean addTrustedPlayer(Location location, Player player);

    /**
     * 将玩家从容器的信任列表中移除。
     *
     * @param location 容器的位置
     * @param player   要移除的玩家
     * @return 如果移除成功，则返回 true，否则返回 false
     */
    boolean removeTrustedPlayer(Location location, Player player);

    /**
     * 锁定容器。
     *
     * @param location 容器的位置
     * @param owner    容器的所有者
     * @return 如果锁定成功，则返回 true，否则返回 false
     */
    boolean lockContainer(Location location, Player owner);

    /**
     * 解锁容器，移除所有限制。
     *
     * @param location 容器的位置
     * @return 如果解锁成功，则返回 true，否则返回 false
     */
    boolean unlockContainer(Location location);

    /**
     * 注册自定义容器
     *
     * @param containerType 方块类型
     */
    void registerCustomContainer(Material containerType);

    /**
     * 取消注册自定义容器
     *
     * @param containerType 方块类型
     */
    void unregisterCustomContainer(Material containerType);

    /**
     * 检查木牌是否属于某个锁定容器
     * @param signLocation 木牌位置
     * @return 是否为锁定容器的关联木牌
     */
    boolean isLockedSign(Location signLocation);

    /**
     * 强制移除容器的所有关联木牌（仅限所有者）
     * @param containerLoc 容器位置
     * @param requester 请求操作的玩家
     * @return 是否操作成功
     */
    boolean removeSigns(Location containerLoc, Player requester);

}