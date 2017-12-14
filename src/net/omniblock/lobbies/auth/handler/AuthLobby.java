package net.omniblock.lobbies.auth.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.omniblock.lobbies.OmniLobbies;
import net.omniblock.lobbies.api.LobbyUtility;
import net.omniblock.lobbies.api.object.LobbyBoard;
import net.omniblock.lobbies.api.object.LobbyScan;
import net.omniblock.lobbies.api.object.LobbySystem;
import net.omniblock.lobbies.api.object.LobbyWorld;
import net.omniblock.lobbies.api.type.CommonLobby;
import net.omniblock.lobbies.apps.attributes.type.AttributeType;
import net.omniblock.lobbies.utils.PlayerUtils;
import net.omniblock.network.handlers.base.bases.type.AuthBase;
import net.omniblock.network.library.addons.resourceaddon.ResourceHandler;
import net.omniblock.network.library.addons.resourceaddon.type.ResourceType;
import net.omniblock.network.library.utils.TextUtil;
import net.omniblock.packets.util.Lists;

public class AuthLobby extends CommonLobby {

	public static LobbyWorld lobbyWorld = LobbyUtility.getLobbyWorld("Auth");
	
	protected AuthLobby instance;
	
	protected Map<String, List<Location>> scan;
	
	public AuthLobby() {
		super(lobbyWorld);
		return;
		
	}

	@Override
	public void onScanCompleted(Map<String, List<Location>> scan) {
		
		this.scan = scan;
		return;
		
	}

	@Override
	public void onLobbyUnloaded() {
		
	}

	@Override
	public void setup() {
		
		this.start();
		this.instance = this;
		
	}

	@Override
	public void onStartBeingExecute() {
		
		this.setSpawnPoint(new Location(this.getWorld().getBukkitWorld(), 23.5, 55, 3.5, 90, (float) 0));
		return;
		
	}

	@Override
	public void onStopBeingExecute() {
		
		return;
		
	}

	@Override
	public void giveItems(Player player) {
		
	}

	@Override
	public LobbyScan getScan() {
		return new LobbyScan() {
			
			@Override
			public Map<String, Material> getKeys() {
				return new HashMap<String, Material>();
			}

			@Override
			public String getScanName() {
				return "AUTHLOBBY_SCAN";
			}
			
		};
	}

	public Map<String, List<Location>> getLastScan() {
		return scan;
	}
	
	@Override
	public String getLobbyName() {
		return "MainLobby";
	}

	@Override
	public Listener getEvents() {
		return new Listener() {
			
			@SuppressWarnings("deprecation")
			@EventHandler
			public void onJoin(PlayerJoinEvent e){
				
				PlayerUtils.forcePlayerGameMode(e.getPlayer(), GameMode.ADVENTURE);
				PlayerUtils.clearPlayerInventory(e.getPlayer());
				PlayerUtils.clearPlayerPotions(e.getPlayer());
				
				e.getPlayer().setAllowFlight(false);
				e.getPlayer().setFlying(false);
				
				e.getPlayer().setCanPickupItems(false);
				e.getPlayer().setFireTicks(0);
				
				e.getPlayer().resetMaxHealth();
				e.getPlayer().resetTitle();
				e.getPlayer().resetPlayerWeather();
				e.getPlayer().resetPlayerTime();
				
				e.getPlayer().setExp(0);
				e.getPlayer().setLevel(0);
				
				AuthBase.evaluate(e.getPlayer());
				
				for(Player player : Bukkit.getOnlinePlayers()) {
					
					player.hidePlayer(e.getPlayer());
					e.getPlayer().hidePlayer(e.getPlayer());
					
				}
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						
						if(e.getPlayer() != null)
							if(e.getPlayer().isOnline())
								e.getPlayer().kickPlayer(TextUtil.format("&6&l�Se te agot� el tiempo de Acceso!"));
						
					}
					
				}.runTaskLater(OmniLobbies.getInstance(), 20 * 60);
				
				ResourceHandler.sendResourcePack(e.getPlayer(), ResourceType.OMNIBLOCK_DEFAULT);
				
				teleportPlayer(e.getPlayer());
				
			}
			
		};
	}

	@Override
	public List<LobbySystem> getSystems() {
		return Lists.newArrayList();
	}

	@Override
	public List<BukkitTask> getTasks() {
		return Lists.newArrayList();
	}

	@Override
	public List<AttributeType> getAttributes() {
		return Arrays.asList(
				AttributeType.VOID_TELEPORTER,
				AttributeType.GAMEMODE_ADVENTURE,
				AttributeType.NO_DAMAGE,
				AttributeType.NO_HUNGER,
				AttributeType.NOT_COLLIDE
				);
	}

	@Override
	public LobbyBoard getBoard() {
		return null;
	}

}