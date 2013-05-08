import java.io.*;
import java.util.*;
import java.text.*;
import static java.lang.System.*;
import static java.lang.Integer.*;
import static java.lang.Double.*;
import static java.lang.Character.*;
import static java.util.Collections.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;


public class reformatter
{
	public void run()throws Exception
	{
		Scanner file=new Scanner(new File("strings.txt"));
		PrintWriter f= new PrintWriter(new FileWriter("results.xml"));
		
		while(file.hasNextLine()){	
			f.println("\t<string name=\""+file.next()+"\">"+file.nextLine().substring(1)+"</string>");
		}
		f.close();
	}

	public static void main(String[] args)throws Exception
	{
		reformatter a=new reformatter();
		a.run();
	}
}