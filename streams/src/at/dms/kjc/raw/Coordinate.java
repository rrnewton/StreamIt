package at.dms.kjc.raw;

public class Coordinate {
    private int row;
    private int column;

    public Coordinate(int r, int c) 
    {
        row = r;
        column = c;
    }
    
    public int getColumn() 
    {
        return column;
    }
    
    public int getRow() 
    {
        return row;
    }

    @Override
	public String toString() {
        return "(" + row + ", " + column + ")";
    }
}

    
