package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.net.URLEncoder;
import java.net.URI;

//client.util.URIUtil.encodeQuery;

public class Test {
	
	private static class C {
		private int i;
		
		public C(int j) {
			i=j;
		}
		
		public int getI() {
			return i;
		}
		
		public void setI(int j) {
			i = j;
		}
	}
	
	public static void main (String[] args) throws IOException, URISyntaxException {
		
//		System.setProperty("http.proxyHost", "172.20.0.9");
//		System.setProperty("http.proxyPort", "3128");
//		Document doc = Jsoup.connect("http://www.larousse.fr/dictionnaires/francais/"
//				+ "boite").get();
//		
//		String text = doc.select("li.DivisionDefinition").first().text();
		
		TreeSet<String> set = new TreeSet<String>();
		
		set.add("ciao");
		set.add("ciao");
		set.add("ciaop");
		
		System.out.println(set.size());
		
//		Document doc = Jsoup.connect("http://www.larousse.fr/dictionnaires/francais/"
//				+ word).get();
		
//		String cleanedText = text
//				.replaceAll("[ \u00a0]-|-[ \u00a0]", ". ") //Removes useless semicolons.
//				.replaceAll("[\\p{Punct}&&[^'-]]", ""); // Apostrophe is kept because Larousse.fr understands it.
//		String[] splitText = cleanedText.split("[ \u00a0]+");
		
//		TreeSet<String> vector = new TreeSet<String>();
//		
//		
//		
//		System.out.println(vector.size());
		
//		System.setProperty("http.proxyHost", "172.20.0.9");
//		System.setProperty("http.proxyPort", "3128");
//		
//		HashSet<String> hashSet = new HashSet<String>();
//		TreeSet<String> treeSet = new TreeSet<String>();
//		String[] array = new String[100000];
//		
//		char an = 'a';
//		char ai = 'a';
//		char aj = 'a';
//		char ak = 'a';
//		int arrayCounter = 0;
//		for (int n = 0; n < 6; n++) {
//			for (int i = 0; i < 25; i++) {
//				for (int j = 0; j < 25; j++) {
//					for (int k = 0; k < 25; k++) {
//						String word = Character.toString(an)
//								+ Character.toString(ai)
//								+ Character.toString(aj)
//								+ Character.toString(ak);
//						
//						hashSet.add(word);
//						treeSet.add(word);
//						array[arrayCounter] = word;
//						arrayCounter++;
//						ak++;
//					}
//					aj++;
//					ak = 'a';
//				}
//				ai++;
//				aj = ak = 'a';
//			}
//			an++;
//			ai = aj = ak = 'a';
//		}
//		
//		Date time1 = new Date();
//		System.out.println("Hash: " + hashSet.contains("beio"));
//		Date time2 = new Date();
//		System.out.println(time2.getTime() - time1.getTime());
//		time1 = new Date();
//		System.out.println("Tree: " + treeSet.contains("beio"));
//		time2 = new Date();
//		System.out.println(time2.getTime() - time1.getTime());
//		time1 = new Date();
//		for (int i = 0; i < array.length; i++) {
//			if (array[i].equals("beio")) {
//				System.out.println("Array: true");
//				time2 = new Date();
//				System.out.println(time2.getTime() - time1.getTime());
//				break;
//			}
//		}
	}
}
