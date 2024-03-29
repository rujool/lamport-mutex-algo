import java.io.*;
import java.net.Socket;

public class ReceiverThread extends Thread {
    private Socket socket = null;
    private MutexService mutexService;

    public ReceiverThread(Socket socket,MutexService mutexService) {
        super("ReceiverThread");
        this.socket = socket;
        this.mutexService = mutexService;
    }

    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            while(true) {
                synchronized (Main.class) {
                    try {
                        Message m = (Message) in.readObject();
                        System.out.println(Main.ownHostName + " received " + m.getType() + " message from " + m.getSenderHost());
                        if(m.getType().equals("end")){
                            System.out.println(Main.ownHostName + " received end msg from "+m.getSenderHost());
                            Main.receivedEndMsgMap.put(m.getSenderId(),true);
                            System.out.println(Main.numCSexecuted);
                            if(Main.receivedEndFromAll() && Main.numCSexecuted == Main.requestsPerNode){
                                System.out.println("Writing to file");
                                Main.writeToFile();
                            }
                        }
                        else{
                            Main.timestamp = Math.max(Main.timestamp, m.getTimestamp()) + 1;
                            if (m.getType().equals("request")) {
                                //                        synchronized (Main.class) {
                                Main.priorityQueue.add(new QueueEntry(m.getTimestamp(), m.getSenderId()));
                                if (!Main.executingCS) {    // send reply if waiting for CS
                                    Sender.sendReply(m.getSenderHost(), m.getSenderPort());
                                }
                                //                        }
                            } else if (m.getType().equals("release")) {
                                //                        synchronized (lock){
                                QueueEntry queueEntry = Main.priorityQueue.poll();
                                                            System.out.println("removing request: timestamp "+queueEntry.getTimestamp()+", id: "+queueEntry.getId());
                                //                        }
                            }
                        System.out.println("last req timestamp: " + Main.lastRequestTimestamp + ", m.gettimestamp: " + m.getTimestamp());

                        if (!Main.priorityQueue.isEmpty()) {
                            System.out.println("Priority Queue top id: " + Main.priorityQueue.peek().getId());
                        }
                            if (m.getTimestamp() > Main.lastRequestTimestamp) {                 // Received message with
                                Main.receivedMsgLargerTimestamp.put(m.getSenderId(), true);        // larger timestamp
                            }
                            synchronized (this.mutexService) {
                                if (Main.isL1True() && Main.isL2True()) {
                                    this.mutexService.notify();
                                }
                            }
                        }
                    } catch (EOFException e) {
//                        this.sender.canSend = true;
//                        this.sender.notify();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//      catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}