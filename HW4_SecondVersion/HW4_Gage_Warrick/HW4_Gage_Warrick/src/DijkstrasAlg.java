import java.lang.*;
import java.io.*;
import java.util.ArrayList;

public class DijkstrasAlg{

    //This function finds the shortest distance to each node using
    //Dijkstra's Algorithm
    public static void findDijkstra(int[][]graph, int src)
    {

        int numVertices = graph.length;
        Node[] vertices = new Node[numVertices];

        //initialize array of vertices (nodes)
        for(int i = 0; i < numVertices; ++i)
        {
            vertices[i] = new Node();
        }
        //initialize source node
        vertices[src].distance = 0;

        //visit each node in the graph, updating distances as they are discovered
        //and adding the minimum-cost node to the MST.
        for(int i = 0; i < numVertices; ++i)
        {
            int next = get_next_node(vertices);

            vertices[next].visited = true;

            //update the distances of neighboring nodes by checking
            //its row in the adjacency matrix (graph)
            for(int g = 0; g < numVertices; g++)
            {
                if(next != g)
                {
                    if(( graph[next][g] != 0) && !vertices[g].visited) {
                        if (graph[next][g] + vertices[next].distance <
                                vertices[g].distance) {
                            vertices[g].distance = graph[next][g] + vertices[next].distance;
                            //add the previous node's path to its neighbor
                            vertices[g].path.clear();
                            for(int f = 0; f < vertices[next].path.size(); ++f)
                            {
                                vertices[g].path.add(vertices[next].path.get(f));
                            }
                            if(next !=0) {
                                vertices[g].path.add(next);
                            }
                        }
                    }
                }
            }

        }

        System.out.println("Node ID     Distance");
        System.out.println("-------     --------");
        //print Dijkstras distance information
        for(int i = 0; i < numVertices; ++i)
        {
            if(i != src) {
                System.out.println(" " + i + "           " + vertices[i].distance);
            }
        }

        System.out.println();
        System.out.println("Forwarding Table");
        System.out.println("Node ID     Forward Node");
        System.out.println("--------    ------------");

        //print forwarding table
        for(int i = 0; i < numVertices; ++i)
        {
            //if the current node is a direct neighbor and there were no smaller paths,
            //print it
            if(i != src) {
                System.out.print(" " + i + "           ");
                if (!vertices[i].path.isEmpty()) {
                    if (vertices[i].path.get(0) == src) {
                        System.out.print(i);
                    } else {
                        System.out.print(vertices[i].path.get(0));
                    }
                } else {
                    System.out.print(i);
                }

                System.out.println();
            }
        }

    }

    //this function returns the index of the unvisited node with the smallest distance
    public static int get_next_node(Node[] verts)
    {
        int min = Integer.MAX_VALUE;
        int index = -1;

        for(int i = 0; i < verts.length; ++i)
        {
            if( (verts[i].visited == false) && (verts[i].distance < min) )
            {
                min = verts[i].distance;
                index = i;
            }
        }

        return index;
    }

}