import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        String easy = "024080500003020008900305040200600807009042060356900200030060700790230000005000103";
        String hard = "400100038000390000000200001530070080009000700020000010005003900000400000000900065";
        String superHard = "002500070004160080058000040000025003000081007160000000800007100009302700300000500";
        Grid easyGrid = new Grid(Grid.stringToArray(easy));
        Grid hardGrid = new Grid(Grid.stringToArray(hard));
        Grid superHardGrid = new Grid(Grid.stringToArray(superHard));
        play(superHardGrid);
    }

    public static void play(Grid grid) {
        grid.print(-1, -1);
        int a = grid.solve();
        grid.print(-1, -1);
        int[] err = grid.isSolved(false);
        if(err == null) System.out.println("The grid was correctly solved !");
        else System.out.println("Error at " + err[0] + ", " + err[1] + "   ; " + a);
    }
}
