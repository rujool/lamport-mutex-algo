import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class Sender {


    public static void sendReply(String hostname, int port){
        synchronized (Main.class){
            Main.timestamp++;
            Main.totalMessagesTransmitted ++;
            Message m = new Message(Main.ownId, Main.ownHostName, Main.ownPort,"reply",Main.timestamp);
            boolean wait = true;
            while (wait) {
                try {
                    System.out.println(Main.ownHostName+" sending reply message to "+hostname);
                    Socket socket = new Socket(hostname,port);
                    ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                    wait = false;
                    os.writeObject(m);
                    os.close();
                    socket.close();
                }catch (ConnectException e) {
                    System.out.println("Connection to "+hostname+" failed, waiting and trying again");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            Main.writeToFile();

        }

    }

    public void broadcastMessages(String type) {
        new Thread(() -> {
            synchronized (Main.class){
                Main.timestamp ++;
                if(type.equals("request")){
                    Main.lastRequestTimestamp = Main.timestamp;
                    Main.priorityQueue.add(new QueueEntry(Main.timestamp,Main.ownId));
                }
                Message m = new Message(Main.ownId, Main.ownHostName, Main.ownPort,type,Main.timestamp);
                for(Main.Node node: Main.nodes){
                    if(node.getId() == Main.ownId){
                        continue;
                    }
                    boolean wait = true;
                    while (wait) {
                        try {
                            System.out.println(Main.ownHostName+" sending " + type +" message to "+node.getHostname());
                            Socket socket = new Socket(node.getHostname(), node.getPort());
                            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                            wait = false;
                            os.writeObject(m);
                            if(!m.getType().equals("end")){
                                Main.totalMessagesTransmitted++;
                            }
                            os.close();
                            socket.close();
                        }catch (ConnectException e) {
                            System.out.println("Connection to "+node.getHostname()+" failed, waiting and trying again");
                            try {
                                Thread.sleep(2000);
                                if(type.equals("request")){
                                    Main.totalResponseTime -= 2000;
                                }
                            } catch (InterruptedException e1) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
//                    Main.writeToFile();
                }
            }
        }).start();
    }



}
