package intelliractive.chatcode;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Chatcode extends JavaPlugin implements Listener {
    static CommandChatcode chatcodeCmd = new CommandChatcode();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(chatcodeCmd, this);
        getCommand("chatcode").setExecutor(chatcodeCmd);
        getLogger().log(Level.FINEST, "ChatCode has loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
