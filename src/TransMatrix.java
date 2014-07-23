import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

/*
 * TODO #2 Solve looping after a while. See log 20140722_log_loop_1.txt. SOLUTION: Deal with word *before* dealing with its definition
 * TODO #3 Ignore "-" in cleaning up in getNextWord()
 * TODO #4 Fix wordAppearance regex bug: see log 20140723_log_wordAppearance_1.txt.
 * TODO #5 Explore NoSuchElementException bug: see log 20140723_log_wordAppearance_1.txt. Does not raise in master version.
 */

public class TransMatrix {
	
	public static class TMatrix {
		private HashMap<String,HashSet<String>> map = new HashMap<String,HashSet<String>>();
		
		public void add (String parentWord, String childWord) {
			if (parentWord != null) {	
				HashSet<String> vector;
				
				if (this.map.containsKey(parentWord)) {
					vector = this.map.get(parentWord);
				}
				else {
					vector = new HashSet<String>();
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
	
	public static TMatrix matrix = new TMatrix();
	
	public static class Cursor {
		public static Iterator<Element> iterator;
		public static int page = 1;
		public static char letter = '`'; // Before 'a' for first call to getNextWord()
	}
	
	public static boolean isNatureRelevant(String nature) {
		String regex = "(adj|v(\u002e|erbe)|nom|n\u002e[mf]).*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(nature);
		return matcher.matches();
	}
	
	public static void mapText (String text, String parentWord) {
		String cleanedText = text.replaceAll("[\\p{Punct}&&[^']]", ""); // Apostrophe is kept because Larousse.fr understands it.
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
	public static String getNextWord() throws IOException {
		Elements next;
		try {
			next = Cursor.iterator.next().select("a");
		}
		catch (NullPointerException e) {
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
		System.out.println(next.attr("href"));
		String word =  next.attr("href")
				.replaceAll("dictionnaires|francais|/\\d+", "").replaceAll("/", "");
		System.out.println(next.text());
		String wordAppearance1 = next.text().replaceAll("[a-zA-Z]+\u002e[a-zA-Z]*", "");
		System.out.println(wordAppearance1);
		String wordAppearance2 = wordAppearance1.replaceAll("[\\p{Punct}&&[^']]", "");
		System.out.println(wordAppearance2);
		String wordAppearance = wordAppearance2.replaceAll("[ \u00a0]$", "");
		System.out.println(wordAppearance);
		if (wordAppearance.length() > 1) {
			return word;
		}
		else {
			return getNextWord();
		}
	}
	
	public static void mapWord (String word, String parentWord) throws IOException {
		System.out.println("Mapping: " + parentWord + ", " + word);
		Document doc = Jsoup.connect("http://www.larousse.fr/dictionnaires/francais/"
				+ word).get();
		
		Element header = doc.select("header.with-section").first();
		
		if (header == null) {return;}
		
		String realWord = header.select("h2.AdresseDefinition").first().text().replaceAll("^\\p{Z}+", ""); // &nbsp;
		
		if (matrix.contains(realWord)) {return;}
		
		boolean added = false;
		
		// in main def
		if (isNatureRelevant(header.select("p.CatgramDefinition").first().text())) {
			matrix.add(parentWord,realWord);
			added = true;
			mapDefs(realWord, doc);
		};
		
		// in left wrapper
		Element leftWrapper = doc.select("div.wrapper-search").first();
		Elements items = leftWrapper.select("a");
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
	public static void mapDefs (String word, Document doc) {
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
	
	public static void main (String[] args) {
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
	}
}
