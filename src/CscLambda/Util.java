package CscLambda;



public class Util { //Classes that pertain to all kinds of MortalCoil solvers
	
	public static class Solution{
		Point start = new Point();
		String path;
		Level l;
		float solvetime;
		
		public Solution print(){
			float solvesecs = solvetime%60;
			float solvemins = (solvetime-solvesecs)/60;
			
			System.out.println("\nURL: http://www.hacker.org/coil/index.php?x="+ start.x +"&y="+ start.y +"&path="+ path);
			if(solvesecs>=10)System.out.println("Solve Time: " + (int)solvemins + ":" + (int)solvesecs);
			else System.out.println("Solve Time: " + (int)solvemins + ":0" + (int)solvesecs);
			System.out.println("Start: " + start.x + ", " + start.y);
			
			return this;
		}
		
		public String getUrl(String username, String password){
			return "http://www.hacker.org/coil/index.php?name=" + username + "&password=" + password + "&x="+ start.x +"&y="+ start.y +"&path="+ path;
		}
	}

	
	public static class Point implements Cloneable{
		public int x;
		public int y;
		public Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		public Point(){}
		
		public boolean equals(Point other){
			return other.x==this.x && other.y == this.y;
		}
		
		public Point clone(){
			Point r = new Point();
			r.x = x;
			r.y = y;
			return r;
		}
	}

}
