import java.util.*;


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
	DiscardType[] selfDiscardable; // what we can guarantee from our own hand is discardable based on hints
		// after each move, check whether the card(s) are guaranteed discardable
	boolean[] selfPlayable; // what we can guarantee from our own hand is playable based on hints
	// after each move, check whether there are guarenteed plays
	// Special case: if we get a single card hint, and it's not discardable, assume it is a guaranteed play
	DiscardType[] otherDiscardable; // what we can guarentee the other player can discard // TODO: remove, we don't care about this, we only care about discardHint
	boolean[] otherPlayable; // what we can guarentee the other player can play
	// TODO: should be updated each time we update otherHand and each time we update the board, not necessarily in the ask function
	String playHint;
	String discardHint;
	int constantBoard;


	// possible optimization: storing what possible cards could be in our hand (not in board, table, or other's hand)

	HashMap<Integer, Integer> CARD_MAP = new HashMap<>() {{
		put(1,3); // there are three 1s in each color
		put(2,2); // there are two 2s in each color
		put(3,2); // there are two 3s in each color
		put(4,2); // there are two 4s in each color
		put(5,1); // there is one 5 in each color
	}};

	//TODO:
	// 1) implement ranking system - Set up initial values for play, discard, and hint in ask method with some initial if-statements
	// 2) implement logic for determining whether we have playable and discardable cards
	// 		(update after each move and after hints received)
	// 3) implement logic for determining guaranteed discardable and playable hints (** use Board.isLegalPlay) ** DONE - Kat
	// 		a) String hint constructor
	// 4) implement logic for producing random hints if we don't have (3)

	/**
	 * This default constructor should be the only constructor you supply.
	 */
	public Player() {
		selfHand = new Hand(); // Initial Known Hand for Player 1 (current player)
		otherHand = new Hand(); // Initial Known Hand for Player 2
		otherHandKB = new Hand(); // What Player 1 knows Player 2 knows about their hand

		selfDiscardable = new DiscardType[5]; //An empty array of 5 variables telling which cards can be discarded
		selfPlayable = new boolean[5]; // An empty array of 5 variables telling which cards can be played
		otherDiscardable = new DiscardType[5]; // An empty array of 5 variables telling which of the other player's cards can be discarded
		otherPlayable = new boolean[5]; // An empty array of 5 variables telling which of the other player's cards can be played

		// Initialize what we know about our hand and what the other player knows about theirs
		try {
			for (int i = 0; i < 5; i++) {
				otherHandKB.add(i, new Card(-1,-1));
				selfHand.add(i, new Card(-1,-1));
			}
		}
		catch(Exception e){
			e.printStackTrace();
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
			// removes the card that the player discarded from his own knowledge base and from the actual hand
			otherHandKB.remove(disIndex);

			// if he draws a card and the deck isn't empty
			if (draw != null){
				// adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandKB.add(drawIndex, new Card(-1,-1));
			}
		}  catch (Exception e){
			e.printStackTrace();
		}
		// what player knows about partners hand
		otherHand = finalHand;
		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever you discard a card, to let you know what you discarded.
	 * @param discard The card you discarded.
	 * @param boardState The state of the board after play.
	 */
	public void tellYourDiscard(Card discard, Board boardState) {
		removeCardFromPlayerHand(discard, boardState);
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
		try {
			//Removes the card that the player discarded from his own knowledge base (whatever he knew about it)
			otherHandKB.remove(playIndex);

			//if he draws a card and the deck isn't empty - otherwise does nothing
			if(draw != null){
				//adds a null card in the space where he added it in his hand, offsetting any other cards he may know at that index
				otherHandKB.add(drawIndex, new Card(-1,-1));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		// what player knows about partners hand
		otherHand = finalHand;

		knownBoard = boardState;
	}
	
	/**
	 * This method runs whenever you play a card, to let you know what you played.
	 * @param play The card you played.
	 * @param wasLegalPlay Whether the play was legal or not.
	 * @param boardState The state of the board after play.
	 */
	public void tellYourPlay(Card play, boolean wasLegalPlay, Board boardState) {
		removeCardFromPlayerHand(play, boardState);
	}
	
	/**
	 * This method runs whenever your partner gives you a hint as to the color of your cards.
	 * @param color The color hinted, from Colors.java: RED, YELLOW, BLUE, GREEN, or WHITE.
	 * @param indices The indices (from 0-4) in your hand with that color.
	 * @param otherHand Your partner's current hand.
	 * @param boardState The state of the board after the hint.
	 */
	public void tellColorHint(int color, ArrayList<Integer> indices, Hand otherHand, Board boardState) throws Exception {
		for (Integer index : indices) {
			Card oldCard = selfHand.get(index);
			Card newCard = new Card(color, oldCard.value);
			selfHand.remove(index);
			selfHand.add(index, newCard);
		}

		knownBoard = boardState;
		if (indices.size() == 1) {
			selfPlayable[indices.get(0)] = true;
		}
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
				Card newCard = new Card(oldCard.color, number);
				selfHand.remove(index);
				selfHand.add(index, newCard);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.otherHand = otherHand;
		knownBoard = boardState;

		if (indices.size() == 1) {
			selfPlayable[indices.get(0)] = true;
		}
	}
	
	/**
	 * This method runs when the game asks you for your next move.
	 * @param yourHandSize How many cards you have in hand.
	 * @param partnerHand Your partner's current hand.
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
	public String ask(int yourHandSize, Hand partnerHand, Board boardState) throws Exception {
//		int play = 0;
//		int discard = 0;
//		int hint = 0;
		knownBoard = boardState;
		otherHand = partnerHand;

		checkConstantBoard(); // check if the board is all one number

		// manage discardable cards for player hand
		for (int i = 0; i < yourHandSize; i++) {
			selfDiscardable[i] = isDiscardable(selfHand.get(i)); // sets all values in selfDiscardable to true or false
		}

		// manage playable and discardable cards for partner hand
		for (int i = 0; i < otherHand.size(); i++) {
			otherDiscardable[i] = isDiscardable(otherHand.get(i)); // sets all values in otherDiscardable to true or false
			otherPlayable[i] = knownBoard.isLegalPlay(otherHand.get(i));// sets all values in otherPlayable to true or false
		}
		// if any cards are playable, play them
		for (int i = 0; i < selfPlayable.length; i++) {
			if (selfPlayable[i]) {
				return play(i);
			}
		}

		// if any cards are discardable, discard them
		for (int i = 0; i < selfDiscardable.length; i++) {
			if (selfDiscardable[i] != DiscardType.CANNOT_DISCARD) {
				return discard(i);
			}
		}

		if (boardState.numHints == 0) {
			//TODO: make logic for picking card that has the most information
			return discard(0);
		}

		findHints();
		System.out.println(discardHint);
		System.out.println(playHint);
		//TODO: possibly consider the board when deciding what hint type to pick
		return hint();
	}

	/**
	 * Method for causing the player to play a card, choosing the best card available based on the knowledge base
	 * @return String representation of play move
	 */
	public String play(int index) {
		return String.format("PLAY %d %d",index,index);
	}

	/**
	 * Method for causing the player to hint a card, choosing the best card available based on the knowledge base
	 * @return String representation of hint move
	 */
	public String hint() throws Exception {
		if (discardHint == null && playHint == null) {
			return randomHint();
		}
		if (discardHint != null) {
			return discardHint;
		}
		return playHint;
	}

	/**
	 * Method for causing the player to discard a card, choosing the best card available based on the knowledge base
	 * @return String representation of discard move
	 */
	public String discard(int index) {
		return String.format("DISCARD %d %d", index, index);
	}

	public void checkConstantBoard() throws Exception {
		int tableauScore = knownBoard.tableau.get(0);
		for (int i=1; i<5; i++) {
			if (knownBoard.tableau.get(i) != tableauScore) {
				tableauScore = -1;
				break;
			}
		}
		constantBoard = tableauScore;
		if (constantBoard != -1) {
			for (int i=0; i<5; i++) {
				if (selfHand.get(i).value == constantBoard + 1) {
					selfPlayable[i] = true;
				}
			}
		}
	}


	/**
	 * This method tells whether a card in self's knowledge base is discardable based on the state of the board.
	 * @param check The card being checked
	 * @return a boolean value telling whether the card can be discarded
	 */
	public DiscardType isDiscardable(Card check) {
		// both color and value are known
		if (check.color != -1 && check.value != -1) {
			// has card already been played
			return knownBoard.tableau.get(check.color) >= check.value ? DiscardType.DISCARD_BY_EITHER : DiscardType.CANNOT_DISCARD;
		} else if (check.color != -1){ // only color is known
			return isCardWithKnownColorDiscardable(check);
		} else if (check.value != -1) { // only value is known
			return knownBoard.tableau.stream().allMatch(x -> x >= check.value) ? DiscardType.DISCARD_BY_NUMBER : DiscardType.CANNOT_DISCARD;
		}
		// neither color nor value is known
		return DiscardType.CANNOT_DISCARD;
	}

	private DiscardType isCardWithKnownColorDiscardable(Card check) {
		// if there is already a full stack of 5 for that color
		if (knownBoard.tableau.get(check.color) == 5) {
			return DiscardType.DISCARD_BY_COLOR;
		}

		// check if next card needed is already exhausted by searching through the discarded pile
		// next value needed on the tableau for this card's color
		int nextVal = knownBoard.tableau.get(check.color) + 1;
		if (check.value != nextVal){
			int numColorDiscarded = 0;
			// count how many of that nextVal in that color are already discarded
			for(Card discardedCard : knownBoard.discards){
				if(discardedCard.value == nextVal && discardedCard.color == check.color) {
					numColorDiscarded++;
				}
			}

			// if all n in that color and number are already discarded, then you can discard
			return numColorDiscarded == CARD_MAP.get(nextVal) ? DiscardType.DISCARD_BY_EITHER : DiscardType.CANNOT_DISCARD;
		}
		return DiscardType.CANNOT_DISCARD;
	}

	/**
	 * This method tells whether a card in is discardable based on the state of the board.
	 * @param idx The index of card being checked from otherHand
	 * @return an int value telling whether the card can be discarded, where
	 * 	 		CANNOT_DISCARD indicates it is not discardable,
	 * 	 		DISCARD_BY_COLOR indicates it is discardable because of its number,
	 * 			DISCARD_BY_NUMBER indicates it is discardable because of its color, and
	 * 			DISCARD_BY_EITHER indicates it is discardable because of either its number or its color
	 */
	public DiscardType isDiscardableOther(int idx) {
		Card check;
		try {
			check = otherHand.get(idx);
			// 1) possible color only hints
			boolean gotColorDiscard = false;
			// 1a) that color stack is full
			if(knownBoard.tableau.get(check.color) == 5){
				gotColorDiscard = true;
				// 1b) the next value on the color stack have all been discarded
			} else {
				int nextVal = knownBoard.tableau.get(check.color)+1;
				int numcol = 0;
				for(Card c1 : knownBoard.discards){ // count how many of that nextVal in that color are already discarded
					if((c1.value == nextVal)&&(c1.color == check.color)){numcol++;}
				}
				// check if all the next cards have been discarded, where there are three 1s, two 2s 3s and 4s, and one 5
				if ((nextVal == 1 && numcol == 3) || (nextVal > 1 && nextVal < 5 && numcol == 2) || (nextVal == 5 && numcol == 1)) {
					gotColorDiscard = true;
				}
				// TODO: potential runtime time optimization - keeping a data structure of dead colors
			}

			// 2) possible number only hints
			boolean gotNumberDiscard = true;
			for(int i = 0; i < 5; i ++){ // checks to see if any stack could possibly take the number on the card (now or later)
				if(knownBoard.tableau.get(i) < check.value){ gotNumberDiscard = false;} // not discardable if it's possible
			}

			// 3) discardable because already in play - would need to already know both color or number
			// 		(there may be some overlap between this and 1 and 2, which is okay)
			if (knownBoard.tableau.get(check.color) >= check.value) { // if the card has already been played
				if (otherHandKB.get(idx).color != -1) {
					gotNumberDiscard = true;
				}
				if (otherHandKB.get(idx).value != -1) {
					gotColorDiscard = true;
				}
			}

			// 4) return appropriate value
			if (gotNumberDiscard && gotColorDiscard) {
				return DiscardType.DISCARD_BY_EITHER;
			} else if (gotNumberDiscard) {
				return DiscardType.DISCARD_BY_NUMBER;
			} else if (gotColorDiscard) {
				return DiscardType.DISCARD_BY_COLOR;
			} else {
				return DiscardType.CANNOT_DISCARD;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void removeCardFromPlayerHand(Card discard, Board boardState) {
		try {
			// finds where the discarded card was at in your hand and then removes that index from your known hand
			for (int i = 0; i < selfHand.size(); i++){
				if(selfHand.get(i) == discard){
					selfHand.remove(i);
					break;
				}
			}
		}  catch(Exception e) {
			e.printStackTrace();
		}
		knownBoard = boardState;
	}

	/**
	 * This method finds discard and play hints for otherHand, if they exist, and sets class discardHint and playHint variables accordingly
	 * 		There are two types of guaranteed discard hints:
	 * 			(1) those derived solely from the board state,
	 * 			(2) those derived from the board state AND the other players existing knowledge of their own hand
	 * 				ex. if they know a card is a 2, and they only need to know it is yellow to play it
	 * 				note: these could give extra information while still guaranteeing a discard
	 * 		In a similar way, there are two types of guaranteed play hints:
	 * 			(1) those derived from assuming single card hints are playable hints
	 * 			(2) those derived from the board state AND the other players existing knowledge of their own hand
	 * TODO: not sure if this would be a problem, but how do we make sure we aren't giving hints that give no new information?
	 */
	public void findHints() throws Exception {
		DiscardType[] discardable = new DiscardType[5];
		// each value is DISCARD_BY_EITHER (3), DISCARD_BY_NUMBER (2), DISCARD_BY_COLOR (1), or CANNOT_DISCARD (0)
		// this method uses the class variable otherPlayable
		int maxNumDiscard = 0; // max possible discardable cards using a number hint
		int maxNumDiscardIdx = -1;
		int maxColorDiscard = 0; // max possible discardable cards using a color hint
		int maxColorDiscardIdx = -1;

		int numDiscard; // intermediate counter for number of discardable cards using a number hint
		int colorDiscard; // intermediate counter for number of discardable cards using a color hint

		boolean colorPlay; // tracker for whether play hint can guarenteed using a color hint
		boolean numPlay; // tracker for whether play hint can guarenteed using a number hint
		int colorPlayIdx = -1;
		int numPlayIdx = -1;

		int hintColor; // holds potential hint card's color
		int hintNumber; // holds potential hint card's number
		playHint = null;

		Card compare; // holds current card being compared to the potential hint card

//			// 1) find all discardable cards, and what makes them discardable #### find all playable cards (already done elsewhere)
		for (int i = 0; i<5; i++) {
			discardable[i] = isDiscardableOther(i);
		}


		// 2) find max number of discards possible via hints #### find other's playable hints
		for (int i=0; i<5; i++) {
			if (discardable[i] != DiscardType.CANNOT_DISCARD || otherPlayable[i]) {
				hintColor = otherHand.get(i).color;
				hintNumber = otherHand.get(i).value;
				if (discardable[i] == DiscardType.DISCARD_BY_COLOR) {
					colorDiscard = 0;
					numDiscard = -1;
				} else if (discardable[i] == DiscardType.DISCARD_BY_NUMBER){
					colorDiscard = -1;
					numDiscard = 0;
				} else {
					colorDiscard = 0;
					numDiscard = 0;
				}

				if (otherPlayable[i]) {
					colorPlay = true; // need assumed true start values for the play hint section below (see *)
					numPlay = true;
				} else {
					colorPlay = false; // so we don't log incorrect guaranteed plays
					numPlay = false;
				}

				// compare values for this card, finding guaranteed hints that cover multiple cards if possible
				for (int j=0; j<5; j++) {
					// discard cases:
					if (j != i && discardable[i] != DiscardType.CANNOT_DISCARD) { // don't compare it to itself
						compare = otherHand.get(j);
						if (colorDiscard != -1 && compare.color == hintColor && (discardable[j] == DiscardType.DISCARD_BY_COLOR || discardable[j] == DiscardType.DISCARD_BY_EITHER)) {
							colorDiscard++; // color of discardable card overlaps with our discardable card
						} else if (compare.color == hintColor && (discardable[j] != DiscardType.DISCARD_BY_COLOR && discardable[j] != DiscardType.DISCARD_BY_EITHER)) { // TODO am I thinking about this right? I think so...
							colorDiscard = -1; // non-guaranteed hint
							// i.e. as far as the other player can discern, there are other potentially playable cards of the same color
						}
						if (numDiscard != -1 && compare.value == hintNumber && (discardable[j] == DiscardType.DISCARD_BY_NUMBER || discardable[j] == DiscardType.DISCARD_BY_EITHER)) {
							numDiscard++; // number of discardable card overlaps with our discardable card
						} else if (compare.value == hintNumber && (discardable[j] != DiscardType.DISCARD_BY_NUMBER && discardable[j] != DiscardType.DISCARD_BY_EITHER)) {
							numDiscard = -1; // non-guaranteed hint
							// i.e. as far as the other player can discern, there are other potentially playable cards of the same number
						}
					}
					// playable cases: check if there is anything unique we can hint about (*)
					if (j!=i && otherPlayable[i]) {
						compare = otherHand.get(j);
						if (hintColor == compare.color) {
							colorPlay = false;
						}
						if (hintNumber == compare.value && constantBoard == -1) {
							numPlay = false;
						}
					}
				}

				// check for guaranteed play and discard hints we can acquire using the other player's knowledge base
				if (discardable[i] != DiscardType.CANNOT_DISCARD) { // if the card has already been played
					if (otherHandKB.get(i).color != -1) {
						if (numDiscard < 1) {numDiscard = 1;} // i.e. we don't want to override larger hint values
					}
					if (otherHandKB.get(i).value != -1) {
						if (colorDiscard < 1) colorDiscard = 1;
					}
				}
				if (otherPlayable[i] && otherHandKB.get(i).value != -1) {
					colorPlay = true;
				} else if (otherPlayable[i] && otherHandKB.get(i).value != -1) {
					numPlay = true;
				}

				// update max discardable counts and indices
				if (numDiscard > maxNumDiscard) {
					maxNumDiscard = numDiscard;
					maxNumDiscardIdx = i;
				}
				if (colorDiscard > maxColorDiscard) {
					maxColorDiscard = colorDiscard;
					maxColorDiscardIdx = i;
				}

				// update single play indices
				if (numPlay) {
					numPlayIdx = i;
				}
				if (colorPlay) {
					colorPlayIdx = i;
				}
			}
		}


		// 3) extract any possible discard hint and any possible play hint
		if (maxNumDiscard > 0 || maxColorDiscard > 0) { // if we did find a hint
			if (maxNumDiscard > maxColorDiscard) { // pick the largest hint, defaulting to the color hint
				discardHint = "NUMBERHINT " + otherHand.get(maxNumDiscardIdx).value;
			} else {
				discardHint = "COLORHINT " + otherHand.get(maxNumDiscardIdx).color;
			}
		} else {
			discardHint = null; // indicates that there is no discard hint
		}

		if (numPlayIdx != -1) {
			playHint = "NUMBERHINT " + otherHand.get(numPlayIdx).value;
		} else if (colorPlayIdx != -1) {
			playHint = "COLORHINT " + otherHand.get(colorPlayIdx).color;
		}
		// TODO: currently prioritizes number hints and has no preference for other knowledge-base hints (2) vs. single card hints (1),
		//  could alter/optimize selection ---- knowledge-base hints (type 2) may be more useful...
	}

	private String randomHint() throws Exception {
		// get color hint with maximum coverage
		int[] colorMap = {0, 0, 0, 0, 0};
		for (int i = 0; i < otherHand.size(); i++) {
			colorMap[otherHand.get(i).color] += 1;
		}
		int maxColorIndex = 0;
		for (int i = 0; i < colorMap.length; i++) {
			maxColorIndex = colorMap[i] > colorMap[maxColorIndex] ? i : maxColorIndex;
		}

		// get number hint with maximum coverage
		int[] numberMap = {0, 0, 0, 0, 0};
		for (int i = 0; i < otherHand.size(); i++) {
			numberMap[otherHand.get(i).value-1] += 1;
		}
		int maxNumberIndex = 0;
		for (int i = 0; i < numberMap.length; i++) {
			maxNumberIndex = numberMap[i] > numberMap[maxNumberIndex] ? i : maxNumberIndex;
		}


		if (maxNumberIndex > maxColorIndex) {
			return "NUMBERHINT " + otherHand.get(maxNumberIndex+1).value;
		}
		return "COLORHINT " + otherHand.get(maxColorIndex).color;
	}
}
