public class HintManager {
    public HintManager() {

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
    public void findHints(DiscardType[] discardable, Hand otherHand, boolean[] otherPlayable, Hand otherHandKB, int constantBoard) throws Exception {
//        DiscardType[] discardable = new DiscardType[5];
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
        String playHint = null;
        String discardHint = null;

        Card compare; // holds current card being compared to the potential hint card

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
        if (maxNumDiscard > 0 && maxNumDiscard > maxColorDiscard) {
            discardHint = "NUMBERHINT " + otherHand.get(maxNumDiscardIdx).value;
        } else if (maxColorDiscard > 0 && maxColorDiscard > maxNumDiscard) {
            discardHint = "COLORHINT " + otherHand.get(maxColorDiscardIdx).color;
        }

        if (numPlayIdx != -1) {
            playHint = "NUMBERHINT " + otherHand.get(numPlayIdx).value;
        } else if (colorPlayIdx != -1) {
            playHint = "COLORHINT " + otherHand.get(colorPlayIdx).color;
        }
        // TODO: currently prioritizes number hints and has no preference for other knowledge-base hints (2) vs. single card hints (1),
        //  could alter/optimize selection ---- knowledge-base hints (type 2) may be more useful...
    }
}
