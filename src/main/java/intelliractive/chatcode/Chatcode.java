package intelliractive.chatcode;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Chatcode extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        CommandChatcode chatcodeCmd = new CommandChatcode();
        Bukkit.getPluginManager().registerEvents(chatcodeCmd, this);
        getLogger().log(Level.FINEST, "ChatCode has loaded!");
        getCommand("chatcode").setExecutor(chatcodeCmd);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
