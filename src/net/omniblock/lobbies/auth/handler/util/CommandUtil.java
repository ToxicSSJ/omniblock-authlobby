package net.omniblock.lobbies.auth.handler.util;

import org.bukkit.command.CommandExecutor;

import net.omniblock.lobbies.auth.AuthLobbyPlugin;

/**
 * 
 * Esta clase contiene un metodo importante
 * para el registro de varios comandos en
 * un solo CommandExecutor.
 * 
 * @author zlToxicNetherlz
 * @see CommandExecutor
 *
 */
public class CommandUtil {

	/**
	 * 
	 * Esta clase contiene un metodo importante
	 * para el registro de varios comandos en
	 * un solo CommandExecutor.
	 * 
	 */
	public static void setExecutor(CommandExecutor executor, String...cmds) {
		
		for(String cmd : cmds)
			AuthLobbyPlugin.getInstance().getCommand(cmd).setExecutor(executor);
		
	}
	
}
