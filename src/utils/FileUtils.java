package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {
	public FileUtils() {
		File file = new File("test.txt");
		if(file.exists()) 
			file.delete();
	}
	
	public void write(String str) {
		try (FileWriter fw = new FileWriter("test.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println(str);
		} catch (IOException e) {
			System.err.println("splat");
		}
	}
}
