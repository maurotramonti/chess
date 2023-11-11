package chess;

import java.net.*;  
import java.io.*;  
import javax.swing.*;


class Server extends Thread {    

    public final int port;
    public boolean abortedByUser = false;
    
    private final Thread self;

    private final Chess parent;
    
    private final int startingMinutes, startingSeconds, increment;

    public ServerSocket serverSocket = null;

    private DataInputStream wis, bis; 
    private DataOutputStream wos, bos;

    private Socket whitePlayer, blackPlayer;

    private String lastInfoRead;

    Server(int port, Chess parent, int startingMinutes, int startingSeconds, int increment) throws IOException {   
    	super(); this.self = this;
        this.port = port; this.parent = parent; 
        this.startingMinutes = startingMinutes; this.startingSeconds = startingSeconds; this.increment = increment;

		serverSocket = new ServerSocket(port);

    }

    @SuppressWarnings("finally")
	@Override
    public void run() {
        try {

            whitePlayer = serverSocket.accept();
            wis = new DataInputStream(new BufferedInputStream(whitePlayer.getInputStream()));
            wos = new DataOutputStream(new BufferedOutputStream(whitePlayer.getOutputStream()));
            wos.writeUTF("white"); wos.flush();
            wos.writeUTF(Integer.toString(this.startingMinutes)); wos.flush();
            wos.writeUTF(Integer.toString(this.startingSeconds)); wos.flush();
            wos.writeUTF(Integer.toString(this.increment)); wos.flush();

            WaitForPlayerDialog wfpd = new WaitForPlayerDialog(parent);
            Thread t = new Thread(){
                @Override
                public void run() {
                    wfpd.setVisible(true);
                    while(this.isInterrupted() == false) {
                    	if (wfpd.isVisible() == false) {
                    		if (abortedByUser) {
                    			try {
                                    wis.close(); wos.close(); whitePlayer.close(); serverSocket.close(); parent.socket.close();
                                } catch (IOException ex2) {
                                	System.err.println("[WARNING]: some sockets couldn't be closed properly (" + ex2.getMessage() + ")");
                                } finally {
                                	parent.cb.isGameEnded = true;
                                	self.interrupt(); 
                                	this.interrupt();
                                }
                    		} else break;         		
                    		
                    		
                    	}                    
                    	try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							break;
						}
                    }
                }
            }; t.start();

            

            blackPlayer = serverSocket.accept(); 
            bis = new DataInputStream(new BufferedInputStream(blackPlayer.getInputStream()));
            bos = new DataOutputStream(new BufferedOutputStream(blackPlayer.getOutputStream()));
            bos.writeUTF("black"); bos.flush();
            bos.writeUTF(Integer.toString(this.startingMinutes)); bos.flush();
            bos.writeUTF(Integer.toString(this.startingSeconds)); bos.flush();
            bos.writeUTF(Integer.toString(this.increment)); bos.flush();
            
            wfpd.closingByXButton = false;
            wfpd.dispose();

        
        } catch (IOException ex) {
        	if (abortedByUser == false) {
        		JOptionPane.showMessageDialog(parent, "Impossibile avviare il server! Messaggio di errore:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        		System.err.println("[ERROR]: unable to start the server (" + ex.getMessage() + ")");
        	}
            
            try {
                serverSocket.close(); parent.socket.close(); wos.close(); bos.close(); wis.close(); bis.close(); whitePlayer.close(); blackPlayer.close();
            } catch (IOException ex2) {
            	JOptionPane.showMessageDialog(parent, "Impossibile chiudere i socket! Messaggio di errore:\n" + ex2.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                System.err.println("[ERROR]: unable to close sockets (" + ex2.getMessage() + ")");
            } finally {
            	parent.cb.isGameEnded = true;
            	return;
            }
            
        }        
        // riceve i dati nel formato: arrayIndex,tipo di pezzo,colore,x,y
        while (parent.cb.isGameEnded == false) {
            try {
                for (int i = 0; i < 32; i++) {
                    lastInfoRead = wis.readUTF();
                    bos.writeUTF(lastInfoRead); bos.flush();
                }  

                bos.writeUTF("end"); bos.flush();

                for (int i = 0; i < 32; i++) {
                    lastInfoRead = bis.readUTF();
                    wos.writeUTF(lastInfoRead); wos.flush();
                } 

                wos.writeUTF("end"); wos.flush();

            } catch (IOException ex) {  
                JOptionPane.showMessageDialog(parent, "La connessione è stata persa!\nPuoi ricominciare una nuova partita tramite le opzioni del menù.", "Info", JOptionPane.INFORMATION_MESSAGE);
                try {
                    serverSocket.close(); parent.socket.close(); wos.close(); bos.close(); wis.close(); bis.close(); whitePlayer.close(); blackPlayer.close();
                } catch (IOException ex2) {
                	JOptionPane.showMessageDialog(parent, "Impossibile chiudere i socket! Messaggio di errore:\n" + ex2.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    System.err.println("[ERROR]: unable to close sockets (" + ex2.getMessage() + ")");
                } finally {
                	parent.cb.isGameEnded = true;
                }
                
                return;
            }
            
            
        }
        try {
            wos.close(); bos.close(); wis.close(); bis.close(); whitePlayer.close(); blackPlayer.close(); parent.socket.close(); serverSocket.close(); 
        } catch (IOException ex2) {
            ex2.printStackTrace();
        } finally {
        	parent.cb.isGameEnded = true;
        	return;
        }
        
    }
}