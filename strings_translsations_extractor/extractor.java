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


public class extractor
{
	public void run()throws Exception
	{
		Scanner file=new Scanner(new File("strings.xml"));
		PrintWriter f= new PrintWriter(new FileWriter("strings.txt"));
		
		file.nextLine();//skip <?xml version...
		file.nextLine();// skip <resources>
		while(file.hasNextLine()){
			//output+= file.nextLine()+", ";
			String line = file.nextLine();
			line = line.replaceAll("<string name=\"","");
			line = line.replaceAll("</string>","");
			line = line.replaceAll(">","\t");
			f.println(line);
		}
		f.close();
	}

	public static void main(String[] args)throws Exception
	{
		extractor a=new extractor();
		a.run();
	}
}