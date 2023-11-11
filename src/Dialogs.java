package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.*;

@SuppressWarnings("serial")
final class StartGameDialog extends JDialog implements WindowListener {
    private final Chess parent;
    private final ButtonGroup group = new ButtonGroup();

    private final JDialog dialog;
    private final JSpinner minutes, seconds, increment, minutes2, seconds2, increment2, port, port2;
    
    private final EnDisWidgets edw;
    
    private final String[] choices = {"Nome host", "Indirizzo IP"};
    private final JComboBox<String> ipOrHostname = new JComboBox<String>(choices);

    private final JTextField ipOrHostnameField = new JTextField("localhost");

    private final JRadioButton localGame, hostGame, connectToGame;

    private boolean closingByXButton = true;

    StartGameDialog(Chess parent) {
        super(parent, "Nuova partita", true); this.parent = parent; this.dialog = this;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);


        minutes = new JSpinner(new SpinnerNumberModel(15, 0, 59, 1)); seconds = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        increment = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));

        minutes2 = new JSpinner(new SpinnerNumberModel(15, 0, 59, 1)); seconds2 = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        increment2 = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));

        port = new JSpinner(new SpinnerNumberModel(7777, 1001, 32000, 1)); port2 = new JSpinner(new SpinnerNumberModel(7777, 1001, 32000, 1));
    

        localGame = new JRadioButton("Partita locale", true); hostGame = new JRadioButton("Ospita partita"); connectToGame = new JRadioButton("Connetti a partita online");
        group.add(localGame); group.add(hostGame); group.add(connectToGame);

        addWindowListener(this);
        
        setLayout(new BorderLayout());
        final JPanel contents = new JPanel(new GridBagLayout()); 
        contents.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 8; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 5, 10, 5);
        contents.add(localGame, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        contents.add(new JLabel("Minuti: "), gbc); gbc.gridx++; contents.add(minutes, gbc);
        
        gbc.gridx++;;

        contents.add(new JLabel("Secondi: "), gbc); gbc.gridx++; gbc.gridwidth = 1; contents.add(seconds, gbc); gbc.gridx++;
        contents.add(new JLabel("Incremento: "), gbc); gbc.gridx++; contents.add(increment, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 8; contents.add(hostGame, gbc); gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; contents.add(new JLabel("Porta: "), gbc); gbc.gridx++; contents.add(port, gbc);
        gbc.gridx++; contents.add(new JLabel("Minuti: "), gbc); gbc.gridx++; contents.add(minutes2, gbc);
        
        gbc.gridx++;;

        contents.add(new JLabel("Secondi: "), gbc); gbc.gridx++; gbc.gridwidth = 1; contents.add(seconds2, gbc); gbc.gridx++;
        contents.add(new JLabel("Incremento: "), gbc); gbc.gridx++; contents.add(increment2, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 8; contents.add(connectToGame, gbc); gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2; contents.add(ipOrHostname, gbc); gbc.gridx+=2; gbc.gridwidth = 3; contents.add(ipOrHostnameField, gbc); gbc.gridx+=3;
        gbc.gridwidth = 1; contents.add(new JLabel("Porta: "), gbc); gbc.gridx++; gbc.gridwidth = 2; contents.add(port2, gbc); 
        

        final JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(new StartGameButton()); 

        add(contents, BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
        edw = new EnDisWidgets(); edw.start();

        pack(); setMinimumSize(getPreferredSize());
        setVisible(true);

    }

    @Override

    public void windowClosing(WindowEvent e) {
        if (closingByXButton == false) return;
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}

    private final class EnDisWidgets extends Thread {
        @Override
        public void run() {
            while (this.isInterrupted() == false) {
                if (localGame.isSelected()) {
                    port.setEnabled(false); port2.setEnabled(false);
                    minutes2.setEnabled(false); seconds2.setEnabled(false); increment2.setEnabled(false);
                    minutes.setEnabled(true); seconds.setEnabled(true); increment.setEnabled(true);
                    ipOrHostnameField.setEnabled(false); ipOrHostname.setEnabled(false);
                } else if (hostGame.isSelected()) {
                    port.setEnabled(true); port2.setEnabled(false);
                    minutes2.setEnabled(true); seconds2.setEnabled(true); increment2.setEnabled(true);
                    minutes.setEnabled(false); seconds.setEnabled(false); increment.setEnabled(false);
                    ipOrHostnameField.setEnabled(false); ipOrHostname.setEnabled(false);
                } else if (connectToGame.isSelected()) {
                    port.setEnabled(false); port2.setEnabled(true);
                    minutes2.setEnabled(false); seconds2.setEnabled(false); increment2.setEnabled(false);
                    minutes.setEnabled(false); seconds.setEnabled(false); increment.setEnabled(false);
                    ipOrHostnameField.setEnabled(true); ipOrHostname.setEnabled(true);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    } 


    private final class StartGameButton extends JButton implements ActionListener {
        StartGameButton() {
            super("Avvia");
            setOpaque(false);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (localGame.isSelected()) {
                parent.startingMinutes = (int) minutes.getValue();
                parent.startingSeconds = (int) seconds.getValue();
                parent.increment = (int) increment.getValue();
                closingByXButton = false;

                edw.interrupt();
                dialog.dispose();
                return;
            } else if (hostGame.isSelected()) {

                parent.thinClientMode = true; parent.ip = new String("127.0.0.1"); parent.port = (Integer) port.getValue();
                try {
                    parent.server = new Server(parent.port, parent, (int) minutes2.getValue(), (int) seconds2.getValue(), (int) increment2.getValue()); parent.server.start();
                    parent.socket = new Socket("localhost", (Integer) port.getValue());
                } catch (Exception ex) {
                    System.err.println("[ERROR]: unable to start and/or connect to the server (" + ex.getMessage() + ")");
                    JOptionPane.showMessageDialog(parent, "Si è verificato un errore nella connessione al server!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    parent.thinClientMode = false;
                    return;
                } 
                closingByXButton = false;
                parent.titleSuffixes = new String("[Partita ospitata - Porta " + Integer.toString(parent.port) + "]");

                edw.interrupt();
                dialog.dispose();
            } else if (connectToGame.isSelected()) {
                parent.startingMinutes = (Integer) minutes2.getValue();
                parent.startingSeconds = (Integer) seconds2.getValue();
                parent.increment = (Integer) increment2.getValue(); // poi vanno cambiate e prese dal server

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
                
                parent.port = (Integer) port.getValue();
                try {
                    parent.socket = new Socket(parent.ip, (Integer) port.getValue());
                } catch (IOException ex) {
                    System.err.println("[ERROR]: unable to connect to remote server (" + ex.getMessage() + ")");
                    JOptionPane.showMessageDialog(dialog, "Non è stato possibile connettersi!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    parent.thinClientMode = false;
                    return;
                } 
                closingByXButton = false;
                parent.titleSuffixes = new String("[Partita remota - IP:  " + parent.ip + " - Porta: " + Integer.toString(parent.port) + "]");
                
                edw.interrupt();
                dialog.dispose();
            } 
        }
    }
}



@SuppressWarnings("serial")
final class PromotePawnDialog extends JDialog implements ActionListener {
    private final JDialog dialog;
    private final Chessboard chessboard;

    private Chessboard.Piece pawn;

    PromotePawnDialog(Chess parent, Chessboard cb, Chessboard.Piece p) {
        super(parent, "Promuovi pedone", true);
        dialog = this; this.chessboard = cb; this.pawn = p;
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        try {
            final JButton queen = new JButton(); queen.setActionCommand("queen"); queen.addActionListener(this);
            queen.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/images/" + chessboard.currentPlayer + "-queen.png"))));

            final JButton rook = new JButton(); rook.setActionCommand("rook"); rook.addActionListener(this);
            rook.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/images/" + chessboard.currentPlayer + "-rook.png"))));

            final JButton pony = new JButton(); pony.setActionCommand("pony"); pony.addActionListener(this);
            pony.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/images/" + chessboard.currentPlayer + "-pony.png"))));

            final JButton bishop = new JButton(); bishop.setActionCommand("bishop"); bishop.addActionListener(this);
            bishop.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/images/" + chessboard.currentPlayer + "-bishop.png"))));

            add(queen); add(pony); add(bishop); add(rook);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        pack();
        setVisible(true);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	pawn.setType(e.getActionCommand());           
        dialog.dispose();
    }

}

@SuppressWarnings("serial")
final class AboutDialog extends JDialog {
    AboutDialog(Chess parent) {
        super(parent, "Informazioni", true);
        setLayout(new GridBagLayout()); setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        final JPanel cp = (JPanel) getContentPane(); cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        final GridBagConstraints gbc = new GridBagConstraints();

        try {
            final JLabel icon = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/images/icon.png"))));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.gridheight = 7; gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

            add(icon, gbc);

            gbc.gridx = 3; gbc.gridheight = 1; 
            add(new JLabel("Scacchi - versione 1.0 (build 111123)"), gbc);
            gbc.gridy++;
            add(new JLabel("Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")"), gbc);
            gbc.gridy++;
            add(new JLabel("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")"), gbc);
            gbc.gridy++;
            add(new JSeparator(), gbc); gbc.gridy++;
            add(new JLabel("Autore: Mauro Tramonti"), gbc); gbc.gridy++;
            add(new JLabel("mtramonti2007@gmail.com"), gbc); gbc.gridy++;
            add(new JLabel("<html>Tema utilizzato: \"<i>FlatLafLight</i>\" (<a href=\'https://www.formdev.com/flatlaf/'>sito</a>)</html>"), gbc);

        } catch (IOException ex) {
            ex.printStackTrace();
        }    
        
        

        pack();
        setVisible(true);
    }
}

@SuppressWarnings("serial")
final class EditChessboardDialog extends JDialog implements ActionListener {
    private final JLabel[] colorOutput = new JLabel[2];
    
    private final JButton[] openColorChooser = new JButton[2];
    
    private final Chess parent; 
    private final JDialog dialog;
    
 

    EditChessboardDialog(Chess parent) {
        super(parent, "Personalizza scacchiera", true); this.parent = parent; dialog = this; 
        setLayout(new GridBagLayout()); final JPanel cp = (JPanel) getContentPane(); cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));	
        
        try {
            setIconImage(ImageIO.read(getClass().getResource("/images/paint-icon.png")));
        } catch (Exception ex) {
            System.err.println("[ERROR]: unable to load the logo image!");
        }
        
        final GridBagConstraints gbc = new GridBagConstraints();
        
        for (int i = 0; i < 2; i++) {
        	colorOutput[i] = new JLabel(""); colorOutput[i].setBackground(parent.cb.boxes[i].getBackground());
        	colorOutput[i].setOpaque(true); colorOutput[i].setBorder(BorderFactory.createLineBorder(Color.black, 1));
        }
        
        for (int i = 0; i < 2; i++) {
        	openColorChooser[i] = new JButton("Scegli");
        	openColorChooser[i].addActionListener(this); openColorChooser[i].setActionCommand(Integer.toString(i));        	
        }

        final JButton submit = new JButton("Conferma"); submit.addActionListener(this); submit.setActionCommand("submit");
        
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START; gbc.insets = new Insets(15, 15, 15, 15); add(new JLabel("Caselle bianche: "), gbc); 
        gbc.gridx++; gbc.ipadx = 35; gbc.ipady = 35; add(colorOutput[0], gbc); gbc.gridx++;
        
        gbc.ipadx = 0; gbc.ipady = 0;
        
        add(openColorChooser[0], gbc); gbc.gridy++; gbc.gridx = 0;
        
        add(new JLabel("Caselle nere: "), gbc); gbc.ipadx = 35; gbc.ipady = 35;
        gbc.gridx++; add(colorOutput[1], gbc); gbc.gridx++; gbc.ipadx = 0; gbc.ipady = 0;
    
        
        
        add(openColorChooser[1], gbc);  gbc.gridy++; gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 3; add(new JSeparator(), gbc); gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++; gbc.anchor = GridBagConstraints.CENTER;  gbc.insets = new Insets(5, 0, 5, 0);
        
        add(submit, gbc);
        
        pack(); setMinimumSize(getPreferredSize());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (e.getActionCommand().equals("submit")) {
    		for (Chessboard.Box b : parent.cb.boxes) {
    			if (b.getColor().equals("white")) b.setBackground(colorOutput[0].getBackground());
    			else b.setBackground(colorOutput[1].getBackground());
    		}
    		dialog.dispose();
    	} else {
    		final int index = Integer.parseInt(e.getActionCommand()); 
    		final Color c = JColorChooser.showDialog(dialog, "Scegli colore", Color.red);
    		
    		colorOutput[index].setBackground(c);
    		
    		
    	}
    }

}

