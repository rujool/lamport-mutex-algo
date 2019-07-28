import java.io.*;

public class MutexService {

    private Sender sender;

    public MutexService(Sender sender) {
        this.sender = sender;
    }

    public void enterCS(){
        this.sender.broadcastMessages("request");
//        System.out.println("Waiting for message with larger timestamp from all processes, and also own request at top");
        synchronized (this){
            try{
                System.out.println("Waiting for L1 and L2 to be true");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Received notification from receiver thread.");
        }
    }

    public void leaveCS(){
        Main.priorityQueue.poll();
        System.out.println(Main.ownHostName+" leaving CS");
        File logFile = new File(Main.PROJECT_DIR + "/AOS/lamport-mutex-algo/logs.txt");
        try {
            // if file doesnt exists, then create it
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fw = new FileWriter(logFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(Main.ownHostName + " leaving CS");
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sender.broadcastMessages("release");
    }
}
