
package edu.colostate.cs.cs414.andyetitcompiles.p3.common;

public class User {
  
    private String email;
	private String password;
	private String nickname;
	private GameRecord GR = new GameRecord();
	private UserStatus ustatus;
	private JunglePiece piece;
		
	public User(String string, String string2, String string3) {
		// TODO Auto-generated constructor stub
	}
	public String getemail() {return this.email;}
	public String getpassword() {return this.password;}
	public String getnickname() {return this.nickname;}
	public String setemail(String email) {return this.email = email;}
	public String setpassword(String password) {return this.password = password;}
	public String setnickname(String nickname) {return this.nickname = nickname;}

}