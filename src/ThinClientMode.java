package chess;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")
class ThinClientChessboard extends Chessboard {

    private final Socket s; 
    private DataOutputStream dos; 
    private DataInputStream dis;

    ThinClientChessboard(Chess parent, Socket s) {
        super(parent);
        this.s = s;
        try {
            this.dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            this.dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            whatPlayerAreYou = dis.readUTF(); 
            if (whatPlayerAreYou.equals("white") == false) { 
                waitingForUpdates = true;
                new WaitForUpdates().start(); 
            } 
            parent.startingMinutes = Integer.parseInt(dis.readUTF());
            parent.startingSeconds = Integer.parseInt(dis.readUTF());
            parent.increment = Integer.parseInt(dis.readUTF());
            
            
        } catch (Exception ex) {
            if (parent.server.abortedByUser) return;
            System.err.println("[ERROR]: unable to connect to the server (" + ex.getMessage() + ")");
            JOptionPane.showMessageDialog(parent, "Si è verificato un errore nella connessione!\nMessaggio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            isGameEnded = true;
        }
        parent.sb.reinitializeClocks();
        addBoxes();
    	
        
    }

    @Override
    public final void sendData() {

        try {
            for (int i = 0; i < 32; i++) {
                String datas = "" + i + "," + pieces[i].getType() + "," + pieces[i].owner + "," + pieces[i].x + "," + pieces[i].y + "," + lastMove;
                dos.writeUTF(datas); dos.flush();
            }        
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                dis.close(); dos.close(); s.close(); 
            } catch (IOException ex2) {
                ex2.printStackTrace(); 
            }
            
            JOptionPane.showMessageDialog(parent, "La connessione è stata persa!\nPuoi ricominciare una nuova partita tramite le opzioni del menù.", "Info", JOptionPane.INFORMATION_MESSAGE);
            isGameEnded = true; 
            try {
				parent.socket.close();
				parent.server.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return;
        }
    	logLastMove();
        waitingForUpdates = true;
        new WaitForUpdates().start();
    }
    
   
    private final void addBoxes() {
    	truePanel.removeAll(); numbersPanel.removeAll();
    	
    	if (whatPlayerAreYou.equals("black")) {
    		for(int i = 1; i <= 8; i++) {
    			final JLabel l = new JLabel(Integer.toString(i));
    			l.setForeground(Color.white);
    			numbersPanel.add(l);
    		}
    		
    		for (int y = 1; y <= 8; y++) {
    			for (int x = 1; x <= 8; x++) {
    				truePanel.add(boxes[((8 - y) * 8) + x - 1]); 
    			}    			
    		}
    		parent.sb.remove(parent.sb.blackClock); parent.sb.remove(parent.sb.blackClock);
    		parent.sb.add(parent.sb.whiteClock, BorderLayout.NORTH); parent.sb.add(parent.sb.blackClock, BorderLayout.SOUTH);
    	} else {
    		for(int i = 8; i >= 1; i--) {
    			final JLabel l = new JLabel(Integer.toString(i));
    			l.setForeground(Color.white);
    			numbersPanel.add(l);
    		}
    		for (int i = 0; i < 64; i++) {
    			truePanel.add(boxes[i]); 
    		}
    	}
				
	}

    final class WaitForUpdates extends Thread {
        @Override
        public void run() {
            try {
                String datas = dis.readUTF();
                if (currentPlayer.equals("black")) {
                    currentPlayer = "white"; parent.sb.whiteClock.setActive(true); parent.sb.blackClock.setActive(false);
                    parent.setTitle("Scacchi - Tocca al Bianco " + parent.titleSuffixes);
                } else {
                    currentPlayer = "black"; parent.sb.whiteClock.setActive(false); parent.sb.blackClock.setActive(true);
                    parent.setTitle("Scacchi - Tocca al Nero " + parent.titleSuffixes);
                }
                for (int y = 1; y <= 8; y++) {
                    for (int x = 1; x <= 8; x++) {
                    	final int index = ((8 - y) * 8) + x - 1;
                        boxes[index].setPiece(new EmptyBox(x, y));
                    }
                } // pulisce la scacchiera
                
                final String[][] piecesInfo = new String[32][6];
                for (int i = 0; i < 32; i++) {
                
                    
                    piecesInfo[i] = datas.split(",", -1);
                    pieces[i].x = Integer.parseInt(piecesInfo[i][3]); pieces[i].y = Integer.parseInt(piecesInfo[i][4]);
                    
                    if (pieces[i].getType().equals(piecesInfo[i][1]) == false) {
                    	pieces[i].setType(piecesInfo[i][1]); pieces[i].updateImage(); 
                    }
                    
                    
                    if ((((8 - pieces[i].y) * 8) + pieces[i].x - 1) <= 63 && (((8 - pieces[i].y) * 8) + pieces[i].x - 1) >= 0) { // solo i pezzi non mangiati che invece hanno x e y strane
                        boxes[((8 - pieces[i].y) * 8) + pieces[i].x - 1].setPiece(pieces[i]);
                    }
                    
                    datas = dis.readUTF();
             
                
                }
                
                lastMove = new String(piecesInfo[0][5]);
                logLastMove();
               
                
                waitingForUpdates = false;
                if (isCheckmate(currentPlayer)) {
                    if (currentPlayer.equals("white")) {
                        JOptionPane.showMessageDialog(parent, "Scaccomatto! Vince il nero", "Bella mossa", JOptionPane.INFORMATION_MESSAGE);
                        isGameEnded = true;
                    } else {
                        JOptionPane.showMessageDialog(parent, "Scaccomatto! Vince il bianco", "Bella mossa", JOptionPane.INFORMATION_MESSAGE);
                        isGameEnded = true;
                    }
                }
                if (isStalemate(currentPlayer)) {
                	JOptionPane.showMessageDialog(parent, "Stallo!", "Info", JOptionPane.INFORMATION_MESSAGE);
                	isGameEnded = true;
                } 
                
                if (isKingInCheck(currentPlayer)) JOptionPane.showMessageDialog(parent, "Scacco al re!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
            	System.err.println("[WARNING]: lost connection (" + ex.getMessage() + ")");
                JOptionPane.showMessageDialog(parent, "La connessione è stata persa!\nPuoi ricominciare una nuova partita tramite le opzioni del menù.", "Info", JOptionPane.INFORMATION_MESSAGE);
                isGameEnded = true;
                return;
            } 
        }
    }
}