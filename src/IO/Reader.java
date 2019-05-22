package IO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
	
	private FileReader file;
	private BufferedReader reader;
	
	public Reader(String fileName) {
		open(fileName);
	}

	public void open(String fileName) {
		try {
			file = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("reader.open(): FATAL ERROR");
			e.printStackTrace();
			System.exit(-1);
		}
		reader = new BufferedReader(file);
	}
	
	public String readLine() {
		String result = "";
		try {
			result = reader.readLine();
		} catch (IOException e) {
			System.err.println("reader.readLine(): FATAL ERROR");
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	public void close() {
		try {
			reader.close();
			file.close();
		} catch (IOException e) {
			System.err.println("reader.close(): FATAL ERROR");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
