
package CscLambda;


import java.util.ArrayList;

import CscLambda.RegionGraph.Partition;
import CscLambda.RegionGraph.Region;
import CscLambda.Util.Point;

public class PartialSolver { 
	//Generates all possible solutions to each region, then returns them to the RegionGraphSolver 
	//where dead ends and conflicting paths will be removed and the remaining solutions will be weaved together
	
	//This file is currently broken, as the new "PartialLevelState" object is being implemented TODO
	
	public static ArrayList<PartialSolution> solveRegion(Region r, RegionGraph g){ 
		
		PartialLevel l = isolateRegion(r, g);
		
		ArrayList<PartialSolution> solutions = new ArrayList<PartialSolution>();
		
		for(int ei = 0; ei<r.exits.length; ei++){
			
			Point start = r.exits[ei];
			char startDir = r.exitOrientations[ei];
			
			if(startDir == 'U'|| startDir == 'D')startDir = 'L';
			else startDir = 'U';
			
			ArrayList<PartialMove> movestack = new ArrayList<PartialMove>();
			
			PartialMove firstmove = new PartialMove();
			firstmove.state = l.state.clone();
			firstmove.state.tiles[start.x - l.x][start.y - l.y] = true;
			firstmove.position = new Point(start.x - l.x, start.y - l.y);
			
			firstmove.direction = startDir;
			firstmove.axis = (startDir == 'U' || startDir == 'D') ? PartialMove.VERTICAL : PartialMove.HORIZONTAL;
			movestack.add(firstmove);
			
			StringBuilder moves = new StringBuilder();
			
			while(true){
				if(movestack.isEmpty())break; 
				
				PartialMove lastmove = movestack.get(movestack.size()-1);
				
				if(lastmove.movedTwice){ 
					movestack.remove(movestack.size()-1);
					try{
						moves.deleteCharAt(moves.length()-1);							
					}catch(Exception e){
						break;
					}
					continue;
				}
				
				PartialMove nextmove;
				if(lastmove.axis == PartialMove.HORIZONTAL)nextmove = simulateMove(lastmove, lastmove.movedOnce ? 'U' : 'D');
				else nextmove = simulateMove(lastmove, lastmove.movedOnce ? 'R' : 'L');
				
				if(!lastmove.movedOnce)lastmove.movedOnce = true;
				else lastmove.movedTwice = true;
				
				if(nextmove!=null){
					if(!isDeadEnd(nextmove)){
						movestack.add(nextmove);
						moves.append(nextmove.direction);
					}
					
					Point pos = nextmove.position;
					if(nextmove.state.isSolved() && nextmove.state.exits[pos.x][pos.y]){
						movestack.add(nextmove);
						moves.append(nextmove.direction);
						PartialSolution ps = new PartialSolution();
						ps.endPoint = new Point(nextmove.position.x + l.x, nextmove.position.y + l.y);
						ps.startPoint = start;
						ps.moves = moves.toString();
						ps.area = new ArrayList<Region>();
						ps.area.add(r);
						
						
						PartialMove[] mvs = new PartialMove[movestack.size()];
						for(int i = 0; i<movestack.size(); i++)mvs[i] = movestack.get(i);
						
						
						
						int exitindex = -1;
						for(int i = 0; i<r.exits.length; i++){
							Point p = r.exits[i];
							if(p.x == ps.endPoint.x && p.y == ps.endPoint.y){
								exitindex = i;
								break;
							}
						}
						solutions.add(ps);
					}
				}
			}
			
		}
		return solutions;
		
	}
	
	static PartialMove simulateMove(PartialMove previous, char direction){
		
		PartialLevelState result = previous.state.clone(); //Break references to the previous state
		
		int x = previous.position.x;
		int y = previous.position.y;
		boolean moved = false;
		
		while(true){
			if(direction=='U' && y>0 && !result.tiles[x][y-1] ) y--;  //Determine if there can be a move in the direction, then step it forward if possible
			else if(direction=='D' && y<result.height-1 && !result.tiles[x][y+1] ) y++;
			else if(direction=='R' && x<result.width-1 && !result.tiles[x+1][y]) x++;
			else if(direction=='L'&& x>0 && !result.tiles[x-1][y] ) x--;
			else break;

			moved = true; //If it completes a step, the move is a success
			result.tiles[x][y] = true;
		}
		
		if(!moved)return null; //No point in returning the same state
		
		PartialMove thismove = new PartialMove();

		thismove.position = new Point(x,y);
		thismove.direction = direction;
		thismove.axis = (direction == 'U' || direction == 'D') ? PartialMove.VERTICAL : PartialMove.HORIZONTAL;
		thismove.state = result;
		
		return thismove;
	}
	
	
	public static boolean isDeadEnd(PartialMove m){ 
		PartialLevelState l = m.state;
		Point pos = m.position;
		
		ArrayList<Point> marked = new ArrayList<Point>(); //Check for divisions
		if(pos.y<(l.height-1) && !(l.tiles[pos.x][pos.y+1] || l.exits[pos.x][pos.y+1]))marked.add(new Point(pos.x, pos.y+1));
		else if(pos.x<(l.width-1) && !(l.tiles[pos.x+1][pos.y] || l.exits[pos.x+1][pos.y]))marked.add(new Point(pos.x+1, pos.y));
		else if(pos.y>0 && !(l.tiles[pos.x][pos.y-1] || l.exits[pos.x][pos.y-1]))marked.add(new Point(pos.x, pos.y-1));
		else if(pos.x>0 && !(l.tiles[pos.x-1][pos.y] || l.exits[pos.x-1][pos.y]))marked.add(new Point(pos.x-1, pos.y));
		
		else if(pos.y<(l.height-1) && !l.tiles[pos.x][pos.y+1])marked.add(new Point(pos.x, pos.y+1));
		else if(pos.x<(l.width-1) && !l.tiles[pos.x+1][pos.y])marked.add(new Point(pos.x+1, pos.y));
		else if(pos.y>0 && !l.tiles[pos.x][pos.y-1])marked.add(new Point(pos.x, pos.y-1));
		else if(pos.x>0 && !l.tiles[pos.x-1][pos.y])marked.add(new Point(pos.x-1, pos.y));
		if(marked.size() == 0){ 
			
			return true;
		}
		
		int[][] squaremarks = new int[l.width][l.height];
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++){
			squaremarks[x][y] = ((l.tiles[x][y] || l.exits[x][y]) ?1:0);
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
			
			if(cpu.y>=0 && !l.tiles[cpu.x][cpu.y]){
				
				if(squaremarks[cpu.x][cpu.y] == 0){
					marked.add(cpu);
					squaremarks[cpu.x][cpu.y] = 2;
				}
			}
			if(cpd.y<l.height  && !l.tiles[cpd.x][cpd.y]){
				if(squaremarks[cpd.x][cpd.y] == 0){
					marked.add(cpd);
					squaremarks[cpd.x][cpd.y] = 2;
				}
			}
			if(cpl.x>=0  && !l.tiles[cpl.x][cpl.y]){
				if(squaremarks[cpl.x][cpl.y] == 0){
					marked.add(cpl);
					squaremarks[cpl.x][cpl.y] = 2;
				}
			}
			if(cpr.x<l.width  && !l.tiles[cpr.x][cpr.y]){
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
	
	static PartialLevel isolateRegion(Region r, RegionGraph g){
		PartialLevel result = new PartialLevel();
		result.state = new PartialLevelState();

		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		
		for(Partition p: r.area){
			minx = Math.min(minx, p.x);
			miny = Math.min(miny, p.y);
			maxx = Math.max(maxx, p.x+p.w-1);
			maxy = Math.max(maxy, p.y+p.h-1);
		}
		
		result.width = maxx-minx + 1 + 4; //Add 2 layers of solid tiles to outside
		result.height = maxy-miny + 1 + 4;
		result.x = minx-2;
		result.y = miny-2;
		
		result.state.tiles = new byte[result.width][result.height];
		for(int y = 0; y<result.height; y++)for(int x = 0; x<result.width; x++)result.state.tiles[x][y] = 2;

		for(Point p: r.exits)result.state.tiles[p.x - result.x][p.y - result.y] = 1;
		 
		for(Partition p : r.area)
			for(int y = 0; y<p.h; y++)
				for(int x = 0; x<p.w; x++)
					result.state.tiles[p.x + x - result.x][p.y + y - result.y] = 0;
		
		for(int y = 0; y< result.height; y++)
			for(int x = 0; x<result.width; x++)
				result.tiles[x][y] = g.levelTileGrid[x][y];
				
		result.exits = new boolean[result.width][result.height];
		for(Point p: r.exits)result.exits[p.x-result.x][p.y-result.y] = true;
		
		return result;
		
	}
	
	static class PartialLevel{ //Things that remain constant when solving a partial
		int x, y;
		int width, height;
		boolean[][] tiles;
		boolean[][] exits;
		PartialLevelState state;
	}
	
	static class PartialLevelState implements Cloneable{ //Solution state 
		
		PartialLevel level;
		byte[][] tiles;
		
		public PartialLevelState clone(){
			PartialLevelState pp = new PartialLevelState();
			
			pp.tiles = new byte[level.width][];
			for(int i = 0; i<level.width; i++)pp.tiles[i] = tiles[i].clone();
			
			pp.level = level;
			
			return pp;
		}
		
		public boolean isSolved(){ //Free space is 0, exits are 1, blocked tiles are 2,  paths are >=3 
			for(int x = 0; x<level.width; x++)for(int y = 0; y<level.height; y++)
				if(tiles[x][y] == 0)return false;
			return true;
		}

		public void print(){
			
			System.out.println("Position: " + level.x + ", " + level.y);
			
			for(int y = 0; y < level.height; y++){
				for(int x = 0; x < level.width; x++){
					System.out.print(tiles[x][y] > 2 ? "X " : ". ");
				}
				System.out.println();
			}
		}
		
		
	}
	
	static class PartialMove{
		final static boolean VERTICAL = true;
		final static boolean HORIZONTAL = false;
		
		char direction;
		
		PartialLevelState state;
		Point position;
		
		boolean axis;
		
		boolean movedOnce;
		boolean movedTwice;
	}
	
	static class PartialSolution{
		Region area;
		PartialPath[] paths;
		//TODO: dead-end evaluation
	}
	
	static class PartialPath{
		Point start;
		Point end;
		String moves;
		PartialPath[] dependencies;
		PartialSolution parent;
	}
	
}
