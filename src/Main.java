
import org.omg.CORBA.DoubleHolder;

import javax.swing.*;
import java.util.PriorityQueue;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Javad
 * Date: Mar 16, 2009
 * Time: 4:53:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main{
    public static void main(String[] args) throws InterruptedException {
        new Main();
    }

    public Main() throws InterruptedException {
        simulateAblePreferred();
        simulateRandomPreferred();
        double[] rates=new double[100];
        double[] ablePs=new double[100];
        double[] bakerPs=new double[100];
        double rate=0;
        for (int i = 0; i < rates.length; i++) {
            rates[i]=(rate+=0.05);
            DoubleHolder abl=new DoubleHolder(0);
            DoubleHolder bkr=new DoubleHolder(0);
            simulateExponential(rate,abl,bkr);
            ablePs[i]=abl.value;
            bakerPs[i]=bkr.value;
        }
        Graph g=new Graph(rates, ablePs, bakerPs);
    }

    private void simulateAblePreferred() throws InterruptedException {
        double lTotalQueueWaitTime=0;
        double lTotalServerWaitTime=0;

        double lTotalQueueLength=0;
        double lTotalSystemLength=0;

        double lAbleIdle=0;
        double lBakerIdle=0;

        double lAbleStopped=0;
        double lBakerStopped=0;

        int iPeopleAtService=0;
        int iPreviousPeopleAtService=0;

        long lTotalArrived=0;

        Queue<Customer> sysQ=new Queue<Customer>();
        PriorityQueue eventList=new PriorityQueue(3);
        Server able=new Server("Able");
        Server baker=new Server("Baker");
        boolean stopCondition=false;
        double clock=0;
        double lPreviousClock=0;
        Event pe=new Event();
        pe.involvedCustomer = new Customer();
        pe.type = Event.ARRIVE;
        pe.setOccuranceTime(0);
        eventList.add(pe);

        int qLength=0;
        while(!stopCondition){
            Event currentEvent=(Event) eventList.poll();
            if(currentEvent==null){
                //todo: Error  x
            }                            
            lPreviousClock=clock;

            clock=currentEvent.getOccuranceTime();
//            System.out.println(lTotalQueueLength);
//            System.out.println("");

            switch(currentEvent.type){
                case Event.DEPART:
                    if(!sysQ.isEmpty()){
                        long nextDepart=0;
                        lTotalServerWaitTime+=clock-currentEvent.involvedCustomer.serviceTime;
                        if(currentEvent.involvedServer==able){
                            nextDepart=calculateNextAbleDeparureTime();
                        }else{
                            nextDepart=calculateNextBakerDeparureTime();
                        }
                        Event e=new Event();
                        e.involvedServer = currentEvent.involvedServer;
                        e.involvedCustomer=(Customer)sysQ.dequeue();
                        e.involvedServer.atService=e.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.involvedServer.atService.serviceTime=clock;
                        e.type = Event.DEPART;
                        e.setOccuranceTime(clock+nextDepart);
                        lTotalQueueWaitTime+=clock-e.involvedCustomer.getQueueTime();
                        lTotalQueueLength+=(clock-lPreviousClock)*qLength;
                        lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
                        qLength--;
                        eventList.add(e);
                    }else{
                        currentEvent.involvedServer.atService= null;
                        if(currentEvent.involvedServer==able){
                            lAbleStopped=clock;
                        }else{
                            lBakerStopped=clock;
                        }
                        iPreviousPeopleAtService=iPeopleAtService;
                        iPeopleAtService--;
                    }
                    //Update criteria
                    break;
                case Event.ARRIVE:
                    lTotalArrived++;
                    long nextArrival=calculateNextArrivaTime();
                    Event e1=new Event();
                    e1.involvedCustomer = new Customer();
                    e1.type=Event.ARRIVE;
                    e1.setOccuranceTime(clock+nextArrival);
                    eventList.add(e1);
                    Server idle=null;
                    long nextDepart=0;
                    if(able.isIdle()){
                        lAbleIdle+=clock-lAbleStopped;
                        idle=able;
                        nextDepart = calculateNextAbleDeparureTime();
                    }else if(baker.isIdle()){
                        lBakerIdle+=clock-lBakerStopped;
                        nextDepart = calculateNextBakerDeparureTime();
                        idle=baker;
                    }else{
                        currentEvent.involvedCustomer.queueTime=clock;
                        sysQ.enqueue(currentEvent.involvedCustomer);
                        lTotalQueueLength+=(clock-lPreviousClock)*qLength;
                        lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
                        qLength++;
                    }
                    if(idle!=null){
                        currentEvent.involvedCustomer.serviceTime=clock;
                        idle.atService = currentEvent.involvedCustomer;
                        Event e=new Event();
                        e.involvedServer = idle;
                        e.involvedCustomer = currentEvent.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.type=Event.DEPART;
                        iPreviousPeopleAtService=iPeopleAtService;
                        iPeopleAtService++;
                        e.setOccuranceTime(clock+nextDepart);
                        eventList.add(e);
                    }
                    //Update criteria
                    break;
            }
            //check stop condition
            stopCondition=clock>500000;
        }
        lTotalQueueLength+=(clock-lPreviousClock)*qLength;
        lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
        if(able.isIdle()){
            lAbleIdle+=clock-lAbleStopped;
        }
        if(baker.isIdle()){
            lBakerIdle+=clock-lBakerStopped;
        }
        for (int i = 0; i < sysQ.size(); i++) {
            Customer c=(Customer) sysQ.elementAt(i);
            lTotalQueueWaitTime+=clock-c.queueTime;
        }
        double dAblePerformance=1.0-(double)lAbleIdle/clock;
        double dBakerPerformance=1.0-(double)lBakerIdle/clock;
        double Wq=(double) lTotalQueueWaitTime/lTotalArrived;
        double Ws=(double) (lTotalServerWaitTime+lTotalQueueWaitTime)/lTotalArrived;
        double Lq=((double)lTotalQueueLength)/clock;
        double Ls=(double) lTotalSystemLength/clock;

        System.out.println("Simulation done (Able Preffered): ");
        System.out.println("Total Customers Arrived: "+lTotalArrived);
        System.out.println("Lq = "+Lq+" people.");
        System.out.println("Ls = "+Ls+" people.");
        System.out.println("Wq = "+Wq+" minutes.");
        System.out.println("Ws = "+Ws+" minutes.");
        System.out.println("Pa = "+dAblePerformance+".");
        System.out.println("Pb = "+dBakerPerformance+".");
    }

    private void simulateRandomPreferred() throws InterruptedException {
        double lTotalQueueWaitTime=0;
        double lTotalServerWaitTime=0;

        double lTotalQueueLength=0;
        double lTotalSystemLength=0;

        double lAbleIdle=0;
        double lBakerIdle=0;

        double lAbleStopped=0;
        double lBakerStopped=0;

        int iPeopleAtService=0;
        int iPreviousPeopleAtService=0;

        long lTotalArrived=0;

        Queue<Customer> sysQ=new Queue<Customer>();
        PriorityQueue eventList=new PriorityQueue(3);
        Server able=new Server("Able");
        Server baker=new Server("Baker");
        boolean stopCondition=false;
        double clock=0;
        double lPreviousClock=0;
        Event pe=new Event();
        pe.involvedCustomer = new Customer();
        pe.type = Event.ARRIVE;
        pe.setOccuranceTime(0);
        eventList.add(pe);

        int qLength=0;
        while(!stopCondition){
            Event currentEvent=(Event) eventList.poll();
            if(currentEvent==null){
                //todo: Error  x
            }
            lPreviousClock=clock;

            clock=currentEvent.getOccuranceTime();
//            System.out.println(lTotalQueueLength);
//            System.out.println("");

            switch(currentEvent.type){
                case Event.DEPART:
                    if(!sysQ.isEmpty()){
                        long nextDepart=0;
                        lTotalServerWaitTime+=clock-currentEvent.involvedCustomer.serviceTime;
                        if(currentEvent.involvedServer==able){
                            nextDepart=calculateNextAbleDeparureTime();
                        }else{
                            nextDepart=calculateNextBakerDeparureTime();
                        }
                        Event e=new Event();
                        e.involvedServer = currentEvent.involvedServer;
                        e.involvedCustomer=(Customer)sysQ.dequeue();
                        e.involvedServer.atService=e.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.involvedServer.atService.serviceTime=clock;
                        e.type = Event.DEPART;
                        e.setOccuranceTime(clock+nextDepart);
                        lTotalQueueWaitTime+=clock-e.involvedCustomer.getQueueTime();
                        lTotalQueueLength+=(clock-lPreviousClock)*qLength;
                        lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
                        qLength--;
                        eventList.add(e);
                    }else{
                        currentEvent.involvedServer.atService= null;
                        if(currentEvent.involvedServer==able){
                            lAbleStopped=clock;
                        }else{
                            lBakerStopped=clock;
                        }
                        iPreviousPeopleAtService=iPeopleAtService;
                        iPeopleAtService--;
                    }
                    //Update criteria
                    break;
                case Event.ARRIVE:
                    lTotalArrived++;
                    long nextArrival=calculateNextArrivaTime();
                    Event e1=new Event();
                    e1.involvedCustomer = new Customer();
                    e1.type=Event.ARRIVE;
                    e1.setOccuranceTime(clock+nextArrival);
                    eventList.add(e1);
                    Server idle=null;
                    long nextDepart=0;
                    if(able.isIdle() && baker.isIdle()){
                        if(Math.random()<0.5){
                            lAbleIdle+=clock-lAbleStopped;
                            idle=able;
                            nextDepart = calculateNextAbleDeparureTime();
                        }else{
                            lBakerIdle+=clock-lBakerStopped;
                            nextDepart = calculateNextBakerDeparureTime();
                            idle=baker;
                        }
                    }else{
                        if(able.isIdle()){
                            lAbleIdle+=clock-lAbleStopped;
                            idle=able;
                            nextDepart = calculateNextAbleDeparureTime();
                        }
                        if(baker.isIdle()){
                            lBakerIdle+=clock-lBakerStopped;
                            nextDepart = calculateNextBakerDeparureTime();
                            idle=baker;
                        }else{
                            currentEvent.involvedCustomer.queueTime=clock;
                            sysQ.enqueue(currentEvent.involvedCustomer);
                            lTotalQueueLength+=(clock-lPreviousClock)*qLength;
                            lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
                            qLength++;
                        }
                    }
                    if(idle!=null){
                        currentEvent.involvedCustomer.serviceTime=clock;
                        idle.atService = currentEvent.involvedCustomer;
                        Event e=new Event();
                        e.involvedServer = idle;
                        e.involvedCustomer = currentEvent.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.type=Event.DEPART;
                        iPreviousPeopleAtService=iPeopleAtService;
                        iPeopleAtService++;
                        e.setOccuranceTime(clock+nextDepart);
                        eventList.add(e);
                    }
                    //Update criteria
                    break;
            }
            //check stop condition
            stopCondition=clock>500000;
        }
        lTotalQueueLength+=(clock-lPreviousClock)*qLength;
        lTotalSystemLength+=(clock-lPreviousClock)*qLength+iPreviousPeopleAtService*(clock-lPreviousClock);
        if(able.isIdle()){
            lAbleIdle+=clock-lAbleStopped;
        }
        if(baker.isIdle()){
            lBakerIdle+=clock-lBakerStopped;
        }
        for (int i = 0; i < sysQ.size(); i++) {
            Customer c=(Customer) sysQ.elementAt(i);
            lTotalQueueWaitTime+=clock-c.queueTime;
        }
        double dAblePerformance=1.0-(double)lAbleIdle/clock;
        double dBakerPerformance=1.0-(double)lBakerIdle/clock;
        double Wq=(double) lTotalQueueWaitTime/lTotalArrived;
        double Ws=(double) (lTotalServerWaitTime+lTotalQueueWaitTime)/lTotalArrived;
        double Lq=((double)lTotalQueueLength)/clock;
        double Ls=(double) lTotalSystemLength/clock;

        System.out.println("Simulation done (Random Prefferred): ");
        System.out.println("Total Customers Arrived: "+lTotalArrived);
        System.out.println("Lq = "+Lq+" people.");
        System.out.println("Ls = "+Ls+" people.");
        System.out.println("Wq = "+Wq+" minutes.");
        System.out.println("Ws = "+Ws+" minutes.");
        System.out.println("Pa = "+dAblePerformance+".");
        System.out.println("Pb = "+dBakerPerformance+".");
    }

    private void  simulateExponential(double Lambda,DoubleHolder ableP, DoubleHolder bakerP) throws InterruptedException {
        double lAbleIdle=0;
        double lBakerIdle=0;

        double lAbleStopped=0;
        double lBakerStopped=0;

        Queue<Customer> sysQ=new Queue<Customer>();
        PriorityQueue eventList=new PriorityQueue(3);
        Server able=new Server("Able");
        Server baker=new Server("Baker");
        boolean stopCondition=false;
        double clock=0;
        double lPreviousClock=0;
        Event pe=new Event();
        pe.involvedCustomer = new Customer();
        pe.type = Event.ARRIVE;
        pe.setOccuranceTime(0);
        eventList.add(pe);

        int qLength=0;
        while(!stopCondition){
            Event currentEvent=(Event) eventList.poll();
            if(currentEvent==null){
                //todo: Error  x
            }
            lPreviousClock=clock;

            clock=currentEvent.getOccuranceTime();
//            System.out.println(lTotalQueueLength);
//            System.out.println("");

            switch(currentEvent.type){
                case Event.DEPART:
                    if(!sysQ.isEmpty()){
                        long nextDepart=0;

                        if(currentEvent.involvedServer==able){
                            nextDepart=calculateNextAbleDeparureTime();
                        }else{
                            nextDepart=calculateNextBakerDeparureTime();
                        }
                        Event e=new Event();
                        e.involvedServer = currentEvent.involvedServer;
                        e.involvedCustomer=(Customer)sysQ.dequeue();
                        e.involvedServer.atService=e.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.involvedServer.atService.serviceTime=clock;
                        e.type = Event.DEPART;
                        e.setOccuranceTime(clock+nextDepart);
                        qLength--;
                        eventList.add(e);
                    }else{
                        currentEvent.involvedServer.atService= null;
                        if(currentEvent.involvedServer==able){
                            lAbleStopped=clock;
                        }else{
                            lBakerStopped=clock;
                        }

                    }
                    //Update criteria
                    break;
                case Event.ARRIVE:
                    double nextArrival=calculateNextArrivaTime(Lambda);
                    Event e1=new Event();
                    e1.involvedCustomer = new Customer();
                    e1.type=Event.ARRIVE;
                    e1.setOccuranceTime(clock+nextArrival);
                    eventList.add(e1);
                    Server idle=null;
                    long nextDepart=0;
                    if(able.isIdle()){
                        lAbleIdle+=clock-lAbleStopped;
                        idle=able;
                        nextDepart = calculateNextAbleDeparureTime();
                    }else if(baker.isIdle()){
                        lBakerIdle+=clock-lBakerStopped;
                        nextDepart = calculateNextBakerDeparureTime();
                        idle=baker;
                    }else{
                        currentEvent.involvedCustomer.queueTime=clock;
                        sysQ.enqueue(currentEvent.involvedCustomer);
                        qLength++;
                    }
                    if(idle!=null){
                        currentEvent.involvedCustomer.serviceTime=clock;
                        idle.atService = currentEvent.involvedCustomer;
                        Event e=new Event();
                        e.involvedServer = idle;
                        e.involvedCustomer = currentEvent.involvedCustomer;
                        e.involvedCustomer.departureTime=clock+nextDepart;
                        e.type=Event.DEPART;
                        e.setOccuranceTime(clock+nextDepart);
                        eventList.add(e);
                    }
                    //Update criteria
                    break;
            }
            //check stop condition
            stopCondition=clock>1000;
        }
        if(able.isIdle()){
            lAbleIdle+=clock-lAbleStopped;
        }
        if(baker.isIdle()){
            lBakerIdle+=clock-lBakerStopped;
        }
        double dAblePerformance=1.0-(double)lAbleIdle/clock;
        double dBakerPerformance=1.0-(double)lBakerIdle/clock;

        ableP.value = dAblePerformance;
        bakerP.value = dBakerPerformance;
    }

    private double calculateNextArrivaTime(double lambda) {
        //long r=(long) ((-1.0/lambda)*Math.log((1.0-Math.random())/lambda));
        //System.out.println(r+1);
        double s=(-1.0/lambda*Math.log(1-Math.random()));
        //System.out.println(s);
        return s;
    }

    /*
    private void draw(Queue<Customer> q, Server able, Server baker, long clock,Event e) {
        int qMemberSize=40;
        Graphics g=getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.green);
        int index=0;
        g.setFont(new Font("Tahoma",Font.BOLD,20));
        int x,y;
        for(int i=q.size()-1; i>=0 ; i--){
            x=getWidth()-300-i*qMemberSize;
            y=180;
            g.setColor(Color.GREEN);
            g.fill3DRect(x,y,qMemberSize,qMemberSize,true);
            g.setColor(Color.RED);
            g.drawRect(x,y,qMemberSize,qMemberSize);
            g.drawString(Integer.toString(q.elementAt(i).id), x+10,y+20);
        }
        g.setColor(Color.BLUE);
        x=getWidth()-200;
        y=120;
        if(!able.isIdle()){
            g.fillRect(x,y,40,40);
            g.setColor(Color.cyan);
            g.drawString(able.atService.id+"",x+10,y+20);
        }
        g.drawRect(x,y,40,40);
        g.drawString("Able",x+50,y+30);
        y=240;
        g.setColor(Color.BLUE);
        if(!baker.isIdle()){
            g.fillRect(x,y,40,40);
            g.setColor(Color.cyan);
            g.drawString(baker.atService.id+"",x+10,y+20);
        }
        g.drawString("Baker",x+50,y+30);
        g.drawRect(x,y,40,40);
        g.setColor(Color.red);
        g.drawString("Time: "+clock,100,500);
        g.drawString("Event: "+e,400,500);
    }
    */
    private long calculateNextArrivaTime() {
        int r=(int) (Math.random()*100);
        if(r>0&&r<26){
            return 1;
        }
        if(r>25 && r<66){
            return 2;
        }
        if(r>65 && r<86){
            return 3;
        }
        return 4;
    }

    private long calculateNextAbleDeparureTime() {
        int r=(int) (Math.random()*100);
        if(r>0&&r<31){
            return 2;
        }
        if(r>30 && r<59){
            return 3;
        }
        if(r>58 && r<84){
            return 4;
        }

        return 5;
    }
    private long calculateNextBakerDeparureTime() {
        int r=(int) (Math.random()*100);
        if(r>0&&r<36){
            return 3;
        }
        if(r>35 && r<61){
            return 4;
        }
        if(r>60 && r<81){
            return 5;
        }
        return 6;
    }
}
