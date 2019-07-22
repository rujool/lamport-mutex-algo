import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {

    private int timestamp;
    private String senderHost;
    private int senderId;
    private int senderPort;
    private String type;

    public Message(int senderId, String senderHost, int senderPort, String type, int timestamp) {
        this.senderId = senderId;
        this.senderHost = senderHost;
        this.senderPort = senderPort;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSenderHost() {
        return senderHost;
    }

    public void setSenderHost(String senderHost) {
        this.senderHost = senderHost;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(int senderPort) {
        this.senderPort = senderPort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTimestamp() {
        return timestamp;
    }

}
