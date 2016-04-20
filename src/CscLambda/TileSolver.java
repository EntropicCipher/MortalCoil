package CscLambda;

import java.util.ArrayList;

import CscLambda.Util.Point;
import CscLambda.Util.Solution;

public class TileSolver{ 
	//Somewhat brute-force tile-by-tile search pattern. Uses multithreading. Gets to about level ~90 before it becomes impractical
	
	static final char[] directions = {'U', 'D', 'R', 'L' };
	
	Level l;
	final int THREADS;
	
	static class Move{
		Level state;
		Point position = new Point();
		int moves = 0;
	}
	
	public TileSolver(Level l, int threads){
		THREADS = threads;
		this.l = l;
	}
	
	static Move simulateMove(Move previous, char direction){
		Level result = previous.state.clone(); //Break references to the previous state
		
		int x = previous.position.x;
		int y = previous.position.y;
		boolean moved = false;
		
		while(true){
			if(direction=='U' && y>0 && !result.contents[x][y-1]) y--;  //Determine if there can be a move in the direction, then step it forward if possible
			else if(direction=='D' && y<result.height-1 && !result.contents[x][y+1]) y++;
			else if(direction=='R' && x<result.width-1 && !result.contents[x+1][y]) x++;
			else if(direction=='L'&& x>0 && !result.contents[x-1][y]) x--;
			else break;

			moved = true; //If it completes a step, the move is a success
			result.contents[x][y] = true;
		}
		
		if(!moved)return null; //No point in returning the same state
		
		Move thismove = new Move();
		thismove.position.x = x;
		thismove.position.y = y;
		thismove.state = result;
		
		return thismove;
	}

	Solution solve(){
		long starttime = System.currentTimeMillis(); //Log solve time
		ArrayList<Point> startpoints = getOrderedStartPoints(l);
		
		ArrayList<ArrayList<Point>> threadlists = new ArrayList<ArrayList<Point>>();
		for(int i = 0; i<THREADS; i++)threadlists.add(new ArrayList<Point>());
		for(int i = 0; i<startpoints.size(); i++)threadlists.get(i%THREADS).add(startpoints.get(i));
		
		
		SolverThread[] sthreads = new SolverThread[THREADS];
		Thread[] tthreads = new Thread[THREADS];
		
		for(int i = 0; i<THREADS; i++){ //Starts threads
			sthreads[i] = new SolverThread(l, threadlists.get(i));
			tthreads[i] = new Thread(sthreads[i]);
			tthreads[i].start();
		}
		
		Solution s = null;
		
		while(s == null){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			boolean allaredead = true;
			for(int i = 0; i<THREADS; i++){ //Check threads to see if any have solved
				boolean isdead = (!tthreads[i].isAlive());
				if(!isdead)allaredead = false;
				if(isdead && sthreads[i].s!=null){
					s = sthreads[i].s;
					break;
				}
			}
			if(allaredead)break; //Breaks if all threads have finished
			if(s != null)for(SolverThread t: sthreads)t.running = false; //Stops threads if one has found a solution
		}
		
		for(Thread t: tthreads) //Waits for all threads to die
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		if(s == null)return null;
		
		s.solvetime = (float)(System.currentTimeMillis()-starttime) / 1000f;
		return s;
	}
	
	static Solution solveFromStartPoint(Level l, Point start){
		Move firstmove = new Move();
		firstmove.state = l.clone();
		firstmove.state.contents[start.x][start.y] = true;
		firstmove.position = start;
		ArrayList<Move> movestack = new ArrayList<Move>();
		movestack.add(firstmove);
		StringBuilder moves = new StringBuilder();
		boolean solved = false;
		
		while(true){
			if(movestack.isEmpty())break;
			Move lastmove = movestack.get(movestack.size()-1);
			if(lastmove.moves == 4){ 
				movestack.remove(movestack.size()-1);
				try{
					moves.deleteCharAt(moves.length()-1);							
				}catch(Exception e){
					break;
				}
				continue;
			}
			
			Move nextmove = simulateMove(lastmove, directions[lastmove.moves]);
			if(nextmove!=null){
				if(!isDeadEnd(nextmove)){
					moves.append(directions[lastmove.moves]);
					movestack.add(nextmove);
				}
				if(nextmove.state.isSolved()){
					moves.append(directions[lastmove.moves]);
					solved = true;
					break;
				}
			}
			lastmove.moves++;
		}
		if(solved){
			Solution s = new Solution();
			s.start = start;
			s.path = moves.toString();
			return s;
		}
		return null;
	}

	static ArrayList<Point> getOrderedStartPoints(Level l){
		ArrayList<Point> startpoints = new ArrayList<Point>();
		
		int corner = 0;
		int shell = 0;
		
		int ssx = 0;
		int ssy = 0;
		
		if(!l.contents[0][0])startpoints.add(new Point(0,0));
		for(int i = 0; i<l.width*l.height; i++){
			do{
				if(corner == 0 || corner == 2){
					ssy--;
					ssx++;
				}else if(corner == 1 || corner == 3){
					ssy++;
					ssx++;
				}
			}while(containsPoint(startpoints, ssx, ssy) );
			if(! (ssx>=0 && ssy>=0 && ssx<l.width && ssy<l.height)){
				corner++;
				if(corner == 4){
					corner = 0;
					shell++;
				}
				if(corner == 0){
					ssx = 0;
					ssy = shell;
				}
				if(corner == 1){
					ssx = l.width-shell-1;
					ssy = 0;
				}
				if(corner == 2){
					ssx = l.width-shell-1 ;
					ssy = l.height-1;
				}
				if(corner == 3){
					ssx = 0;
					ssy = l.height-shell-1;
				}
			}
			
			try{
				if(!l.contents[ssx][ssy]){
					startpoints.add(new Point(ssx, ssy));
				}else{
					i--;
				}
			}catch(IndexOutOfBoundsException e){
				break;
			}
			
		}
		
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++)if((!l.contents[x][y]) && !containsPoint(startpoints,x,y))startpoints.add(new Point(x,y));
		
		ArrayList<Point> first = new ArrayList<Point>();
		ArrayList<Point> second = new ArrayList<Point>();
		ArrayList<Point> third = new ArrayList<Point>();
		ArrayList<Point> last = new ArrayList<Point>();
		
		for(Point p: startpoints){
			int neighbors = 0;
			if((p.y<(l.height-1) && l.contents[p.x][p.y+1]) ||p.y==(l.height-1))neighbors++;
			else if((p.x<(l.width-1) && l.contents[p.x+1][p.y]) ||p.x==(l.width-1))neighbors++;
			else if((p.y>0 && l.contents[p.x][p.y-1]) || p.y == 0)neighbors++;
			else if((p.x>0 && l.contents[p.x-1][p.y])|| p.x == 0)neighbors++;
			
			if(neighbors == 1)first.add(p);
			else if(neighbors == 0)second.add(p);
			else if(neighbors == 2)third.add(p);
			else if(neighbors == 3)last.add(p);
		}
		
		startpoints.clear();
		startpoints.addAll(first);
		startpoints.addAll(second);
		startpoints.addAll(third);
		startpoints.addAll(last);
		
		return startpoints;
	}
	
	public static boolean isDeadEnd(Move m){
		Level l = m.state;
		Point pos = m.position;
		
		int ends = 0; //Check for simple dead ends
		l.contents[pos.x][pos.y] = false;
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++){
			if(l.contents[x][y] || new Point(x,y).equals(pos))continue;
			int neighbors = 0;
			if((y<(l.height-1) && l.contents[x][y+1]) ||y==(l.height-1))neighbors++;
			else if((x<(l.width-1) && l.contents[x+1][y]) ||x==(l.width-1))neighbors++;
			else if((y>0 && l.contents[x][y-1]) || y == 0)neighbors++;
			else if((x>0 && l.contents[x-1][y])|| x == 0)neighbors++;
			if(neighbors == 3)ends++;
			if(ends>1 || neighbors == 4)return true;
		}
		l.contents[pos.x][pos.y] = true;
		
		ArrayList<Point> marked = new ArrayList<Point>(); //Check for divisions
		if(pos.y<(l.height-1) && !l.contents[pos.x][pos.y+1])marked.add(new Point(pos.x, pos.y+1));
		else if(pos.x<(l.width-1) && !l.contents[pos.x+1][pos.y])marked.add(new Point(pos.x+1, pos.y));
		else if(pos.y>0 && !l.contents[pos.x][pos.y-1])marked.add(new Point(pos.x, pos.y-1));
		else if(pos.x>0 && !l.contents[pos.x-1][pos.y])marked.add(new Point(pos.x-1, pos.y));
		if(marked.size() == 0){
			return true;
		}
		
		int[][] squaremarks = new int[l.width][l.height];
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++){
			squaremarks[x][y] = (l.contents[x][y]?1:0);
		}
		
		Point first = marked.get(0);
		squaremarks[first.x][first.y] = 2;

		int checkindex = 0;
		
		while(true){
			Point checkpoint = marked.get(checkindex);
			
			Point cpu = new Point(checkpoint.x, checkpoint.y-1);
			Point cpd = new Point(checkpoint.x, checkpoint.y+1);
			Point cpl = new Point(checkpoint.x-1, checkpoint.y);
			Point cpr = new Point(checkpoint.x+1, checkpoint.y);
			
			if(cpu.y>=0 && !l.contents[cpu.x][cpu.y]){
				
				if(squaremarks[cpu.x][cpu.y] == 0){
					marked.add(cpu);
					squaremarks[cpu.x][cpu.y] = 2;
				}
			}
			if(cpd.y<l.height  && !l.contents[cpd.x][cpd.y]){
				if(squaremarks[cpd.x][cpd.y] == 0){
					marked.add(cpd);
					squaremarks[cpd.x][cpd.y] = 2;
				}
			}
			if(cpl.x>=0  && !l.contents[cpl.x][cpl.y]){
				if(squaremarks[cpl.x][cpl.y] == 0){
					marked.add(cpl);
					squaremarks[cpl.x][cpl.y] = 2;
				}
			}
			if(cpr.x<l.width  && !l.contents[cpr.x][cpr.y]){
				if(squaremarks[cpr.x][cpr.y] == 0){
					marked.add(cpr);
					squaremarks[cpr.x][cpr.y] = 2;
				}
			}
			if(checkindex >= marked.size()-1)break;
			
			checkindex++;
		}
			
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++){
			
			if(squaremarks[x][y] == 0){
				return true;
			}
			
		}
		return false;
	}
	
	static boolean containsPoint(ArrayList<Point> points, int x, int y){
		boolean found = false;
		for(Point p: points)if(p.x == x && p.y == y){
			found = true;
			break;
		}
		return found;
	}
	
	static class SolverThread implements Runnable{
		
		Level l;
		ArrayList<Point> startpoints;
		Solution s = null;
		boolean running = true;
		
		final static int width = 50;
		static int points = 0;
		
		static void printPoint(){
			System.out.print(".");
			points++;
			if(points >= width){
				System.out.println();
				points = 0;
			}
		}
		
		public SolverThread(Level lv, ArrayList<Point> s){
			l = lv;
			startpoints = s;
		}
		
		public void run(){
			for(Point start: startpoints){
				s = solveFromStartPoint(l, start);
				if(s!=null || !running)break;
				printPoint();
			}
			
		}
	}
}