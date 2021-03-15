import java.util.Arrays;
import java.util.LinkedList;

public class Cell {

    /**
     * Represents one cell of a grid
     * possibilities[i] = true if i is a possible value,
     * possibilities[0] is not used
     * value represents the final value of the cell
     * value = 0 if the value is not yet defined
     */

    private boolean[] possibilities;
    private int value;

    public Cell(int value) {
        possibilities = new boolean[Grid.N + 1];
        Arrays.fill(possibilities, true);
        this.value = value;
    }


    public Cell clone() {
        Cell clone = new Cell(value);
        System.arraycopy(possibilities, 0, clone.possibilities, 0, Grid.N + 1);
        return clone;
    }

    public void removePossibility(int value) {
        if(value <= 0 || value > Grid.N) throw new IllegalArgumentException();
        possibilities[value] = false;
    }

    public boolean hasPossibleValue(int value) {
        if(value <= 0 || value > Grid.N) throw new IllegalArgumentException();
        if(this.value != 0) return false;
        return possibilities[value];
    }

    public LinkedList<Integer> getPossibleValues() {
        LinkedList<Integer> possibleValues = new LinkedList<>();
        for(int i = 1; i < Grid.N + 1; i++) {
            if (possibilities[i]) possibleValues.add(i);
        }
        return possibleValues;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        Arrays.fill(possibilities, false);
    }

    public int getLonelyPossibility() {
        /**
         * returns the only possibility of the cell, 0 if it has many or if its value is already known
         */
        if(value != 0) return 0; //the value is already known
        int retVal = 0;
        for(int i = 1; i <= Grid.N; i++) {
            if(possibilities[i]) { //if i is possible
                if(retVal == 0) retVal = i; //first possible value found
                else return 0; //another possible value was already found
            }
        }
        return retVal;
    }
}
