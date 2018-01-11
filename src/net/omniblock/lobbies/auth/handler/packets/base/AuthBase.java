package net.omniblock.lobbies.auth.handler.packets.base;

import java.sql.SQLException;

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

		msu.rowOperation("p_pass", password);
		msu.whereOperation("p_id", Resolver.getNetworkID(player));

		try {

			msu.execute();
			return;

		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}

		return;

	}

	public static String getPassword(Player player) {

		MakeSQLQuery msq = new MakeSQLQuery(TableType.PLAYER_SETTINGS).select("p_pass").where("p_id",
				Resolver.getNetworkID(player));

		try {

			SQLResultSet sqr = msq.execute();

			if (sqr.next()) {
				return sqr.get("p_pass");
			}

		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}

		return DEFAULT_PASS;

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

}
