package edu.colostate.cs.cs414.andyetitcompiles.p3.client;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;

import edu.colostate.cs.cs414.andyetitcompiles.p3.common.User;
import edu.colostate.cs.cs414.andyetitcompiles.p3.protocol.*;

public class JungleClient {
	Client kryoClient;
	boolean loggedIn;
	User clientUser;
	String host;
	Set<User> requestedUsers;
	Map<Integer, ClientGameController> activeGames;
	
	public JungleClient() {
		loggedIn = false;
		kryoClient = new Client(8192, 4096);
		requestedUsers = new HashSet<User>();
		activeGames = new HashMap<Integer, ClientGameController>();
		initializeKryoClient();
	}
	
	// This contains all the code for setting up the kryo client
	public void initializeKryoClient() {
		kryoClient.start();
		// Register the client with the Network class
		Network.register(kryoClient);
		
		// Define listeners that run on the main update thread
		kryoClient.addListener(new Listener() {
			// Called after client successfully connects to the server
			public void connected(Connection c) {
				handleConnection();
			}
			// Listeners for server requests and responses
			public void received(Connection c, Object o) {
				// An invite from another user
				if(o instanceof InviteRequest) {
					handleInviteRequest((InviteRequest)o);
				}
				// Response from server after attempting to log in
				if(o instanceof LoginResponse) {
					handleLoginResponse((LoginResponse)o);
				}
				// Response from server after attempting to register
				if(o instanceof RegisterResponse) {
					handleRegisterResposne((RegisterResponse)o);
				}
				// Response from the server after attempting to unregister
				if(o instanceof UnregisterResponse) {
					handleUnregisterResponse((UnregisterResponse)o);
				}
				// Response from the server after attempting to find a user
				if(o instanceof UserResponse) {
					handleUserResponse((UserResponse)o);	
				}
				// Response from the server after an invite has been accepted
				if(o instanceof InviteResponse) {
					handleInviteResponse((InviteResponse)o);
				}
				// Sent by the server after it creates a new game for the client
				if(o instanceof GameInstance) {
					handleGameInstance((GameInstance)o);
				}
			}
			// Called whenever the client is disconnected from the server
			public void disconnected(Connection c) {
				loggedIn = false;
				System.out.println("You have been disconnected from the server");
			}
		});
		
		// Define a listener that runs on its own thread to handle game communication
		kryoClient.addListener(new ThreadedListener (new Listener() {
			public void received(Connection c, Object o) {
				// This listener is only concerned with GameMessages
				if(o instanceof GameMessage) {
					GameMessage message = (GameMessage)o;
					// Find the associated game
					ClientGameController game = activeGames.get(message.getGameID());
					if(game == null) { // Do nothing if the message gameID is not for one of the clients active games
						System.out.println("Received packet for game that couldn't be found on the client");
						return;
					}
					game.handleMessage(message);
				}
			}
		}));
		// It is best to attempt the connection on its own thread so it does block the loading of other client components (like ui)
		new Thread("Connect") {
			public void run () {
				try {
					kryoClient.connect(5000, Network.host, Network.port);
				} catch (IOException ex) {
					System.out.println("Something went wrong while connecting to the server: " + ex.getMessage());
				}
			}
		}.start();
	}

	// Called when the client first connects to the server
	private void handleConnection() {
		System.out.println("Successfully connected");
	}
	
	// Called when the client receives a new game instance from the server
	private void handleGameInstance(GameInstance game) {
		// Create a new game controller and add it to the active games. 
		activeGames.put(game.getGameID(), new ClientGameController(game.getGameID(), clientUser, game.getOpponent(), game.getColor(), kryoClient));
	}
	
	// Called when the client receives an invite request from another player
	private void handleInviteRequest(InviteRequest request) {
		System.out.println("You have been invited to play jungle by: " + request.getInviter().getNickname());
		// Now we send a notification to the ui, which will then communicate back whether the user accepts or rejects
		// I can't do this until the interface to the ui is defined.
	}

	// Sends login request to the server
	public void login(String email, String password) {
		if(loggedIn) {
			System.out.println("Already logged in!");
			return;
		}
		LoginRequest request = new LoginRequest(email, password);
		kryoClient.sendTCP(request);
	}
	
	// Called when the client receives a response form the server after trying to log in
	private void handleLoginResponse(LoginResponse response) {
		if(response.successful()) {
			loggedIn = true;
			clientUser = response.getUser();
			System.out.println(response.getMessage());
		}
		else {
			System.out.println(response.getMessage());
		}
		
	}

	// Sends registration request to the server
	public void register(String email, String nickname, String password) {
		if(loggedIn) {
			System.out.println("Can't register a new user if you are already logged in!");
			return;
		}
		RegisterRequest request = new RegisterRequest(email, nickname, password);
		kryoClient.sendTCP(request);
	}
	
	// Called when the client receives a response from the server after trying to register a new user
	private void handleRegisterResposne(RegisterResponse response) {
		if(response.successful()) {
			System.out.println(response.getMessage());
		}
		else {
			System.out.println(response.getMessage());
		}
	}

	// Sends unregistration request to the server
	public void unregister(String email, String password) {
		if(!loggedIn) {
			System.out.println("Please login with the account you want to unregister");
			return;
		}
		UnregisterRequest request = new UnregisterRequest(email, password);
		kryoClient.sendTCP(request);
	}
	
	// Called when the client receives a response from the server after trying to unregister
	private void handleUnregisterResponse(UnregisterResponse response) {
		if(response.successful()) {
			System.out.println(response.getMessage());
		}
		else {
			System.out.println(response.getMessage());
		}
	}

	// Sends findUser request to the server. 
	public void findUser(String nickname) {
		if(!loggedIn) {
			System.out.println("Please login before searching for other users");
			return;
		}
		UserRequest request = new UserRequest(nickname);
		kryoClient.sendTCP(request);
	}
	
	// Called when the client receives a result from their user query
	private void handleUserResponse(UserResponse response) {
		if(response.successful()) {
			System.out.println(response.getMessage());
			// Add the user to the set of users the client has searched for
			User requestedUser = response.getUser();
			requestedUsers.add(requestedUser);
			// Then we would send an update to the ui, maybe with a popup,
			// maybe just updating a side menu with a set of a users "friends"
		}
		else {
			System.out.println(response.getMessage());
		}
	}
	
	// Sends an invite request to the server 
	public void invite(User otherUser) {
		if(!loggedIn) {
			System.out.println("Please login before inviting another user");
			return;
		}
		InviteRequest request = new InviteRequest(otherUser, clientUser);
		kryoClient.sendTCP(request);
	}
	
	// Called when the client receives a reply to an invite they sent
	private void handleInviteResponse(InviteResponse response) {
		if(response.isAccepted()) {
			System.out.println(response.getMessage());
		}
		else {
			System.out.println(response.getMessage());
		}
	}

	public boolean getConnectedStatus() {
		return kryoClient.isConnected();
	}
	
	public boolean getLoggedInStatus() {
		return loggedIn;
	}
	
	// Stops the client
	public void stop() {
		kryoClient.stop();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
