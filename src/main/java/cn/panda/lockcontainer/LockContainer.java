package cn.panda.lockcontainer;

import cn.panda.lockcontainer.api.LockContainerAPI;
import cn.panda.lockcontainer.core.ContainerManager;
import cn.panda.lockcontainer.core.DataManager;
import cn.panda.lockcontainer.core.GUIHandler;
import cn.panda.lockcontainer.core.LockContainerAPIImpl;
import cn.panda.lockcontainer.listener.*;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class LockContainer extends JavaPlugin {

    // 单例模式支持
    private static LockContainer instance;

    private DataManager dataManager;
    private ContainerManager containerManager;
    private GUIHandler guiHandler;
    private LockContainerAPIImpl api;

    @Override
    public void onEnable() {
        instance = this; // 关键：保存实例引用
        saveDefaultConfig();

        this.dataManager = new DataManager(this);
        this.containerManager = new ContainerManager(this);
        this.guiHandler = new GUIHandler(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new ContainerMergeListener(this), this);

        // 注册API
        this.api = new LockContainerAPIImpl(this);
        getServer().getServicesManager().register(LockContainerAPI.class, this.api, this, ServicePriority.Normal);
        getLogger().info("锁箱插件 已加载!");
    }

    @Override
    public void onDisable() {
        dataManager.saveAllData();
        getLogger().info("锁箱插件 已卸载!");
    }

    // 关键：提供单例访问方法
    public static LockContainer getInstance() {
        return instance;
    }

    // 其他Getter方法
    public DataManager getDataManager() { return dataManager; }
    public ContainerManager getContainerManager() { return containerManager; }
    public GUIHandler getGUIHandler() { return guiHandler; }
    public LockContainerAPIImpl getAPI() { return api; }
}