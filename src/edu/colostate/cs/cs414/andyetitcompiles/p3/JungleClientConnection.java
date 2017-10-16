package edu.colostate.cs.cs414.andyetitcompiles.p3;

import com.esotericsoftware.kryonet.Connection;

/*
 * This class extends the default Connection class that kryonet provides.
 * Now we can associate each connection with a user account
 */
public class JungleClientConnection extends Connection {
	private User user;
	
	public JungleClientConnection() {
		this.user = null;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}
}
