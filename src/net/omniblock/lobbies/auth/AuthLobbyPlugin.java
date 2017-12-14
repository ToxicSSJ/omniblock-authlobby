package net.omniblock.lobbies.auth;

import org.bukkit.plugin.java.JavaPlugin;

import net.omniblock.lobbies.api.LobbyHandler;
import net.omniblock.lobbies.auth.handler.AuthLobby;
import net.omniblock.network.handlers.Handlers;
import net.omniblock.network.handlers.network.NetworkManager;
import net.omniblock.packets.object.external.ServerType;

public class AuthLobbyPlugin extends JavaPlugin {

	private static AuthLobbyPlugin instance;
	private static AuthLobby lobby;
	
	@Override
	public void onEnable() {
		
		instance = this;
		lobby = new AuthLobby();
		
		if(NetworkManager.getServertype() != ServerType.MAIN_AUTH_SERVER) {
			
			Handlers.LOGGER.sendModuleInfo("&7Se ha registrado AuthLobby v" + this.getDescription().getVersion() + "!");
			Handlers.LOGGER.sendModuleMessage("OmniLobbies", "Se ha inicializado AuthLobby en modo API!");
			return;
			
		}
		
		Handlers.LOGGER.sendModuleInfo("&7Se ha registrado AuthLobby v" + this.getDescription().getVersion() + "!");
		Handlers.LOGGER.sendModuleMessage("OmniLobbies", "Se ha inicializado este lobby como un AuthLobby!");
		
		LobbyHandler.startLobby(lobby);
		
	}
	
	public AuthLobbyPlugin getInstance() {
		return instance;
	}
	
}
