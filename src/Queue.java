import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Javad
 * Date: Mar 16, 2009
 * Time: 11:41:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Queue<T> {
    private LinkedList<T> ll;

    public Queue() {
        ll=new LinkedList<T>();
    }

    public int size(){
        return ll.size();
    }
    public T elementAt(int index){
        return ll.get(index);
    }
    public void enqueue(T val){
        ll.addLast(val);
    }
    public T dequeue(){
        return ll.removeFirst();
    }

    public boolean isEmpty() {
        return ll.size()==0;
    }
}
