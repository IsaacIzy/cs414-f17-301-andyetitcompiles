package edu.colostate.cs.cs414.andyetitcompiles.p3.protocol;

import edu.colostate.cs.cs414.andyetitcompiles.p3.common.User;

// UserResponses are sent by the server to any user that sent a UserRequest
public class UserResponse {
	boolean isSuccesful;
	User user;
	String message;

	public UserResponse(boolean isSuccesful, User user, String message) {
		this.isSuccesful = isSuccesful;
		this.user = user;
		this.message = message;
	}

}