public class QueueEntry implements Comparable<QueueEntry>{
    private int timestamp;
    private int id;

    public QueueEntry(int timestamp, int id){
        this.timestamp = timestamp;
        this.id = id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(QueueEntry other) {
        if(this.getTimestamp() == other.getTimestamp()){
            return this.getId() - other.getId();
        }
        else{
            return this.getTimestamp() - other.getTimestamp();
        }
    }

}