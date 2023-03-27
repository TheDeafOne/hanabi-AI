import java.util.*;


/**
 * This is the only class you should edit.
 * @author AlthoffLJ, WoodburnKB, BennettKE
 *
 */
public class Player {
	Board knownBoard;
	Hand selfHand;
	Hand otherHand;
	Hand otherHandKB;

	DiscardType[] selfDiscardable;
	DiscardType[] otherDiscardable;
	boolean[] selfPlayable;
	boolean[] otherPlayable;

	int[] cardAges;

	String playHint;
	String discardHint;
	int constantBoard;
	String prevRandHint;

	HintManager hintManager;
	DiscardManager discardManager;
	/**
	 * This default constructor should be the only constructor you supply.
	 */
	public Player() {
		prevRandHint = "";
		selfHand = new Hand();
		otherHand = new Hand();
		otherHandKB = new Hand();

		selfDiscardable = new DiscardType[5]; //An empty array of 5 variables telling which cards can be discarded
		selfPlayable = new boolean[5]; // An empty array of 5 variables telling which cards can be played
		otherDiscardable = new DiscardType[5]; // An empty array of 5 variables telling which of the other player's cards can be discarded
		otherPlayable = new boolean[5]; // An empty array of 5 variables telling which of the other player's cards can be played
		cardAges = new int[5];

		hintManager = new HintManager();
		discardManager = new DiscardManager();
		// Initialize what we know about our hand and what the other player knows about theirs
		try {
			for (int i = 0; i < 5; i++) {
				otherHandKB.add(i, new Card(-1,-1));
				selfHand.add(i, new Card(-1,-1));
				cardAges[i] = 0;
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
		removeCardFromPlayerHand(discard, otherHandKB);
		try {
			// if he draws a card and the deck isn't empty
			otherHandKB.remove(disIndex);
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
		removeCardFromPlayerHand(discard, selfHand);
		knownBoard = boardState;
	}
	private void removeCardFromPlayerHand(Card discard, Hand hand) {
		try {
			// finds where the discarded card was at in your hand and then removes that index from your known hand
			for (int i = 0; i < hand.size(); i++){
				if(hand.get(i) == discard){
					hand.remove(i);
					hand.add(i, new Card(-1,-1));
					break;
				}
			}
		}  catch(Exception e) {
			e.printStackTrace();
		}
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
		removeCardFromPlayerHand(play, selfHand);
		knownBoard = boardState;
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
			//TODO: maybe reset age here
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
	public void tellNumberHint(int number, ArrayList<Integer> indices, Hand otherHand, Board boardState) throws Exception {
		for (Integer index : indices) {
			Card oldCard = selfHand.get(index);
			Card newCard = new Card(oldCard.color, number);
			selfHand.remove(index);
			selfHand.add(index, newCard);
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


		// update game state
		System.out.println("INFO:");
		System.out.println(Arrays.toString(selfPlayable));
		initializeTurn(boardState, partnerHand);
		System.out.println(Arrays.toString(selfPlayable));
		System.out.println("SELF KNOWLEDGE BASE");
		System.out.println(selfHand);
		System.out.println("OTHER KNOWLEDGE BASE");
		System.out.println(otherHandKB);
		System.out.println("\nACTION:");


		// if play can be made, do so
		String playAction = canPlay();
		if (!playAction.equals("CANNOT_PLAY")) {
			return playAction;
		}

		// if discard can be made, do so
		String discardAction = canDiscard();
		if (!discardAction.equals("CANNOT_DISCARD")) {
			return discardAction;
		}

		// if hint can be made, do so
		String hintAction = canHint();
		if (!hintAction.equals("CANNOT_HINT")) {
			return hintAction;
		}

		// none of the three actions can be done well, so discard a random card
		return discardRandom();
	}

	public void initializeTurn(Board boardState, Hand partnerHand) throws Exception {
		for (int i = 0; i < selfHand.size(); i++) {
			cardAges[i]++;
		}
		knownBoard = boardState;
		otherHand = partnerHand;
		checkConstantBoard();
		loadSelfDiscardableCards();
		loadOtherDiscardableCards();
		loadOtherPlayableCards();
		loadSelfPlayableCards();
	}


	/**
	 * Method for causing the player to hint a card, choosing the best card available based on the knowledge base
	 * @return String representation of hint move
	 */
	public String canHint() throws Exception {
		if (knownBoard.numHints == 0) {
			return "CANNOT_HINT";
		}
		String[] hints = hintManager.findHints(discardManager, knownBoard, otherHand, otherPlayable, otherHandKB, constantBoard);
		discardHint = hints[0];
		playHint = hints[1];

		// neither hint available
		if (discardHint == null && playHint == null) {
			String toReturn = hintManager.randomHint(otherHand,otherPlayable,prevRandHint, discardManager);
			prevRandHint = toReturn;
			return toReturn;
		}
		// discard hint available
		if (playHint == null) {
			setOtherKnowledgeBase(discardHint.split(" "));
			return discardHint;
		}

		// play hint available
		setOtherKnowledgeBase(playHint.split(" "));

		return playHint;
	}

	private void setOtherKnowledgeBase(String[] hintStrings) throws Exception {
		for (int i = 0; i < otherHand.size(); i++) {
			Card card = otherHand.get(i);
			Card oldCard = otherHandKB.get(i);
			if (hintStrings[0].equals("NUMBERHINT")) {
				if (card.value == Integer.parseInt(hintStrings[1])) {
					otherHandKB.remove(i);
					otherHandKB.add(i,new Card(oldCard.color,card.value));
				}
			}

			if (hintStrings[0].equals("COLORHINT")) {
				if (card.color == Integer.parseInt(hintStrings[1])) {
					otherHandKB.remove(i);
					otherHandKB.add(i,new Card(card.color,oldCard.value));
				}
			}
		}
	}


	public String canPlay() throws Exception {
		for (int i = 0; i < selfPlayable.length; i++) {
			if (selfPlayable[i]) {
				selfPlayable[i] = false;
				cardAges[i] = 0;
				selfHand.remove(i);
				selfHand.add(i, new Card(-1,-1));
				return String.format("PLAY %d %d",i,i);
			}
		}
		return "CANNOT_PLAY";
	}

	public String canDiscard() throws Exception {
		for (int i = 0; i < selfDiscardable.length; i++) {
			if (selfDiscardable[i] != DiscardType.CANNOT_DISCARD) {
				selfHand.remove(i);
				cardAges[i] = 0;
				selfHand.add(i, new Card(-1,-1));
				return discardManager.discard(i);
			}
		}
		return "CANNOT_DISCARD";
	}



	public void loadSelfDiscardableCards() throws Exception {
		// manage discardable cards for player hand
		for (int i = 0; i < selfHand.size(); i++) {
			selfDiscardable[i] = discardManager.isSelfDiscardable(selfHand.get(i),knownBoard); // sets all values in selfDiscardable to true or false
		}
	}

	public void loadOtherDiscardableCards() throws Exception {
		// manage playable and discardable cards for partner hand
		for (int i = 0; i < otherHand.size(); i++) {
			otherDiscardable[i] = discardManager.isOtherDiscardable(i,otherHand,knownBoard,otherHandKB); // sets all values in otherDiscardable to true or false
		}
	}

	public void loadOtherPlayableCards() throws Exception {
		for (int i = 0; i < otherHand.size(); i++) {
			otherPlayable[i] = knownBoard.isLegalPlay(otherHand.get(i));// sets all values in otherPlayable to true or false
		}
	}

	public void loadSelfPlayableCards() throws Exception {
		for (int i = 0; i < selfHand.size(); i++) {
			Card card = selfHand.get(i);
			if (card.value != -1 && card.color != -1) {
				selfPlayable[i] = knownBoard.isLegalPlay(card);
			}
		}
	}

	public String discardRandom() throws Exception {
		int maxAgeIndex = 0;
		for (int i = 0; i < selfHand.size(); i++) {
			if (cardAges[i] > cardAges[maxAgeIndex]) {
				maxAgeIndex = i;
			}
		}

		selfHand.remove(maxAgeIndex);
		selfHand.add(maxAgeIndex, new Card(-1,-1));
		return discardManager.discard(maxAgeIndex);
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
}
