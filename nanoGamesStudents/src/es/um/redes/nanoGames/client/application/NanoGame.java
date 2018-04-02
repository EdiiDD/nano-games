package es.um.redes.nanoGames.client.application;

import java.util.HashMap;

import es.um.redes.nanoGames.client.shell.NGCommands;
import es.um.redes.nanoGames.server.NGPlayerInfo;

public class NanoGame {

	public static void main(String[] args) {
		
		// Check the two required arguments
		if (args.length != 2) {
			System.out.println("Usage: java NanoGame <broker_hostname> <server_hostname>");
			return;
		}

		// Create controller object that will accept and process user commands
		NGController controller = new NGController(args[0], args[1]);

		// Begin conversation with broker by getting the token
		if (controller.sendToken()) {
			// Begin accepting commands from user using shell
			do {
				System.out.println(">Insertar comando:");
				controller.readGeneralCommandFromShell();
				controller.processCommand();
			} while (controller.shouldQuit() == false);
		} else
			System.out.println("ERROR: broker not available.");

		System.out.println("Bye.");
	}
}
