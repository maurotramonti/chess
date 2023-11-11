package chess;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

@SuppressWarnings("serial")
class Sidebar extends JPanel {
    private final Chess parent;

    protected final Clock whiteClock, blackClock;
    
    protected final JTable moves;

    private int startingMinutes, startingSeconds, increment;

    Sidebar(Chess parent) {
        super(new BorderLayout(0, 15));
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.parent = parent;
        
        final String[] columnNames = {"#", "Bianco", "Nero"};
		moves = new JTable(new DefaultTableModel(null, columnNames));
        
        
        moves.getTableHeader().setOpaque(false);  moves.getTableHeader().setBackground(Color.lightGray); 
        moves.getTableHeader().setForeground(Color.black); moves.setGridColor(Color.black); moves.getTableHeader().setReorderingAllowed(false);
        moves.setOpaque(true); moves.setShowVerticalLines(true); moves.setShowHorizontalLines(true); moves.setEnabled(false); moves.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        moves.getColumnModel().getColumn(0).setPreferredWidth(20); 
        moves.getColumnModel().getColumn(1).setPreferredWidth(75);  
        moves.getColumnModel().getColumn(2).setPreferredWidth(75); 
        moves.setBackground(new Color(204, 204, 204));
        
        final JScrollPane sp = new JScrollPane(moves); sp.setPreferredSize(new Dimension(170, 0)); sp.setMinimumSize(getPreferredSize());
        sp.setOpaque(false); sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createLineBorder(Color.black, 3));
        

        this.startingMinutes = parent.startingMinutes; this.startingSeconds = parent.startingSeconds; this.increment = parent.increment;

        whiteClock = new Clock("white", startingMinutes, startingSeconds, increment); blackClock = new Clock("black", startingMinutes, startingSeconds, increment);
        add(whiteClock, BorderLayout.SOUTH); add(blackClock, BorderLayout.NORTH); add(sp, BorderLayout.CENTER);

    }
    
    public void reinitializeClocks() {
    	whiteClock.setActive(false); blackClock.setActive(false); 
    	whiteClock.setValues(parent.startingMinutes, parent.startingSeconds, parent.increment);
    	blackClock.setValues(parent.startingMinutes, parent.startingSeconds, parent.increment); 
    }

    class Clock extends JPanel {
        private final String color; 
        private boolean active = false;

        private final JPanel self;
        
        private final JLabel timeLabel;
        private int minutes, seconds, increment;

        Clock(String color, int minutes, int seconds, int increment) {
            super(); self = this;
            setBorder(BorderFactory.createLineBorder(Color.black, 3)); 

            this.minutes = minutes; this.seconds = seconds; this.increment = increment; this.color = color;
            
            setBackground(Color.lightGray);
            String extraZero;
            if (seconds <= 9) extraZero = "0";
            else extraZero = "";  
            timeLabel = new JLabel(Integer.toString(this.minutes) + ":" + extraZero + Integer.toString(this.seconds), SwingConstants.CENTER); timeLabel.setForeground(Color.black); timeLabel.setFont(new Font("Sans-Serif", Font.BOLD, 32)); 

            add(timeLabel);

            new ClockThread().start(); 



        }
        
        public void setValues(final int minutes, final int seconds, final int increment) {
        	this.minutes = minutes; this.seconds = seconds; this.increment = increment; 
        	
        	// updates the label, otherwise it would still be displayed the old time until the player makes his move 
        	
        	String extraZero;
            if (seconds <= 9) extraZero = "0";
            else extraZero = "";
            timeLabel.setText(Integer.toString(minutes) + ":" + extraZero + Integer.toString(seconds));
        }

        public void setActive(boolean a) {
        	active = a;
        	if (!a) {
        		setBackground(Color.lightGray);
                seconds+=increment;
                if (seconds >= 60) {
            		minutes++; seconds -= 59;
            	}
                String extraZero;
                if (seconds <= 9) extraZero = "0";
                else extraZero = "";
                timeLabel.setText(Integer.toString(minutes) + ":" + extraZero + Integer.toString(seconds));
                
            } else {
            	setBackground(new Color(255, 204, 102));
            }
  
            
        }
        
      

        class ClockThread extends Thread {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    try {
                        Thread.sleep(5); // per evitare il 50% di cpu solo per sti due thread
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        break;
                    }
                    if (active && parent.cb.isGameEnded == false) {
                    	
                    	if (seconds >= 60) {
                    		minutes++; seconds -= 59;
                    	}
                        
                        if (--seconds < 0) {
                            minutes--; seconds = 59;
                        }
                        
                        
                        
                        String extraZero;
                        if (seconds <= 9) extraZero = "0";
                        else extraZero = "";
                        timeLabel.setText(Integer.toString(minutes) + ":" + extraZero + Integer.toString(seconds));
                        try {
                            Thread.sleep(995);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            break;
                        }
                        if ((seconds + minutes) == 0) {
                            self.setBackground(new Color(255, 102, 102));
                            JOptionPane.showMessageDialog(parent, "Il " + color + "ha esaurito il tempo!", "Attenzione", JOptionPane.WARNING_MESSAGE);
                            parent.cb.isGameEnded = true;
                            break;
                        } 
                    
                    }                    
                }
            }
        }
    	
    }
}