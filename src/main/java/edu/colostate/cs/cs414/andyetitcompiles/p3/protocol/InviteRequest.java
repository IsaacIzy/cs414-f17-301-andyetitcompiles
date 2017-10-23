
package edu.colostate.cs.cs414.andyetitcompiles.p3.protocol;

import edu.colostate.cs.cs414.andyetitcompiles.p3.common.User;

// InviteRequests are sent by the client to the server when a client invites another user to play a game
// InviteRequests are also sent by the server to the recipient of an InviteRequest sent by another client
public class InviteRequest {
	User invitee;
	User inviter;
	
	public InviteRequest(User invitee, User inviter) {
		this.invitee = invitee;
		this.inviter = inviter;
	}
	
	public InviteRequest() {}
	
<<<<<<< HEAD
	public User getRecipient() {return recipient;}
	public User getSender() {return sender;}
}
=======
	public User getInvitee() {return invitee;}
	public User getInviter() {return inviter;}
}
>>>>>>> branch 'master' of https://github.com/tartona/cs414-f17-301-andyetitcompiles
