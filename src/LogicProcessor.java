import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LogicProcessor {
    Board knownBoard;

    Hand selfHand;
    Hand otherHand;
    Hand otherHandKB;

    DiscardType[] selfDiscardable;
    DiscardType[] otherDiscardable;
    boolean[] selfPlayable;
    boolean[] otherPlayable;

    String playHint;
    String discardHint;
    int constantBoard;
    String prevRandHint;

    HashMap<Integer, Integer> CARD_MAP = new HashMap<>() {{
        put(1,3); // there are three 1s in each color
        put(2,2); // there are two 2s in each color
        put(3,2); // there are two 3s in each color
        put(4,2); // there are two 4s in each color
        put(5,1); // there is one 5 in each color
    }};

    public LogicProcessor() {
        prevRandHint = "";
        selfHand = new Hand();
        otherHand = new Hand();
        otherHandKB = new Hand();

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

    public void initializeTurn(Board boardState, Hand partnerHand) throws Exception {
        knownBoard = boardState;
        otherHand = partnerHand;
        checkConstantBoard();
        loadSelfDiscardableCards();
        loadOtherDiscardableCards();
        loadOtherPlayableCards();
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
     * Method for causing the player to play a card, choosing the best card available based on the knowledge base
     * @return String representation of play move
     */
    public String play(int index) {
        selfPlayable[index] = false;
        return String.format("PLAY %d %d",index,index);
    }

    /**
     * Method for causing the player to hint a card, choosing the best card available based on the knowledge base
     * @return String representation of hint move
     */
    public String canHint() throws Exception {
        if (knownBoard.numHints == 0) {
            return "CANNOT_HINT";
        }
        findHints();
        if (discardHint == null && playHint == null) {
            String toReturn = randomHint();
            prevRandHint = toReturn;
            return toReturn;
        }
        if (playHint == null) {
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

    public void loadSelfDiscardableCards() throws Exception {
        // manage discardable cards for player hand
        for (int i = 0; i < selfHand.size(); i++) {
            selfDiscardable[i] = isDiscardable(selfHand.get(i)); // sets all values in selfDiscardable to true or false
        }
    }

    public void loadOtherDiscardableCards() throws Exception {
        // manage playable and discardable cards for partner hand
        for (int i = 0; i < otherHand.size(); i++) {
            otherDiscardable[i] = isDiscardable(otherHand.get(i)); // sets all values in otherDiscardable to true or false
        }
    }

    public void loadOtherPlayableCards() throws Exception {
        for (int i = 0; i < otherHand.size(); i++) {
            otherPlayable[i] = knownBoard.isLegalPlay(otherHand.get(i));// sets all values in otherPlayable to true or false
        }
    }

    public String canPlay() {
        for (int i = 0; i < selfPlayable.length; i++) {
            if (selfPlayable[i]) {
                return play(i);
            }
        }
        return "CANNOT_PLAY";
    }

    public String canDiscard() {
        for (int i = 0; i < selfDiscardable.length; i++) {
            if (selfDiscardable[i] != DiscardType.CANNOT_DISCARD) {
                return discard(i);
            }
        }
        return "CANNOT_DISCARD";
    }


    public String discardRandom() {
        //TODO: make logic better for discarding random card
        return discard(0);
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
        HintManager hintManager = new HintManager();
        DiscardType[] discardable = new DiscardType[5];
        for (int i = 0; i<5; i++) {
            discardable[i] = isDiscardableOther(i);
        }
        hintManager.findHints(discardable, otherHand, otherPlayable, otherHandKB, constantBoard);
    }

    private String randomHint() throws Exception {
        int[] colorCounts = {0, 0, 0, 0, 0};
        for (int i = 0; i < otherHand.size(); i++) {
            colorCounts[otherHand.get(i).color] += 1;
        }
        int[] numberCounts = {0, 0, 0, 0, 0};
        for (int i = 0; i < otherHand.size(); i++) {
            numberCounts[otherHand.get(i).value-1] += 1;
        }

        int maxColorIndex = 0;
        int maxNumberIndex = 0;
        for (int i = 0; i < otherHand.size(); i++) {
            if (colorCounts[i] > colorCounts[maxColorIndex]) {
                maxColorIndex = i;
            }
            if (numberCounts[i] > numberCounts[maxNumberIndex]) {
                maxNumberIndex = i;
            }
        }

        if (numberCounts[maxNumberIndex] > colorCounts[maxColorIndex]) {
            return "NUMBERHINT " + (maxNumberIndex+1);
        }
        return "COLORHINT " + maxColorIndex;

//		boolean hintPossible = false;
//		for (int i=0; i<5; i++) {
//			if (numberCounts[i] > 1) {
//				hintPossible = true;
//				if(otherPlayable[i] && prevRandHint != ("NUMBERHINT " + (i+ 1))) {
//					return "NUMBERHINT " + (i+1);
//				}
//			} else if (colorCounts[i] > 1) {
//				hintPossible = true;
//				if (otherPlayable[i] && prevRandHint != ("COLORHINT " + i)) {
//					return "COLORHINT " + i;
//				}
//			}
//		}


//		Random myRand = new Random();
//		if (hintPossible) {
//			while (true) {
//
//				int idx = Math.abs(myRand.nextInt()%5);
//				int isColor = Math.abs(myRand.nextInt()%2);
//
//				if (isColor == 1) {
//					if (colorCounts[idx] > 1) {
//						return "COLORHINT " + idx;
//					}
//				} else {
//					if (numberCounts[idx] > 1) {
//						return "NUMBERHINT " + (idx + 1);
//					}
//				}
//			}
//		}
//		else {
//			return discard(Math.abs(myRand.nextInt(5)));
//		}
    }
}
