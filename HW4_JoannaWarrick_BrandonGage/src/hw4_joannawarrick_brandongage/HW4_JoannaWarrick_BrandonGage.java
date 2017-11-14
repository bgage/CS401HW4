/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw4_joannawarrick_brandongage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author bgage
 */
public class HW4_JoannaWarrick_BrandonGage {
    static final int NUM_NODES = 4;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Scanner sc = new Scanner(System.in);
        int id = sc.nextInt();
        int port = 8000 + id;
        
        System.out.println("Node " + id);
        String host = "localhost";
        String filename = "input" + Integer.toString(id) + ".txt";

        //initialize topology from input<id>.txt
        int[][] topology = new int[NUM_NODES][NUM_NODES];
        String line = null;
        int[] Adjacent = new int[NUM_NODES]; 

        try{
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null)
            {
               //parse line and add to adjacency matrix
                String[] arr = line.split(" ");
                for(int i=0; i < arr.length;i++){
                    Adjacent[i] = Integer.parseInt(arr[i]);
                }
                
            }
            
            for(int i = 0; i < NUM_NODES; i++){
                for(int j = 0; j < NUM_NODES; j++){
                    if(i == id){
                        topology[i][j] = Adjacent[j];
                    }
                    else {
                        topology[i][j] = Integer.MAX_VALUE;
                    }
                }
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Unable to open file " + filename);
        }
        catch (Exception e)
        {
            System.out.println("Error reading file " + filename);
        }
        

        try {
            Thread broadcast = new Thread(){
                
                @Override
                public void run(){
                    LinkStatePacket lsp = new LinkStatePacket(id, Adjacent);
                    while(true){
                        try {
                            Socket out = new Socket("localhost", port+1);

                            OutputStream outTo = new DataOutputStream(out.getOutputStream());
                            ObjectOutputStream sendObject = new ObjectOutputStream(outTo);
                            sendObject.writeObject(lsp);
                        }
                        catch(Exception e) {
                            System.out.println("Error in broadcast run: " + e);
                        }
                    }
                }
            };
            
            System.out.println("starting to broadcast...");
            broadcast.start();
            
            //lambda function to listen
            Thread listen = new Thread() {
                
                @Override
                public void run(){
                    while(true){
                        try{
                            ServerSocket serversocket = new ServerSocket(port);
                            Socket socket = serversocket.accept();
                            Thread handle = new Thread(){
                                @Override
                                public void run(){
                                    try {
                                        System.out.println("handle thread started");
                                        InputStream in = new DataInputStream(socket.getInputStream());
                                        ObjectInputStream objectIn = new ObjectInputStream(in);

                                        LinkStatePacket pack = (LinkStatePacket)objectIn.readObject();
                                        System.out.println("Packet recieved from: " + pack.senderID);
                                    }
                                    catch(Exception e){
                                        System.out.println("Error in listen handle: " + e);
                                    }
                                }
                            };
                            System.out.println("starting to listen");
                            handle.start();
                        }
                        catch(Exception e) {
                            System.out.println("Error in listen: " + e);
                        }
                    }
                }
  
            };//end listen
            listen.start();
        }
        catch(Exception e){
            System.out.println(e);
        }
       

        //run dijkstra's algorithm on the adjacency matrix to find the shortest path
        //and generate the forwarding table

        //print forwarding table


        int[] neighbors = new int[4];

        //initialize forwarding table to 0's
        int[][] forwardingTable = new int[4][2];

    }

}

class LinkStatePacket implements Serializable{
    int senderID;
    int[] senderAdjacents = new int[4];
    
    LinkStatePacket(int ID, int[] matrix){
        senderID = ID;
        senderAdjacents = matrix;
    }
    
    @Override
    public String toString(){
        String matrix = "{";
        for(int i = 0; i < 4; i++){
            if(i != 3){
                matrix = matrix + Integer.toString(senderAdjacents[i]) + ", ";
            }
            else {
                matrix = matrix + Integer.toString(senderAdjacents[i]) + "}";
            }
        }
        String packet = "LinkStatePacket [senderID=" + senderID + ", senderAdjacents=" + matrix +"]" ;
        return packet;        
    }
}
