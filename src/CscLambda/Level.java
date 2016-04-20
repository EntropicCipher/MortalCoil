package CscLambda;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CscLambda.Util.Solution;


public class Level implements Cloneable{
	
	public int width;
	public int height;
	public int number;
	public boolean[][] contents;
	
	
	public Level print(){
		Level l = this;
		for(int y = 0; y<l.height; y++){
			for(int x = 0; x<l.width; x++){
				if(l.contents[x][y]){
					System.out.print("X ");
				}else{
					System.out.print(". ");
				}
			}
			System.out.println();
		}
		return this;
	}
	
	public boolean isSolved(){
		for(int x = 0; x<width; x++)for(int y = 0; y<height; y++)if(!contents[x][y])return false;
		return true;
	}
	
	
	public Level clone(){
		Level cl = new Level();
		cl.width = width;
		cl.height = height;
		cl.contents = new boolean[cl.width][];
		for(int i = 0; i<width; i++)cl.contents[i] = contents[i].clone();
		return cl;
	}
	
	
	public static Level getLevel(String username, String password){
		String url = "http://hacker.org/coil/index.php?name=" + username + "&password=" + password;
		return parseLevel(getPage(url));
	}
	
	public static Level getLevel(String username, String password, int num){
		String url = "http://hacker.org/coil/index.php?gotolevel=" + num + "&go=Go+To+Level&name=" + username + "&password=" + password;
		return parseLevel(getPage(url));
	}
	
	public static Level getNextLevel(String username, String password, Solution s){
		return parseLevel(getPage(s.getUrl(username, password)));
	}
	
	private static String getPage(String addr){
		HttpURLConnection connection;
		try{
			URL url;
			url = new URL(addr);
			connection  = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			StringBuilder source = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				source.append(line);
				source.append("\n");
			}
			
			reader.close();
			return source.toString();
			
		}catch(Exception e){
			return null;
		}
		
	}
	
	private static Level parseLevel(String page){
		Matcher m = Pattern.compile("FlashVars").matcher(page);
		m.find();
		
		int datastart;
		try{
			datastart = m.start();
		}catch(IllegalStateException e){
			System.out.println(page);
			System.exit(1);
			return null;
		}
		
		int dataend = 0;
		for(int i = datastart; true; i++){
			if(page.charAt(i) == '/'){
				dataend = i;
				break;
			}
		}
		
		Level l = new Level();
		
		String leveldata = page.substring(datastart, dataend).split("\"")[2];
		
		String[] pieces = leveldata.split("[&=]");
		
		l.width = Integer.parseInt(pieces[1]);
		l.height = Integer.parseInt(pieces[3]);
		String levelstring = pieces[5];
		
		l.contents = new boolean[l.width][l.height];
		
		for(int x = 0; x<l.width; x++)for(int y = 0; y<l.height; y++)
			l.contents[x][y] = (levelstring.charAt(y*l.width + x) == 'X');
		
		m = Pattern.compile("Level: ").matcher(page);
		m.find();
		StringBuilder levelnum = new StringBuilder();
		for(int i = m.end(); true; i++){
			char c = page.charAt(i);
			if(c == '<')break;
			levelnum.append(c);
		}
		l.number = Integer.parseInt(levelnum.toString());
		
		return l;
	}

}
