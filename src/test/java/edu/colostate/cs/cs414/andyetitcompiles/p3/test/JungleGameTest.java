
package edu.colostate.cs.cs414.andyetitcompiles.p3.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.colostate.cs.cs414.andyetitcompiles.p3.common.JungleBoard;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.JungleGame;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.JunglePiece;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.Rat;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.User;

import java.awt.Color;

public class JungleGameTest {
	JungleGame game;
	User player1 = new User("player1", null, null);
	User player2 = new User("player2", null, null);

	@Before
	public void setUp(){
		game = new JungleGame(player1, player2);
	}
	
	@Test
	public void testMoveOffBoard(){
		JunglePiece bwolf = game.getPiece(Color.BLACK, "wolf");
		game.makeMove(bwolf, game.getTile(1, 4));
		game.makeMove(bwolf, game.getTile(0, 4));
		game.makeMove(bwolf, game.getTile(-1, 4));
		assertEquals(0, bwolf.getCurrentTile().getRow());
		assertEquals(4, bwolf.getCurrentTile().getCol());
	}
	
	@Test
	public void testGetWinnerNoWinner(){
		assertEquals(null, game.getWinner());
	}
	
	@Test
	public void testGetWinnerPlayer1(){
		JunglePiece wleopard = game.getPiece(Color.WHITE, "leopard");
		game.makeMove(wleopard, game.getTile(6, 3));
		game.makeMove(wleopard, game.getTile(5, 3));
		game.makeMove(wleopard, game.getTile(4, 3));
		game.makeMove(wleopard, game.getTile(3, 3));
		game.makeMove(wleopard, game.getTile(2, 3));
		game.makeMove(wleopard, game.getTile(1, 3));
		game.makeMove(wleopard, game.getTile(0, 3));
		assertEquals(player1.getNickname(), game.getWinner().getNickname());
	}
	
	@Test
	public void testGetWinnerPlayer2(){
		JunglePiece bleopard = game.getPiece(Color.BLACK, "leopard");
		game.makeMove(bleopard, game.getTile(2, 3));
		game.makeMove(bleopard, game.getTile(3, 3));
		game.makeMove(bleopard, game.getTile(4, 3));
		game.makeMove(bleopard, game.getTile(5, 3));
		game.makeMove(bleopard, game.getTile(6, 3));
		game.makeMove(bleopard, game.getTile(7, 3));
		game.makeMove(bleopard, game.getTile(8, 3));
		assertEquals(player2.getNickname(), game.getWinner().getNickname());
	}
	
	@Test
	public void testGetPiece(){
		JunglePiece wrat = game.getPiece(Color.WHITE, "rat");
		assertEquals(6, wrat.getCurrentTile().getCol());
		assertEquals(6, wrat.getCurrentTile().getRow());
	}
	
	@Test 
	public void testInvalidMoveNotAdjacent(){
		JunglePiece wdog = game.getPiece(Color.WHITE, "dog");
		assertEquals(7, wdog.getCurrentRow());
		assertEquals(5, wdog.getCurrentCol());
		assertFalse(game.isValidMove(wdog, game.getTile(0, 0)));
		assertEquals(7, wdog.getCurrentRow());
		assertEquals(5, wdog.getCurrentCol());
	}
	
	@Test
	public void testInvalidMoveCaptureSameColor(){
		JunglePiece bcat = game.getPiece(Color.BLACK, "cat");
		assertEquals(1, bcat.getCurrentRow());
		assertEquals(5, bcat.getCurrentCol());
		game.makeMove(bcat, game.getTile(2, 5));
		assertEquals(2, bcat.getCurrentRow());
		assertEquals(5, bcat.getCurrentCol());
		game.makeMove(bcat, game.getTile(4,2));
		assertEquals(2, bcat.getCurrentRow());
		assertEquals(5, bcat.getCurrentCol());
	}
	
	@Test
	public void testValidMoveCapture(){
		JunglePiece belephant = game.getPiece(Color.BLACK, "elephant");
		JunglePiece bcat = game.getPiece(Color.BLACK, "cat");
		game.makeMove(belephant, game.getTile(2, 5));
		assertEquals(2, belephant.getCurrentRow());
		assertEquals(5, belephant.getCurrentCol());
		game.makeMove(bcat, game.getTile(1, 6));
		game.makeMove(bcat, game.getTile(2, 6));
		game.makeMove(bcat, game.getTile(3, 6));
		game.makeMove(bcat, game.getTile(4, 6));
		assertEquals(4, bcat.getCurrentRow());
		assertEquals(6, bcat.getCurrentCol());
		game.makeMove(bcat, game.getTile(5, 6));
		assertEquals(5, bcat.getCurrentRow());
		assertEquals(6, bcat.getCurrentCol());
		game.makeMove(bcat, game.getTile(6, 6));
		assertEquals(6, bcat.getCurrentRow());
		assertEquals(6, bcat.getCurrentCol());
		assertEquals(null, game.getBoard().getPiece(Color.WHITE, "rat"));
	}
	
	@Test
	public void testLegalRiverJump(){
		JunglePiece btiger = game.getPiece(Color.BLACK, "tiger");
		game.makeMove(game.getPiece(Color.BLACK, "cat"), game.getTile(1, 4));
		game.makeMove(btiger, game.getTile(0, 5));
		game.makeMove(btiger, game.getTile(1, 5));
		game.makeMove(btiger, game.getTile(2, 5));
		game.makeMove(btiger, game.getTile(3, 5));
		assertEquals(2, btiger.getCurrentRow());
		assertEquals(5, btiger.getCurrentCol());
		game.makeMove(btiger, game.getTile(6, 5));
		assertEquals(6, btiger.getCurrentRow());
		assertEquals(5, btiger.getCurrentCol());
	}
	
	@Test
	public void testIllegalRiverJump(){
		JunglePiece wwolf = game.getPiece(Color.WHITE, "wolf");
		assertEquals(6, wwolf.getCurrentRow());
		assertEquals(2, wwolf.getCurrentCol());
		game.makeMove(wwolf, game.getTile(5, 2));
		assertEquals(6, wwolf.getCurrentRow());
		assertEquals(2, wwolf.getCurrentCol());
		game.makeMove(wwolf, game.getTile(2, 2));
		assertEquals(6, wwolf.getCurrentRow());
		assertEquals(2, wwolf.getCurrentCol());
	}
	
	@Test
	public void testResetGame(){
		JunglePiece bLeopard = game.getPiece(Color.BLACK, "leopard");
		JungleBoard board = game.getBoard();
		board.movePieceToTile(bLeopard, board.getTile(7, 4));
		assertEquals(4, bLeopard.getCurrentTile().getCol());
		game.resetGame();
		bLeopard = game.getPiece(Color.BLACK, "leopard");
		assertEquals(2, bLeopard.getCurrentTile().getCol());
		assertEquals(2, bLeopard.getCurrentTile().getCol());
	}
	
	@Test
	public void testCapturePiece(){
		JunglePiece wrat = game.getPiece(Color.WHITE, "rat");
		JunglePiece wlion = game.getPiece(Color.WHITE, "lion");
		assertFalse(game.capturePiece(wrat, wlion));
	}
	
}
