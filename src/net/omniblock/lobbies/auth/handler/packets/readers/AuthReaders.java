/*
 * Omniblock Developers Team - Copyright (C) 2018 - All Rights Reserved
 *
 * 1. This software is not a free license software, you are not authorized to read, copy, modify, redistribute or
 * alter this file in any form without the respective authorization and consent of the Omniblock Developers Team.
 *
 * 2. If you have acquired this file violating the previous clause described in this Copyright Notice then you must
 * destroy this file from your hard disk or any other storage device.
 *
 * 3. As described in the clause number one, no third party are allowed to read, copy, modify, redistribute or
 * alter this file in any form without the respective authorization and consent of the Omniblock Developers Team.
 *
 * 4. Any concern about this Copyright Notice must be discussed at our support email: soporte.omniblock@gmail.com
 * -------------------------------------------------------------------------------------------------------------
 *
 * Equipo de Desarrollo de Omniblock - Copyright (C) 2018 - Todos los Derechos Reservados
 *
 * 1. Este software no es un software de libre uso, no está autorizado a leer, copiar, modificar, redistribuir
 * o alterar este archivo de ninguna manera sin la respectiva autorización y consentimiento del
 * Equipo de Desarrollo de Omniblock.
 *
 * 2. Si usted ha adquirido este archivo violando la clausula anterior descrita en esta Noticia de Copyright entonces
 * usted debe destruir este archivo de su unidad de disco duro o de cualquier otro dispositivo de almacenamiento.
 *
 * 3. Como se ha descrito en la cláusula número uno, ningun tercero está autorizado a leer, copiar, modificar,
 * redistribuir o alterar este archivo de ninguna manera sin la respectiva autorización y consentimiento del
 * Equipo de Desarrollo de Omniblock.
 *
 * 4. Cualquier duda acerca de esta Noticia de Copyright deberá ser discutido mediante nuestro correo de soporte:
 * soporte.omniblock@gmail.com
 */

package net.omniblock.lobbies.auth.handler.packets.readers;

import net.omniblock.network.handlers.base.sql.util.Resolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.omniblock.lobbies.auth.AuthLobbyPlugin;
import net.omniblock.lobbies.auth.handler.packets.base.AuthBase;
import net.omniblock.lobbies.auth.handler.util.CommandUtil;
import net.omniblock.network.OmniNetwork;
import net.omniblock.network.handlers.base.bases.type.AccountBase;
import net.omniblock.network.library.helpers.actions.SimpleEventListener;
import net.omniblock.network.library.utils.TextUtil;
import net.omniblock.network.systems.account.AccountManager;
import net.omniblock.network.systems.account.AccountManager.AccountTagType;
import net.omniblock.network.systems.adapters.GameCMDAdapter;
import net.omniblock.packets.network.Packets;
import net.omniblock.packets.network.structure.data.PacketSocketData;
import net.omniblock.packets.network.structure.data.PacketStructure;
import net.omniblock.packets.network.structure.data.PacketStructure.DataType;
import net.omniblock.packets.network.structure.packet.PlayerSendToServerPacket;
import net.omniblock.packets.network.structure.packet.ResposeAuthEvaluatePacket;
import net.omniblock.packets.network.structure.type.PacketSenderType;
import net.omniblock.packets.network.tool.object.PacketReader;
import net.omniblock.packets.object.external.ServerType;

public class AuthReaders {

	public static CommandExecutor executor = new CommandExecutor() {

		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			
			if(cmd.getName().equalsIgnoreCase("register") || cmd.getName().equalsIgnoreCase("r") ||
					cmd.getName().equalsIgnoreCase("login") || cmd.getName().equalsIgnoreCase("l")) {
				
				if(!(sender instanceof Player)){
					sender.sendMessage(TextUtil.format("&cEste comando solo puede ser ejecutado por un jugador."));
					return true;
					
				}
				
				Player player = (Player) sender;
				
				if(args.length >= 1) {
					
					if(!AuthBase.isRegister(player)) {

						if (args[0].length() <= 4) {

							player.sendMessage(TextUtil.format(
									"&8&lC&8uentas &c&l» &4ERROR &7Tu contraseña es demasiado corta, escribe una contraseña fácil de aprender:"));
							return false;

						}

						if (args[0].length() >= 20) {

							player.sendMessage(TextUtil.format(
									"&8&lC&8uentas &c&l» &4ERROR &7Tu contraseña es demasiado larga, escribe una contraseña corta y fácil de aprender:"));
							return false;

						}

						String pass = args[0];

						AuthBase.setPassword(player, pass);

						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &a&l» &aTe has registrado correctamente!" + (AccountManager
										.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))
												? " &7También se ha registrado tu &9&lIP &7de acceso!"
												: "")));
						
						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &9&l» &7Se te enviará al Lobby principal en &e&l3&7 segundos..."));

						AuthBase.sucess(player);

						new BukkitRunnable() {

							@Override
							public void run() {

								cancel();
								if (!player.isOnline())
									return;

								Packets.STREAMER.streamPacket(new PlayerSendToServerPacket()
										.setServertype(ServerType.MAIN_LOBBY_SERVER)
										.setPlayername(player.getName())
										.setParty(false)
										.build().setReceiver(PacketSenderType.OMNICORE));
								return;

							}

						}.runTaskLater(OmniNetwork.getInstance(), 20 * 3);
						
						return true;
						
					}
					
					if (args[0].startsWith("/")) {

						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &c&l» &4ERROR &7No debes escribir nungún comando en el proceso de logeo, solo escribe tu contraseña:"));
						return false;

					}

					AuthBase.PassInfo passInfo = AuthBase.getPassword(player);
					String encodedUserPass = AuthBase.passwordToSha256(args[0], passInfo.getSalt());
					String databasePass = passInfo.getPass();

					if (encodedUserPass.equals(databasePass)) {
						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &a&l» &aTe has logeado correctamente!" + (AccountManager
										.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))
												? " &7También se ha registrado tu &9&lIP &7de acceso!"
												: "")));
						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &9&l» &7Se te enviará al servidor en &e&l3 &7segundos..."));

						AuthBase.sucess(player);

						new BukkitRunnable() {

							@Override
							public void run() {

								cancel();
								if (!player.isOnline())
									return;

								Packets.STREAMER.streamPacket(new PlayerSendToServerPacket()
										.setServertype(ServerType.MAIN_LOBBY_SERVER)
										.setPlayername(player.getName())
										.setParty(false)
										.build().setReceiver(PacketSenderType.OMNICORE));
								return;

							}

						}.runTaskLater(OmniNetwork.getInstance(), 20 * 3);
						return true;
						
					}

					player.sendMessage(TextUtil.format("&8&lC&8uentas &c&l» &cClave incorrecta!"));
					return true;
					
				}
				
				return true;
				
			}
			
			return false;
			
		}
		
	};
	
	public static void start() {

		String[] cmds = new String[] { "register", "login", "r", "l" };
		
		for(String cmd : cmds)
			GameCMDAdapter.ACTIVED_COMMANDS.add(cmd);
		
		CommandUtil.setExecutor(executor, cmds);
		
		Packets.READER.registerReader(new PacketReader<ResposeAuthEvaluatePacket>() {

			@Override
			public void readPacket(PacketSocketData<ResposeAuthEvaluatePacket> packetsocketdata) {

				PacketStructure data = packetsocketdata.getStructure();

				String name = data.get(DataType.STRINGS, "playername");
				String status = data.get(DataType.STRINGS, "status");
				
				if (AuthLobbyPlugin.getLobby().getPlayer(name) == null)
					return;

				if (status.equalsIgnoreCase("LOGIN")) {

					Player player = AuthLobbyPlugin.getLobby().getPlayer(name);

					if (!AuthBase.isRegister(player)) {

						player.sendMessage(TextUtil.format(
								"&8&lC&8uentas &9&l» &7Aún no te has registrado, A continuación escribe la contraseña con la cual te registrarás:"));
						
						new SimpleEventListener<AsyncPlayerChatEvent>(AsyncPlayerChatEvent.class, true) {

							@Override
							public boolean incomingEvent(AsyncPlayerChatEvent e) {

								if (e.getPlayer() == player) {

									e.setCancelled(true);

									if (e.getMessage().startsWith("/")) {

										player.sendMessage(TextUtil.format(
												"&8&lC&8uentas &c&l» &4ERROR &7No debes escribir nungún comando en el proceso de registro, solo escribe la contraseña:"));
										return false;

									}

									if (e.getMessage().length() <= 4) {

										player.sendMessage(TextUtil.format(
												"&8&lC&8uentas &c&l» &4ERROR &7Tu contraseña es demasiado corta, escribe una contraseña fácil de aprender:"));
										return false;

									}

									if (e.getMessage().length() >= 20) {

										player.sendMessage(TextUtil.format(
												"&8&lC&8uentas &c&l» &4ERROR &7Tu contraseña es demasiado larga, escribe una contraseña corta y fácil de aprender:"));
										return false;

									}

									String pass = e.getMessage();

									AuthBase.setPassword(player, pass);

									player.sendMessage(TextUtil.format(
											"&8&lC&8uentas &a&l» &aTe has registrado correctamente!" + (AccountManager
													.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))
															? " &7También se ha registrado tu &9&lIP &7de acceso!"
															: "")));
									
									player.sendMessage(TextUtil.format(
											"&8&lC&8uentas &9&l» &7Se te enviará al Lobby principal en &e&l3&7 segundos..."));

									AuthBase.sucess(player);

									new BukkitRunnable() {

										@Override
										public void run() {

											cancel();
											if (!player.isOnline())
												return;

											Packets.STREAMER.streamPacket(new PlayerSendToServerPacket()
													.setServertype(ServerType.MAIN_LOBBY_SERVER)
													.setPlayername(name)
													.setParty(false)
													.build().setReceiver(PacketSenderType.OMNICORE));
											return;

										}

									}.runTaskLater(OmniNetwork.getInstance(), 20 * 3);

									return true;

								}

								return false;
							}

						};

						return;

					}

					player.sendMessage(TextUtil.format(
							"&8&lC&8uentas &9&l» &7Parece que no te has logeado, A continuación &7escribe la contraseña &7con la cual te has registrado:"));

					new SimpleEventListener<AsyncPlayerChatEvent>(AsyncPlayerChatEvent.class, true) {

						@Override
						public boolean incomingEvent(AsyncPlayerChatEvent e) {

							if (e.getPlayer() == player) {

								e.setCancelled(true);

								if (e.getMessage().startsWith("/")) {

									player.sendMessage(TextUtil.format(
											"&8&lC&8uentas &c&l» &4ERROR &7No debes escribir nungún comando en el proceso de logeo, solo escribe tu contraseña:"));
									return false;

								}

								AuthBase.PassInfo passInfo = AuthBase.getPassword(player);
								String encodedUserPass = AuthBase.passwordToSha256(e.getMessage(), passInfo.getSalt());
								String databasePass = passInfo.getPass();

								if (encodedUserPass.equals(databasePass)) {

									player.sendMessage(TextUtil.format(
											"&8&lC&8uentas &a&l» &aTe has logeado correctamente!" + (AccountManager
													.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))
															? " &7También se ha registrado tu &9&lIP &7de acceso!"
															: "")));
									player.sendMessage(TextUtil.format(
											"&8&lC&8uentas &9&l» &7Se te enviará al servidor en &e&l3 &7segundos..."));

									AuthBase.sucess(player);

									new BukkitRunnable() {

										@Override
										public void run() {

											cancel();
											if (!player.isOnline())
												return;

											Packets.STREAMER.streamPacket(new PlayerSendToServerPacket()
													.setServertype(ServerType.MAIN_LOBBY_SERVER)
													.setPlayername(name)
													.setParty(false)
													.build().setReceiver(PacketSenderType.OMNICORE));
											return;

										}

									}.runTaskLater(OmniNetwork.getInstance(), 20 * 3);
									return true;
									
								}

								player.sendMessage(TextUtil.format("&8&lC&8uentas &c&l» &cClave incorrecta!"));
								return false;

							}

							return false;
						}

					};

					return;

				} else if (status.equalsIgnoreCase("SUCESS")) {

					Player player = Bukkit.getPlayer(name);

					player.sendMessage(TextUtil.format("&8&lC&8uentas &a&l» &aTe has logeado correctamente!"));
					player.sendMessage(
							TextUtil.format("&8&lC&8uentas &9&l» &7Se te enviará al servidor en &e&l3 &7segundos..."));

					new BukkitRunnable() {

						@Override
						public void run() {

							cancel();
							if (!player.isOnline())
								return;

							Packets.STREAMER.streamPacket(
									new PlayerSendToServerPacket()
									.setServertype(ServerType.MAIN_LOBBY_SERVER)
									.setPlayername(name)
									.setParty(false)
									.build().setReceiver(PacketSenderType.OMNICORE));
							return;

						}

					}.runTaskLater(OmniNetwork.getInstance(), 20 * 3);

				}

			}

			@Override
			public Class<ResposeAuthEvaluatePacket> getAttachedPacketClass() {
				return ResposeAuthEvaluatePacket.class;
			}

		});

	}

}
