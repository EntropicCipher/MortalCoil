package CscLambda;

import java.util.ArrayList;
import java.util.LinkedList;

import CscLambda.RegionGraph.Partition;
import CscLambda.RegionGraph.Region;
import CscLambda.Util.Point;
import CscLambda.Util.Solution;

public class RegionGraphSolver { //Module responsible for solving the RegionGraph and returning a solution
	
	int rootdirections = 0;
	
	Point position;
	
	
	void solve(){
		
		
	}
	
	
	void solveFromPoint(RegionGraph rg, Point start){
		
		
		//TODO: search pattern
		//TODO: delta partial solver
		//TODO: dead-end management
		
		
	}
	
	
	
	static MoveFrame simulateMove(RegionGraph rg, Point startpos, char direction){ //Edits the regiongraph to simulate a movement
		ArrayList<Point> thispath = new ArrayList<Point>();
		StringBuilder moves = new StringBuilder();
		MoveFrame newFrame = new MoveFrame();
		Point position = startpos.clone();
		char mdir = direction;
		
		while(true){
			
			boolean axis = (direction == 'U' || direction == 'D');
			
			Partition cpart = null;
			
			if(mdir == 'U' && position.y > 0)cpart = rg.partitionCoverage[position.x][position.y-1];
			else if(mdir == 'D' && position.y < rg.height-1)cpart = rg.partitionCoverage[position.x][position.y+1];
			else if(mdir == 'R' && position.x < rg.width-1)cpart = rg.partitionCoverage[position.x+1][position.y];
			else if(mdir == 'L' && position.x > 0)cpart = rg.partitionCoverage[position.x-1][position.y];
			
			while(cpart != null){
				if(axis){//Divide vertically
					if(cpart.w == 1){
						newFrame.deletedParts.add(cpart);
						rg.partitions.remove(cpart);
					}else if(position.x == cpart.x){
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.x, cpart.w));
						cpart.w--;
						cpart.x++;
					}else if(position.x == cpart.x+cpart.w-1){
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.x, cpart.w));
						cpart.w--;
					}else{
						Partition left = null;
						Partition right = null;
						
						left = cpart;
						left.w = position.x-cpart.x;
						
						right = new Partition();
						right.x = position.x + 1;
						right.y = cpart.y;
						right.h = cpart.h;
						right.w = cpart.w - (position.x - cpart.x) - 1;
						right.mark = cpart.mark;
						
						for(int x = 0; x<right.w; x++)for(int y = 0; y<right.h; y++)rg.partitionCoverage[x+right.x][y+right.y] = right;
						
						newFrame.newParts.add(right);
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.x, cpart.w));
						
						rg.partitions.add(right);
					} 
					for(int y = 0; y<cpart.h; y++)rg.partitionCoverage[position.x][cpart.y+y] = null;
					
					
				}else{ //Divide horizontally
					if(cpart.h == 1){
						rg.partitions.remove(cpart);
						newFrame.deletedParts.add(cpart);
					}else if(position.y == cpart.y){
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.y, cpart.h));
						cpart.h--;
						cpart.y++;
					}else if(position.y == cpart.y+cpart.h-1){
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.y, cpart.h));
						cpart.h--;
					}else{
						Partition top = null;
						Partition bottom = null;
						
						top = cpart;
						top.h = position.y-cpart.y;
						
						bottom = new Partition();
						bottom.x = cpart.x;
						bottom.y = position.y+1;
						bottom.h = cpart.h - (position.y - cpart.y) - 1;
						bottom.w = cpart.w;
						bottom.mark = cpart.mark;
						for(int x = 0; x<bottom.w; x++)for(int y = 0; y<bottom.h; y++)rg.partitionCoverage[x+bottom.x][y+bottom.y] = bottom;
						
						newFrame.partChanges.add(new GraphChange(cpart, axis, cpart.y, cpart.h));
						newFrame.newParts.add(bottom);
						
						rg.partitions.add(bottom);
					}
					for(int x = 0; x<cpart.w; x++)rg.partitionCoverage[cpart.x + x][position.y] = null;
					
				}
				
				if(mdir == 'U' && position.y > 0)position.y -= cpart.h;
				else if(mdir == 'D' && position.y < rg.height-1)position.y += cpart.h;
				else if(mdir == 'R' && position.x < rg.width-1)position.x += cpart.w;
				else if(mdir == 'L' && position.x > 0)position.x -= cpart.w;
				
				if(mdir == 'U' && position.y > 0)cpart = rg.partitionCoverage[position.x][position.y-1];
				else if(mdir == 'D' && position.y < rg.height-1)cpart = rg.partitionCoverage[position.x][position.y+1];
				else if(mdir == 'R' && position.x < rg.width-1)cpart = rg.partitionCoverage[position.x+1][position.y];
				else if(mdir == 'L' && position.x > 0)cpart = rg.partitionCoverage[position.x-1][position.y];
				else cpart = null;
			}
			
			moves.append(mdir);
			thispath.add(position.clone());
			
			boolean positive = false;
			boolean negative = false;
			if(axis){
				positive = position.x < rg.width-1 ? rg.partitionCoverage[position.x+1][position.y] != null : false;
				negative = position.x > 0 ? rg.partitionCoverage[position.x-1][position.y] != null : false;
				
				if(positive && !negative)mdir = 'R';
				if(negative && !positive)mdir = 'L';
				
			}else{
				positive = rg.partitionCoverage[position.x][position.y+1] != null;
				negative = rg.partitionCoverage[position.x][position.y-1] != null;
				
				if(positive && !negative)mdir = 'D';
				if(negative && !positive)mdir = 'U';
			}
			if(positive && negative)break;
			else if(positive == negative)return null; //hits dead end
			
			
		}
		newFrame.path = new Point[thispath.size()];
		for(int i = 0; i<thispath.size(); i++)newFrame.path[i] = thispath.get(i);
		
		newFrame.moves = moves.toString();
		
		
		//Post-process?
		
		return newFrame;
	}	

	
	
	//TODO: reverse move simulation
	
	public static class MoveFrame{ //Stores information about a logged move, conserves memory when reverting moves
		Point[] path; //contains final point, not initial 
		String moves;
		
		LinkedList<GraphChange> partChanges = new LinkedList<GraphChange>();
		LinkedList<Partition> newParts = new LinkedList<Partition>();
		LinkedList<Partition> deletedParts = new LinkedList<Partition>();
		
		LinkedList<Region> newRegions = new LinkedList<Region>();
		LinkedList<Region> deletedRegions = new LinkedList<Region>();
		
		LinkedList<Solution> newSolutions = new LinkedList<Solution>();
		LinkedList<Solution> deletedSolutions = new LinkedList<Solution>();
		
	}
	
	public static class GraphChange{//TODO: Use this?
		Partition part;
		boolean axis;
		int olddim;
		int oldpos;
		
		public GraphChange(Partition pt, boolean a, int p, int d){
			part = pt;
			olddim = d;
			oldpos = p;
			axis = a;
		}
	}
	
}
