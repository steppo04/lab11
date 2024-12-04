package it.unibo.oop.reactivegui03;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This is a first example on how to realize a reactive GUI.
 * This shows an alternative solutions using lambdas
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final Counter1 counter1 = new Counter1();
    private final StopCounter1 stopcounter1 = new StopCounter1();


    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(stop);
        panel.add(down);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */

        new Thread(counter1).start();
        new Thread(stopcounter1).start();
        /*
         * Register a listener that stops it
         */
        stop.addActionListener((e) -> counter1.stopCounting());
        up.addActionListener((e) -> counter1.increment());
        down.addActionListener((e) -> counter1.decrement());
        
    } 
    private void stopCounting() {
        counter1.stopCounting();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }
        });
    
    }
    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Counter1 implements Runnable, Serializable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
         @Serial
        private static final long serialVersionUID = 1L;
        private volatile boolean stop;
        private volatile boolean up;
        private volatile boolean down;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if(this.down) {
                        this.counter--;
                    } else if (this.up) {
                        this.counter++;
                    }
                    
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
        public void increment(){
            this.up=true;
            this.down=false;
        }
        public void decrement(){
            this.up=false;
            this.down=true;
        }
    }


private class StopCounter1 implements Runnable, Serializable {
    /*
     * Stop is volatile to ensure visibility. Look at:
     * 
     * http://archive.is/9PU5N - Sections 17.3 and 17.4
     * 
     * For more details on how to use volatile:
     * 
     * http://archive.is/4lsKW
     * 
     */
     @Serial

    @Override
    public void run() {
        
            try {
                Thread.sleep(10000);
                
            } catch (InterruptedException ex) {
                /*
                 * This is just a stack trace print, in a real program there
                 * should be some logging and decent error reporting
                 */
                ex.printStackTrace();
            }
            AnotherConcurrentGUI.this.stopCounting();
        }
    }
}


