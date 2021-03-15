public class Square {

    private Cell[][] cells;

    public Square(Cell[][] cells) {
        this.cells = cells;
    }

    public int[] isCorrect(boolean ignoreZeros) {
        /**
         * Checks if the square is correct
         * Return the coordinates of the error, null otherwise
         */
        boolean[] values = new boolean[Grid.N + 1]; //true if the value is present
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) { //going through every cell
                int value = cells[i][j].getValue();
                if (ignoreZeros && value == 0) continue; //ignore zeros if requested
                if (value == 0 || values[value]) return new int[]{i, j}; //if the cell has no value or the value was already present
                else values[value] = true;
            }
        }
        return null;
    }

    public int[] lonelyPossibility() {
        /**
         * Looks for a cell that is the only one to be able to take a certain value
         * Return int[0] = i, int[1] = j, int[2] = value; for the cell and value found, null otherwise
         */
        int[] index = new int[2]; //coordinates of the cell that can take the current value
        boolean flag; //indicates if we need to break out of two loops and skip to checking next value
        for (int value = 1; value <= Grid.N; value++) { //check for every value
            flag = false;
            index[0] = -1;
            index[1] = -1;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) { //check every cell
                    if (cells[i][j].hasPossibleValue(value) && cells[i][j].getValue() == 0) {
                        if (index[0] == -1) { //if this is the first cell to be able to take the value
                            index[0] = i;
                            index[1] = j;
                        }
                        else { //if multiple cells in the square can take the value
                            index[0] = -1;
                            index[1] = -1; //notify no cell will take the value
                            flag = true; //notify we want to break two loops
                            break;
                        }
                    }
                }
                if (flag) break;
            }
            if (index[0] != -1) { //if a unique possibility was spotted
                return new int[]{index[0], index[1], value};
            }
        }
        return null;
    }

    public void updatePossibilities(int val) {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                cells[i][j].removePossibility(val);
            }
        }
    }

    public void initializePossibilities() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) { //Going through every cell
                int val = cells[i][j].getValue();
                if(val != 0) { //If the value is known
                    for(int k = 0; k < 3; k++) {
                        for(int l = 0; l < 3; l++) {
                            cells[k][l].removePossibility(val);
                        }
                    }
                }
            }
        }
    }
}
