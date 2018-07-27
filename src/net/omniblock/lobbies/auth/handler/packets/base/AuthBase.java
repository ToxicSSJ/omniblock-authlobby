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

package net.omniblock.lobbies.auth.handler.packets.base;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.omniblock.network.OmniNetwork;
import net.omniblock.network.handlers.base.bases.type.AccountBase;
import net.omniblock.network.handlers.base.sql.make.MakeSQLQuery;
import net.omniblock.network.handlers.base.sql.make.MakeSQLUpdate;
import net.omniblock.network.handlers.base.sql.make.MakeSQLUpdate.TableOperation;
import net.omniblock.network.handlers.base.sql.type.TableType;
import net.omniblock.network.handlers.base.sql.util.Resolver;
import net.omniblock.network.handlers.base.sql.util.SQLResultSet;
import net.omniblock.network.library.utils.TextUtil;
import net.omniblock.network.systems.account.AccountManager;
import net.omniblock.network.systems.account.AccountManager.AccountTagType;
import net.omniblock.packets.network.Packets;
import net.omniblock.packets.network.structure.packet.PlayerLoginEvaluatePacket;
import net.omniblock.packets.network.structure.packet.PlayerLoginSucessPacket;
import net.omniblock.packets.network.structure.packet.PlayerSendToServerPacket;
import net.omniblock.packets.network.structure.type.PacketSenderType;
import net.omniblock.packets.object.external.ServerType;

public class AuthBase {

	public static final String DEFAULT_PASS = "$ZPASS";

	public static void setPassword(Player player, String password) {

		MakeSQLUpdate msu = new MakeSQLUpdate(TableType.PLAYER_SETTINGS, TableOperation.UPDATE);

		String randomHex = generateSafeRandomHex(30);
		String encodedPass = passwordToSha256(password, randomHex);

		msu.rowOperation("p_pass_salt", randomHex);
		msu.rowOperation("p_pass", encodedPass);
		msu.whereOperation("p_id", Resolver.getNetworkID(player));

		try {
			msu.execute();
		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static PassInfo getPassword(Player player) {

		MakeSQLQuery msq = new MakeSQLQuery(TableType.PLAYER_SETTINGS).select("p_pass_salt").select("p_pass").where("p_id",
				Resolver.getNetworkID(player));

		try {

			SQLResultSet sqr = msq.execute();

			if (sqr.next()) {
				PassInfo passInfo = new PassInfo();
				passInfo.pass = sqr.get("p_pass");
				passInfo.salt = sqr.get("p_pass_salt");
				return passInfo;
			}

		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}

		PassInfo defPass = new PassInfo();
		defPass.pass = DEFAULT_PASS;
		defPass.salt = DEFAULT_PASS;
		return defPass;

	}

	public static String generateSafeRandomHex(int length) {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[length];
		random.nextBytes(bytes);
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		return encoder.encodeToString(bytes);
	}

	public static String passwordToSha256(String password, String salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte encodedByte : encodedhash) {
				String hex = Integer.toHexString(0xff & encodedByte);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "EXCEPTION:" + password;
	}

	public static boolean isRegister(Player player) {

		MakeSQLQuery msq = new MakeSQLQuery(TableType.PLAYER_SETTINGS).select("p_pass").where("p_id",
				Resolver.getNetworkID(player));

		try {

			SQLResultSet sqr = msq.execute();

			if (sqr.next()) {

				String pass = sqr.get("p_pass");

				if (pass.equalsIgnoreCase(DEFAULT_PASS))
					return false;
				return true;

			}

		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}

		return false;

	}

	public static void sucess(Player player) {

		Packets.STREAMER.streamPacket(new PlayerLoginSucessPacket().setPlayername(player.getName())
				.useIPLogin(AccountManager.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))).build()
				.setReceiver(PacketSenderType.OMNICORD));
		return;

	}

	public static void evaluate(Player player) {

		if (Resolver.getOnlineUUIDByName(player.getName()) == null) {

			player.sendMessage(TextUtil.format("&8&lC&8uentas &6&l» &fSe está comprobando el estado de tu cuenta..."));
			
			Packets.STREAMER.streamPacket(new PlayerLoginEvaluatePacket().setPlayername(player.getName())
					.useIPLogin(AccountManager.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))).build()
					.setReceiver(PacketSenderType.OMNICORD));
			return;

		}

		Packets.STREAMER.streamPacket(new PlayerSendToServerPacket().setPlayername(player.getName())
				.setServertype(ServerType.MAIN_LOBBY_SERVER).setParty(false).build()
				.setReceiver(PacketSenderType.OMNICORE));

		Packets.STREAMER.streamPacket(new PlayerLoginSucessPacket().setPlayername(player.getName())
				.useIPLogin(AccountManager.hasTag(AccountTagType.IP_LOGIN, AccountBase.getTags(player))).build()
				.setReceiver(PacketSenderType.OMNICORD));
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				if(player != null)
					if(player.isOnline())
						player.kickPlayer(TextUtil.format("&c&lNo hay servidores disponibles, Intentalo nuevamente."));
				
			}
			
		}.runTaskLater(OmniNetwork.getInstance(), 20 * 10);
		return;

	}

	public static class PassInfo {
		private String pass;
		private String salt;

		public String getPass() {
			return pass;
		}

		public String getSalt() {
			return salt;
		}
	}

}
