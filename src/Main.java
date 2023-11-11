package chess;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.event.*;
import java.net.*;
import com.formdev.flatlaf.FlatLightLaf;


@SuppressWarnings("serial")
class Chess extends JFrame implements ActionListener, ComponentListener {

    public Chess chess;
    public Chessboard cb;

    public boolean thinClientMode = false;
    public String titleSuffixes = new String("");
    
    public volatile boolean alreadyRunning = false;

    public final Sidebar sb;

    public int startingMinutes, startingSeconds, increment, port;

    public Socket socket;
    public String ip;
    
    public Server server;

    private final JMenuItem restart, pause, connectToGame, hostGame;
    
    private int oldW, oldH;
    
    private final float ratio;  // constant ratio between width and height


    Chess() {
        super();
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

        try {
            setIconImage(ImageIO.read(getClass().getResource("/images/icon.png")));
        } catch (Exception ex) {
            System.err.println("[ERROR]: unable to load the logo image!");
        }


        boolean success = false;
        
        this.chess = this;
        sb = new Sidebar(this); 

        while (success == false) {
            new StartGameDialog(this);

            if (thinClientMode == false) {
                success = true;
                cb = new Chessboard(this);
                sb.reinitializeClocks();
            } else {
                try {
                    cb = new ThinClientChessboard(this, socket);
                    success = true;
                } catch (Exception ex) {
                    System.err.println("[ERROR]: unable to connect to server (" + ex.getMessage() + ")");
                    JOptionPane.showMessageDialog(this, "Si è verificato un errore di connessione!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            
            }
        }

        setTitle("Scacchi - Tocca al Bianco " + titleSuffixes);

        
        
        if (thinClientMode) {
        	if (cb.whatPlayerAreYou.equals("white")) sb.moves.getColumnModel().getColumn(1).setHeaderValue(new String("Bianco (tu)"));
        	else sb.moves.getColumnModel().getColumn(2).setHeaderValue(new String("Nero (tu)"));
        } 
        
        
        final BorderLayout layout = new BorderLayout(0, 0);
        final JPanel contentPane = new JPanel(layout){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    final Image background = ImageIO.read(getClass().getResource("/images/wood_texture.jpg"));
                    g.drawImage(background, 0, 0, null);
                } catch (Exception ex) {
                    System.err.println("[ERROR]: unable to load the background image!");
                }
        
            }
        }; contentPane.setBorder(BorderFactory.createLineBorder(Color.black, 3));
        

        final JMenuBar menuBar = new JMenuBar();
        final JMenu game = new JMenu("Partita");
        final JMenu appearance = new JMenu("Aspetto");
        final JMenu aboutMenu = new JMenu("?");

        connectToGame = new JMenuItem("Connetti a partita online..."); connectToGame.addActionListener(this); connectToGame.setActionCommand("connectToGame");
        hostGame = new JMenuItem("Ospita partita"); hostGame.addActionListener(this); hostGame.setActionCommand("hostGame");
        pause = new JMenuItem("Pausa"); pause.addActionListener(this); pause.setActionCommand("pause"); pause.setEnabled(!thinClientMode);
        restart = new JMenuItem("Ricomincia"); restart.addActionListener(this); restart.setActionCommand("restart");

        final JMenuItem recordMoves = new JMenuItem("Registra le mosse"); recordMoves.addActionListener(this); recordMoves.setActionCommand("recordMoves"); recordMoves.setEnabled(false);

        game.add(hostGame); game.add(connectToGame); game.addSeparator(); game.add(restart); game.add(pause); game.addSeparator(); game.add(recordMoves);

        final JMenuItem chooseChessboard = new JMenuItem("Scegli scacchiera"); chooseChessboard.addActionListener(this); chooseChessboard.setActionCommand("chooseChessboard");
        final JMenuItem customizePieces = new JMenuItem("Pezzi personalizzati..."); customizePieces.setEnabled(false); customizePieces.addActionListener(this); customizePieces.setActionCommand("customizePieces");

        appearance.add(chooseChessboard); appearance.add(customizePieces);

        final JMenuItem about = new JMenuItem("Informazioni"); about.setActionCommand("about"); about.addActionListener(this);

        aboutMenu.add(about);


        menuBar.add(game); menuBar.add(appearance); menuBar.add(aboutMenu);


        setContentPane(contentPane);    
        add(cb, BorderLayout.CENTER); 
        add(sb, BorderLayout.EAST); 
        setJMenuBar(menuBar);
        
        pack(); setMinimumSize(getPreferredSize()); ratio = (float)((float) getPreferredSize().width / (float) getPreferredSize().height); /*setMaximumSize(new Dimension((int)(getPreferredSize().width * 1.5f), (int)(getPreferredSize().height * 1.5f)));*/ //setMaximumSize(new Dimension(800, 800)); revalidate(); 
        this.oldW = getWidth(); this.oldH = getHeight();
        
        new Thread() {
        	@Override
        	public void run() {
        		while (this.isInterrupted() == false) {
        			pause.setEnabled(!thinClientMode);
        			hostGame.setEnabled(cb.isGameEnded || !thinClientMode);
        			connectToGame.setEnabled(cb.isGameEnded || !thinClientMode);
        			try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
        		}
        	}
        }.start();

        addComponentListener(this);
        setVisible(true);
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("about")) {
            new AboutDialog(chess);
        } else if (e.getActionCommand().equals("restart")) {
            int r = JOptionPane.showConfirmDialog(chess, "Sei sicuro di voler ricominciare?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
            chess.dispose();
            chess = new Chess();
        } else if (e.getActionCommand().equals("pause")) {
            sb.blackClock.setActive(false); sb.whiteClock.setActive(false);
            JOptionPane.showMessageDialog(chess, "Il gioco è attualmente in pausa,\npremere Ok per continuare", "Messaggio", JOptionPane.INFORMATION_MESSAGE);
            if (cb.currentPlayer.equals("white")) sb.whiteClock.setActive(true);
            else sb.blackClock.setActive(true);
        } else if (e.getActionCommand().equals("chooseChessboard")) {
            new EditChessboardDialog(chess);
        } else if (e.getActionCommand().equals("connectToGame")){
            try {
                new ConnectToServerDialog(chess);

            } catch (Exception ex) {
            	System.err.println("[ERROR]: unable to connect to the server (" + ex.getMessage() + ")");
            	thinClientMode = false;
            }
        } else if (e.getActionCommand().equals("hostGame")) {
            new HostGameDialog(chess);      

        }
    }
    

	@Override
	public void componentResized(ComponentEvent e) {
		if (alreadyRunning) return;
		alreadyRunning = true;
		
		int currentWidth = getWidth(); int currentHeight = getHeight();
		
		if (currentWidth > 900) currentWidth = 900; 
		if (currentHeight > (int)((float) currentWidth / ratio)) currentHeight = (int)(900.0f / ratio); // to avoid displaying blank background (bgImage is 900x900)
		if (oldW != currentWidth && oldH == currentHeight) { // ridimensionamento sull'asse x
			setPreferredSize(new Dimension(currentWidth, ((int)((float) currentWidth / ratio))));
			//System.out.println("Cambia la W, ratio: " + ratio);
		} else if (oldH != currentHeight && oldW == currentWidth) { // ridimensionamento sull'asse y
			setPreferredSize(new Dimension((int)((float) currentHeight * ratio), currentHeight));
			//System.out.println("Cambia la H, ratio: " + ratio);
		} else if (oldW != currentWidth && oldH != currentHeight){ // ridimensionamento contemporaneo
			setPreferredSize(new Dimension(currentWidth, ((int)((float) currentWidth / ratio)))); // prevale la x
			//System.out.println("Cambiano entrambe, ratio: " + ratio);
		} else { // se rimangono uguali, non fare un cazzo
			//System.out.println("Non cambia nessuna!"); 
			pack();
			alreadyRunning = false;
			return;
		}

		revalidate(); // pack is done in chessboard thread

		oldW = getPreferredSize().width; oldH = getPreferredSize().height; // because pack() may haven't been called yet

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	


}

public class Main {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            
        } catch (Exception e) {
        	e.printStackTrace();
        	try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
        }
        SwingUtilities.invokeLater(new Runnable() {
        	@Override
        	public void run() {
        		new Chess();
        	}
        });
    }
}