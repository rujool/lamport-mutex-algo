import java.io.*;

public class MutexService {

    private Sender sender;

    public MutexService(Sender sender) {
        this.sender = sender;
    }

    public void enterCS(){
        Main.initMap();
        this.sender.broadcastMessages("request");
        Main.currentTime = System.currentTimeMillis();
//        System.out.println("Waiting for message with larger timestamp from all processes, and also own request at top");
        synchronized (this){
            try{
//                System.out.println("Waiting for L1 and L2 to be true");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            System.out.println("Received notification from receiver thread.");
        }
    }

    public void leaveCS(){
        Main.totalResponseTime += System.currentTimeMillis() - Main.currentTime;
        Main.priorityQueue.poll();
        System.out.println(Main.ownHostName+" leaving CS");
//        if(Main.numCSexecuted == Main.requestsPerNode - 1){
//            File responseTimeFile = new File(Main.PROJECT_DIR + "/AOS/lamport-mutex-algo/response-time-"+Main.ownId+".txt");
//            try{
//                if(!responseTimeFile.exists()){
//                    responseTimeFile.createNewFile();
//                    FileWriter fw = new FileWriter(responseTimeFile.getAbsoluteFile());
//                    BufferedWriter bw = new BufferedWriter(fw);
//                    bw.write(String.valueOf(Main.totalResponseTime/Main.numCSexecuted));
//                    bw.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
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
