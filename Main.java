import javafx.util.Pair;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;

/*
class CountRunnable implements Runnable{
    private MulticastSocket socket;
    CountRunnable(MulticastSocket socket){
        this.socket = socket;
    }
    public void run(){
        try {
            List<InterfaceAddress> list = socket.getNetworkInterface().getInterfaceAddresses();
            System.out.println(1 + (list == null ? 0 : list.size()));
        } catch (SocketException ignored) {
        }
    }
}
*/

class UpdateRunnable implements Runnable {
    private MulticastSocket socket;
    private int count = -1;
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
                int size = Main.members.size();
                if (count != size)
                    System.out.println(count = size);
                Main.members.clear();
            } catch (InterruptedException ignored) {
            }
        }
    }
}

class SendRunnable implements  Runnable{
    private MulticastSocket socket;
    private InetAddress group;
    SendRunnable(MulticastSocket socket, InetAddress group){
        this.socket = socket;
        this.group = group;
    }
    public void run(){
        try {
            while (true){
                DatagramPacket dg = new DatagramPacket(Main.DG.getBytes(), Main.DG.length(), group, Main.port);
                socket.send(dg);
                Thread.sleep(500);
            }
        } catch (InterruptedException | IOException ignored) {
        }
    }
}

class ListenRunnable implements Runnable{
    private MulticastSocket socket;
    ListenRunnable(MulticastSocket socket){
        this.socket = socket;
    }
    public void run(){
        byte[] buf = new byte[256];
        DatagramPacket dg;
        while (true) {
            dg = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(dg);
                String s = new String(dg.getData(), 0, dg.getLength());
                if (Main.DG.equals(s))
                    Main.members.add(new Pair<>(dg.getAddress(), dg.getPort()));
            } catch (IOException ignored) {
            }
        }
    }
}

public class Main
{
    static HashSet<Pair<InetAddress, Integer>> members = new HashSet<>();
    static int count;
    final static int port = 4446;
    final static String DG = "0";
    public static void main(String[] args) throws IOException{
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(args[0]);
        socket.joinGroup(group);
        new Thread(new ListenRunnable(socket)).start();
        new Thread(new SendRunnable(socket, group)).start();
        new Thread(new UpdateRunnable()).start();
        Scanner s = new Scanner(System.in);
        while (!s.nextLine().equals("q"));
        socket.leaveGroup(group);
        System.exit(0);
    }
}