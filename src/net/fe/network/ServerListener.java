package net.fe.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import net.fe.FEMultiplayer;
import net.fe.Player;
import net.fe.fightStage.CombatCalculator;
import net.fe.fightStage.FightStage;
import net.fe.fightStage.HealCalculator;
import net.fe.lobbystage.LobbyStage;
import net.fe.network.message.ClientInit;
import net.fe.network.message.CommandMessage;
import net.fe.network.message.JoinLobby;
import net.fe.network.message.PartyMessage;
import net.fe.network.message.QuitMessage;
import net.fe.network.message.ReadyMessage;
import net.fe.network.message.StartGame;
import net.fe.overworldStage.ClientOverworldStage;
import net.fe.overworldStage.OverworldStage;
import net.fe.transition.OverworldFightTransition;
import net.fe.unit.UnitIdentifier;

public class ServerListener extends Thread {
	
	private Socket socket = null;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Server main;
	private boolean clientQuit;
	final byte[] begin = new byte[]{0x42,0x45,0x47,0x49,0x4e};
	
	public ServerListener(Server main, Socket socket) {
		super("Listener "+main.getCount());
		try {
			this.socket = socket;
			this.main = main;
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("LISTENER: I/O streams initialized");
			sendMessage(new ClientInit(0, main.getCount(), main.getPlayers()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			System.out.println("LISTENER: Start");
			Message message;
			clientQuit = false;
			while(!clientQuit) {
				message = (Message) in.readObject();
				processInput(message);
			}
			System.out.println("LISTENER: Exit");
			main.clients.remove(this);
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			main.clients.remove(this);
		}
	}
	
	public void processInput(Message message) {
		if(message instanceof QuitMessage) {
			clientQuit = true;
		}
		else if(message instanceof CommandMessage) {
			// If the unit attacked, we need to generate battle results
			main.messages.add(message);
			return;	// Wait for the server's overworld stage to get results
		}
		else if(message instanceof PartyMessage) {
			main.messages.add(message);
			return;
		}
		main.broadcastMessage(message);
		main.messages.add(message);
	}
	
	public void sendMessage(Message message) {
		try {
			out.writeObject(message);
			out.flush();
//			System.out.println("SERVER sent message: [" + message.toString() + "]");
		} catch (IOException e) {
			System.err.println("SERVER Unable to send message!");
		}
	}

}
