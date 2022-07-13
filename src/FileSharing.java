import javax.swing.*;                               //GUI
import java.awt.*;                                  //GUI
import java.io.*;                                   //File Handling
import java.net.Inet4Address;                       //IP Address
import java.net.ServerSocket;                       //Server Socket to communicate with other computers
import java.net.Socket;                             //Client Socket to communicate with other computers
import java.net.UnknownHostException;               //Exception for socket, wrong ip
import java.util.Scanner;                           //Input from keyboard or to read a file
import java.util.concurrent.TimeUnit;               //to get time

public class FileSharing {
    public static String filename;

    public static final int port = 9004;

    public static void main(String[] args) throws UnknownHostException {

        int choice = getChoice();

        switch(choice) {
            case 1:
                System.out.println("Ask the receiver for the IP address and to select the saving directory");
                String ip = getIP();
                String pathIn = getInputPath();
                System.out.println("Selected " + pathIn);
                sendName(ip);
                sendFile(pathIn, ip);
                System.exit(0);
                break;
            case 2:
                System.out.println("Select the directory where you want to save the file.");
                String pathOut = getOutputPath();
                System.out.println("Give this IP to sender");
                System.out.println("IP: " + Inet4Address.getLocalHost().getHostAddress());
                String fileName =  getFileName();
                getFile(pathOut, fileName);
                System.exit(0);
                break;
        }
    }

    static int getChoice() {
        int choice;
        while (true) {
            try {
                Scanner input = new Scanner(System.in);
                System.out.println("1. Send a file");
                System.out.println("2. Receive a file");
                System.out.print("Enter your choice: ");
                choice = input.nextInt();
                if (choice == 1 || choice == 2) {
                    break;
                } else {
                    System.out.println("Invalid choice. Enter Again.\n");
                }
            } catch (Exception e) {
                System.out.println("Invalid Choice. Enter again.\n");
            }
        }
        return choice;
    }

    //Methods below are for sender

    static String getIP() {
        String ip;
        while (true) {
            try {
                Scanner input = new Scanner(System.in);
                System.out.print("Enter receiver's IP address: ");
                ip = input.next();
                if (validIP(ip)) {
                    break;
                } else {
                    System.out.println("Invalid IP. Enter Again.\n");
                }
            } catch (Exception e) {
                System.out.println("Invalid IP. Enter again.\n");
            }
        }
        return ip;
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    static String getInputPath() {

        System.out.println("Select the file you want to send.");
        FileDialog fd = new FileDialog(new Frame(), "Choose a file", FileDialog.LOAD);

        while(true) {
            fd.setVisible(true);

            filename = fd.getFile();
            if (filename == null)
                System.out.println("Please select a file");
            else {
                break;
            }
        }
        return (fd.getDirectory() + filename);
    }

    static void sendName(String ip){
        try {
            System.out.println("Sending file name");
            System.out.println("Connecting to receiver...");
            Socket soc = new Socket(ip, port);

            System.out.println("Connected to receiver");

            DataOutputStream d = new DataOutputStream(soc.getOutputStream());
            System.out.println("Data Output Stream initialized");
            // message to display
            d.writeUTF(filename);

            System.out.println("\u001B[32m" + "File name sent"  + "\u001B[0m");

            d.flush();

            // closing DataOutputStream
            d.close();

            // closing socket
            soc.close();
        } catch (Exception e) {
            System.out.println("Error sending file name");
        }
    }

    static void sendFile(String path, String ip){
        try {
            System.out.println("Sending file...");
            System.out.println("Connecting to receiver");
            TimeUnit.SECONDS.sleep(1);
            // initializing Socket
            Socket soc = new Socket(ip, port);

            System.out.println("Connected to receiver");

            DataOutputStream d = new DataOutputStream( soc.getOutputStream());
            System.out.println("Data output stream initialized");

            System.out.println("Sending file");
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            // send file size
            d.writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[4*1024];

            while ((bytes=fileInputStream.read(buffer))!=-1){
                d.write(buffer,0,bytes);
                d.flush();
            }
            System.out.println("\u001B[32m" + "File sent" + "\u001B[0m");

            d.close();
            fileInputStream.close();
            soc.close();
        }

        catch (Exception e) {
            System.out.println("Error while sending file");
        }

    }

    //All methods below are for Receiver

    static String getOutputPath() {
        while(true) {
            JFileChooser jd = new JFileChooser();
            jd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jd.setDialogTitle("Choose output directory");
            int returnVal = jd.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION){
                System.out.println("Select a directory");
            }
            else{
                return jd.getSelectedFile().toString();
            }
        }
    }

    static String getFileName() {
        String fileName = null;
        try {
            ServerSocket ss = new ServerSocket(port);           //to receive
            System.out.println("Server running");
            // establishes connection
            Socket sock = ss.accept();                          //waiting for connection
            System.out.println("Connected to client");

            // invoking input stream
            DataInputStream dis = new DataInputStream(sock.getInputStream());
            System.out.println("Data Stream established");
            fileName = dis.readUTF();
            System.out.println("\u001B[32m" + "File name (" + fileName + ") received" + "\u001B[0m");

            dis.close();
            sock.close();
            ss.close();
        }
        catch (Exception e) {
            System.out.println("Error receiving file");
        }
        return fileName;
    }

    static void getFile(String path, String fileName){
        try {

            // initializing Socket
            ServerSocket soc = new ServerSocket(port);

            System.out.println("Socket Started");

            Socket clientSocket = soc.accept();

            System.out.println("Connected to sender");

            DataInputStream di= new DataInputStream(clientSocket.getInputStream());
            System.out.println("Data Input Stream initialized");

            FileOutputStream fileOutputStream = new FileOutputStream(path+"//"+fileName);
            System.out.println("Receiving file");
            int bytes = 0;
            long size = di.readLong();     // read file size
            byte[] buffer = new byte[4*1024];
            while (size > 0 && (bytes = di.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read up to file size
            }
            fileOutputStream.close();

            // closing DataOutputStream
            di.close();
            //d.close();
            clientSocket.close();
            // closing socket
            soc.close();
            System.out.println("\u001B[32m" + "File saved at " + path + "\u001B[0m");
        }

        // to initialize Exception in run time
        catch (Exception e) {
            System.out.println("Error while receiving/saving file");
        }
    }
}