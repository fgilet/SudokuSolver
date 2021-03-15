import java.util.Arrays;
import java.util.LinkedList;

public class Grid implements Cloneable {

    public static final int N = 9; //not yet portable to other dimensions
    private Cell[][] cells;
    private Grid next; //contains a clone of the grid, used for bruteforce solution

    public Grid() {
        cells = new Cell[N][N];
    }

    public Grid(int[][] hints) {
        /**
         * hints represents the starting grid
         * 0 means a blank cell
         */
        cells = new Cell[N][N];
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                cells[i][j] = new Cell(hints[i][j]);
            }
        }
        initializePossibilities();
    }

    public Grid clone() {
        Grid clone = new Grid();
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                clone.cells[i][j] = this.cells[i][j].clone();
            }
        }
        return clone;
    }

    public int solve() {
        /**
         * Tries to solve the grid
         * Starts with two basic algorithms
         * When both algorithms are unable to find new values :
         *      - a deep copy of the grid is saved in backup to brute-force a value
         *      - a possible value is given to a cell and this function is recursively called
         *  Sets this as the solution and return 0 on success
         *  Return -1 otherwise
         */
        while (lookForLonelyPossibilities()) { //basic solving algorithms
            while (lookForUniquePossibilities());
        }
        int[] err = isSolved(false); //check if the grid is complete and correct
        if (err == null) { //the grid is solved
            return 0;
        }

        err = isSolved(true); //check for incorrect values
        if (err != null) { //there is an error in the grid
            return -1; //notify failure
        }
        //there are still empty cells

        next = this.clone(); //create a deep copy of the grid to try random values

        int[] bruteForce = next.findBruteForceOption(); //find best bruteforce option

        if (bruteForce != null) { //if there are still possibilities
            next.cells[bruteForce[0]][bruteForce[1]].setValue(bruteForce[2]); //set bruteforce value in the copy
            next.updatePossibilities(bruteForce[0], bruteForce[1], bruteForce[2]); //update possibilities in the copy

            int result = next.solve(); //try to solve the copy with one uncertain value

            if (result == 0) { //success of brute force
                cells = next.cells; //set cells value as the bruteforce solution
                return 0;
            } else { //failure of bruteforce
                cells[bruteForce[0]][bruteForce[1]].removePossibility(bruteForce[2]); //remove failing value
            }
        } else return -2;
        return solve(); //repeating
    }

    public int[] isSolved(boolean ignoreZeros) {
        /**
         * Verifies that the grid is correct
         * Returns the coordinates of the cell that has an issue, null otherwise
         */
        int[] err;
        err = checkLines(ignoreZeros);
        if(err != null) return err;

        transpose();
        err = checkLines(ignoreZeros);
        if(err != null) return err;
        transpose();

        err = checkSquares(ignoreZeros);

        return err;
    }

    private int[] checkSquares(boolean ignoreZeros) {
        int[] err;
        for(int i = 0; i < N; i += 3) {
            for(int j = 0; j < N; j += 3) {
                Square square = getSquare(i, j);
                err = square.isCorrect(ignoreZeros);
                if(err != null) return err;
            }
        }
        return null;
    }

    private int[] checkLines(boolean ignoreZeros) {
        /**
         * Verifies that lines are correct
         * Returns the coordinates of the cell that has an issue, null otherwise
         */
        boolean[] values = new boolean[N + 1]; //true if the value is present in the line
        for(int i = 0; i < N; i++) {
            Arrays.fill(values, false); //resetting the array
            for(int j = 0; j < N; j++) {
                int value = cells[i][j].getValue();
                if (ignoreZeros && value == 0) continue; //ignore zeros if requested
                if (value == 0 || values[value]) return new int[]{i, j}; //if the cell has no value or if the value is already present in the line
                else values[value] = true;
            }
        }
        return null;
    }

    private int[] findBruteForceOption() {
        /**
         * Finds the most likely brute-force option to be accurate
         * Returns int[0] = i, int[1] = j, int[2] = value; null if no option was found
         */
        for (int nPossibilities = 2; nPossibilities <= 9; nPossibilities++) { //looks for cells that have only 2 possibilities, then 3, 4,...
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) { //checks every cell
                    if (cells[i][j].getValue() == 0) { //if the cell is not known
                        LinkedList<Integer> possibleValues = cells[i][j].getPossibleValues();
                        if (possibleValues.size() == nPossibilities) { //if it has the right number of possible values
                            return new int[]{i, j, possibleValues.getFirst()};
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean lookForUniquePossibilities() {
        /**
         * Looks for cells that are the only ones in their line, column or square to
         * be able to take some value
         * Return true if it found at least ona value, false otherwise
         */
        boolean retVal;
        retVal = lookForUniquePossibilityLines();

        transpose();
        retVal = retVal && lookForUniquePossibilityLines();
        transpose();

        retVal = retVal && lookForUniquePossibilitySquares();

        return retVal;
    }

    private boolean lookForUniquePossibilitySquares() {
        /**
         * Looks for cells that are the only ones in their square to ba able to take a certain value
         */
        boolean retVal = false;
        int[] res;
        for (int i = 0; i < N; i += 3) {
            for (int j = 0; j < N; j += 3) { //check for every square
                Square square = getSquare(i, j);
                res = square.lonelyPossibility();
                if (res != null) { //if a result was returned
                    cells[i + res[0]][j + res[1]].setValue(res[2]);
                    updatePossibilities(i + res[0], j + res[1], res[2]);
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    private boolean lookForUniquePossibilityLines() {
        boolean retVal = false;
        int index = -1; //index of the cell that can take the current value
        for (int i = 0; i < N; i++) { //check on every line
            for (int value = 1; value <= N; value++) { //check for every value
                for (int j = 0; j < N; j++) { //check every cell of the line
                    if (cells[i][j].hasPossibleValue(value)) {
                        if (index == -1) index = j; //if j is the first cell with this possibility
                        else { //if multiple cells on the line can take this possibility
                            index = -1; //notify that no cell will change
                            break; //skip to next value
                        }
                    }
                }
                if (index != -1) { //unique possibility found
                    cells[i][index].setValue(value);
                    updatePossibilities(i, index, value);
                    retVal = true;
                    index = -1;
                }
            }
        }
        return retVal;
    }

    private boolean lookForLonelyPossibilities() {
        /**
         * Looks for a cell that can only take one possible value
         * Returns true if it found at least one value, false otherwise
         */
        boolean retVal = false;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) { //Going through every cell
                int lonelyPossibility = cells[i][j].getLonelyPossibility();
                if (lonelyPossibility != 0) { //if there is only one possibility
                    cells[i][j].setValue(lonelyPossibility);
                    updatePossibilities(i, j, lonelyPossibility);
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    private void updatePossibilities(int i, int j, int val) {
        /**
         * Removes val as a possibility on line i, then temporarily transposes the grid to repeat on column j,
         * then removes val as a possibility in the square
         */
        updatePossibilitiesLine(i, val);
        transpose();
        updatePossibilitiesLine(j, val);
        transpose();
        updatePossibilitiesSquare(i, j, val);
    }

    private void updatePossibilitiesSquare(int i, int j, int val) {
        /**
         * Removes val as a possibility in the square of cells[i][j]
         */
        //setting i, j as the top left cell of the square
        i = i - (i%3);
        j = j - (j%3);
        Square square = getSquare(i, j);
        square.updatePossibilities(val);
    }

    private void updatePossibilitiesLine(int i, int val) {
        /**
         * Removes val as a possible value on line i
         */
        for(int k = 0; k < N; k++) {
            cells[i][k].removePossibility(val);
        }
    }

    private void initializePossibilities() {
        /**
         * Initializes possibilities on lines, then temporarily transposes the grid to repeat on the columns,
         * then initializes possibilities on squares
         */
        initializePossibilitiesLines();
        transpose();
        initializePossibilitiesLines();
        transpose();
        initializePossibilitiesSquares();
    }

    private void initializePossibilitiesSquares() {
        /**
         * Update the possibilities of each cell based on the hints on the same square
         */
        for(int i = 0; i < N; i += 3) {
            for(int j = 0; j < N; j += 3) {
                Square square = getSquare(i, j);
                square.initializePossibilities();
            }
        }
    }

    private void initializePossibilitiesLines() {
        /**
         * Update the possibilities of each cell based on the hints on the same line
         */
        for(int i = 0; i < N; i++) { //Going through every line
            for(int k = 0; k < N; k++) { //Going through every cell
                int val = cells[i][k].getValue();
                if(val != 0) { //if its value is known
                    for(int l = 0; l < N; l++) { //going through every cell to remove this value
                        cells[i][l].removePossibility(val);
                    }
                }
            }
        }
    }

    private Square getSquare(int i, int j) {
        Square square = new Square(new Cell[][] {
                {cells[i][j], cells[i][j + 1], cells[i][j + 2]},
                {cells[i + 1][j], cells[i + 1][j + 1], cells[i + 1][j + 2]},
                {cells[i + 2][j], cells[i + 2][j + 1], cells[i + 2][j + 2]}
        });
        return square;
    }

    private void transpose() {
        Cell[][] transpose = new Cell[N][N];
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                transpose[i][j] = cells[j][i];
            }
        }
        cells = transpose;
    }

    public static int[][] stringToArray(String s) {
        if(s.length() != N*N) throw new IllegalArgumentException();
        int[][] array = new int[N][N];
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                char ch = s.charAt(i * N + j);
                if(!Character.isDigit(ch)) throw new IllegalArgumentException();
                array[i][j] = Character.getNumericValue(ch);
            }
        }
        return array;
    }

    public void print(int k, int l) {
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                if (i == k && j == l) System.out.print(cells[i][j].getValue() + ".  ");
                else System.out.print(cells[i][j].getValue() + "  ");
                if(j == 2 || j == 5) System.out.print(" ");
            }
            System.out.println();
            if(i == 2 || i == 5) System.out.println();
        }
        System.out.println();
        System.out.println();
    }
}
