import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

/**
 * This is the only class you should edit.
 * @author AlthoffLJ, WoodburnKB, BennettKE
 *
 */
public class Player {
	// Add whatever variables you want. You MAY NOT use static variables, or otherwise allow direct communication between
	// different instances of this class by any means; doing so will result in a score of 0.
	Board knownBoard;
	Hand selfHand; // what you know about your hand (you only know some things)
	Hand otherHand; // what you know about your partners hand (you know everything)
	Hand otherHandP2KB; // what your partner knows about their hand (they only know some things)
	boolean[] selfDiscardable; // what we can guarantee from our own hand is discardable based on hints
		// after each move, check whether the card(s) are guaranteed discardable
	boolean[] selfPlayable; // what we can guarantee from our own hand is playable based on hints
		// after each move, check whether there are guarenteed plays
		// Special case: if we get a single card hint and it's not discardable, assume it is a guaranteed play
	boolean[] otherDiscardable; // what we can guarentee the other player can discard 
	boolean[] otherPlayable; // what we can guarentee the other player can play
	String playHint;
	String discardHint;
	// possible optimization: storing what possible cards could be in our hand (not in board, table, or other's hand)

	//TODO:
	// 1) implement ranking system
	// 2) implement logic for determining whether we have playable and discardable cards
	// 		(update after each move and after hints received)
	// 3) implement logic for determining discardable and playable hints (** use Board.isLegalPlay)
	// 		a) logic for whether a card is discardable (see method stub)
	// 		b) String hint constructor
	
	/**
	 * This default constructor should be the only constructor you supply.
	 */
	public Player() {
		selfHand = new Hand(); // Initial Known Hand for Player 1 (current player)
		otherHand = new Hand(); // Initial Known Hand for Player 2
		otherHandP2KB = new Hand(); // What Player 1 knows Player 2 knows about their hand
		selfDiscardable = new boolean[5]; //An empty array of 5 variables telling which cards can be discarded 
		selfPlayable = new boolean[5]; // An empty array of 5 variables telling which cards can be played
		
		try { //Initializes what the other player knows as a list of null values
			for (int i = 0; i < 5; i++) {
				otherHandP2KB.add(i, new Card(-1,-1));
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	/**
	 * This method runs whenever your partner discards a card.
	 * @param startHand The hand your partner started with before discarding.
	 * @param discard The card he discarded.
	 * @param disIndex The index from which he discarded it.
	 * @param draw The card he drew to replace it; null, if the deck is empty.
	 * @param drawIndex The index to which he drew it.
	 * @param finalHand The hand your partner ended with after redrawing.
	 * @param boardState The state of the board after play.
	 */
	public void tellPartnerDiscard(Hand startHand, Card discard, int disIndex, Card draw, int drawIndex, Hand finalHand, Board boardState) {
		try{
			//Removes the card that the player discarded from his own knowledge base (whatever he knew about it)
			otherHandP2KB.remove(disIndex);
			//if he draws a card and the deck isn't empty - otherwise does nothing
			if(draw != null){
				//adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandP2KB.add(drawIndex, new Card(-1,-1));
			}
		}  catch (Exception e){
			System.out.println(e);
		}
		// what player 1 knows player 2's hand is 
		otherHand = finalHand;
		// what player 1 knows about the board 
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever you discard a card, to let you know what you discarded.
	 * @param discard The card you discarded.
	 * @param boardState The state of the board after play.
	 */
	public void tellYourDiscard(Card discard, Board boardState) {
		try{
			//Finds where the discarded card was at in your hand and then removes that index from your known hand
			for (int i = 0; i < selfHand.size(); i++){
				if(selfHand.get(i) == discard){
					selfHand.remove(i);
					break;
				}
			}
		}
		catch(Exception e){ e.printStackTrace();}
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever your partner played a card
	 * @param startHand The hand your partner started with before playing.
	 * @param play The card she played.
	 * @param playIndex The index from which she played it.
	 * @param draw The card she drew to replace it; null, if the deck was empty.
	 * @param drawIndex The index to which she drew the new card.
	 * @param finalHand The hand your partner ended with after playing.
	 * @param wasLegalPlay Whether the play was legal or not.
	 * @param boardState The state of the board after play.
	 */
	public void tellPartnerPlay(Hand startHand, Card play, int playIndex, Card draw, int drawIndex, Hand finalHand, boolean wasLegalPlay, Board boardState) {
		try{
			//Removes the card that the player discarded from his own knowledge base (whatever he knew about it)
			otherHandP2KB.remove(playIndex);
			//if he draws a card and the deck isn't empty - otherwise does nothing
			if(draw != null){
				//adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandP2KB.add(drawIndex, draw);
			}
		}
		catch (Exception e){ e.printStackTrace();}
		// what player 1 knows player 2's hand is 
		otherHand = finalHand;
		// what player 1 knows about the board 
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever you play a card, to let you know what you played.
	 * @param play The card you played.
	 * @param wasLegalPlay Whether the play was legal or not.
	 * @param boardState The state of the board after play.
	 */
	public void tellYourPlay(Card play, boolean wasLegalPlay, Board boardState) {
		try{
			//Finds where the played card was at in your hand and then removes that index from your known hand
			for (int i = 0; i < selfHand.size(); i++){
				if(selfHand.get(i) == play){
					selfHand.remove(i);
					break;
				}
			}
		}
		catch(Exception e){ e.printStackTrace();}
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever your partner gives you a hint as to the color of your cards.
	 * @param color The color hinted, from Colors.java: RED, YELLOW, BLUE, GREEN, or WHITE.
	 * @param indices The indices (from 0-4) in your hand with that color.
	 * @param otherHand Your partner's current hand.
	 * @param boardState The state of the board after the hint.
	 */
	public void tellColorHint(int color, ArrayList<Integer> indices, Hand otherHand, Board boardState) {
		try {
			for (Integer index : indices) {
				Card oldCard = selfHand.get(index);
				//Card newCard = oldCard != null ? new Card(color, oldCard.value) : new Card(color, -1); Deprecated
				Card newCard = new Card(color, oldCard.value);
				selfHand.remove(index);
				selfHand.add(index, newCard);
			}
		}
		catch(Exception e) {e.printStackTrace();}
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever your partner gives you a hint as to the numbers on your cards.
	 * @param number The number hinted, from 1-5.
	 * @param indices The indices (from 0-4) in your hand with that number.
	 * @param otherHand Your partner's current hand.
	 * @param boardState The state of the board after the hint.
	 */
	public void tellNumberHint(int number, ArrayList<Integer> indices, Hand otherHand, Board boardState) {
		try {
			for (Integer index : indices) {
				Card oldCard = selfHand.get(index);
				//Card newCard = oldCard != null ? new Card(oldCard.value, number) : new Card(-1, number); Deprecated
				Card newCard = new Card(oldCard.color, number);
				selfHand.remove(index);
				selfHand.add(index, newCard);
			}
		}
		catch(Exception e) { e.printStackTrace();}
		this.otherHand = otherHand;
		knownBoard = boardState;
	}
	
	/**
	 * This method runs when the game asks you for your next move.
	 * @param yourHandSize How many cards you have in hand.
	 * @param otherHand Your partner's current hand.
	 * @param boardState The current state of the board.
	 * @return A string encoding your chosen action. Actions should have one of the following formats; in all cases,
	 *  "x" and "y" are integers.
	 * 	a) "PLAY x y", which instructs the game to play your card at index x and to draw a card back to index y. You
	 *     should supply an index y even if you know the deck to be empty. All indices should be in the range 0-4.
	 *     Illegal plays will consume a fuse; at 0 fuses, the game ends with a score of 0.
	 *  b) "DISCARD x y", which instructs the game to discard the card at index x and to draw a card back to index y.
	 *     You should supply an index y even if you know the deck to be empty. All indices should be in the range 0-4.
	 *     Discarding returns one hint if there are fewer than the maximum number available.
	 *  c) "NUMBERHINT x", where x is a value from 1-5. This command informs your partner which of his cards have a value
	 *     of the chosen number. An error will result if none of his cards have that value, or if no hints remain.
	 *     This command consumes a hint.
	 *  d) "COLORHINT x", where x is one of the RED, YELLOW, BLUE, GREEN, or WHITE constant values in Colors.java.
	 *     This command informs your partner which of his cards have the chosen color. An error will result if none of
	 *     his cards have that color, or if no hints remain. This command consumes a hint.
	 */
	public String ask(int yourHandSize, Hand otherHand, Board boardState) {
		int numRemainFuses = boardState.numFuses;
		int numRemainHints = boardState.numHints;
		
		
		
		
		
		return "";
	}

	public boolean isDiscardable(Card check) {
		if((check.color != -1) && (check.value == -1)){
			if(knownBoard.tableau.get(check.color) == 4){ return true;}
			int nextVal = knownBoard.tableau.get(check.color)+1;
			if(check.value != nextVal){
				int numcol = 0;
				for(Card c1 : knownBoard.discards){
					if((c1.value == nextVal)&&(c1.color == check.color)){numcol++;}
				}
				if(numcol == 5){return true;}
			}
		}
		else if((check.color == -1) && (check.value != -1)){
			for(int i = 0; i < 5; i ++){
				if(knownBoard.tableau.get(i) < check.value){ return false;}
			}
		}
		else if((check.color != -1) && (check.value != -1)){
			if(knownBoard.tableau.get(check.color) >= check.value) {return true;}
		}
		return false;
	}
}
