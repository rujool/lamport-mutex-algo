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
        this.sender.broadcastMessages("release");
    }
}
