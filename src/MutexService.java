public class MutexService {
    public static synchronized void enterCS(){
        Main.broadcastMessages("request");
        System.out.println("Waiting for message with larger timestamp from all processes, and also own request at top");

    }

    public static void leaveCS(){

    }
}
