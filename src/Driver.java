
public class Driver {
	public static void main(String[] args) {
		Hanabi game = new Hanabi(true);
		game.play();
		
		// This is the code I will actually use to test your code's behavior.
//		int total = 0;
//		for (int i = 0; i < 100; i++) {
//			Hanabi next = new Hanabi(false);
//			int score = next.play();
//			if (score == 0) {
//				System.out.println(next.seed);
//			}
//			System.out.println("Game " + i + " score: " + score);
//			total += score;
//		}
//		System.out.println("Final average: " + (total/100.0));
	}

}
