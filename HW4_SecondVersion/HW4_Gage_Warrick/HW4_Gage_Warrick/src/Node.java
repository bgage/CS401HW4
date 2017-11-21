import java.util.ArrayList;

public class Node
{
    boolean visited;
    int distance;
    ArrayList<Integer> path;

    Node() {
        visited = false;
        distance = Integer.MAX_VALUE;
        path = new ArrayList<Integer>();
    }

    void print_parents()
    {
        for(int i = 0; i < path.size(); ++i)
        {
            System.out.print(" " + path.get(i));
        }
    }
}//end node class