import java.util.HashMap;

public class DiscardManager {
    private final HashMap<Integer, Integer> CARD_MAP = new HashMap<>() {{
        put(1,3); // there are three 1s in each color
        put(2,2); // there are two 2s in each color
        put(3,2); // there are two 3s in each color
        put(4,2); // there are two 4s in each color
        put(5,1); // there is one 5 in each color
    }};

    public DiscardManager() {
    }

    /**
     * This method tells whether a card in self's knowledge base is discardable based on the state of the board.
     * @param check The card being checked
     * @return a boolean value telling whether the card can be discarded
     */
    public DiscardType isSelfDiscardable(Card check, Board knownBoard) {
        // both color and value are known
        if (check.color != -1 && check.value != -1) {
            // has card already been played
            return knownBoard.tableau.get(check.color) >= check.value ? DiscardType.DISCARD_BY_EITHER : DiscardType.CANNOT_DISCARD;
        } else if (check.color != -1){ // only color is known
            return isCardWithKnownColorDiscardable(check, knownBoard);
        } else if (check.value != -1) { // only value is known
            return knownBoard.tableau.stream().allMatch(x -> x >= check.value) ? DiscardType.DISCARD_BY_NUMBER : DiscardType.CANNOT_DISCARD;
        }
        // neither color nor value is known
        return DiscardType.CANNOT_DISCARD;
    }


    private DiscardType isCardWithKnownColorDiscardable(Card check, Board knownBoard) {
        /// if there is already a full stack of 5 for that color
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
    public DiscardType isOtherDiscardable(int idx, Hand otherHand, Board knownBoard, Hand otherHandKB) {
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

    /**
     * Method for causing the player to discard a card, choosing the best card available based on the knowledge base
     * @return String representation of discard move
     */
    public String discard(int index) {
        return String.format("DISCARD %d %d", index, index);
    }

}
