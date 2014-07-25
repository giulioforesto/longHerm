import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

/*
 * 
 */

public class TransMatrix {
	
	public static class TMatrix {
		public TreeMap<String,TreeSet<String>> map = new TreeMap<String,TreeSet<String>>();
		
		public void add (String parentWord, String childWord) {
			if (parentWord != null) {	
				TreeSet<String> vector;
				
				if (this.map.containsKey(parentWord)) {
					vector = this.map.get(parentWord);
				}
				else {
					vector = new TreeSet<String>();
					System.out.println(parentWord);
					System.out.println(this.map.size()+1);
				}
				
				vector.add(childWord);
				this.map.put(parentWord, vector);
			}
		}
		
		public boolean contains (String word) {
			return this.map.containsKey(word);
		}
	}
	
	private static TMatrix matrix = new TMatrix();
	
	private static class Cursor {
		public static Iterator<Element> iterator;
		public static int page = 1;
		public static char letter = '`'; // Before 'a' for first call to getNextWord()
	}
	
	private static boolean isNatureRelevant(String nature) {
		String regex = "(adj|v(\u002e|erbe)|nom|n\u002e[mf]).*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(nature);
		return matcher.matches();
	}
	
	private static void mapText (String text, String parentWord) {
		String cleanedText = text
				.replaceAll("[ \u00a0]-|-[ \u00a0]", ". ")
				.replaceAll("'[ \u00a0]+", "'")
				.replaceAll("[\\p{Punct}&&[^'-]]", ""); // Apostrophe is kept because Larousse.fr understands it.
		String[] splitText = cleanedText.split(" +");
		for (String word : splitText) {
			if (word.length() > 1) {
				try {
					mapWord(word, parentWord);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				matrix.add(parentWord, word);
			}
		}
	}
	
	/**
	 * getNextWord() valable uniquement pour larousse.fr
	 */
	private static String getNextWord() throws IOException {
		Elements next;
		try {
			next = Cursor.iterator.next().select("a");
		}
		catch (NoSuchElementException|NullPointerException e) { // Catches page ends and when Cursor is not initialized. 
			Cursor.page++;
			String URL = "http://www.larousse.fr/index/dictionnaires/francais/"
					+ Cursor.letter + "/" + Cursor.page;
			Document doc;
			try {
				doc = Jsoup.connect(URL).get();
			}
			catch (HttpStatusException e2) {
				if (Cursor.letter == 'z') {
					return null;
				}
				else {
					Cursor.letter++;
					Cursor.page = 1;
					URL = "http://www.larousse.fr/index/dictionnaires/francais/"
							+ Cursor.letter + "/" + Cursor.page;
					doc = Jsoup.connect(URL).get();
				}
			}
			Cursor.iterator = doc.select("section.content.olf").first()
					.select("li").iterator();
			next = Cursor.iterator.next().select("a");
		}
		String word =  next.attr("href")
				.replaceAll("dictionnaires|francais|/\\d+", "").replaceAll("/", "");
		System.out.println(next.text());
		String wordAppearance = next.text()
				.replaceAll("[a-zA-Z]+\\u002e.*$", "")
				.replaceAll("\\p{Punct}", "")
				.replaceAll("[ \u00a0]$", "");
		System.out.println(wordAppearance);
		if (wordAppearance.length() > 1) {
			return word;
		}
		else {
			return getNextWord();
		}
	}
	
	private static void mapWord (String word, String parentWord) throws IOException {
		System.out.println("Mapping: " + parentWord + ", " + word);
		Document doc = Jsoup.connect("http://www.larousse.fr/dictionnaires/francais/"
				+ word).get();
		
		Element header = doc.select("header.with-section").first();
		
		if (header == null) {return;}
		
		String realWord = header.select("h2.AdresseDefinition").first().text().replaceAll("^\\p{Z}+", ""); // &nbsp;
		
		if (matrix.contains(realWord)) {return;}
		
		boolean added = false;
		
		// in main def
		try {
			if (isNatureRelevant(header.select("p.CatgramDefinition").first().text())) { // throws NullPointerException
				matrix.add(parentWord,realWord);
				added = true;
				mapDefs(realWord, doc);
			};
		} catch (NullPointerException e) {}
		
		// in left wrapper
		Element leftWrapper = doc.select("div.wrapper-search").first();
		Elements items;
		try {
			items = leftWrapper.select("a"); // throws NullPointerException
		} 
		catch (NullPointerException e) {return;}
		
		Iterator<Element> iterator = items.iterator();
		while (iterator.hasNext()) {
			Element item = iterator.next();
			
			//Post-condition: item.text() must be "<exact word> <nature>"
			String[] split = item.text().split(" ");
			if (split.length == 2 && split[0] == realWord && isNatureRelevant(split[1])) {
				if (!added) {
					matrix.add(parentWord, realWord);
				}
				String URL = "http://www.larousse.fr" + item.attr("href");
				Document newDoc;
				newDoc = Jsoup.connect(URL).get();
				mapDefs(realWord, newDoc);
			}
		}
	}
	
	/**
	 * mapDefs valable uniquement pour larousse.fr 
	 */
	private static void mapDefs (String word, Document doc) {
		Elements definitions = doc.select("li.DivisionDefinition");
		Iterator<Element> iterator = definitions.iterator();
		while (iterator.hasNext()) {
			Element item = iterator.next();
			try { // ignores Special Field definitions
				item.select("span.RubriqueDefinition").first().text();
			}
			catch (NullPointerException e) { // normal case: ignores all child nodes (examples, ...)
				for (Node node : item.children()) {
					node.remove();
				}
				mapText(item.text(), word);
			}
		}
	}
	
	public static TMatrix calculateMatrix () {
		System.setProperty("http.proxyHost", Proxy.host);
		System.setProperty("http.proxyPort", Proxy.port);
		
		try {
			String startWord = getNextWord();
			
			while(startWord != null) {
				mapWord(startWord, null);
				startWord = getNextWord();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Calculated transition matrix.");
		return matrix;
	}
}
