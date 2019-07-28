import org.xml.sax.ext.Locator2;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {


    private static int numCSexecuted;

    public static class Node{
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
    private static MutexService mutexService;
    private static int n, interRequestDelay, csExecTime, requestsPerNode;
    public static List<Node> nodes;
    private static Map<Integer, Node> idToNodeMap;
    public static String ownHostName;
    public static String PROJECT_DIR = System.getProperty("user.dir")+"/";
    public static int ownId, ownPort;
    public static int timestamp, lastRequestTimestamp;
    public static Map<Integer, Boolean> receivedMsgLargerTimestamp;
    public static PriorityBlockingQueue<QueueEntry> priorityQueue;
    public static final Object sendRcvLock = new Object();
    public static boolean executingCS,canSend;
    public static BlockingQueue<ReceiverThread> receiverThreadQueue;


    public static void main(String[] args) {
        try {
            Sender sender = new Sender();
            mutexService = new MutexService(sender);
            ownHostName = InetAddress.getLocalHost().getHostName();
            System.out.println("Own hostname:"+ownHostName);
            readConfig();
            priorityQueue = new PriorityBlockingQueue<>();
            initMap();
            startReceiver(mutexService);
            int numRequests = 0;
            do{
                double waitTime = generateRandomExponential(interRequestDelay);
                busyWait(waitTime);
                mutexService.enterCS();
                executeCS(csExecTime);
                numRequests ++;
            }while(numRequests < requestsPerNode);
            System.out.println("Num of CS executed: "+numCSexecuted);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void readConfig() throws Exception {
        File file = new File(PROJECT_DIR + "/AOS/lamport-mutex-algo/config.txt");
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

    public static void startReceiver(MutexService mutexService) {
        (new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(ownPort)) {
                    while (true) {
                        ReceiverThread receiverThread = new ReceiverThread(serverSocket.accept(),mutexService);
                        receiverThread.start();
                    }
                } catch (IOException e) {
                    System.err.println("Could not listen on port " + ownPort);
                    System.exit(-1);
                }
            }
        }).start();

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



    public static boolean isL1True() {
        for(Map.Entry<Integer,Boolean> e: receivedMsgLargerTimestamp.entrySet()){
            if(!e.getValue()){
                return false;
            }
        }
        return true;
    }

    public static void initMap(){
        receivedMsgLargerTimestamp = new ConcurrentHashMap<>();
        for(Node node: nodes){
            if(node.getId() != ownId) {
                receivedMsgLargerTimestamp.put(node.getId(),false);
            }
        }
    }

    public static boolean isL2True() {
        return (!priorityQueue.isEmpty() && priorityQueue.peek().getId() == ownId);
    }

    public static void executeCS(double meanExecTime) {
        double csExecTime = generateRandomExponential(meanExecTime);
        System.out.println(ownHostName + " executing CS for "+csExecTime/1000+"s");
        File logFile = new File(PROJECT_DIR + "/AOS/lamport-mutex-algo/logs.txt");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fw = new FileWriter(logFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(Main.ownHostName + " entering CS");
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        busyWait(csExecTime);
        mutexService.leaveCS();
        executingCS = false;
        Main.numCSexecuted ++;
    }

}
