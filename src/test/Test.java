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
		
		System.setProperty("http.proxyHost", "172.20.0.9");
		System.setProperty("http.proxyPort", "3128");
		
		try {
			//Connection connection = Jsoup.connect("http://www.larousse.fr/dictionnaires/francais/faire");
			//connection.get();
			//System.out.println(connection.response().body());
			
			char a = 'a';
			a--;
			System.out.println(--a);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}