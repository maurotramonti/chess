package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.IOException;

@SuppressWarnings("serial")
final class WaitForPlayerDialog extends JDialog implements WindowListener {
	public boolean closingByXButton = true;
	
	private final Chess parent;
	
    WaitForPlayerDialog(final Chess parent) {
        super(parent, "Attendi...", true);
        setResizable(false); this.parent = parent;
        setLayout(new FlowLayout()); setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); addWindowListener(this);
        
        final JPanel cp = (JPanel) getContentPane(); cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JLabel("Sono in attesa della connessione dell'avversario..."));

        pack(); 
    }
    
    public void windowClosing(WindowEvent e) {
        if (closingByXButton == false) return;
        parent.server.abortedByUser = true;
        this.dispose();
    }

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
}

@SuppressWarnings("serial")
class HostGameDialog extends JDialog {
    private final Chess parent; private final JDialog dialog;

    private final JSpinner portSpinner, minutesSpinner, secondsSpinner, incrementSpinner;


    HostGameDialog(Chess parent) {
        super(parent, "Ospita partita", true);
        this.dialog = this; this.parent = parent;

       
        setLayout(new GridBagLayout()); final GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(6, 6, 6, 6); gbc.anchor = GridBagConstraints.LINE_START; gbc.fill = GridBagConstraints.HORIZONTAL;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Porta: "), gbc); gbc.gridx++; 
        portSpinner = new JSpinner(new SpinnerNumberModel(7777, 1001, 32000, 1)); minutesSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 59, 1)); 
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1)); incrementSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));  

        add(portSpinner, gbc); gbc.gridx = 0; gbc.gridy++; add(new JLabel("Minuti: "), gbc); gbc.gridx++;
        add(minutesSpinner, gbc); gbc.gridy++; gbc.gridx = 0; add(new JLabel("Secondi: "), gbc); gbc.gridx++; add(secondsSpinner, gbc); gbc.gridy++; gbc.gridx = 0; 
        add(new JLabel("Incremento: "), gbc); gbc.gridx++; add(incrementSpinner, gbc); gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; add(new JSeparator(), gbc); gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        
        add(new SubmitButton(), gbc);

        pack();
        
        setMinimumSize(getPreferredSize());
        setVisible(true);
    }

    class SubmitButton extends JButton implements ActionListener {
        SubmitButton() {
            super("Apri");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        	parent.cb.isGameEnded = true;
        	
        	final int portNumber = (int) portSpinner.getValue();
        	final int seconds = (int) secondsSpinner.getValue();
        	final int minutes = (int) minutesSpinner.getValue();
        	final int increment = (int) incrementSpinner.getValue();
        	
            try {
            	parent.thinClientMode = true;
            	parent.cb.isGameEnded = false;
            	
            	parent.server = new Server(portNumber, parent, minutes, seconds, increment); 
                parent.server.start();
            	
                parent.socket = new Socket("localhost", portNumber);
                
            } catch (IOException ex) {
            	System.err.println("[ERROR]: unable to start and/or connect to the server (" + ex.getMessage() + ")");
            	JOptionPane.showMessageDialog(parent, "Si è verificato un errore!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                parent.cb.isGameEnded = true;
            	dialog.dispose();
            	return;
            }
            parent.titleSuffixes = new String("[Partita ospitata - Porta " + portNumber + "]");
            parent.remove(parent.cb); parent.revalidate(); 
            parent.cb = new ThinClientChessboard(parent, parent.socket);
            parent.add(parent.cb, BorderLayout.CENTER); parent.revalidate(); parent.repaint(); 
            
            parent.startingSeconds = seconds; parent.startingMinutes = minutes; parent.increment = increment; parent.port = portNumber; 
            parent.ip = new String("127.0.0.1");
            
            parent.sb.reinitializeClocks();
            
            
            dialog.dispose();
        }
    }
}

@SuppressWarnings("serial")
final class ConnectToServerDialog extends JDialog {
	private final Chess parent;
	
	private final JDialog dialog;
	
	private final String[] choices = {"Nome host", "Indirizzo IP"};
	
	private final JComboBox<String> ipOrHostname = new JComboBox<>(choices);
	
	private final JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(7777, 1001, 32000, 1));
	private final JTextField ipOrHostnameField = new JTextField("localhost");
	
	ConnectToServerDialog(final Chess parent) {
		super(parent, "Connetti a partita remota...", true);
		this.parent = parent; this.dialog = this; setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setLayout(new GridBagLayout()); final GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
		add(ipOrHostname, gbc); gbc.gridx++; add(ipOrHostnameField, gbc); gbc.gridy++; gbc.gridx = 0;
		add(new JLabel("Porta: "), gbc); gbc.gridx++; add(portSpinner, gbc); gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
		add(new JSeparator(), gbc); gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		add(new ConnectButton(), gbc);
		
		
		pack(); setMinimumSize(getPreferredSize());
		setVisible(true);
	}
	
	private final class ConnectButton extends JButton implements ActionListener {
		ConnectButton() {
			super("Connetti");
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {

            parent.thinClientMode = true; 
            
            if (((String) ipOrHostname.getSelectedItem()).equals("Indirizzo IP")) {
                String ip = ipOrHostnameField.getText();
                String[] parts = ip.split("\\."); 
                boolean everythingOK = true;
                if (parts.length == 4) {
                    for (String part : parts) {
                        if (Integer.parseInt(part) > 255 || Integer.parseInt(part) < 0) {
                            everythingOK = false;
                            break;
                        }
                    } 
                } else everythingOK = false;

                if (everythingOK == false) {
                    JOptionPane.showMessageDialog(dialog, "L'indirizzo IP inserito non è valido!", "Errore", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                parent.ip = new String(ip);
            } else {
                try {
                    InetAddress addr = InetAddress.getByName(ipOrHostnameField.getText());
                    parent.ip = new String(addr.getHostAddress());
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Non è stato possibile risolvere il nome host o esso non è valido!", "Errore", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
            }
            
            parent.port = (Integer) portSpinner.getValue();
            try {
                parent.socket = new Socket(parent.ip, (Integer) portSpinner.getValue());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Non è stato possibile connettersi!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            } 
         
            parent.titleSuffixes = new String("[Partita remota - IP:  " + parent.ip + " - Porta: " + Integer.toString(parent.port) + "]");
            parent.remove(parent.cb); parent.revalidate(); 
            parent.cb = new ThinClientChessboard(parent, parent.socket);
            parent.add(parent.cb, BorderLayout.CENTER); parent.revalidate(); parent.repaint(); 
			
			
			dialog.dispose();
		}
	}
}