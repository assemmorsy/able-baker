/**
 * Created by IntelliJ IDEA.
 * User: Javad
 * Date: Mar 16, 2009
 * Time: 4:56:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Event implements Comparable{
    //public static final int SERVE=0;
    public static final int DEPART=1;
    public static final int ARRIVE=2;
    private double occuranceTime;
    Customer involvedCustomer=null;
    Server involvedServer=null;
    int type;
    private String title;

    public double getOccuranceTime() {
        return occuranceTime;
    }

    public void setOccuranceTime(double occuranceTime) {
        this.occuranceTime = Math.abs(occuranceTime);
    }

    public int compareTo(Object o) {
        Event ev=(Event)o;
        if(getOccuranceTime()>ev.getOccuranceTime()){
            return 1;
        }
        if(getOccuranceTime()<ev.getOccuranceTime())
            return -1;
        return 0;
    }

    @Override
    public String toString() {
        return getOccuranceTime()+" "+(type==1?"Departure":"Arrival")+" : "+(involvedServer==null?"":involvedServer.getName());
    }
    
}
