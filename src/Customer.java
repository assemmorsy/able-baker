/**
 * Created by IntelliJ IDEA.
 * User: Javad
 * Date: Mar 16, 2009
 * Time: 4:59:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Customer {
    double queueTime;
    double serviceTime;
    double departureTime;
    static int c=0;
    public int id;
    public double getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(double queueTime) {
        this.queueTime = queueTime;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public Customer() {
        id=c++;
    }

    public Customer(long queueTime) {
        this.queueTime = queueTime;
    }
}
