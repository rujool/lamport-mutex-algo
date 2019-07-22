import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    private static class Node{
        private int id;
        private String hostname;
        private int port;

        public int getId() {
            return id;
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public Node(int id, String hostname, int port){
            this.id = id;
            this.hostname = hostname;
            this.port = port;
        }
    }

    private static int n, interRequestDelay, csExecTime, requestsPerNode;
    private static List<Node> nodes;
    private static Map<Integer, Node> idToNodeMap;
    public static String ownHostName;
    public static String PROJECT_DIR = System.getProperty("user.dir")+"/";
    public static int ownId, ownPort;
    public static int timestamp, lastRequestTimestamp;
    public static Map<Integer, Boolean> receivedMsgLargerTimestamp;
    public static PriorityBlockingQueue<QueueEntry> priorityQueue;
    private static final Object lock = new Object();


    public static void main(String[] args) {
        try {
            ownHostName = InetAddress.getLocalHost().getHostName();
            System.out.println("Own hostname:"+ownHostName);
            readConfig();
            priorityQueue = new PriorityBlockingQueue<>();
            startReceiver();
            int numRequests = 0;
            do{
                double waitTime = generateRandomExponential(5000);
                busyWait(waitTime);

            }while(numRequests < requestsPerNode);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void readConfig() throws Exception {
        File file = new File(PROJECT_DIR + "/AOS/AOS_Project2/config.txt");
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (isUnsignedInteger(line.split(" ")[0])) {          // Valid line
                if (line.split(" ").length != 4) {
                    throw new Exception("Config file format is invalid. Exactly six tokens required in first valid line");
                }
                String[] strArr = line.split(" ");
                n = Integer.parseInt(strArr[0]);       // No. of nodes
                interRequestDelay = Integer.parseInt(strArr[1]);
                csExecTime = Integer.parseInt(strArr[2]);
                requestsPerNode = Integer.parseInt(strArr[3]);
                break;
            }
        }

        int i = 0;
        nodes = new ArrayList<>();
        idToNodeMap = new HashMap<>();
        while (i < n && sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (isUnsignedInteger(line.split(" ")[0])) {
                String[] strArr = line.split(" ");
                int id = Integer.parseInt(strArr[0]);
                String hostname = strArr[1] + ".utdallas.edu";
                int port = Integer.parseInt(strArr[2]);
                Node node = new Node(id, hostname, port);
                if (hostname.equals(ownHostName)) {
                    ownPort = port;
                    ownId = id;
                }
                nodes.add(node);
                idToNodeMap.put(id, node);
                i++;
            }
        }
    }

    public static boolean isUnsignedInteger(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                return false;
            }
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

    public static void broadcastMessages(String type) {
        new Thread(() -> {
            Message m = new Message(ownId,ownHostName,ownPort,type,timestamp);
            for(Node node: nodes){
                if(node.getId() == ownId){
                    continue;
                }
                boolean wait = true;
                while (wait) {
                    try {
                        System.out.println(ownHostName+" sending " + type +" message to "+node.getHostname());
                        Socket socket = new Socket(node.getHostname(), node.getPort());
                        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                        wait = false;
                        os.writeObject(m);
                        os.close();
                        socket.close();
                    }catch (ConnectException e) {
                        System.out.println("Connection failed, waiting and trying again");
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
            }
        }).start();
    }

    public static void startReceiver() {
        (new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(ownPort)) {
                    while (true) {
                        new ReceiverThread(serverSocket.accept()).start();
                    }
                } catch (IOException e) {
                    System.err.println("Could not listen on port " + ownPort);
                    System.exit(-1);
                }
            }
        }).start();

    }

    private static void generateCSRequest() {
        QueueEntry queueEntry = new QueueEntry(timestamp,ownId);
        priorityQueue.add(queueEntry);
        synchronized (lock){
            timestamp ++;
        }
        lastRequestTimestamp = timestamp;
        broadcastMessages("request");
    }

    private static double generateRandomExponential(double mean){
        double random = Math.random();
        return -(Math.log(1-random)*mean);  // mean = 1/(lambda = rate parameter)
    }

    private static void busyWait(double micros){
        long waitUntil = (long)(micros * 1000000) + System.nanoTime();
        while(waitUntil > System.nanoTime()){
            // do nothing
        }
    }

}
