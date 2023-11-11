package chess;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.event.*;
import java.lang.Math;
import java.util.ArrayList;


@SuppressWarnings("serial")
class Chessboard extends JPanel implements ComponentListener {

	protected final Chess parent; 
	protected final Chessboard chessboard;	

	protected final JPanel truePanel = new JPanel(new GridLayout(8, 8));
	protected final JPanel numbersPanel = new JPanel(new GridLayout(8, 1));
	
	protected final Box[] boxes = new Box[64];
	
	protected String currentPlayer = "white";
	protected String lastMove;
	protected String whatPlayerAreYou;	

	protected Piece[] pieces = new Piece[32];

	protected boolean isGameEnded = false;
	protected boolean waitingForUpdates = false;
	
	private final String[] lettersArray = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	private boolean isAPieceSelected = false; 
	private boolean castle = false, kingHasBeenMoved = false;	
	private boolean doubleCheck = false;
	
	private Piece pieceWhichIsChecking;
	private Piece pieceSelected;
	
	private float currentScaling = 1.0f;	


	/* STRUTTURA BOX */
	/* POSIZIONI 0-7: 8 PEDONI BIANCHI
       POSIZIONI 8-15: 8 PEDONI NERI 
       POSIZIONI 16-17: RE BIANCO E RE NERO   
       POSIZIONI 18-19: REGINA BIANCA E REGINA NERA
       POSIZIONI 20-23: DUE TORRI BIANCHE E DUE TORRI NERE
       POSIZIONI 24-27: DUE ALFIERI BIANCHI E DUE ALFIERI NERI
       POSIZIONI 28-31: DUE CAVALLI BIANCHI E DUE CAVALLI NERI
	 */

	

	Chessboard(Chess parent) {
		super(new BorderLayout(0, 0));
		this.parent = parent; this.chessboard = this; 

		truePanel.setOpaque(false); truePanel.addComponentListener(this);


		final JPanel lettersPanel = new JPanel(new GridLayout(1, 8)); lettersPanel.setOpaque(false); lettersPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));
		numbersPanel.setOpaque(false); numbersPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		final JLabel[] numbers = new JLabel[8];

		for(int i = 8; i >= 1; i--) {
			numbers[i - 1] = new JLabel(Integer.toString(i));
			numbers[i - 1].setForeground(Color.white);
			numbersPanel.add(numbers[i - 1]);
		}

		final JLabel[] letters = new JLabel[8];



		for(int i = 0; i < 8; i++) {
			letters[i] = new JLabel(lettersArray[i]);
			letters[i].setForeground(Color.white); letters[i].setHorizontalAlignment(JLabel.CENTER);
			lettersPanel.add(letters[i]);
		}

		setOpaque(false);

		for (int y = 8, index = 0; y >= 1; y--) {
			for (int x = 1; x <= 8; x++, index++) {
				String color;
				if(y % 2 == 0) {
					if (x % 2 == 0) {
						color = "black";
					} else color = "white";
				} else {
					if (x % 2 == 0) {
						color = "white";
					} else color = "black";
				}
				boxes[index] = new Box(color, x, y);
				truePanel.add(boxes[index]); 
			}
		}
		
		addPieces();

		truePanel.setPreferredSize(new Dimension(500, 500)); 
		add(truePanel, BorderLayout.CENTER); add(lettersPanel, BorderLayout.SOUTH); add(numbersPanel, BorderLayout.WEST);

		new ScalerThread().start();


	}    
	
	protected void addPieces() {
		for (int x = 1; x <= 8; x++) {
			pieces[x - 1] = new Piece(x, 2, "pawn", "white");
			boxes[x - 1 + 48].setPiece(pieces[x - 1]);
		}

		for (int x = 1; x <= 8; x++) {
			pieces[x - 1 + 8] = new Piece(x, 7, "pawn", "black");
			boxes[x - 1 + 8].setPiece(pieces[x - 1 + 8]);
		}
		
		
		pieces[16] = new Piece(5, 1, "king", "white");
		pieces[17] = new Piece(5, 8, "king", "black");

		boxes[4].setPiece(pieces[17]); boxes[60].setPiece(pieces[16]);


		pieces[20] = new Piece(1, 1, "rook", "white"); pieces[21] = new Piece(8, 1, "rook", "white");
		pieces[22] = new Piece(1, 8, "rook", "black"); pieces[23] = new Piece(8, 8, "rook", "black");

		boxes[0].setPiece(pieces[22]); boxes[7].setPiece(pieces[23]);
		boxes[56].setPiece(pieces[20]); boxes[63].setPiece(pieces[21]);



		pieces[18] = new Piece(4, 1, "queen", "white");
		pieces[19] = new Piece(4, 8, "queen", "black");

		boxes[3].setPiece(pieces[19]); boxes[59].setPiece(pieces[18]);

		pieces[24] = new Piece(3, 1, "bishop", "white"); pieces[25] = new Piece(6, 1, "bishop", "white");
		pieces[26] = new Piece(3, 8, "bishop", "black"); pieces[27] = new Piece(6, 8, "bishop", "black");

		boxes[2].setPiece(pieces[26]); boxes[5].setPiece(pieces[27]);
		boxes[58].setPiece(pieces[24]); boxes[61].setPiece(pieces[25]);

		pieces[28] = new Piece(2, 1, "pony", "white"); pieces[29] = new Piece(7, 1, "pony", "white");
		pieces[30] = new Piece(2, 8, "pony", "black"); pieces[31] = new Piece(7, 8, "pony", "black");

		boxes[1].setPiece(pieces[30]); boxes[6].setPiece(pieces[31]);
		boxes[57].setPiece(pieces[28]); boxes[62].setPiece(pieces[29]);
	}

	private final class ScalerThread extends Thread {
		private float oldScaling = 1.0f;

		@Override
		public void run() {
			while (true) {
				if (currentScaling != oldScaling) {
					for (Piece p : pieces) p.updateImage();
					oldScaling = currentScaling;
					parent.alreadyRunning = false;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}
	}
	
	protected final boolean isLegalMove(final Piece p, final int destX, final int destY) {
		switch(p.type) {
			case "pawn":
				if (p.owner.equals("white")) {
					if ((destY < p.y) || (destY - p.y > 2)) { // se si muove verso il basso o di più di 2 caselle
						return false;
					} else if (((destY - p.y) == 2) && (p.firstMove == false || destX != p.x)) { // se si muove di due caselle e non è la prima mossa
						return false;
					}
					if ((destY - p.y) == 2 && destX == p.x) {
						if (!boxes[((8 - (destY - 1)) * 8) + destX - 1].current.type.equals("empty") || !boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty")) return false;
					}
					
					if (Math.abs(destX - p.x) > 1) return false;
					if ((((p.y - destY) == -1) && (p.x == destX)) && !boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty")) return false;
					if ((Math.abs(destX - p.x) == 1) && (boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty") || boxes[((8 - destY + 1) * 8) + destX - 1].current.owner.equals(p.owner) || ((p.y - destY) != -1))) return false;
				}
				else if (p.owner.equals("black")) {
					if ((destY > p.y) || (destY - p.y < -2)) { // se si muove verso l'alto o di più di 2 caselle
						return false;
					} else if (((destY - p.y) == -2) && (p.firstMove == false || p.x != destX)) {
						return false;
					} 

					if ((destY - p.y) == -2 && destX == p.x) {
						if (!boxes[((8 - (destY + 1)) * 8) + destX - 1].current.type.equals("empty") || !boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty")) return false;
					}
					
					if (Math.abs(destX - p.x) > 1) return false;
					if ((((p.y - destY) == 1) && (p.x == destX)) && !boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty")) return false;
					if ((Math.abs(destX - p.x) == 1) && (boxes[((8 - destY) * 8) + destX - 1].current.type.equals("empty") || boxes[((8 - destY - 1) * 8) + destX - 1].current.owner.equals(p.owner) || ((p.y - destY) != 1))) return false;

				}
				break;

			case "rook":

				if (!(p.x != destX) ^ (p.y != destY)) { // se non va in linea retta
					return false;
				} 

				if (p.x == destX) {
					int start = p.y; int end = destY;
					if ((start - end) < 0) {
						for (end--; end > start; --end) { // a salire
							if (boxes[((8 - end ) * 8) + destX - 1].current.type.equals("empty") == false) {
								return false;
							}
						}
					} else if ((start - end) > 0) { // a scendere
						for (end++; start > end; ++end) {
							if (boxes[((8 - end ) * 8) + destX - 1].current.type.equals("empty") == false) {
								return false;
							}
						}
					}
				} else if (p.y == destY) {
					int start = p.x; int end = destX;
					if ((start - end) < 0) { // verso destra
						for (end--; start < end; --end) {
							if (boxes[((8 - p.y ) * 8) + end - 1].current.type.equals("empty") == false) {
								return false;
							}
						}
					} else if ((start - end) > 0) { // verso sinistra
						for (end++; end < start; ++end) {
							if (boxes[((8 - p.y ) * 8) + end - 1].current.type.equals("empty") == false) {
								return false;
							}
						}
					}
				}              
				break;

			case "bishop":
				if (p.x == destX || p.y == destY) {
					return false; // se va dritto
				}
				if ((Math.abs(p.x - destX) != Math.abs(p.y - destY))) {
					return false; //se non segue la diagonale
				}


				int startX = p.x; int startY = p.y;
				if (startY - destY < 0) { // va verso l'alto
					startY++;
					if (startX - destX < 0) {// va verso destra {
						startX++;
						for (;startX < destX; startX++) {
							if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
								return false;
							}
							startY++;
						}
					} else { // va verso sinistra
						startX--;
						for (;startX > destX; startX--) {
							if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
								return false;
							}
							startY++;
						}
					}
				} else { // va verso il basso
					startY--;
					if (startX - destX < 0) {// va verso destra {
						startX++;
						for (;startX < destX; startX++) {
							if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
								return false;
							}
							startY--;
						}
					} else { // va verso sinistra
						startX--;
						for (;startX > destX; startX--) {
							if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
								return false;
							}
							startY--;
						}
					}
				}
				break;

			case "queen":
				startX = p.x; startY = p.y;
				if (startX != destX ^ startY != destY) { // movimento in linea retta
					if (startX == destX) { // si muove verticalmente
						if (startY < destY) { // verso l'alto
							startY++;
							for (;startY < destY; startY++) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) return false;
							}
						} else { // verso il basso
							startY--;
							for (;startY > destY; startY--) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) return false;
							}
						}
					} else {
						if (startX < destX) { // verso destra
							startX++;
							for (;startX < destX; startX++) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) return false;
							}
						} else { // verso sinistra
							startX--;
							for (;startX > destX; startX--) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) return false;
							}
						}
					}
				} else if (startX != destX && startY != destY) { // movimento in diagonale
					if (Math.abs(startX - destX) != Math.abs(startY - destY)) return false;
					if (startY - destY < 0) { // va verso l'alto
						startY++;
						if (startX - destX < 0) {// va verso destra {
							startX++;
							for (;startX < destX; startX++) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
									return false;
								}
								startY++;
							}
						} else { // va verso sinistra
							startX--;
							for (;startX > destX; startX--) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
									return false;
								}
								startY++;
							}
						}
					} else { // va verso il basso
						startY--;
						if (startX - destX < 0) {// va verso destra {
							startX++;
							for (;startX < destX; startX++) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
									return false;
								}
								startY--;
							}
						} else { // va verso sinistra
							startX--;
							for (;startX > destX; startX--) {
								if (boxes[((8 - startY) * 8) + startX - 1].current.type.equals("empty") == false) {
									return false;
								}
								startY--;
							}
						}
					}
				}
				break;

			case "pony":
				startX = p.x; startY = p.y;
				if (Math.abs(destX - startX) > 2 || Math.abs(destX - startX) == 0) {
					return false;
				}
				if (Math.abs(destY - startY) > 2 || Math.abs(destY - startY) == 0) {
					return false; 
				} 
				if (Math.abs(destY - startY) == Math.abs(destX - startX)) {
					return false;
				}

				break;

			case "king":
				startX = p.x; startY = p.y;


				if (p.neverMoved && isKingInCheck(p, true) == false) {
					if ((Math.abs(startX - destX) == 2) && startY == destY) { // se si prova a fare l'arrocco
						if (p.owner.equals("white")) {
							if (destX == 7) { // arrocco corto
								if (boxes[((8 - 1) * 8) + 6 - 1].current.type.equals("empty") && isKingInCheck(new Piece(6, 1, "king", "white"), true) == false) { // se la casella in mezzo è libera e non attaccata
									if (pieces[21].neverMoved) { // se la torre di dx non è mai stata mossa
										if (isKingInCheck(new Piece(7, 1, "king", "white"), true) == false) {
											pieces[21].x = 6; pieces[21].y = 1;
											boxes[((8 - 1) * 8) + 6 - 1].setPiece(pieces[21]); boxes[((8 - 1) * 8) + 8 - 1].setPiece(new EmptyBox(8, 1));
											castle = true; pieces[21].neverMoved = false; pieces[16].neverMoved = false;
											lastMove = new String("O-O");
										}
									}
								}
							} else if (destX == 3) { // arrocco lungo
								if ((boxes[((8 - 1) * 8) + 4 - 1].current.type.equals("empty") && isKingInCheck(new Piece(4, 1, "king", "white"), true) == false) && boxes[((8 - 1) * 8) + 2 - 1].current.type.equals("empty")) { // se le caselle in mezzo sono libere e non attaccate
									if (pieces[20].neverMoved) { // se la torre di sx non è mai stata mossa
										if (isKingInCheck(new Piece(3, 1, "king", "white"), true) == false) {
											pieces[20].x = 4; pieces[20].y = 1;
											boxes[((8 - 1) * 8) + 4 - 1].setPiece(pieces[20]); boxes[((8 - 1) * 8) + 1 - 1].setPiece(new EmptyBox(1, 1));
											castle = true; pieces[20].neverMoved = false; pieces[16].neverMoved = false;
											lastMove = new String("O-O-O");
										}
									}
								}
							}
						} else if (p.owner.equals("black")) {
							if (destX == 7) { // arrocco corto
								if (boxes[((8 - 8) * 8) + 6 - 1].current.type.equals("empty") && isKingInCheck(new Piece(6, 8, "king", "black"), true) == false) { // se la casella in mezzo è libera e non attaccata
									if (pieces[23].neverMoved) { // se la torre di dx non è mai stata mossa
										if (isKingInCheck(new Piece(7, 8, "king", "black"), true) == false) {
											pieces[23].x = 6; pieces[23].y = 8;
											boxes[((8 - 8) * 8) + 6 - 1].setPiece(pieces[23]); boxes[((8 - 8) * 8) + 8 - 1].setPiece(new EmptyBox(8, 8));
											castle = true; pieces[23].neverMoved = false; pieces[17].neverMoved = false;
											lastMove = new String("O-O");
										}
									}
								} 
							} else if (destX == 3) { // arrocco lungo
								if ((boxes[((8 - 8) * 8) + 4 - 1].current.type.equals("empty") && isKingInCheck(new Piece(4, 8, "king", "black"), true) == false) && boxes[((8 - 8) * 8) + 2 - 1].current.type.equals("empty")) { // se le caselle in mezzo sono libere e non attaccate
									if (pieces[22].neverMoved) { // se la torre di sx non è mai stata mossa
										if (isKingInCheck(new Piece(3, 8, "king", "black"), true) == false) {
											pieces[22].x = 4; pieces[22].y = 8;
											boxes[((8 - 8) * 8) + 4 - 1].setPiece(pieces[22]); boxes[((8 - 8) * 8) + 1 - 1].setPiece(new EmptyBox(1, 8));
											castle = true; pieces[22].neverMoved = false; pieces[17].neverMoved = false;
											lastMove = new String("O-O-O");
										}
									}
								}
							}
						}
					}
				}
				if (castle == false) {
					if (!(Math.abs(destX - startX) < 2 && Math.abs(destY - startY) < 2)) return false;
				}

				kingHasBeenMoved = true;
				if(isKingInCheck(new Piece(destX, destY, "king", currentPlayer), true)) {
					JOptionPane.showMessageDialog(parent, "Mossa irregolare!", "Attenzione", JOptionPane.WARNING_MESSAGE); 
					kingHasBeenMoved = false;
					return false;
				}
				break;

			default:
				throw new IllegalArgumentException("Tipo di pezzo non valido!");						

		}
		return true;
		
	}
	
	protected final boolean isCheckmate(String color) {
		final Piece king; boolean seemsToBeCheckmate = false;
		if (color.equals("white")) king = pieces[16];
		else king = pieces[17];


		if (kingCannotMove(color)) {
			if (isKingInCheck(king, false)) {
				if (doubleCheck) return true; // con scacco doppio se il re non può muoversi è matto
				seemsToBeCheckmate = true;
			}
		}

		if (seemsToBeCheckmate == false) {
			doubleCheck = false;
			return false;
		}
		
		
		// controllo se il pezzo possa essere mangiato
		if (pieceWhichIsChecking != null) {
			if (isPieceInCheck(pieceWhichIsChecking)) return false;
		}


		// controllo se si possa parare lo scacco
		if (pieceWhichIsChecking != null) {
			switch (pieceWhichIsChecking.type) {
				case "rook": // se dà scacco una torre
					if (pieceWhichIsChecking.x == king.x) { // se siamo sulla stessa colonna
						if (Math.abs(pieceWhichIsChecking.y - king.y) > 1) { // se c'è dello spazio sopra o sotto al re
							if (king.y > pieceWhichIsChecking.y) { // se il re è più in alto
								for (int y = king.y - 1; y > pieceWhichIsChecking.y; y--) { // per ogni riga al di sotto
									for (int x = 1; x <= 8; x++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;
										
										
									}
									// controllo le diagonali del quadratino
									for (int x2 = king.x + 1, y2 = y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x + 1, y2 = y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}

								}

							} else if (king.y < pieceWhichIsChecking.y) { // se il re è al di sotto
								for (int y = king.y + 1; y < pieceWhichIsChecking.y; y++) { // per ogni riga al di sopra
									for (int x = 1; x <= 8; x++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;    
										
									}
									for (int x2 = king.x + 1, y2 = y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x + 1, y2 = y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
								}
							}
						}
					} else if (pieceWhichIsChecking.y == king.y) { // se siamo sulla stessa riga
						if (Math.abs(pieceWhichIsChecking.x - king.x) > 1) { // se c'è dello spazio a dx o sx del re
							if (king.x > pieceWhichIsChecking.x) { // se il re è più a dx
								for (int x = king.x - 1; x > pieceWhichIsChecking.x; x--) { // per ogni colonna a sx
									for (int y = 1; y <= 8; y++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
											if (king.owner.equals("white")) {
												if (king.y - y == 1) return false;
												if (king.y - y == 2 && p.firstMove) return false;
											} else if (king.owner.equals("black")) {
												if (king.y - y == -1) return false;
												if (king.y - y == -2 && p.firstMove) return false;
											}
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;   
										
									}
									for (int x2 = x + 1, y2 = king.y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x + 1, y2 = king.y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
								}

							} else if (king.x < pieceWhichIsChecking.x) { // se il re è più a sx
								for (int x = king.x + 1; x < pieceWhichIsChecking.x; x++) { // per colonna a dx
									for (int y = 1; y <= 8; y++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
											if (king.owner.equals("white")) {
												if (king.y - y == 1) return false;
												if (king.y - y == 2 && p.firstMove) return false;
											} else if (king.owner.equals("black")) {
												if (king.y - y == -1) return false;
												if (king.y - y == -2 && p.firstMove) return false;
											}
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;    
										
									}
									for (int x2 = x + 1, y2 = king.y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x + 1, y2 = king.y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
								}
							}
						}
					}
					break;

				case "bishop":
					if (Math.abs(pieceWhichIsChecking.x - king.x) > 1) { // se l'alfiere non è appiccicato

						// CONTROLLO LE RIGHE E LE COLONNE

						if (pieceWhichIsChecking.y > king.y) { // se l'alfiere è sopra al re
							for (int y = king.y + 1; y < pieceWhichIsChecking.y; y++) {
								for (int x = king.x + 1; x <= 8; x++) { // controllo la parte destra di ogni riga tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
										if (p.owner.equals("white")) {
											if ((y - p.y) == 1) return false;
											if (p.firstMove && ((y - p.y) == 2)) return false;
										} else if (p.owner.equals("black")) {
											if ((y - p.y) == -1) return false;
											if (p.firstMove && ((y - p.y) == -2)) return false;
										}
									}
								}

								for (int x = king.x; x >= 1; x--) { // controllo la parte sinistra di ogni riga tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
										if (p.owner.equals("white")) {
											if ((y - p.y) == 1) return false;
											if (p.firstMove && ((y - p.y) == 2)) return false;
										} else if (p.owner.equals("black")) {
											if ((y - p.y) == -1) return false;
											if (p.firstMove && ((y - p.y) == -2)) return false;
										}
									}
								}
							}
						} else if (pieceWhichIsChecking.y < king.y) { // se l'alfiere è sotto al re
							for (int y = king.y - 1; y > pieceWhichIsChecking.y; y--) {
								for (int x = king.x + 1; x <= 8; x++) { // controllo la parte destra di ogni riga tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
										if (p.owner.equals("white")) {
											if ((y - p.y) == 1) return false;
											if (p.firstMove && ((y - p.y) == 2)) return false;
										} else if (p.owner.equals("black")) {
											if ((y - p.y) == -1) return false;
											if (p.firstMove && ((y - p.y) == -2)) return false;
										}
									}
								}

								for (int x = king.x; x >= 1; x--) { // controllo la parte sinistra di ogni riga tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
										if (p.owner.equals("white")) {
											if ((y - p.y) == 1) return false;
											if (p.firstMove && ((y - p.y) == 2)) return false;
										} else if (p.owner.equals("black")) {
											if ((y - p.y) == -1) return false;
											if (p.firstMove && ((y - p.y) == -2)) return false;
										}
									}
								}
							}
						}

						if (pieceWhichIsChecking.x < king.x) { // se l'alfiere è a sinistra del re
							for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++) {
								for (int y = pieceWhichIsChecking.y - 1; y >= 1; y--) { // controllo la parte bassa di ogni colonna tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									} 
								}
								for (int y = pieceWhichIsChecking.y + 1; y <= 8; y++) { // controllo la parte alta di ogni colonna tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									}
								}                                 
							}                        
						} else if (pieceWhichIsChecking.x > king.x) { // se l'alfiere è a destra del re
							for (int x = pieceWhichIsChecking.x - 1; x > king.x; x--) {
								for (int y = pieceWhichIsChecking.y - 1; y >= 1; y--) { // controllo la parte bassa di ogni colonna tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									}
								}
								for (int y = pieceWhichIsChecking.y + 1; y <= 8; y++) { // controllo la parte alta di ogni colonna tra re e alfiere
									Piece p = getPieceFromXY(x, y);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("rook")) && p.owner.equals(king.owner)) {
										return false;
									}
								}                                 
							}             
						}

						// CONTROLLO LE DIAGONALI

						if (pieceWhichIsChecking.x < king.x) { // se l'alfiere è a sinistra
							if (pieceWhichIsChecking.y < king.y) { // se l'alfiere è sotto, allora controllo diag. in alto a dx
								int y = pieceWhichIsChecking.y + 1;
								for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++) {
									for (int x2 = x + 1, y2 = y + 1; x2 <= 8; ++y2, ++x2) { // controllo la diag in alto a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										} 
									}


									
									for (int x2 = x + 1, y2 = y - 1; x2 <= 8; --y2, ++x2) { // controllo la diag in basso a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
									}
									
									for (int x2 = x - 1, y2 = y + 1; x2 >= 1; ++y2, --x2) { // controllo la diag in alto a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
									}


									for (int x2 = x - 1, y2 = y - 1; x2 >= 1; --x2, --y2) { // controllo la diag in basso a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
									}

									for (Piece p : pieces) {
										if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
										}
									}


									y++;
								}
							} else { // altrimenti, controllo la diag in basso a dx
								int y = pieceWhichIsChecking.y - 1;
								for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++) {
									int y2 = y + 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in alto a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2++;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in basso a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2--;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2--;
									}
									y2 = y + 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in alto a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2++;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in basso a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2--;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2--;
									}
									for (Piece p : pieces) {
										if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
										}
									}




									y--;
								}
							}
						} else {
							if (pieceWhichIsChecking.y < king.y) { // se l'alfiere è sotto, allora controllo diag. in alto a sx
								int y = pieceWhichIsChecking.y + 1;
								for (int x = pieceWhichIsChecking.x - 1; x > king.x; x--) {
									int y2 = y + 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in alto a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2++;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in basso a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2--;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2--;
									}
									y2 = y + 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in alto a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2++;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in basso a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) {
											y2--;
											continue;
										}
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}

									}

									for (Piece p : pieces) {
										if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
										}
									}




									y++;
								}
							} else { // altrimenti, controllo la diag in basso a sx
								int y = pieceWhichIsChecking.y - 1;
								for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++) {
									int y2 = y + 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in alto a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x + 1; x2 <= 8; x2++) { // controllo la diag in basso a dx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2--;
									}
									y2 = y + 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in alto a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2++;
									}

									y2 = y - 1;
									for (int x2 = x - 1; x2 >= 1; x2--) { // controllo la diag in basso a sx del quadratino
										Piece p = getPieceFromXY(x2, y2);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
											return false;
										}
										y2--;
									}


									for (Piece p : pieces) {
										if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
										}
									}


									y--;
								}
							}
						}
					}
					break;

				case "queen":
					// CONTROLLO LE RIGHE E COLONNE
					if (pieceWhichIsChecking.x == king.x) { // se siamo sulla stessa colonna
						if (Math.abs(pieceWhichIsChecking.y - king.y) > 1) { // se c'è dello spazio sopra o sotto al re
							if (king.y > pieceWhichIsChecking.y) { // se il re è più in alto
								for (int y = king.y - 1; y > pieceWhichIsChecking.y; y--) { // per ogni riga al di sotto
									for (int x = 1; x <= 8; x++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;                          
									}
									// controllo le diagonali del quadratino
									for (int x2 = king.x + 1, y2 = y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x + 1, y2 = y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}

								}

							} else if (king.y < pieceWhichIsChecking.y) { // se il re è al di sotto
								for (int y = king.y + 1; y < pieceWhichIsChecking.y; y++) { // per ogni riga al di sopra
									for (int x = 1; x <= 8; x++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;       
									}
									// controllo le diagonali del quadratino
									for (int x2 = king.x + 1, y2 = y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x + 1, y2 = y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = king.x - 1, y2 = y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
								}
							}
						}
					} else if (pieceWhichIsChecking.y == king.y) { // se siamo sulla stessa riga
						if (Math.abs(pieceWhichIsChecking.x - king.x) > 1) { // se c'è dello spazio a dx o sx del re
							if (king.x > pieceWhichIsChecking.x) { // se il re è più a dx
								for (int x = king.x - 1; x > pieceWhichIsChecking.x; x--) { // per ogni colonna a sx
									for (int y = 1; y <= 8; y++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
											if (king.owner.equals("white")) {
												if (king.y - y == 1) return false;
											} else if (king.owner.equals("black")) {
												if (king.y - y == -1) return false;
											}
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;       
									}
									// controllo le diagonali del quadratino
									for (int x2 = x + 1, y2 = king.y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x + 1, y2 = king.y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
								}

							} else if (king.x < pieceWhichIsChecking.x) { // se il re è più a sx
								for (int x = king.x + 1; x < pieceWhichIsChecking.x; x++) { // per colonna a dx
									for (int y = 1; y <= 8; y++) {
										Piece p = getPieceFromXY(x, y);
										if (p.owner == null) continue;
										if (p.owner.equals(king.owner) == false) break;
										if (p.isPinned()) break;
										else if ((p.type.equals("rook") || p.type.equals("queen")) && p.owner.equals(king.owner)) {
											return false;
										} else if (p.type.equals("pawn") && p.owner.equals(king.owner)) {
											if (king.owner.equals("white")) {
												if (king.y - y == 1) return false;
											} else if (king.owner.equals("black")) {
												if (king.y - y == -1) return false;
											}
										} else if (p.type.equals("pony") && p.owner.equals(king.owner)) {
											if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) {
												return false;
											}
										}
										if (p.type.equals("empty") == false) break;       
									}
									// controllo le diagonali del quadratino
									for (int x2 = x + 1, y2 = king.y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x + 1, y2 = king.y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}
									for (int x2 = x - 1, y2 = king.y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
										final Piece p2 = getPieceFromXY(x2, y2);
										if (p2.type.equals("empty")) continue;
										if (p2.owner.equals(king.owner) == false) break;
										if (p2.isPinned()) break;
										if (p2.type.equals("bishop") || p2.type.equals("queen")) return false;
									}

								}
							}
						}
					}

					// CONTROLLO LE DIAGONALI
					if (pieceWhichIsChecking.x < king.x) { // se la regina è a sinistra
						if (pieceWhichIsChecking.y < king.y) { // se la regina è sotto, allora controllo diag. in alto a dx
							int y = pieceWhichIsChecking.y + 1;
							for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++, y++) {
								
								for (int x2 = x + 1, y2 = y + 1; x2 <= 8; x2++, y2++) { // controllo la diag in alto a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}


								for (int x2 = x + 1, y2 = y - 1; x2 <= 8; x2++, y2--) { // controllo la diag in basso a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;

								}

								for (int x2 = x - 1, y2 = y + 1; x2 >= 1; x2--, y2++) { // controllo la diag in alto a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) y2++;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y - 1; x2 >= 1; x2--, y2--) { // controllo la diag in basso a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (Piece p : pieces) {
									if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
										if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
									}
								}

							}
						} else { // altrimenti, controllo la diag in basso a dx
							int y = pieceWhichIsChecking.y - 1;
							for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++, y--) {

								for (int x2 = x + 1, y2 = y + 1; x2 <= 8; x2++, y2++) { // controllo la diag in alto a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;

								}

								for (int x2 = x + 1, y2 = y - 1; x2 <= 8; x2++, y2--) { // controllo la diag in basso a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y + 1; x2 >= 1; x2--, y2++) { // controllo la diag in alto a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y - 1; x2 >= 1; x2--, y2--) { // controllo la diag in basso a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}
								for (Piece p : pieces) {
									if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
										if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
									}
								}
							}
						}
					} else {
						if (pieceWhichIsChecking.y < king.y) { // se la regina è sotto, allora controllo diag. in alto a sx
							int y = pieceWhichIsChecking.y + 1;
							for (int x = pieceWhichIsChecking.x - 1; x > king.x; x--, y++) {

								for (int x2 = x + 1, y2 = y + 1; x2 <= 8; x2++, y2++) { // controllo la diag in alto a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									else if ((p.type.equals("queen") || p.type.equals("bishop")) && p.owner.equals(king.owner)) {
										return false;
									}
								}

								for (int x2 = x + 1, y2 = y - 1; x2 <= 8; x2++, y2--) { // controllo la diag in basso a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y + 1; x2 >= 1; x2--, y2++) { // controllo la diag in alto a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y - 1; x2 >= 1; x2--, y2--) { // controllo la diag in basso a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;

								}

								for (Piece p : pieces) {
									if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
										if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
									}
								}                       
							}
						} else { // altrimenti, controllo la diag in basso a sx
							int y = pieceWhichIsChecking.y - 1;
							for (int x = pieceWhichIsChecking.x + 1; x < king.x; x++, y--) {
								for (int x2 = x + 1, y2 = y + 1; x2 <= 8; x2++, y2++) { // controllo la diag in alto a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}


								for (int x2 = x + 1, y2 = y - 1; x2 <= 8; x2++, y2--) { // controllo la diag in basso a dx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}
								
								for (int x2 = x - 1, y2 = y + 1; x2 >= 1; x2--, y2++) { // controllo la diag in alto a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}

								for (int x2 = x - 1, y2 = y - 1; x2 >= 1; x2--, y2--) { // controllo la diag in basso a sx del quadratino
									Piece p = getPieceFromXY(x2, y2);
									if (p.owner == null) continue;
									if (p.owner.equals(king.owner) == false) break;
									if (p.isPinned()) break;
									if (p.type.equals("queen") || p.type.equals("bishop")) return false;
								}


								for (Piece p : pieces) {
									if (p.type.equals("pony") && p.owner.equals(king.owner) && (p.isPinned() == false)) {
										if ((Math.abs(p.x - x) == 1 && Math.abs(p.y - y) == 2) || (Math.abs(p.x - x) == 2 && Math.abs(p.y - y) == 1)) return false;
									}
								}

							}

						}

					}
					break;
					
				default:
					throw new IllegalArgumentException("Tipo di pezzo non valido!");


			}


		}

		return seemsToBeCheckmate;
	}

	protected final boolean isStalemate(final String color) {
		final Piece king;
		if (color.equals("white")) king = pieces[16];
		else king = pieces[17];


		boolean areTherePieces = false;
		int nBBishop = 0, nWBishop = 0, nBPony = 0, nWPony = 0, nWQueen = 0, nBQueen = 0, nWPawn = 0, nBPawn = 0, nWRook = 0, nBRook = 0;
		for (Piece p : pieces) { // controllo che non siano rimasti solo i due re o pezzi insufficienti
			if (p.type.equals("king")) continue;
			if ((p.x >= 1 && p.x <= 8) && (p.y >= 1 && p.y <= 8)) {
				areTherePieces = true;

				switch (p.type) {
					case "queen":
						if (p.owner.equals("white")) nWQueen++;
						else nBQueen++;
						break;
					case "bishop":
						if (p.owner.equals("white")) nWBishop++;
						else nBBishop++;
						break;
					case "pony":
						if (p.owner.equals("white")) nWPony++;
						else nBPony++;
						break;
					case "rook":
						if (p.owner.equals("white")) nWRook++;
						else nBRook++;
						break;
					case "pawn":
						if (p.owner.equals("white")) nWPawn++;
						else nBPawn++;
						break;
				}
			}			
		}

		if (areTherePieces == false) return true; // se ci sono solo i re

		if (nWQueen == 0 && nBQueen == 0 && nWRook == 0 && nBRook == 0 && nWPawn == 0 && nBPawn == 0) {
			if (nBBishop == 0 && nWBishop == 0) return true; // se non ci sono alfieri da nessun lato
			if ((nBBishop <= 1 && nBPony == 0) && (nWBishop <= 1 && nWPony == 0)) return true; // se c'è solo un alfiere e non ci sono cavalli


		}


		if (kingCannotMove(color)) {
			if (!isKingInCheck(king, false) && noPieceCanMove(color)) return true;			
		} 

		return false;
	}

	protected final boolean noPieceCanMove(String color) {

		for (Piece p : pieces) {
			if (p.owner.equals(color) == false || p.type.equals("king") || p.y < 1) continue;

			boolean inCheck; int oldX, oldY;
			switch (p.type) {
				case "pawn": // controllo se può avanzare o mangiare senza fare mossa irregolare

					if (p.owner.equals("white")) {
						if (boxes[((8 - (p.y + 1)) * 8) + p.x - 1].current.type.equals("empty")) {
							oldY = p.y; p.y++;
							inCheck = isKingInCheck(color, true);
							p.y = oldY;

							if(inCheck == false) {
								p.y = oldY; 
								return false;							
							}

						} else if (boxes[((8 - (p.y + 1)) * 8) + p.x].current.type.equals("empty") == false && boxes[((8 - (p.y + 1)) * 8) + p.x].current.owner.equals(color) == false) {
							oldY = p.y; oldX = p.x; p.y++; p.x++;

							inCheck = isKingInCheck(color, true); p.x = oldX; p.y = oldY;
							if (inCheck == false) {
								p.y = oldY; p.x = oldX;
								return false;
							}

						} else if (boxes[((8 - (p.y + 1)) * 8) + p.x - 2].current.type.equals("empty") == false && boxes[((8 - (p.y + 1)) * 8) + p.x - 2].current.owner.equals(color) == false) {
							oldY = p.y; oldX = p.x; p.y++; p.x--;

							inCheck = isKingInCheck(color, true); p.x = oldX; p.y = oldY;
							if (inCheck == false) {
								p.y = oldY; p.x = oldX;
								return false;
							}

						}
					} else {
						if (boxes[((8 - (p.y - 1)) * 8) + p.x - 1].current.type.equals("empty")) {
							oldY = p.y; p.y--;
							inCheck = isKingInCheck(color, true);
							p.y = oldY;

							if(inCheck == false) {
								p.y = oldY; 
								return false;							
							}

						} else if (boxes[((8 - (p.y - 1)) * 8) + p.x].current.type.equals("empty") == false && boxes[((8 - (p.y - 1)) * 8) + p.x].current.owner.equals(color) == false) {
							oldY = p.y; oldX = p.x; p.y--; p.x++;

							inCheck = isKingInCheck(color, true); p.x = oldX; p.y = oldY;
							if (inCheck == false) {
								p.y = oldY; p.x = oldX;
								return false;
							}

						} else if (boxes[((8 - (p.y - 1)) * 8) + p.x - 2].current.type.equals("empty") == false && boxes[((8 - (p.y - 1)) * 8) + p.x - 2].current.owner.equals(color) == false) {
							oldY = p.y; oldX = p.x; p.y--; p.x--;

							inCheck = isKingInCheck(color, true); p.x = oldX; p.y = oldY;
							if (inCheck == false) {
								p.y = oldY; p.x = oldX;
								return false;
							}

						}

					}

					break;

				case "rook": // controllo se una torre può muoversi
					if (p.x < 8) { // se non è contro il muro a destra controllo la riga alla sua destra
						oldX = p.x;
						for (;p.x < 8; ++p.x) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.x = oldX; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.x = oldX;
								return false;
							}
						}
						p.x = oldX;

					} 
					if (p.x > 1) { // se non è contro il muro a sinistra controllo la riga alla sua sinistra
						oldX = p.x;
						for (;p.x > 1; --p.x) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.x = oldX; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.x = oldX;
								return false;
							}
						}
						p.x = oldX; 
					}
					if (p.y < 8) { // se non è contro il muro sopra controllo la colonna sopra
						oldY = p.y;
						for (;p.y < 8; ++p.y) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.y = oldY; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.y = oldY;
								return false;
							}
						}
						p.y = oldY;
					}
					if (p.y > 1) { // se non è contro il muro a sotto controllo la colonna sotto
						oldY = p.y;
						for (;p.y > 1; --p.y) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.y = oldY; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.y = oldY;
								return false;
							}
						}
						p.y = oldY;
					}


					break;

				case "bishop":

					if (p.y < 8) { // se ha spazio sopra
						if (p.x < 8) {// se ha spazio a destra, controllo la diagonale in alto a dx
							oldX = p.x; oldY = p.y;
							for (; p.x < 8 && p.y < 8; ++p.x, ++p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
						if (p.x > 1) {// se ha spazio a destra, controllo la diagonale in alto a sx
							oldX = p.x; oldY = p.y;
							for (; p.x > 1 && p.y < 8; --p.x, ++p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
					}
					if (p.y > 1) { // se ha spazio sotto
						if (p.x < 8) {// se ha spazio a destra, controllo la diagonale in basso a dx
							oldX = p.x; oldY = p.y;
							for (; p.x < 8 && p.y < 8; ++p.x, --p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
						if (p.x > 1) {// se ha spazio a destra, controllo la diagonale in basso a sx
							oldX = p.x; oldY = p.y;
							for (; p.x > 1 && p.y < 8; --p.x, --p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
					}
					break;

				case "queen": // fusione di alfiere e torre
					if (p.y < 8) { // se ha spazio sopra
						if (p.x < 8) {// se ha spazio a destra, controllo la diagonale in alto a dx
							oldX = p.x; oldY = p.y;
							for (; p.x < 8 && p.y < 8; ++p.x, ++p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
						if (p.x > 1) {// se ha spazio a destra, controllo la diagonale in alto a sx
							oldX = p.x; oldY = p.y;
							for (; p.x > 1 && p.y < 8; --p.x, ++p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
					}
					if (p.y > 1) { // se ha spazio sotto
						if (p.x < 8) {// se ha spazio a destra, controllo la diagonale in basso a dx
							oldX = p.x; oldY = p.y;
							for (; p.x < 8 && p.y < 8; ++p.x, --p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
						if (p.x > 1) {// se ha spazio a destra, controllo la diagonale in basso a sx
							oldX = p.x; oldY = p.y;
							for (; p.x > 1 && p.y < 8; --p.x, --p.y) {
								if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
									p.y = oldY; p.x = oldX; break;
								}
								inCheck = isKingInCheck(color, true);
								if (inCheck == false) {
									p.y = oldY; p.x = oldX;
									return false;
								}
							}
							p.x = oldX; p.y = oldY;
						}
					}

					if (p.x < 8) { // se non è contro il muro a destra controllo la riga alla sua destra
						oldX = p.x;
						for (;p.x < 8; ++p.x) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.x = oldX; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.x = oldX;
								return false;
							}
						}
						p.x = oldX;

					} 
					if (p.x > 1) { // se non è contro il muro a sinistra controllo la riga alla sua sinistra
						oldX = p.x;
						for (;p.x > 1; --p.x) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.x = oldX; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.x = oldX;
								return false;
							}
						}
						p.x = oldX; 
					}
					if (p.y < 8) { // se non è contro il muro sopra controllo la colonna sopra
						oldY = p.y;
						for (;p.y < 8; ++p.y) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.y = oldY; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.y = oldY;
								return false;
							}
						}
						p.y = oldY;
					}
					if (p.y > 1) { // se non è contro il muro a sotto controllo la colonna sotto
						oldY = p.y;
						for (;p.y > 1; --p.y) {
							if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
								p.y = oldY; break;
							}
							inCheck = isKingInCheck(color, true);
							if (inCheck == false) {
								p.y = oldY;
								return false;
							}
						}
						p.y = oldY;
					}
					break;

				case "pony":
					// #1
					if (p.x < 8 && p.y < 7) { 
						oldX = p.x; oldY = p.y; p.x++; p.y+=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}

					// #2
					if (p.x < 7 && p.y < 8) { 
						oldX = p.x; oldY = p.y; p.y++; p.x+=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// # 3
					if (p.x < 7 && p.y > 1) { 
						oldX = p.x; oldY = p.y; p.y--; p.x+=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// # 4
					if (p.x < 8 && p.y > 2) { 
						oldX = p.x; oldY = p.y; p.x++; p.y-=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// #5
					if (p.x > 1 && p.y > 2) { 
						oldX = p.x; oldY = p.y; p.x--; p.y-=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// #6
					if (p.x > 2 && p.y > 1) { 
						oldX = p.x; oldY = p.y; p.x-=2; p.y--;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// #7
					if (p.x > 2 && p.y < 8) { 
						oldX = p.x; oldY = p.y; p.x-=2; p.y++;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}
					// #8
					if (p.x > 1 && p.y < 7) { 
						oldX = p.x; oldY = p.y; p.x--; p.y+=2;
						if (boxes[((8 - p.y) * 8) + p.x - 1].current.type.equals("empty") == false && boxes[((8 - p.y) * 8) + p.x - 1].current.owner.equals(color)) {
							p.y = oldY; p.x = oldX; break;
						}
						inCheck = isKingInCheck(color, true);
						if (inCheck == false) {
							p.y = oldY; p.x = oldX;
							return false;
						}
						p.y = oldY; p.x = oldX;
					}



				default:
					throw new IllegalArgumentException("Tipo di pezzo non valido!");
			}
		}


		return true;
	}

	protected final boolean kingCannotMove(final String color) {
		final Piece king;
		if (color.equals("white")) king = pieces[16];
		else king = pieces[17];

		Piece fakeKing;
		if (king.y < 8) {

			fakeKing = new Piece(king.x, king.y + 1, "king", color); // casella in alto 
			if (!isKingInCheck(fakeKing, true)) {
				if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;
				else {
					if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
				}
			}

			if (king.x > 1) {
				fakeKing = new Piece(king.x - 1, king.y + 1, "king", color); // casella in alto a sx
				if (!isKingInCheck(fakeKing, true)) {
					if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) {

						return false;
					}
					else {
						if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
					}
				}
			}            

			if (king.x < 8) {
				fakeKing = new Piece(king.x + 1, king.y + 1, "king", color); // casella in alto a dx
				if (!isKingInCheck(fakeKing, true)) {
					if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) {

						return false;
					}
					else {
						if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
					}
				}
			}

		}


		if (king.y > 1) {
			fakeKing = new Piece(king.x, king.y - 1, "king", color); // casella in basso
			if (!isKingInCheck(fakeKing, true)) {
				if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;
				else {
					if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
				}
			}

			if (king.x > 1) {
				fakeKing = new Piece(king.x - 1, king.y - 1, "king", color); // casella in basso a sx
				if (!isKingInCheck(fakeKing, true)) {
					if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;
					else {
						if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
					}
				}
			}

			if (king.x < 8) {
				fakeKing = new Piece(king.x + 1, king.y - 1, "king", color); // casella in basso a dx
				if (!isKingInCheck(fakeKing, true)) {
					if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;
					else {
						if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
					}
				}
			}

		}


		if (king.x < 8) {
			fakeKing = new Piece(king.x + 1, king.y, "king", color); // casella a dx
			if (!isKingInCheck(fakeKing, true)) {
				if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;

				else {
					if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;

				}
			}
		}

		if (king.x > 1) {
			fakeKing = new Piece(king.x - 1, king.y, "king", color); // casella a sx
			if (!isKingInCheck(fakeKing, true)) {
				if (boxes[((8 - fakeKing.y) * 8) + fakeKing.x - 1].current.type.equals("empty")) return false;
				else {
					if (boxes[((8 - fakeKing.y ) * 8) + fakeKing.x - 1].current.owner.equals(color) == false && isPieceInCheck(getPieceFromXY(fakeKing.x, fakeKing.y)) == false) return false;
				}
			}
		}

		return true;

	}

	protected final boolean isKingInCheck(final String color) {
		if (color.equals("white")) return isKingInCheck(pieces[16], false);
		else return isKingInCheck(pieces[17], false);
	}

	protected final boolean isKingInCheck(final String color, final boolean virtualCall) {
		if (color.equals("white")) return isKingInCheck(pieces[16], virtualCall);
		else return isKingInCheck(pieces[17], virtualCall);
		
	}

	protected final boolean isKingInCheck(final Piece king, final boolean virtualCall) {

		final String color = king.owner;
		int nPiecesChecking = 0;

		// controllo la colonna sopra

		for (int y = king.y + 1; y <= 8; y++) {
			final int index = ((8 - y) * 8) + king.x - 1;
			if (!(boxes[index].current.type.equals("empty"))) {

				if (boxes[index].current.owner.equals(color)) {
					break;
				} else if(boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("pony") || boxes[index].current.type.equals("king") || boxes[index].current.type.equals("pawn")) {
					break;
				}
			}


			if (((boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("queen")) && !(boxes[index].current.owner.equals(color)))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x, y);
				nPiecesChecking++;
			} 



		}

		//controllo la colonna sotto
		for (int y = king.y - 1; y >= 1; y--) {
			final int index = ((8 - y) * 8) + king.x - 1;
			if (!(boxes[index].current.type.equals("empty"))) {

				if (boxes[index].current.owner.equals(color)) {
					break;
				} else if(boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("pony") || boxes[index].current.type.equals("king") || boxes[index].current.type.equals("pawn")) {
					break;
				}
			}


			if (((boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("queen")) && !(boxes[index].current.owner.equals(color)))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x, y);
				nPiecesChecking++;
			}




		}

		// controllo la riga a dx
		for (int x = king.x + 1; x <= 8; x++) {
			final int index = ((8 - king.y) * 8) + x - 1;
			if (!(boxes[index].current.type.equals("empty"))) {

				if (boxes[index].current.owner.equals(color)) {
					break;
				} else if(boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("pony") || boxes[index].current.type.equals("king") || boxes[index].current.type.equals("pawn")) {
					break;
				}
			}

			if (((boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("queen")) && !(boxes[index].current.owner.equals(color)))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, king.y);
				nPiecesChecking++;
			} 



		}

		// controllo la riga a sx
		for (int x = king.x - 1; x >= 1; x--) {
			final int index = ((8 - king.y) * 8) + x - 1;

			if (!(boxes[index].current.type.equals("empty"))) {

				if (boxes[index].current.owner.equals(color)) {
					break;
				} else if(boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("pony") || boxes[index].current.type.equals("king") || boxes[index].current.type.equals("pawn")) {
					break;
				}
			}

			if (((boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("queen")) && !(boxes[index].current.owner.equals(color)))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, king.y);
				nPiecesChecking++;
			} 



		}

		// controllo la diag. in alto a dx

		for (int x = king.x + 1, y = king.y + 1; x <= 8; x++, y++) {
			if (y > 7) break;
			final int index = ((8 - y) * 8) + x - 1;
			if (boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("pony")) {
				break;
			} else if ((boxes[index].current.type.equals("queen") || boxes[index].current.type.equals("pawn") || boxes[index].current.type.equals("bishop")) && boxes[index].current.owner.equals(color)) {
				break;
			}
			if (((boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("queen"))) && !(boxes[index].current.owner.equals(color))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);				
				return true;
			} else if (boxes[index].current.type.equals("pawn") && boxes[index].current.owner.equals(color) == false) {
				final int dY = boxes[index].y - king.y, dX = Math.abs(boxes[index].x - king.x);

				if (boxes[index].current.owner.equals("white")) {
					if (dY == -1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				} else {
					if (dY == 1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				}
			} 
		}

		// controllo la diag. in alto a sx

		for (int x = king.x - 1, y = king.y + 1; x >= 1; x--, y++) {
			if (y > 7) break;
			final int index = ((8 - y) * 8) + x - 1;
			if (boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("pony")) {
				break;
			} else if ((boxes[index].current.type.equals("queen") || boxes[index].current.type.equals("pawn") || boxes[index].current.type.equals("bishop")) && boxes[index].current.owner.equals(color)) {
				break;
			}
			if (((boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("queen"))) && !(boxes[index].current.owner.equals(color))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);				
				nPiecesChecking++;
			} else if (boxes[index].current.type.equals("pawn") && boxes[index].current.owner.equals(color) == false) {
				final int dY = boxes[index].y - king.y, dX = Math.abs(boxes[index].x - king.x);
				if (boxes[index].current.owner.equals("white")) {
					if (dY == -1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				} else {
					if (dY == 1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				}

			}
		}



		// controllo la diag. in basso a sx


		for (int x = king.x - 1, y = king.y - 1; x >= 1; x--, y--) {
			if (y < 2) break;
			final int index = ((8 - y) * 8) + x - 1;
			if (boxes[index].current.type.equals("rook") || boxes[((8 - y) * 8) + x - 1].current.type.equals("pony")) {
				break;
			} else if ((boxes[index].current.type.equals("queen") || boxes[index].current.type.equals("pawn") || boxes[index].current.type.equals("bishop")) && boxes[index].current.owner.equals(color)) {
				break;
			}
			if (((boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("queen"))) && !(boxes[index].current.owner.equals(color))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);				
				nPiecesChecking++;
			} else if (boxes[index].current.type.equals("pawn") && boxes[index].current.owner.equals(color) == false) {
				final int dY = boxes[index].y - king.y, dX = Math.abs(boxes[index].x - king.x);
				if (boxes[index].current.owner.equals("white")) {
					if (dY == -1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				} else {
					if (dY == 1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				}

			}
		}


		// controllo la diag. in basso a dx


		for (int x = king.x + 1, y = king.y - 1; x <= 8; x++, y--) {
			if (y < 2) break;
			final int index = ((8 - y) * 8) + x - 1;
			if (boxes[index].current.type.equals("rook") || boxes[index].current.type.equals("pony")) {
				break;
			} else if ((boxes[index].current.type.equals("queen") || boxes[index].current.type.equals("pawn") || boxes[index].current.type.equals("bishop")) && boxes[index].current.owner.equals(color)) {
				break;
			}
			if (((boxes[index].current.type.equals("bishop") || boxes[index].current.type.equals("queen"))) && !(boxes[index].current.owner.equals(color))) {
				if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);
				nPiecesChecking++;
			} else if (boxes[index].current.type.equals("pawn") && boxes[index].current.owner.equals(color) == false) {
				final int dY = boxes[index].y - king.y, dX = Math.abs(boxes[index].x - king.x);
				if (boxes[index].current.owner.equals("white")) {
					if (dY == -1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);						
						nPiecesChecking++;
					}
					else break;
				} else {
					if (dY == 1 && dX == 1) {
						if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(x, y);			
						nPiecesChecking++;
					}
					else break;
				}

			}
		}

		// controllo eventuali cavalli

		if (king.y < 8) { // controllo y+1 x+2 && y+1 x-2
			if (king.x < 7) {
				if (getPieceFromXY(king.x + 2, king.y + 1).type.equals("pony") && !getPieceFromXY(king.x + 2, king.y + 1).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x + 2, king.y + 1);
					nPiecesChecking++;
				}
			}
			if (king.x > 2) {
				if (getPieceFromXY(king.x - 2, king.y + 1).type.equals("pony") && !getPieceFromXY(king.x - 2, king.y + 1).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x - 2, king.y + 1);
					nPiecesChecking++;
				}
			}

		} 

		if (king.y < 7) { // controllo y+2 x+1 && y+2 x-1
			if (king.x < 8) {
				if (getPieceFromXY(king.x + 1, king.y + 2).type.equals("pony") && !getPieceFromXY(king.x + 1, king.y + 2).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x + 1, king.y + 2);
					nPiecesChecking++;
				}
			}
			if (king.x > 1) {
				if (getPieceFromXY(king.x - 1, king.y + 2).type.equals("pony") && !getPieceFromXY(king.x - 1, king.y + 2).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x - 1, king.y + 2);
					nPiecesChecking++;
				}
			}

		}

		if (king.y > 1) { // controllo y-1 x+2 && y-1 x-2
			if (king.x < 7) {
				if (getPieceFromXY(king.x + 2, king.y - 1).type.equals("pony") && !getPieceFromXY(king.x + 2, king.y - 1).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x + 2, king.y - 1);
					nPiecesChecking++;
				}
			}
			if (king.x > 2) {
				if (getPieceFromXY(king.x - 2, king.y - 1).type.equals("pony") && !getPieceFromXY(king.x - 2, king.y - 1).owner.equals(king.owner))  {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x - 2, king.y - 1);
					nPiecesChecking++;
				}
			}
		}

		if (king.y > 2) { // controllo y-2 x+1 && y-2 x-1
			if (king.x < 8) {
				if (getPieceFromXY(king.x + 1, king.y - 2).type.equals("pony") && !getPieceFromXY(king.x + 1, king.y - 2).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x + 1, king.y - 2);
					nPiecesChecking++;
				}
			}
			if (king.x > 1) {
				if (getPieceFromXY(king.x - 1, king.y - 2).type.equals("pony") && !getPieceFromXY(king.x - 1, king.y - 2).owner.equals(king.owner)) {
					if (!virtualCall) pieceWhichIsChecking = getPieceFromXY(king.x - 1, king.y - 2);
					nPiecesChecking++;
				}
			}
		}

		if (nPiecesChecking == 1) {
			return true;
		} else if (nPiecesChecking > 1) {
			doubleCheck = true;
			return true;
		}
		return false;
	}

	protected final boolean isPieceInCheck(final Piece piece) {

		boolean checkedByKing = false;

		// controllo se c'è il re che difende qualcosa (non contemplato in isKingInCheck()

		if (piece.x < 8) checkedByKing = getPieceFromXY(piece.x + 1, piece.y).type.equals("king");

		if (piece.x > 1) checkedByKing = getPieceFromXY(piece.x - 1, piece.y).type.equals("king");


		if (piece.y < 8) checkedByKing = getPieceFromXY(piece.x, piece.y + 1).type.equals("king");

		if (piece.y > 1) checkedByKing = getPieceFromXY(piece.x, piece.y - 1).type.equals("king");




		if (piece.y < 8 && piece.x < 8) checkedByKing = getPieceFromXY(piece.x + 1, piece.y + 1).type.equals("king");


		if (piece.y > 1 && piece.x < 8) checkedByKing = getPieceFromXY(piece.x + 1, piece.y - 1).type.equals("king");


		if (piece.y < 8 && piece.x > 1) checkedByKing = getPieceFromXY(piece.x - 1, piece.y + 1).type.equals("king");


		if (piece.y > 1 && piece.x > 1) checkedByKing = getPieceFromXY(piece.x - 1, piece.y - 1).type.equals("king");


		return isKingInCheck(new Piece(piece.x, piece.y, "king", piece.owner), true) || checkedByKing;
	}

	protected final Piece getPieceFromXY(final int x, final int y) {
		for (Piece p : pieces) {
			if (p.x == x && p.y == y) return p;            
		}

		return new EmptyBox(-64, -64); // mai utilizzato, solo per evitare che la lettura di type generi null pointer exception se ritornasse null
	}


	protected final void capturePiece(final Piece victim, final Piece murderer) {
		if (isGameEnded || waitingForUpdates) return;
		final int victimX = victim.x, victimY = victim.y, murdererX = murderer.x, murdererY = murderer.y;
		
		if (isLegalMove(murderer, victimX, victimY) == false) return;

		if (pieceSelected.type.equals("pawn")) lastMove = new String(lettersArray[murderer.x - 1]);
		else lastMove = new String(murderer.type.substring(0, 1).toUpperCase());


		lastMove = new String(lastMove + "x" + lettersArray[victim.x - 1] + Integer.toString(victim.y));



		murderer.y = victimY; murderer.x = victimX; murderer.selected = false; isAPieceSelected = false; pieceSelected = null;
		final int victimIndex = ((8 - victimY) * 8) + victimX - 1; final int murdererIndex = ((8 - murdererY) * 8) + murdererX - 1;
		boxes[victimIndex].setPiece(murderer); boxes[murdererIndex].setPiece(new EmptyBox(murdererX, murdererY));  

		if (murderer.type.equals("pawn")) {
			if (murderer.firstMove) murderer.firstMove = false;
			if ((murderer.owner.equals("white") && murderer.y == 8) || (murderer.owner.equals("black") && murderer.y == 1)) {
				promote(murderer);
				lastMove+=getPieceFromXY(murderer.x, murderer.y).type.substring(0, 1).toUpperCase();
			}
		}

		victim.x = -64; victim.y = -64;  // per evitare che venga ancora trovato come pezzo sulla scacchiera



		if (currentPlayer.equals("white")) {
			currentPlayer = "black";
			parent.setTitle("Scacchi - Tocca al Nero " + parent.titleSuffixes);
			parent.sb.whiteClock.setActive(false); parent.sb.blackClock.setActive(true);
		} else {
			currentPlayer = "white";
			parent.setTitle("Scacchi - Tocca al Bianco " + parent.titleSuffixes);
			parent.sb.whiteClock.setActive(true); parent.sb.blackClock.setActive(false);
		} 


		if (murderer.type.equals("rook") || murderer.type.equals("king")) {
			if (murderer.neverMoved) murderer.neverMoved = false;   
		}

		if (isStalemate(currentPlayer)) {
			sendData(); isGameEnded = true;
			JOptionPane.showMessageDialog(parent, "Stallo!", "Info", JOptionPane.INFORMATION_MESSAGE);
			return;
		}


		if (isCheckmate(currentPlayer)) {
			lastMove = new String(lastMove + "#");
			sendData(); isGameEnded = true;
			JOptionPane.showMessageDialog(parent, "Scaccomatto! Perde il " + currentPlayer, "Bella mossa", JOptionPane.INFORMATION_MESSAGE);    
			return;
		}
		
		if (isKingInCheck(currentPlayer)) {
			lastMove = new String(lastMove + "+");
			JOptionPane.showMessageDialog(parent, "Scacco al re!", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
		}  

		
		if (murderer.type.equals("rook") || murderer.type.equals("king")) murderer.neverMoved = false; // per arrocco
		castle = false; kingHasBeenMoved = false;
		clearReachableBoxes();
		sendData();	    

	}

	protected final void clearReachableBoxes() {
		for (Box b : boxes) {
			if (b.current.getType().equals("empty")) b.setPiece(new EmptyBox(b.x, b.y));
		}
	}

	protected final void promote(final Piece p) {
		new PromotePawnDialog(parent, chessboard, p);
	}
	
	class Piece extends JButton implements ActionListener {
		public boolean firstMove = true, neverMoved = false;
		private boolean selected = false;		

		public int x, y;

		public final String owner;
		private String type;

		Piece(final int x, final int y, final String type, final String owner) {
			super();
			
			this.x = x; this.y = y; this.owner = owner; this.type = type;
			
			setOpaque(true); updateImage();
			setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(true); addActionListener(this);
			setVisible(true); 
		}
		
		public final void setType(final String newType) {
			this.type = new String(newType);
			updateImage();
		}
		
		public final String getType() {
			return this.type;
		}
		
		public final String getOwner() {
			return this.owner;
		}
		
		public final void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public final boolean isSelected() {
			return this.selected;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isGameEnded || waitingForUpdates) return;
			if (isAPieceSelected == true) {
				if (owner.equals(currentPlayer) == false) {
					capturePiece(this, pieceSelected);
					return;
				}
				else {
					this.selected = true;
					pieceSelected.selected = false;
					pieceSelected = this;
					clearReachableBoxes();
					for (Box b : getReachableBoxes()) b.setPiece(new ReachableBox(b.x, b.y));
				}
			} else {
				if (owner.equals(currentPlayer)) {
					this.selected = true; isAPieceSelected = true;
					pieceSelected = this;
					clearReachableBoxes();
					for (Box b : getReachableBoxes()) b.setPiece(new ReachableBox(b.x, b.y));
				}
			}
		}
		
		public final ArrayList<Box> getReachableBoxes() {
			final ArrayList<Box> reachableBoxes = new ArrayList<Box>();
			
			if (this.isPinned()) return reachableBoxes;
			
			switch (this.type) {
				case "pawn":
					if (this.owner.equals("white")) {
						if (this.firstMove) {
							if (getPieceFromXY(this.x, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + this.x - 1]);
							else break;
							if (getPieceFromXY(this.x, this.y + 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 2)) * 8) + this.x - 1]);
							break;
						}
						if (getPieceFromXY(this.x, this.y + 1).getType().equals("empty")) {
							if (getPieceFromXY(this.x, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + this.x - 1]);
						}
					} else {
						if (this.firstMove) {
							if (getPieceFromXY(this.x, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + this.x - 1]);
							else break;
							if (getPieceFromXY(this.x, this.y - 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 2)) * 8) + this.x - 1]);
							break;
						}
						if (getPieceFromXY(this.x, this.y + 1).getType().equals("empty")) {
							if (getPieceFromXY(this.x, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + this.x - 1]);
						}
					}
					
					break;
				case "rook":
					for (int x = this.x + 1; x <= 8 && getPieceFromXY(x, this.y).type.equals("empty"); x++) reachableBoxes.add(boxes[((8 - this.y) * 8) + x - 1]);
					for (int x = this.x - 1; x >= 1 && getPieceFromXY(x, this.y).type.equals("empty"); x--) reachableBoxes.add(boxes[((8 - this.y) * 8) + x - 1]);
					for (int y = this.y + 1; y <= 8 && getPieceFromXY(this.x, y).type.equals("empty"); y++) reachableBoxes.add(boxes[((8 - y) * 8) + this.x - 1]);
					for (int y = this.y - 1; y >= 1 && getPieceFromXY(this.x, y).type.equals("empty"); y--) reachableBoxes.add(boxes[((8 - y) * 8) + this.x - 1]);
					break;
				
				case "bishop":
					for (int x = this.x + 1, y = this.y + 1; x <= 8 && y <= 8 && getPieceFromXY(x, y).type.equals("empty"); x++, y++) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x - 1, y = this.y + 1; x >= 1 && y <= 8 && getPieceFromXY(x, y).type.equals("empty"); x--, y++) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x + 1, y = this.y - 1; x <= 8 && y >= 1 && getPieceFromXY(x, y).type.equals("empty"); x++, y--) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x - 1, y = this.y - 1; x >= 1 && y >= 1 && getPieceFromXY(x, y).type.equals("empty"); x--, y--) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					break;
				case "queen":
					for (int x = this.x + 1; x <= 8 && getPieceFromXY(x, this.y).type.equals("empty"); x++) reachableBoxes.add(boxes[((8 - this.y) * 8) + x - 1]);
					for (int x = this.x - 1; x >= 1 && getPieceFromXY(x, this.y).type.equals("empty"); x--) reachableBoxes.add(boxes[((8 - this.y) * 8) + x - 1]);
					for (int y = this.y + 1; y <= 8 && getPieceFromXY(this.x, y).type.equals("empty"); y++) reachableBoxes.add(boxes[((8 - y) * 8) + this.x - 1]);
					for (int y = this.y - 1; y >= 1 && getPieceFromXY(this.x, y).type.equals("empty"); y--) reachableBoxes.add(boxes[((8 - y) * 8) + this.x - 1]);
					for (int x = this.x + 1, y = this.y + 1; x <= 8 && y <= 8 && getPieceFromXY(x, y).type.equals("empty"); x++, y++) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x - 1, y = this.y + 1; x >= 1 && y <= 8 && getPieceFromXY(x, y).type.equals("empty"); x--, y++) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x + 1, y = this.y - 1; x <= 8 && y >= 1 && getPieceFromXY(x, y).type.equals("empty"); x++, y--) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					for (int x = this.x - 1, y = this.y - 1; x >= 1 && y >= 1 && getPieceFromXY(x, y).type.equals("empty"); x--, y--) reachableBoxes.add(boxes[((8 - y) * 8) + x - 1]);
					break;
				case "pony":
					if (this.x < 8 && this.y < 7) {
						if (getPieceFromXY(this.x + 1, this.y + 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 2)) * 8) + (this.x + 1) - 1]);
					}
					if (this.x < 8 && this.y > 2) {
						if (getPieceFromXY(this.x + 1, this.y - 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 2)) * 8) + (this.x + 1) - 1]);
					}
					if (this.x < 7 && this.y < 8) {
						if (getPieceFromXY(this.x + 2, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + (this.x + 2) - 1]);
					}
					if (this.x > 2 && this.y > 1) {
						if (getPieceFromXY(this.x - 2, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + (this.x - 2) - 1]);
					}
					if (this.x > 1 && this.y < 7) {
						if (getPieceFromXY(this.x - 1, this.y + 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 2)) * 8) + (this.x - 1) - 1]);
					}
					if (this.x < 7 && this.y > 1) {
						if (getPieceFromXY(this.x + 2, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + (this.x + 2) - 1]);
					}
					if (this.x > 2 && this.y < 7) {
						if (getPieceFromXY(this.x - 2, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + (this.x - 2) - 1]);
					}
					if (this.x > 1 && this.y > 2) {
						if (getPieceFromXY(this.x - 1, this.y - 2).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 2)) * 8) + (this.x - 1) - 1]);
					}
					break;
				
				case "king":
					if (this.x < 8 && getPieceFromXY(this.x + 1, this.y).getType().equals("empty")) reachableBoxes.add(boxes[((8 - this.y) * 8) + (this.x + 1) - 1]);
					if (this.x > 1 && getPieceFromXY(this.x - 1, this.y).getType().equals("empty")) reachableBoxes.add(boxes[((8 - this.y) * 8) + (this.x - 1) - 1]);
					if (this.y < 8 && getPieceFromXY(this.x, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + this.x - 1]);
					if (this.y > 1 && getPieceFromXY(this.x, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + this.x - 1]);
					
					if ((this.x < 8 && this.y < 8) && getPieceFromXY(this.x + 1, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + (this.x + 1) - 1]);
					if ((this.x > 1 && this.y < 8) && getPieceFromXY(this.x - 1, this.y + 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y + 1)) * 8) + (this.x - 1) - 1]);
					if ((this.x < 8 && this.y > 1) && getPieceFromXY(this.x + 1, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + (this.x + 1) - 1]);
					if ((this.x > 1 && this.y > 1) && getPieceFromXY(this.x - 1, this.y - 1).getType().equals("empty")) reachableBoxes.add(boxes[((8 - (this.y - 1)) * 8) + (this.x - 1) - 1]);
					break;
					
				default:
					throw new IllegalArgumentException("Tipo di pezzo non valido!");
			}
			
			
			
			return reachableBoxes;
		}

		public final boolean isPinned() {
			final int n;
			if (this.owner.equals("white")) n = 16;
			else n = 17;
			
			final Piece king = pieces[n];
			
			if (king.x == this.x) { // se sono sulla stessa colonna
				if (this.y > king.y) { // se è più in alto del re
					for (int y = this.y + 1; y <= 8; y++) {
						if (getPieceFromXY(this.x, y).owner.equals(king.owner) == false && (getPieceFromXY(this.x, y).type.equals("rook") || getPieceFromXY(this.x, y).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(this.x, y).type.equals("empty")) continue;
						break;
					}
				} else {
					for (int y = this.y - 1; y >= 1; y--) {
						if (getPieceFromXY(this.x, y).owner.equals(king.owner) == false && (getPieceFromXY(this.x, y).type.equals("rook") || getPieceFromXY(this.x, y).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(this.x, y).type.equals("empty")) continue;
						break;
					}
				}
			} else if (king.y == this.y) { // se sono sulla stessa riga
				if (this.x > king.x) { // se è più a destra del re
					for (int x = this.x + 1; x <= 8; x++) {
						if (getPieceFromXY(x, this.y).owner.equals(king.owner) == false && (getPieceFromXY(x, this.y).type.equals("rook") || getPieceFromXY(x, this.y).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x, this.y).type.equals("empty")) continue;
						break;
					}
				} else {
					for (int x = this.x - 1; x >= 1; x--) {
						if (getPieceFromXY(x, this.y).owner.equals(king.owner) == false && (getPieceFromXY(x, this.y).type.equals("rook") || getPieceFromXY(x, this.y).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x, this.y).type.equals("empty")) continue;
						break;
					}
				}

			} else if (Math.abs(king.y - this.y) == Math.abs(king.x - this.x)) { // se sono sulla stessa diagonale
				if (this.y > king.y && this.x > king.x) { // se il pezzo è in alto a dx
					for (int x2 = this.x + 1, y2 = this.y + 1; x2 <= 8 && y2 <= 8; x2++, y2++) {
						if (getPieceFromXY(x2, y2).owner.equals(king.owner) == false && (getPieceFromXY(x2, y2).type.equals("bishop") || getPieceFromXY(x2, y2).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x2, y2).type.equals("empty")) continue;
						break;
					}
					
				} else if (this.y > king.y && this.x < king.x) { // se il pezzo è in alto a sx
					for (int x2 = this.x - 1, y2 = this.y + 1; x2 >= 1 && y2 <= 8; x2--, y2++) {
						if (getPieceFromXY(x2, y2).owner.equals(king.owner) == false && (getPieceFromXY(x2, y2).type.equals("bishop") || getPieceFromXY(x2, y2).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x2, y2).type.equals("empty")) continue;
						break;
					}
					
				} else if (this.y < king.y && this.x > king.x) { // se il pezzo è in basso a dx
					for (int x2 = this.x + 1, y2 = this.y - 1; x2 <= 8 && y2 >= 1; x2++, y2--) {
						if (getPieceFromXY(x2, y2).owner.equals(king.owner) == false && (getPieceFromXY(x2, y2).type.equals("bishop") || getPieceFromXY(x2, y2).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x2, y2).type.equals("empty")) continue;
						break;
					}
					
				} else if (this.y < king.y && this.x < king.x) { // se il pezzo è in basso a sx
					for (int x2 = this.x - 1, y2 = this.y - 1; x2 >= 1 && y2 >= 1; x2--, y2--) {
						if (getPieceFromXY(x2, y2).owner.equals(king.owner) == false && (getPieceFromXY(x2, y2).type.equals("bishop") || getPieceFromXY(x2, y2).type.equals("queen"))) {
							return true;
						}
						if (getPieceFromXY(x2, y2).type.equals("empty")) continue;
						break;
					}
					
				}
				
			}
			
			return false;
		}


		public void updateImage() {
			try {
				final Image icon = ImageIO.read(getClass().getResource("/images/" + owner + "-" + type + ".png"));
				final ImageIcon ii = new ImageIcon(icon.getScaledInstance((int)(48 * currentScaling), (int)(48 * currentScaling), Image.SCALE_SMOOTH));
				this.setIcon(ii); parent.revalidate(); parent.pack();
			} catch (Exception ex) {
				System.err.println("[ERROR]: unable to read piece icon (" + ex.getMessage() + ")");
			}

		}
	}

	final class Box extends JPanel {		
		public final int x, y;
		
		private Piece current;
		
		private final String color;

		private final BorderLayout layout = new BorderLayout(0, 0);

		Box(final String color, final int x, final int y) {
			super();
			setLayout(layout); 
			if (color.equals("white")) setBackground(Color.lightGray);
			else setBackground(Color.gray);
			setOpaque(true); 
			this.color = color; this.x = x; this.y = y;

			current = new EmptyBox(x, y);
			add(current, BorderLayout.CENTER);
		}
		
		public final String getColor() {
			return this.color;
		}

		public final void setPiece(final Piece piece) {
			this.removeAll();
			add(piece, BorderLayout.CENTER);
			this.revalidate(); this.repaint();
			current = piece;
		}
	}
	
	final class ReachableBox extends EmptyBox {
		ReachableBox(final int x, final int y) {
			super(x, y);
			setText("●"); setFont(new Font("Arial", Font.PLAIN, 25));
		}
	}
	
	

	

	class EmptyBox extends Piece {

		EmptyBox(final int x, final int y) {
			super(x, y, "empty", "yellow");
		}
		
		@Override
		public void updateImage() {}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isGameEnded || waitingForUpdates) return;
			if (isAPieceSelected == false) return;
			
			if (isLegalMove(pieceSelected, this.x, this.y) == false) return;

			boxes[(8 * (8 - pieceSelected.y)) + pieceSelected.x - 1].setPiece(new EmptyBox(pieceSelected.x, pieceSelected.y));
			final int oldX = pieceSelected.x; final int oldY = pieceSelected.y; pieceSelected.x = this.x; pieceSelected.y = this.y;
			boxes[(8 * (8 - pieceSelected.y)) + pieceSelected.x - 1].setPiece(pieceSelected);

			if (kingHasBeenMoved) {
				if (Math.abs(pieces[16].y - pieces[17].y) <= 1 && Math.abs(pieces[16].x - pieces[17].x) <= 1) {
					pieceSelected.x = oldX; pieceSelected.y = oldY;
					boxes[(8 * (8 - pieceSelected.y)) + pieceSelected.x - 1].setPiece(pieceSelected);
					boxes[(8 * (8 - this.y)) + this.x - 1].setPiece(new EmptyBox(this.x, this.y));
					JOptionPane.showMessageDialog(parent, "Mossa irregolare!", "Attenzione", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			if (isKingInCheck(currentPlayer) && kingHasBeenMoved == false) {                    
				pieceSelected.x = oldX; pieceSelected.y = oldY;
				boxes[(8 * (8 - pieceSelected.y)) + pieceSelected.x - 1].setPiece(pieceSelected);
				boxes[(8 * (8 - this.y)) + this.x - 1].setPiece(new EmptyBox(this.x, this.y));
				JOptionPane.showMessageDialog(parent, "Mossa irregolare!", "Attenzione", JOptionPane.WARNING_MESSAGE);                    
				return;
			}          

			if (pieceSelected.type.equals("pawn")) {
				if (pieceSelected.firstMove) pieceSelected.firstMove = false;
				if ((pieceSelected.owner.equals("white") && pieceSelected.y == 8) || (pieceSelected.owner.equals("black") && pieceSelected.y == 1)) {
					promote(pieceSelected);
					lastMove += getPieceFromXY(pieceSelected.x, pieceSelected.y).type.substring(0, 1).toUpperCase();
				}
			}

			if (pieceSelected.type.equals("rook") || pieceSelected.type.equals("king")) {
				if (pieceSelected.neverMoved) pieceSelected.neverMoved = false;
			} 

			if (pieceSelected.type.equals("rook") || pieceSelected.type.equals("king")) pieceSelected.neverMoved = false;

			if (castle == false) {
				if (pieceSelected.type.equals("pawn")) lastMove = new String("");
				else lastMove = new String(pieceSelected.type.substring(0, 1).toUpperCase());


				lastMove = new String(lastMove + lettersArray[this.x - 1] + Integer.toString(this.y));

			}

			if (currentPlayer.equals("white")) {
				currentPlayer = "black";
				parent.setTitle("Scacchi - Tocca al Nero " + parent.titleSuffixes);
				parent.sb.whiteClock.setActive(false); parent.sb.blackClock.setActive(true);
			} else {
				currentPlayer = "white";
				parent.setTitle("Scacchi - Tocca al Bianco " + parent.titleSuffixes);
				parent.sb.whiteClock.setActive(true); parent.sb.blackClock.setActive(false);
			} 

			if (isStalemate(currentPlayer)) {
				JOptionPane.showMessageDialog(parent, "Stallo!", "Info", JOptionPane.INFORMATION_MESSAGE);
				sendData();
				isGameEnded = true;
				return;
			}

			if (isCheckmate(currentPlayer)) {
				lastMove = new String(lastMove + "#"); 	sendData(); isGameEnded = true;	
				JOptionPane.showMessageDialog(parent, "Scaccomatto! Perde il " + currentPlayer, "Bella mossa", JOptionPane.INFORMATION_MESSAGE);		
				return;
			}



			if (isKingInCheck(currentPlayer, true)) {
				lastMove = new String(lastMove + "+");
				JOptionPane.showMessageDialog(parent, "Scacco al re!", "Bella mossa", JOptionPane.INFORMATION_MESSAGE);
			} 
			

			kingHasBeenMoved = false; castle = false; pieceSelected.selected = false; pieceSelected = null; isAPieceSelected = false;
			clearReachableBoxes();
			sendData();              

		}

	}
	

	public void sendData() {
		logLastMove();
	}

	protected void logLastMove() {
		final DefaultTableModel model = (DefaultTableModel) parent.sb.moves.getModel();
		if (currentPlayer.equals("black")) {
			model.addRow(new Object[]{model.getRowCount() + 1, lastMove, ""});
		} else {
			final String previousMove = (String) model.getValueAt(model.getRowCount() - 1, 1);
			model.removeRow(model.getRowCount() - 1);
			model.addRow(new Object[]{model.getRowCount() + 1, previousMove, lastMove});
		}
		lastMove = new String("");

	}

	@Override
	public void componentResized(ComponentEvent e) {
		currentScaling = (float) this.getWidth() / 500;	
		parent.pack();
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
