package CscLambda;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import CscLambda.Level;
//import CscLambda.PartialSolver.PartialSolution;
import CscLambda.Util.Point;

public class RegionGraph{ 
	
	
	public int width;
	public int height;
	
	public boolean[][] levelTileGrid;
	
	public Partition[][] partitionCoverage;
	public HashSet<Partition> partitions = new HashSet<Partition>(); //Possible source of undefined regioning/solution
	
	public Region[][] regionCoverage;
	public HashSet<Region> regions = new HashSet<Region>(); //Possible source of undefined regioning/solution
	
 	public RegionGraph(Level l){
		int[][] grid = new int[l.width][l.height];
		width = l.width;
		height = l.height;
		
		levelTileGrid = l.contents;
		
		Partition[][] pcoverage = new Partition[width][height];
		
		for(int y = 0; y<l.height; y++)
			for(int x = 0; x<l.width; x++){
				grid[x][y] = l.contents[x][y]? 0 : 1; // 0 is blocked, 1 is open, 2 is partitioned
			}

		for(int p = 0; p<2; p++) 
		for(int y = 0; y<l.height; y++)
			for(int x = 0; x<l.width; x++){
				if(grid[x][y] != 1) continue;
				
				int w = 0;
				int h = 0;
				boolean wlim = false;
				boolean hlim = false;
				
				while(true){
					if(!wlim)w++;
					if(!hlim)h++;
					
		
					if(x+w-1 >= width){
						wlim = true;
						w--;
					}
					if(y+h-1 >= height){
						hlim = true;
						h--;
					}
					
					if(!wlim)
						for(int i = 0; i<h-1; i++) 
							if(grid[x+w-1][y+i] != 1){
								wlim = true;
								w--;
								continue;
							}
					
					if(!hlim)
					for(int i = 0; i<w-1; i++) //iterate over new territory
						if(grid[x+i][y+h-1] != 1){
							hlim = true;
							h--;
							continue;
						}
					
					if(grid[x+w-1][y+h-1] != 1){
						if(!hlim){
							hlim = true;
							h--;
							continue;
						}else if(!wlim){
							wlim = true;
							w--; 
							continue;
						}
					}
				
					if(grid[x+w-1][y+h-1] != 1){
						int wmax = 0;
						int hmax = 0;
						for(int ws = 0; ws<w-2; ws++){
							int tx = x+ws;
							int ty = y+h;
							while(true){
								if(grid[tx][ty] != 1)break;
								ty++;
							}
							hmax = Math.max(ty, hmax);
						}
						
						for(int hs = 0; hs<h-2; hs++){
							int tx = x+w;
							int ty = y+hs;
							while(true){
								if(grid[tx][ty] != 1)break;
								tx++;
							}
							wmax = Math.max(tx, wmax);
						}
						if(hlim){
							wlim = true;
							w--;
							continue;
						}else if(wlim){
							hlim = true;
							h--;
							continue;
						}else{
							if(wmax<=hmax)hlim = true;
							else wlim = true;
						}
					}
					
					if(wlim&&hlim){
						break;
					}
				}
				if(w == 0 || h == 0)continue;
				if(p == 0 && (w == 1 || h == 1))continue;
				
				Partition r = new Partition();
				r.x = x;
				r.y = y;
				r.w = w;
				r.h = h;
				partitions.add(r);
				
				
				for(int mx = 0; mx<w; mx++)
					for(int my = 0; my<h; my++){
						grid[x+mx][y+my] = 2;
						pcoverage[x+mx][y+my] = r;
					}
			}
		
		//stitch
		
		partitionCoverage = new Partition[width][height];
		for(int x = 0; x<pcoverage.length; x++)
			for(int y = 0; y<pcoverage[x].length; y++){
				Partition pc = pcoverage[x][y];
				if(pc == null)continue;
				partitionCoverage[x][y] = pc;
			}
		findRegions();
		
	}
	
	public void findRegions(){ 
		regions.clear();
		for(Partition part: partitions){
			if(part.regionmark)continue;
			Region r = findRegion(this, part);
			for(Partition p:r.area){
				p.regionmark = true;
			}
			regions.add(r);
		
		}
		
		for(Partition p: partitions)p.regionmark = false;
		
		regionCoverage = new Region[width][height];
		for(Region r:regions){
			for(Partition p: r.area){
				for(int x = p.x; x<p.x+p.w; x++)
					for(int y = p.y; y<p.y+p.h; y++)regionCoverage[x][y] = r;
			}
		}
		
		//stitch
	
		
		HashSet<Region> result = new HashSet<Region>(regions.size());
		for(Region r: regions)if(r!=null){
			result.add(r);
			r.mark = false;
		}
		regions = result;
	}
	public Region findRegion(RegionGraph l, Partition part){ //Floods to find the region that contains the partition
		int currentcheck = 0;
		ArrayList<Partition> marked = new ArrayList<Partition>();
		marked.add(part);
		part.mark = true;
		
		Region r = new Region();
		ArrayList<Point> exits = new ArrayList<Point>();
		ArrayList<Character> edirs = new ArrayList<Character>();

		while(currentcheck< marked.size()){
			Partition cpart = marked.get(currentcheck);
			
			for(int i = 0; i<cpart.w; i++){
				
				boolean includeTop = false;
				Partition pt = (cpart.y > 0) ? partitionCoverage[cpart.x + i][cpart.y-1] : null;
				if(pt != null && (!pt.mark)){
					boolean leftopen = (cpart.x + i - 1 >= 0) ? !levelTileGrid[cpart.x + i - 1][cpart.y-1] : false;
					boolean rightopen = (cpart.x + i + 1 < width) ? !levelTileGrid[cpart.x + i + 1][cpart.y-1] : false;
					
					if(i == 0){
						if(
							(rightopen && cpart.w > 1)||
							(leftopen && !levelTileGrid[cpart.x - 1][cpart.y])
						)includeTop = true;
					}else if(i == cpart.w-1){
						if(
							(leftopen && cpart.w > 1)||
							(rightopen && !levelTileGrid[cpart.x + cpart.w][cpart.y])
						)includeTop = true;
					}else if(rightopen || leftopen)includeTop = true;
				}
				if(includeTop){
					marked.add(pt);
					pt.mark = true;
				}else if(pt!=null && !pt.mark){
					exits.add(new Point(cpart.x + i, cpart.y-1));
					edirs.add('D');
				}
				
				boolean includeBottom = false;
				Partition pb = (cpart.y+cpart.h<height) ? partitionCoverage[cpart.x + i][cpart.y+cpart.h] : null;
				if(pb != null && (!pb.mark)){
					boolean leftopen = (cpart.x + i - 1 >= 0) ? !levelTileGrid[cpart.x + i - 1][cpart.y+cpart.h] : false;
					boolean rightopen = (cpart.x + i + 1 < width) ? !levelTileGrid[cpart.x + i + 1][cpart.y+cpart.h] : false;
					
					if(i == 0){
						if(
							(rightopen && cpart.w > 1)||
							(leftopen && !levelTileGrid[cpart.x - 1][cpart.y + cpart.h - 1])
						)includeBottom = true;
					}else if(i == cpart.w-1){
						if(
							(leftopen && cpart.w > 1) ||
							(rightopen && !levelTileGrid[cpart.x + cpart.w][cpart.y + cpart.h - 1])
						)includeBottom = true;
					}else if(leftopen || rightopen)includeBottom = true;
				}
				if(includeBottom){
					marked.add(pb);
					pb.mark = true;
				}else if(pb!=null && !pb.mark){
					exits.add(new Point(cpart.x + i, cpart.y+cpart.h));
					edirs.add('U');
				}
				
			}
			for(int i = 0; i<cpart.h; i++){
				
				boolean includeLeft = false;
				Partition pl = (cpart.x > 0) ? partitionCoverage[cpart.x - 1][cpart.y + i] : null;
				if(pl != null && (!pl.mark)){
					boolean topopen = (cpart.y + i - 1 >= 0) ? !levelTileGrid[cpart.x - 1][cpart.y + i - 1] : false;
					boolean bottomopen = (cpart.y + i + 1 < height) ? !levelTileGrid[cpart.x - 1][cpart.y + i + 1] : false;
					
					if(i == 0){
						if(
							(bottomopen && cpart.h>1) ||
							(topopen && !levelTileGrid[cpart.x][cpart.y - 1])
						)includeLeft = true;
					}else if(i == cpart.h-1){
						if(
							(topopen && cpart.h>1) ||
							(bottomopen && !levelTileGrid[cpart.x][cpart.y + cpart.h])
						)includeLeft = true;
					}else if(topopen || bottomopen)includeLeft = true;
				}
				if(includeLeft){
					marked.add(pl);
					pl.mark = true;
				}else if(pl!=null && !pl.mark){
					exits.add(new Point(cpart.x - 1, cpart.y + i));
					edirs.add('R');
				}
				
				boolean includeRight = false;
				Partition pr = (cpart.x + cpart.w < width) ? partitionCoverage[cpart.x + cpart.w][cpart.y + i] : null;
				if(pr != null && (!pr.mark)){
					boolean topopen = (cpart.y + i - 1 >= 0) ? !levelTileGrid[cpart.x + cpart.w][cpart.y + i - 1] : false;
					boolean bottomopen = (cpart.y + i + 1 < height) ? !levelTileGrid[cpart.x + cpart.w][cpart.y + i + 1] : false;
					
					if(i == 0){
						if(
							(bottomopen && cpart.h>1) ||
							(topopen && !levelTileGrid[cpart.x + cpart.w - 1][cpart.y - 1])
						)includeRight = true;
					}else if(i == cpart.h-1){
						if(
							(topopen && cpart.h>1) ||
							(bottomopen && !levelTileGrid[cpart.x + cpart.w - 1][cpart.y + cpart.h])
						)includeRight = true;
					}else if(topopen || bottomopen)includeRight = true;
					
					
				}
				if(includeRight){
					marked.add(pr);
					pr.mark = true;
				}else if(pr!=null && !pr.mark){
					exits.add(new Point(cpart.x + cpart.w, cpart.y + i));
					edirs.add('L');
				}
			}
		

			currentcheck++;
		}
		
		int removed = 0;
		for(int i = 0; i<exits.size(); i++){
			Point pp = exits.get(i);
			if(partitionCoverage[pp.x][pp.y].mark){
				exits.set(i, null);
				removed++;
			}
		}
		
		r.area = new Partition[marked.size()];
		for(int i = 0; i<marked.size(); i++){
			Partition partt = marked.get(i);
			partt.parentRegion = r;
			r.area[i] = partt;
		}
		
		r.exits = new Point[exits.size()-removed];
		int rm = 0;
		for(int i = 0; i<exits.size(); i++){
			Point p = exits.get(i);
			if(p == null){
				rm++;
				continue;
			}
			r.exits[i-rm] = p;
		}
		
		r.exitOrientations = new char[edirs.size()];
		for(int i = 0; i<edirs.size(); i++)r.exitOrientations[i] = edirs.get(i);
		
		for(Partition p:marked)p.mark = false;
		
		return r;
	}
	

	public boolean divisionCheck(){
		ArrayList<Region> marked = new ArrayList<Region>();
		marked.add(regions.iterator().next());
		marked.get(0).mark = true;
		
		for(int i = 0; i<marked.size(); i++){
			Region tp = marked.get(i);
			
			for(Point p: tp.exits){
				Region other = regionCoverage[p.x][p.y];
				if(!other.mark){
					other.mark = true;
					marked.add(other);
				}
			}
		}
		
		for(Region r :marked)r.mark = false;
		
		return marked.size() != regions.size();
	}
	
 	public static class Partition{
		int x, y, w, h;
		boolean mark = false;
		boolean regionmark = false;
		Region parentRegion;
	
	}
	public static class Region{
		Partition[] area;
		//HashSet<PartialSolution> solutions;
		
		boolean mark;
		
		Point[] exits;
		char[] exitOrientations;
		
	}

}

