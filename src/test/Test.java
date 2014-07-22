package test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Test {	
	
	public static class Cursor {
		public static Iterator<Element> iterator;
		public static int page;
	}
	
	public static void main (String[] args) {
		try {
			Connection connection = Jsoup.connect("http://www.larousse.fr/dadadadadadadad");
			connection.get();
			System.out.println(connection.response().body());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}