package intelliractive.chatcode

import groovy.lang.GroovyShell
import jdk.jshell.JShell
import jdk.jshell.Snippet.Status
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.codehaus.groovy.control.CompilationFailedException


class CommandChatcode : CommandExecutor, Listener {
    var codingInChat: List<Player> = listOf()
    var groovyShell = GroovyShell()

    fun reloadShell() {
        groovyShell = GroovyShell()
    }

    // This method is called, when somebody uses our command
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be run by players${ChatColor.RESET}")
            return false
        }

        if (sender !in codingInChat) {
            // add the player to the list
            codingInChat += sender
            sender.sendMessage("${ChatColor.AQUA}${ChatColor.BOLD}${ChatColor.UNDERLINE}[ChatCode] You are now in live coding mode!${ChatColor.RESET}")
            sender.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}[ChatCode] Using Groovy shell${ChatColor.RESET}")
            sender.sendMessage("${ChatColor.GOLD}[ChatCode] Type '>QUIT<' to quit live coding mode${ChatColor.RESET}")
        } else {
            codingInChat -= sender
            liveCodingModeExitMessage(sender)
        }

        return false
    }

    fun snippetValueMessage(player: Player, message: String) {
        player.sendMessage("${ChatColor.GREEN}==> $message${ChatColor.RESET}")
    }

    fun liveCodingModeExitMessage(player: Player) {
        player.sendMessage("${ChatColor.DARK_AQUA}${ChatColor.BOLD}${ChatColor.UNDERLINE}[ChatCode] You have exited live coding mode!${ChatColor.RESET}")
    }

    fun publicErrorMessage(message: String) {
        broadcastMessage("${ChatColor.RED}[ChatCode] /!\\ $message${ChatColor.RESET}")
    }

    var classLoadRegex = Regex(">LOAD< [a-zA-Z_0-9.]+")

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        // for all players in the list, treat chat messages as code instructions
        if (event.player in codingInChat) {
            if (classLoadRegex matches event.message) {
                groovyShell.classLoader.loadClass(event.message.split(" ")[1])
            }
            else {
                when (event.message) {
                    // quit live coding mode
                    ">QUIT<" -> {
                        event.player.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}[ChatCode] Quitting...${ChatColor.RESET}")
                        liveCodingModeExitMessage(event.player)
                        codingInChat -= event.player
                    }

                    else -> {
                        try {
                            var res = groovyShell.evaluate(event.message)
                            snippetValueMessage(event.player, res.toString())
                        }
                        // emergency exit from live coding mode
                        catch (e: CompilationFailedException) {
                            codingInChat -= event.player
                            publicErrorMessage("${event.player.name} caused an error the Groovy shell instance")
                            liveCodingModeExitMessage(event.player)

                            //reload the shell
                            reloadShell()
                        }
                    }
                }
            }
        }
    }
}