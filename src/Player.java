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
	Hand otherHandKB; // what your partner knows about their hand (they only know some things)
	boolean[] selfDiscardable; // what we can guarantee from our own hand is discardable based on hints
		// after each move, check whether the card(s) are guaranteed discardable
	boolean[] selfPlayable; // what we can guarantee from our own hand is playable based on hints
		// after each move, check whether there are guarenteed plays
		// Special case: if we get a single card hint and it's not discardable, assume it is a guaranteed play
	String playHint;
	String discardHint;
	// possible optimization: storing what possible cards could be in our hand (not in board, table, or other's hand)

	// Delete this once you actually write your own version of the class.
	private static Scanner scn = new Scanner(System.in);
	
	/**
	 * This default constructor should be the only constructor you supply.
	 */
	public Player() {
		selfHand = new Hand(); //Initial Known Hand for Player 1 (current player)
		otherHand = new Hand(); //Initial Known Hand for Player 2
		otherHandKB = new Hand(); //What Player 1 knows Player 2 knows about their hand
		selfDiscardable = new boolean[5];
		selfPlayable = new boolean[5];


		try { //Initializes what the other player knows as a list of null values
			for (int i = 0; i < 5; i++) {
				otherHandKB.add(i, null);
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
			otherHandKB.remove(disIndex);
			//if he draws a card and the deck isn't empty - otherwise does nothing
			if(draw != null){
				//adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandKB.add(drawIndex, null);
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
		catch(Exception e){
			System.out.println(e);
		}
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
			otherHandKB.remove(playIndex);
			//if he draws a card and the deck isn't empty - otherwise does nothing
			if(draw != null){
				//adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandKB.add(drawIndex, draw);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
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
				Card newCard = oldCard != null ? new Card(oldCard.color, color) : new Card(color, -1);
				selfHand.remove(index);
				selfHand.add(index, newCard);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
				Card newCard = oldCard != null ? new Card(oldCard.value, number) : new Card(-1, number);
				selfHand.remove(index);
				selfHand.add(index, newCard);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
		// Your method should construct and return a String without user input.
		return "";
	}

	public boolean isDiscardable(Card check) {
		// TODO: if we know something only about color, check if that color is finished or unfinishable (all the next playable cards have been discarded)
		// TODO: if we know something only about the number, check if all stacks have that number or higher
		return false;
	}
}
