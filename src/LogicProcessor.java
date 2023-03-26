import java.util.*;

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

    HintManager hintManager;
    DiscardManager discardManager;

    public LogicProcessor() {
        prevRandHint = "";
        selfHand = new Hand();
        otherHand = new Hand();
        otherHandKB = new Hand();

        selfDiscardable = new DiscardType[5]; //An empty array of 5 variables telling which cards can be discarded
        selfPlayable = new boolean[5]; // An empty array of 5 variables telling which cards can be played
        otherDiscardable = new DiscardType[5]; // An empty array of 5 variables telling which of the other player's cards can be discarded
        otherPlayable = new boolean[5]; // An empty array of 5 variables telling which of the other player's cards can be played

        hintManager = new HintManager();
        discardManager = new DiscardManager();
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
     * @param disIndex The index from which he discarded it.
     * @param draw The card he drew to replace it; null, if the deck is empty.
     * @param drawIndex The index to which he drew it.
     * @param finalHand The hand your partner ended with after redrawing.
     * @param boardState The state of the board after play.
     */
    public void otherDiscard(int disIndex, Card draw, int drawIndex, Hand finalHand, Board boardState) {
        try {
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
    public void selfDiscard(Card discard, Board boardState) {
        removeCardFromPlayerHand(discard, boardState);
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
     * This method runs whenever your partner played a card
     * @param playIndex The index from which she played it.
     * @param draw The card she drew to replace it; null, if the deck was empty.
     * @param drawIndex The index to which she drew the new card.
     * @param finalHand The hand your partner ended with after playing.
     * @param boardState The state of the board after play.
     */
    public void otherPlay(int playIndex, Card draw, int drawIndex, Hand finalHand, Board boardState) {
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
     * @param boardState The state of the board after play.
     */
    public void selfPlay(Card play, Board boardState) {
        removeCardFromPlayerHand(play, boardState);
    }

    /**
     * This method runs whenever your partner gives you a hint as to the color of your cards.
     * @param color The color hinted, from Colors.java: RED, YELLOW, BLUE, GREEN, or WHITE.
     * @param indices The indices (from 0-4) in your hand with that color.
     * @param boardState The state of the board after the hint.
     */
    public void colorHint(int color, ArrayList<Integer> indices, Board boardState) throws Exception {
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
    public void numberHint(int number, ArrayList<Integer> indices, Hand otherHand, Board boardState) {
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
     * Method for causing the player to hint a card, choosing the best card available based on the knowledge base
     * @return String representation of hint move
     */
    public String canHint() throws Exception {
        if (knownBoard.numHints == 0) {
            return "CANNOT_HINT";
        }
        hintManager.findHints(discardManager, knownBoard, otherHand, otherPlayable, otherHandKB, constantBoard);
        if (discardHint == null && playHint == null) {
            String toReturn = hintManager.randomHint(otherHand,otherPlayable,prevRandHint, discardManager);
            prevRandHint = toReturn;
            return toReturn;
        }
        if (playHint == null) {
            return discardHint;
        }
        return playHint;
    }


    public void loadSelfDiscardableCards() throws Exception {
        // manage discardable cards for player hand
        for (int i = 0; i < selfHand.size(); i++) {
            selfDiscardable[i] = discardManager.isDiscardable(selfHand.get(i),knownBoard); // sets all values in selfDiscardable to true or false
        }
    }

    public void loadOtherDiscardableCards() throws Exception {
        // manage playable and discardable cards for partner hand
        for (int i = 0; i < otherHand.size(); i++) {
            otherDiscardable[i] = discardManager.isDiscardable(otherHand.get(i),knownBoard); // sets all values in otherDiscardable to true or false
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
                selfPlayable[i] = false;
                return String.format("PLAY %d %d",i,i);
            }
        }
        return "CANNOT_PLAY";
    }

    public String canDiscard() {
        for (int i = 0; i < selfDiscardable.length; i++) {
            if (selfDiscardable[i] != DiscardType.CANNOT_DISCARD) {
                return discardManager.discard(i);
            }
        }
        return "CANNOT_DISCARD";
    }

    public String discardRandom() {
        //TODO: make logic better for discarding random card
        return discardManager.discard(0);
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
