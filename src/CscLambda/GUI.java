package CscLambda;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;


import CscLambda.RegionGraph.Partition;
import CscLambda.RegionGraph.Region;
import CscLambda.Level;
import CscLambda.Util.Point;
import CscLambda.Util.Solution;

public class GUI { //Simple, hacked-together GUI to visualize pieces of the RegionGraph solution process
	
	Frame frame;
	BufferStrategy buffer;
	StringBuilder moves = new StringBuilder();
	Solution solution = null;
	boolean solved = false;
	Level level;
	Point start = null;
	
	int rnum = 0;
	
	final int WIDTH = 950;
	final int HEIGHT = 950;
	final int XOS = 3;
	final int YOS = 23;
	
	public GUI(Level l){
		level = l;
		frame = new Frame();
		frame.setSize(WIDTH+XOS,HEIGHT+YOS);
		frame.setVisible(true);
		frame.createBufferStrategy(2);
		frame.setResizable(false);
		
		buffer = frame.getBufferStrategy();
		
		frame.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
			
		});
		frame.addKeyListener(new KeyListener(){
			public void keyReleased(KeyEvent arg0) {}
			public void keyTyped(KeyEvent k) {}
			public void keyPressed(KeyEvent k) {
				if(k.getKeyCode() == KeyEvent.VK_UP){
					move('U');
				}else if(k.getKeyCode() == KeyEvent.VK_DOWN){
					move('D');
				}else if(k.getKeyCode() == KeyEvent.VK_LEFT){
					move('L');
				}else if(k.getKeyCode() ==KeyEvent.VK_RIGHT){
					move('R');
				}else if(k.getKeyCode() == KeyEvent.VK_DELETE){
					if(moves.length()>0)moves.deleteCharAt(moves.length()-1);
					else start = null;
				}else if(k.getKeyCode() ==KeyEvent.VK_ESCAPE){
					moves.delete(0, moves.length());
					start = null;
				}else if(k.getKeyCode() == KeyEvent.VK_ENTER){
					Solution result = new Solution();
					result.l = level;
					result.path = moves.toString();
					result.solvetime = 0;
					result.start = start;
					solution = result;
					solved = true;
				}else if(k.getKeyCode() == KeyEvent.VK_X){
					rnum++;
				}else if(k.getKeyCode() == KeyEvent.VK_Z){
					rnum--;
				}
				update();
			}

			
			
		});
		frame.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent m) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent m) {
				if(start!=null)return;
				int tiledim = (int) Math.floor(Math.min(WIDTH/level.width, HEIGHT/level.height));
				
				int x = m.getX() - XOS;
				int y = m.getY() - YOS;
				Point n = new Point((int)Math.min(Math.floor(x/tiledim),level.width-1), (int)Math.min(Math.floor(y/tiledim), level.height-1) );
				if(!level.contents[n.x][n.y])start = n;
				update();
				
			}
			
			
		});
		
		frame.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent arg0) {
				update();
			}
			public void focusLost(FocusEvent e) {
				update();
			}
			
		});
		
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		update();
	}
	
	static final Color PATH_COLOR = new Color(1f,0.5f,0f);
	static final Color BLOCK_COLOR = Color.lightGray;
	
	
	
	public void update(){ //Re-renders the GUI
		int tiledim = (int) Math.floor(Math.min(WIDTH/level.width, HEIGHT/level.height)); //Draw map
		Graphics g = buffer.getDrawGraphics();
		g.translate(XOS, YOS);
		g.setColor(BLOCK_COLOR);
		g.drawRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.darkGray);
		for(int x = 0; x<level.width; x++)for(int y = 0; y<level.height; y++){
			if(level.contents[x][y]) g.setColor(Color.gray);
			else g.setColor(Color.white);
			g.fillRect(x*tiledim, y*tiledim, tiledim, tiledim);
			g.setColor(Color.black);
			g.drawRect(x*tiledim, y*tiledim, tiledim, tiledim);
		}
		
		if(start == null){
			buffer.show();
			return;
		}

		g = buffer.getDrawGraphics();
		tiledim = (int) Math.floor(Math.min(WIDTH/level.width, HEIGHT/level.height)); //Draw map
		g.translate(XOS, YOS);
		
		
		Level slv = level.clone();  //Simulate all moves
		Point position = start.clone();
		
		g.setColor(PATH_COLOR);
		g.fillRect(start.x*tiledim, start.y*tiledim, tiledim, tiledim);
		g.setColor(Color.white);
		g.fillOval(start.x*tiledim + tiledim/3, start.y*tiledim + tiledim/3, tiledim/3, tiledim/3);
		g.setColor(Color.black);
		g.drawRect(start.x*tiledim, start.y*tiledim, tiledim, tiledim);
		slv.contents[start.x][start.y] = true;
		g.setColor(Color.white);
		
		
		
		for(int i = 0; i<moves.length(); i++){
			char direction = moves.charAt(i);
			int x = position.x;
			int y = position.y;
			boolean moved = false;
			
			while(true){
				if(direction=='U' && y>0 && !slv.contents[x][y-1]){ 
					g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim/2 , y*tiledim);
					y--;  //Determine if there can be a move in the direction, then step it forward if possible
				
				}
				else if(direction=='D' && y<slv.height-1 && !slv.contents[x][y+1]){
					g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim/2 , y*tiledim + tiledim);
					y++;
				}
				else if(direction=='R' && x<slv.width-1 && !slv.contents[x+1][y]){
					g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim , y*tiledim + tiledim/2);
					x++;
				}
				else if(direction=='L'&& x>0 && !slv.contents[x-1][y]){
					g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim, y*tiledim + tiledim/2);
					x--;
				}else{
					g.setColor(PATH_COLOR);
					g.fillRect(x*tiledim, y*tiledim, tiledim, tiledim);
					g.setColor(Color.black);
					g.drawRect(x*tiledim, y*tiledim, tiledim, tiledim);
					g.setColor(Color.white);
					if(!moved && i>1)direction = moves.charAt(i-1);
					if(direction=='U')g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim/2 , y*tiledim + tiledim);
					if(direction=='D')g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim/2 , y*tiledim);
					if(direction=='R')g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim, y*tiledim + tiledim/2);
					if(direction=='L')g.drawLine(x*tiledim + tiledim/2, y*tiledim + tiledim/2, x*tiledim + tiledim , y*tiledim + tiledim/2);
					break;
				}

				moved = true; //If it completes a step, the move is a success
				slv.contents[x][y] = true;
				
				g.setColor(PATH_COLOR);
				g.fillRect(x*tiledim, y*tiledim, tiledim, tiledim);
				g.setColor(Color.black);
				g.drawRect(x*tiledim, y*tiledim, tiledim, tiledim);
				
				g.setColor(Color.white);
				if(direction == 'U' || direction == 'D')g.drawLine(x*tiledim + tiledim/2, y*tiledim, x*tiledim + tiledim/2, y*tiledim + tiledim);
				else g.drawLine(x*tiledim, y*tiledim + tiledim/2, x*tiledim + tiledim , y*tiledim + tiledim/2);
				
			}
			
			if(!moved){ //trim illegal moves
				moves.delete(i, moves.length()-1);
				break;
			}
			
			position.x = x;
			position.y = y;
			
		}
		
		RegionGraph pg = new RegionGraph(slv);
		pg.findRegions();
		//ArrayList<PartialSolution> pst = pg.partialSolve();
		
		for(Region r : pg.regions)	
		if(r.exits.length == rnum)for(Partition p: r.area){
			((Graphics2D)g).setStroke(new BasicStroke(3));
			g.setColor(Color.black);
			g.drawRect(p.x*tiledim, p.y*tiledim, tiledim*p.w, tiledim*p.h);
			g.setColor(new Color(0f,0.5f,0.5f,0.2f));
			g.fillRect(p.x*tiledim, p.y*tiledim, tiledim*p.w, tiledim*p.h);
		}
			
		
		buffer.show();
	}
	
	public void move(char d){
		char last = ' ';
		if(moves.length()>0)last = moves.charAt(moves.length()-1);
		if(last == d)return;
		if(last == 'U' && d == 'D')return;
		if(last == 'D' && d == 'U')return;
		if(last == 'R' && d == 'L')return;
		if(last == 'L' && d == 'R')return;
		
		moves.append(d);
	}
	
	public void destroy(){
		frame.dispose();
	}
	public void finalize(){
		destroy();
	}
	
	

	
	

}
