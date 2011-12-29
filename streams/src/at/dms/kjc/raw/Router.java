package at.dms.kjc.raw;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.flatgraph.FlatNode;

public class Router {
    
    //returns a linked list of coordinates that gives the route
    //including source and dest
    public static LinkedList<Coordinate> getRoute(FlatNode from, FlatNode to) 
    {
        LinkedList<Coordinate> route = new LinkedList<Coordinate>();
        Coordinate fromCoord, toCoord;
    
        fromCoord = Layout.getTile(from);
        toCoord = Layout.getTile(to);
    
        route.add(Layout.getTile(from));

        if (fromCoord== null)
            System.out.println("From Coordinate null");

        int row = fromCoord.getRow();
        int column = fromCoord.getColumn();
        //For now just route the packets in a stupid manner

        //column
        if (fromCoord.getColumn() != toCoord.getColumn()) {
            if (fromCoord.getColumn() < toCoord.getColumn()) {
                for (column = fromCoord.getColumn() + 1; 
                     column <= toCoord.getColumn(); column++)
                    route.add(Layout.getTile(row, column));
                column--;
            }
            else {
                for (column = fromCoord.getColumn() - 1;  
                     column >= toCoord.getColumn(); column--)
                    route.add(Layout.getTile(row, column));
                column++;
            }
        }

        //row
        if (fromCoord.getRow() != toCoord.getRow()) {
            if (fromCoord.getRow() < toCoord.getRow()) {
                for (row = fromCoord.getRow() + 1; 
                     row <= toCoord.getRow(); row++)
                    route.add(Layout.getTile(row, column));
                row--;
            }
            else {
                for (row = fromCoord.getRow() - 1; 
                     row >= toCoord.getRow(); row--) 
                    route.add(Layout.getTile(row, column));
                row++;
            }
        }

        //printRoute(from, to, route);
        return route;
    }

    public static void printRoute(FlatNode from, FlatNode to, List route) {
        System.out.println(from.contents.getName() + " -> " + to.contents.getName());
        Iterator it = route.iterator();
        while (it.hasNext()) {
            Coordinate hop = (Coordinate) it.next();
            System.out.println(Layout.getTileNumber(hop));
        }
    }
    
}

