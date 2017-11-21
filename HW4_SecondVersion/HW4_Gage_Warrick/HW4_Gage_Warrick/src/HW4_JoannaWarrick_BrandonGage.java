/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package hw4_joannawarrick_brandongage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HW4_JoannaWarrick_BrandonGage {
    static final int NUM_NODES = 4;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("enter node ID: ");

        //take in an integer ID (0,1,2,or 3) from the console
        int id = sc.nextInt();
        System.out.println();
        int BASE_PORT = 8010;
        int port = id + BASE_PORT;

        System.out.println("Node " + id);
        String host = "localhost";
        String filename = "input" + Integer.toString(id) + ".txt";

        //initialize topology from input<id>.txt
        int[][] topology = new int[NUM_NODES][NUM_NODES];
        String line = null;
        int[] Adjacent = new int[NUM_NODES];

        //open the input file and parse the contents into the "Adjacents" array.
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                //parse line and add to adjacency matrix
                String[] arr = line.split(" ");
                for (int i = 0; i < arr.length; i++) {
                    Adjacent[i] = Integer.parseInt(arr[i]);
                }

            }
            //update the topology matrix to reflect the node's knowledge of
            //its neighbors from the input file. Set all unknown links to "infinity"
            for (int i = 0; i < NUM_NODES; i++) {
                for (int j = 0; j < NUM_NODES; j++) {
                    if (i == id) {
                        topology[i][j] = Adjacent[j];
                    } else {
                        topology[i][j] = 0;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file " + filename);
        } catch (Exception e) {
            System.out.println("Error reading file " + filename);
        }

        try {

            //==== LISTEN =====================================================================
            //lambda function to listen for incoming LS packets
            Thread listen = new Thread() {
                boolean[] receivedAllLSPs = {false, false, false, false};
                volatile boolean receivedAll = false;

                @Override
                public void run() {

                    receivedAllLSPs[id] = true;
                    ServerSocket serversocket;
                    try {
                        serversocket = new ServerSocket(port);

                        while (!receivedAll) {

                            Socket socket = serversocket.accept();

                            //thread runs when the socket receives a msg from a neighboring node.
                            Thread handle = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        InputStream in = new DataInputStream(socket.getInputStream());
                                        ObjectInputStream objectIn = new ObjectInputStream(in);

                                        LinkStatePacket pack = (LinkStatePacket) objectIn.readObject();
                                        System.out.println("Packet received from: " + pack.senderID);
                                        receivedAllLSPs[pack.senderID] = true;
                                        topology[pack.senderID] = pack.senderAdjacents;

                                    } catch (Exception e) {
                                        System.out.println("Error in listen handle: " + e);
                                    }

                                    receivedAll = true;
                                    for (int q = 0; q < NUM_NODES; q++) {
                                        if (!receivedAllLSPs[q]) {
                                            receivedAll = false;
                                        }
                                    }
                                    //if received all LSPs, run Dijkstras Algorithm to generate the
                                    //forwarding table
                                    if(receivedAll)
                                    {
                                        System.out.println();
                                        //print topology
                                        System.out.println("Topology: ");
                                        System.out.println("-----------");
                                        for(int i = 0; i < NUM_NODES;++i)
                                        {
                                            for(int j = 0; j < NUM_NODES; j++)
                                            {
                                                System.out.print(topology[i][j] + " ");
                                            }
                                            System.out.println();
                                        }
                                        System.out.println();

                                        DijkstrasAlg dijkstras = new DijkstrasAlg();
                                        dijkstras.findDijkstra(topology, id);
                                    }

                                    return;

                                }
                            };

                            handle.start();

                            receivedAll = true;
                            for (int q = 0; q < NUM_NODES; q++) {
                                if (!receivedAllLSPs[q]) {
                                    receivedAll = false;
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Error in Listen " + e);
                    }
                }
            }; //end listen
            listen.start();

            //====== BROADCAST =========================================================
            //set up a thread to broadcast the node's local topology to its
            //neighbors
            Thread broadcast = new Thread() {
                @Override
                public void run() {
                    LinkStatePacket lsp = new LinkStatePacket(id, Adjacent);
                    //initialize an array to keep track of whether or not a packet was
                    //received by its target node.
                    boolean[] sentAllLSPs = {false, false, false, false};
                    sentAllLSPs[id] = true;

                    boolean done = false;
                    while (!done) {
                        try {

                            for (int i = 0; i < NUM_NODES; ++i) {
                                //if node<i> hasn't received the LS packet yet, try to send it
                                if (!sentAllLSPs[i]) {
                                    Socket out = new Socket(host, BASE_PORT + i);

                                    OutputStream outTo = new DataOutputStream(out.getOutputStream());
                                    ObjectOutputStream sendObject = new ObjectOutputStream(outTo);
                                    sendObject.writeObject(lsp);
                                    sentAllLSPs[i] = true;
                                }
                            }

                            //check if all LSPs have been sent to the neighboring nodes.
                            done = sentAllLSPs[0] & sentAllLSPs[1] & sentAllLSPs[2] & sentAllLSPs[3];
                            if(done)
                            {
                                return;
                            }

                        } catch (Exception e) {
                           // System.out.println("Error in broadcast run: " + e);
                        }

                    }
                }
            };
            broadcast.start();

        } catch (Exception e) {
           // System.out.println("error in broadcast and listen try-catch, " + e);
        }

    } //end main
}

class LinkStatePacket implements Serializable {
    int senderID;
    int[] senderAdjacents = new int[4];

    LinkStatePacket(int ID, int[] matrix) {
        senderID = ID;
        senderAdjacents = matrix;
    }

    @Override
    public String toString() {
        String matrix = "{";
        for (int i = 0; i < 4; i++) {
            if (i != 3) {
                matrix = matrix + Integer.toString(senderAdjacents[i]) + ", ";
            } else {
                matrix = matrix + Integer.toString(senderAdjacents[i]) + "}";
            }
        }
        String packet = "LinkStatePacket [senderID=" + senderID + ", senderAdjacents=" + matrix + "]";
        return packet;
    }
}
