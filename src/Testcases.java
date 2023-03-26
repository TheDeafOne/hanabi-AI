import java.util.ArrayList;
import java.util.Random;

public class Testcases {
    private Board boardState;
    private ArrayList<Player> players;
    private ArrayList<Hand> hands;
    private ArrayList<Card> deck;

    private int currentPlayer;
    private int otherPlayer;

    public static void main(String[] args) throws Exception {
        isDiscardableOther();
    }



    public static boolean isDiscardableOther() throws Exception {
        Board boardState;
        ArrayList<Player> players;
        ArrayList<Hand> hands;
        ArrayList<Card> deck;

        int currentPlayer;
        int otherPlayer;

        boardState = new Board();

        players = new ArrayList<Player>();
        players.add(new Player());
        players.add(new Player());

        hands = new ArrayList<Hand>();
        hands.add(new Hand());
        hands.add(new Hand());

        currentPlayer = 0;
        otherPlayer = 1;

        deck = new ArrayList<Card>();

        // Loads deck with three of each 1, two of each 2-3-4, and one of each 5.
        for (int i = 0; i < 5; i++) {
            deck.add(new Card(i, 1));
            deck.add(new Card(i, 1));
            deck.add(new Card(i, 1));
            deck.add(new Card(i, 2));
            deck.add(new Card(i, 2));
            deck.add(new Card(i, 3));
            deck.add(new Card(i, 3));
            deck.add(new Card(i, 4));
            deck.add(new Card(i, 4));
            deck.add(new Card(i, 5));
        }

        shuffle(deck);

        // testcase 1: discardable hint - discard all red 3's, any red card should be discardable
        boardState.tableau.add(0,2); // table has score of 2
        boardState.discard(new Card(Colors.RED,3));
        boardState.discard(new Card(Colors.RED,3));

        // Deals  cards to both players.
        for (int i = 0; i < 5; i++) {
            try {
                Card c = dealCard(deck, boardState);
                hands.get(0).add(0, c);
                c = dealCard(deck, boardState);
                //hands.get(1).add(0, c);
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }

        hands.get(1).add(0, new Card(Colors.RED, 1));
        hands.get(1).add(1, new Card(Colors.RED, 4));
        hands.get(1).add(2, new Card(Colors.BLUE, 5));
        hands.get(1).add(3, new Card(Colors.BLUE, 4));
        hands.get(1).add(4, new Card(Colors.BLUE, 1));

        System.out.println(players.get(0).ask(hands.get(0).size(),
                hands.get(1), boardState));
//
//        currentPlayer = 0;
//        otherPlayer = 1;

        return true;
    }


    /**
     * Removes the last card from the deck and updates boardState for reduced deck size.
     * @return The card removed
     * @throws Exception if the deck is empty
     */
    public static Card dealCard(ArrayList<Card> deck, Board boardState) throws Exception {
        if (deck.isEmpty()) {
            throw new Exception("Hanabi.dealCard() - Dealing from an empty deck");
        }
        Card c = deck.remove(deck.size() - 1);
        boardState.deckSize--;
        return c;
    }

    /**
     * Checks for endgame conditions
     * @return True if players are out of fuses, out of cards, or have finished tableau; false otherwise.
     */
    public static boolean gameEnded(Board boardState, ArrayList<Card> deck) {
        return (boardState.numFuses <= 0) || (deck.size() == 0) || (boardState.getTableauScore() == 25);

    }

    /**
     * Shuffle the deck using the Fisher-Yates shuffling algorithm.
     */
    public static void shuffle(ArrayList<Card> deck) {
        Random rand = new Random();
        for (int i = deck.size() - 1; i >= 1; i--) {
            int j = rand.nextInt(i + 1);
            Card temp = deck.get(j);
            deck.set(j, deck.get(i));
            deck.set(i, temp);
        }
    }
}
