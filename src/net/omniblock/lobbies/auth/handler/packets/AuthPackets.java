package net.omniblock.lobbies.auth.handler.packets;

import net.omniblock.lobbies.auth.handler.packets.readers.AuthReaders;

public class AuthPackets {

	public static void registerReaders() {

		AuthReaders.start();

	}
	
}
