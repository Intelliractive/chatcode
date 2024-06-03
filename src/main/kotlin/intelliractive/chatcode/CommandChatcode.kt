package intelliractive.chatcode

import jdk.jshell.JShell
import jdk.jshell.Snippet
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


class CommandChatcode : CommandExecutor, Listener {
    var codingInChat: List<Player> = listOf()
    var jshell: JShell = JShell.create()

    fun reloadJShell() {
        jshell = JShell.create()
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
            sender.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}[ChatCode] Using JShell${ChatColor.RESET}")
            sender.sendMessage("${ChatColor.GOLD}[ChatCode] Type '>GROOVYSH<' or 'GVYSH' to switch to Groovy shell, or '>QUIT<' to quit live coding mode${ChatColor.RESET}")
        } else {
            codingInChat -= sender
            liveCodingModeExitMessage(sender)
        }

        return false
    }

    fun snippetValueMessage(player: Player, message: String) {
        player.sendMessage("${ChatColor.GREEN}==> $message${ChatColor.RESET}")
    }

    fun snippetRecoverableValueMessage(player: Player, snippetStatus: Status, message: String) {
        player.sendMessage("${ChatColor.YELLOW}[$snippetStatus] ==> $message${ChatColor.RESET}")
    }

    fun liveCodingModeExitMessage(player: Player) {
        player.sendMessage("${ChatColor.DARK_AQUA}${ChatColor.BOLD}${ChatColor.UNDERLINE}[ChatCode] You have exited live coding mode!${ChatColor.RESET}")
    }

    fun publicErrorMessage(message: String) {
        broadcastMessage("${ChatColor.RED}[ChatCode] /!\\ $message${ChatColor.RESET}")
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        // for all players in the list, treat chat messages as code instructions
        if (event.player in codingInChat) {
            when (event.message) {
                // use Groovy shell
                ">GROOVYSH<", ">GVYSH<" -> TODO()

                // quit live coding mode
                ">QUIT<" -> {
                    event.player.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}[ChatCode] Quitting...${ChatColor.RESET}")
                    codingInChat -= event.player
                }

                else -> {
                    try {
                        for (se in jshell.eval(event.message)) {
                            when (se.status()) {
                                Status.VALID -> snippetValueMessage(event.player, se.value())

                                Status.RECOVERABLE_DEFINED, Status.RECOVERABLE_NOT_DEFINED ->
                                    snippetRecoverableValueMessage(event.player, se.status(), se.value())

                                Status.DROPPED, Status.OVERWRITTEN, Status.REJECTED, Status.NONEXISTENT -> {
                                    codingInChat -= event.player
                                    publicErrorMessage(
                                        "The code snippet is ${ChatColor.ITALIC}dropped, " +
                                                "overwritten, rejected${ChatColor.RESET}${ChatColor.RED} or " +
                                                "${ChatColor.ITALIC}nonexistent${ChatColor.RESET}${ChatColor.RED}. Restarting JShell"
                                    )
                                    liveCodingModeExitMessage(event.player)

                                    //reload the shell
                                    reloadJShell()
                                }
                            }
                        }
                    }
                    // emergency exit from live coding mode
                    catch (e: IllegalStateException) {
                        codingInChat -= event.player
                        publicErrorMessage("${event.player.name} crashed the JShell instance...")
                        liveCodingModeExitMessage(event.player)

                        //reload the shell
                        reloadJShell()
                    }
                }
            }
        }
    }
}