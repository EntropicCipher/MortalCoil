package CscLambda;


import CscLambda.Util.Solution;
import CscLambda.GUI;


public class MortalCoil {
	
	
	public static final String username = "";
	public static final String password = "";
	
	public static void main(String[] args){
			
		Solution s = null;
		while(true){
			
			Level l;
			if(s==null) l = Level.getLevel(username, password, 101);
			else l = Level.getNextLevel(username, password, s);
			
			GUI g = new GUI(l);
			
			while(!g.solved)
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
			s = g.solution;
			
			
			g.destroy();
			
		}
			
			
	}
	
	

	

}
