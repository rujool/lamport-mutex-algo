import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ReceiverThread extends Thread {
    private Socket socket = null;
    private final Object lock = new Object();
    private boolean monitoringCurrentChannel, monitoringFinished;
    private int numMsgs;

    public ReceiverThread(Socket socket) {
        super("ReceiverThread");
        this.socket = socket;
    }

    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            while(true){
                try{
                    Message m = (Message) in.readObject();
                    synchronized (lock){
                        Main.timestamp = Math.max(Main.timestamp,m.getTimestamp()) + 1;
                    }
                    if(m.getType().equals("request"))
                    {
                        synchronized (lock) {
                            Main.priorityQueue.add(new QueueEntry(m.getTimestamp(), m.getSenderId()));
                        }
                    }
                    else if(m.getType().equals("release")){
                        synchronized (lock){
                            QueueEntry queueEntry = Main.priorityQueue.poll();
                            System.out.println("removing request: timestamp "+queueEntry.getTimestamp()+", id: "+queueEntry.getId());
                        }
                    }
                    // check L1 and L2
//                    if(isL1True() && isL2True()){
//
//                    }
                }catch (EOFException e){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}