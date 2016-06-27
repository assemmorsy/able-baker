import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Javad
 * Date: Apr 17, 2009
 * Time: 3:08:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class Graph extends JFrame {
    private double[] rates;
    private double[] ablePs;
    private double[] bakerPs;
    int leftDist=40,botDist=60;
    private double maxY=1.4;

    public Graph(double[] rates, double[] ablePs, double[] bakerPs) throws HeadlessException {
        this.rates = rates;
        this.ablePs = ablePs;
        this.bakerPs = bakerPs;
        setSize(300,300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
    }
    
    @Override
    public void repaint(long time, int x, int y, int width, int height) {
        super.repaint(time, x, y, width, height);    //To change body of overridden methods use File | Settings | File Templates.
        Graphics g=getGraphics();
        g.setColor(Color.black);
        g.fillRect(x,y,width, height);
        for (int i = 1; i < rates.length; i++) {
            int rateX = rateToX(rates[i]);
            if( rateX>x &&  rateX-x<width){
                int ableY=perToY(ablePs[i]);
                if(ableY>y && ableY-y>height){
                    g.setColor(Color.GREEN);
                    g.drawLine(rateToX(rates[i-1]),perToY(ablePs[i-1]),rateX,ableY);
                }
                int bakerY=perToY(bakerPs[i]);
                if(bakerY>y && bakerY-y>height){
                    g.setColor(Color.RED);
                    g.drawLine(rateToX(rates[i-1]),perToY(bakerPs[i-1]),rateX,bakerY);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.black);
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.drawLine(leftDist,0,leftDist,getHeight()-botDist);
        g.drawLine(leftDist,getHeight()-botDist,getWidth(),getHeight()-botDist);
        for (int i = 1; i < rates.length; i++) {
            int rateX = rateToX(rates[i]);
            int ableY=perToY(ablePs[i]);
            if(i%10==0){
                g.setColor(Color.cyan);
                double v=((int) (rates[i]*100))/100.0;
                g.drawString(Double.toString(v),rateX,getHeight()-botDist+15);
            }
            g.setColor(Color.GREEN);
            g.drawLine(rateToX(rates[i-1]),perToY(ablePs[i-1]),rateX,ableY);
            int bakerY=perToY(bakerPs[i]);
            g.setColor(Color.RED);
            g.drawLine(rateToX(rates[i-1]),perToY(bakerPs[i-1]),rateX,bakerY);
        }
        g.setColor(Color.cyan);
        for (double i = .1 ; i <=1; i+=.1) {
            String x=Double.toString(i);
            if(x.length()>4){
                x=x.substring(0,4);
            }
            if(x.length()==3){
                x+="0";
            }
            g.drawString(x,leftDist-35,perToY(i));
        }
        g.setColor(Color.YELLOW);
        g.drawString("The result for the simulation with parameters in the book " +
                "are printed in the command line!",50,50);
        g.drawString("Here is the graph of Performance vs. Lambda",50,70);
        g.setColor(Color.green);
        g.setFont(new Font("tahoma",Font.BOLD,20));
        g.drawString("Performance for Able",getWidth()-250,botDist);
        g.setColor(Color.RED);
        g.drawString("Performance for Baker",getWidth()-250,botDist+50);
    }


    private int perToY(double rawY) {
        double ratio=(getHeight()-botDist)/(maxY);

        return getHeight()-botDist-(int)(ratio*rawY);

    }

    private int rateToX(double rate){
        double ratio=(getWidth()-leftDist)/(rates[rates.length-1]);
        return leftDist+(int)(ratio*rate);
    }
}
