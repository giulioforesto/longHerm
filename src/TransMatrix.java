import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

/*
 * TODO Check if mapWord (or sth else) does not discard some valid words.
 */

public class TransMatrix {
	
	public static class TMatrix {
		private TreeMap<String,TreeSet<String>> map = new TreeMap<String,TreeSet<String>>();
		
		public TreeMap<String,Boolean> checkedWords = new TreeMap<String,Boolean>();
		
		public void add (String parentWord, String childWord) {
			if (parentWord != null) {
				TreeSet<String> vector;
				
				if (map.containsKey(parentWord)) {
					vector = map.get(parentWord);
					System.out.println("Added child.");
				}
				else {
					vector = new TreeSet<String>();
					System.out.println(parentWord);
					System.out.println(map.size()+1);
				}
				
				vector.add(childWord);
				map.put(parentWord, vector);
				
				if (!map.containsKey(childWord)) {
					System.out.println(childWord);
					System.out.println(map.size()+1);
					map.put(childWord, new TreeSet<String>());
				}
			}
		}
		
		public boolean contains (String word) {
			return map.containsKey(word);
		}
	}
	
	private static TMatrix matrix = new TMatrix();
	
	public static class Queue {
		public Queue(String parent, String child) {
			queue = new TreeMap<String,TreeSet<String>>();
			add(parent, child);
		}
		
		private TreeMap<String,TreeSet<String>> queue;
		
		public void add (String parent, String child) {
			TreeSet<String> vector;
			if (queue.containsKey(parent)) {
				vector = queue.get(parent);
			} else {
				vector = new TreeSet<String>();
			}
			vector.add(child);
			queue.put(parent, vector);
		}
		
		public String[] pollFirst() {
			String[] result = new String[2];
			Entry<String,TreeSet<String>> entry = queue.firstEntry();
			TreeSet<String> set = entry.getValue();
			
			result[0] = entry.getKey();
			result[1] = set.pollFirst();
			
			if (set.isEmpty()) {
				queue.remove(result[0]);
			}
			
			return result;
		}
	}
	
	private static class Cursor {
		public static Iterator<Element> iterator;
		public static int page = 1;
		public static char letter = '`'; // Before 'a' for first call to getNextWord()
	}
	
	private static boolean isNatureRelevant(String nature) {
		String regex = "(adj|v(\u002e|erbe)|nom|n\u002e[mf]|adv).*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(nature);
		return matcher.matches();
	}
	
	private static Document getDocFromPath(String wordURL) {
		String URL = URI.create("http://www.larousse.fr" + wordURL).toASCIIString();
		
		try {
			return Jsoup.connect(URL).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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

		System.out.println(next.text());
		String wordAppearance = next.text()
				.replaceAll("[a-zA-Z]+\\u002e.*$", "")
				.replaceAll("\\p{Punct}", "")
				.replaceAll("[ \u00a0]$", "");
		System.out.println(wordAppearance);
		
		if (wordAppearance.length() > 1) {
			return next.attr("href");
		}
		else {
			return getNextWord();
		}
	}
	
	/**
	 * 
	 * @param wordURL The word URL path.
	 * @param mapping Set the value to `true` for a real word mapping, in order to map the definition and build the matrix. Set to `false` for a simple word relevance checking.
	 * @return Returns the real word if `mapping` is set to `false` if in one of the definitions it is relevant, `null` otherwise.  
	 */
	private static String mapWord (String wordURL, boolean mapping) {
		Document doc = getDocFromPath(wordURL);
		if (doc == null) {return null;}
		
		Element header = doc.select("header.with-section").first();
		if (header == null) {return null;} // Ignores a "not found" definition.
		
		String realWord = header.select("h2.AdresseDefinition").first().text().replaceAll("^\\p{Z}+", ""); // &nbsp;
		
		if (!mapping && matrix.checkedWords.containsKey(realWord)) {
			if (matrix.checkedWords.get(realWord)) {
				return realWord;
			} else {
				return null;
			}
		}
		
		if (mapping) {
			System.out.println("Mapping: " + realWord);
		}
		
		boolean checked = false;
		
		// in main def
		try {
			if (isNatureRelevant(header.select("p.CatgramDefinition").first().text())) { // throws NullPointerException
				matrix.checkedWords.put(realWord, true);
				checked = true;
				if (mapping) {
					mapDefs(realWord, doc);
				} else {
					return realWord;
				}
			};
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		// in left wrapper
		Element leftWrapper = doc.select("div.wrapper-search").first();
		Elements items;
		try {
			items = leftWrapper.select("a"); // throws NullPointerException
		} 
		catch (NullPointerException e) {return null;}
		
		Iterator<Element> iterator = items.iterator();
		while (iterator.hasNext()) {
			Element item = iterator.next();
			
			//Post-condition: item.text() must be "<exact word> <nature>"
			String[] split = item.text().split(" ");
			if (split.length == 2 && split[0] == realWord && isNatureRelevant(split[1])) {
				matrix.checkedWords.put(realWord, true);
				checked = true;
				if (mapping) {
					String childURL = "http://www.larousse.fr" + item.attr("href");
					try {
						Document newDoc = Jsoup.connect(childURL).get();
						mapDefs(realWord, newDoc);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					matrix.checkedWords.put(realWord, true);
					return realWord;
				}
			}
		}
		if (!checked) {
			matrix.checkedWords.put(realWord, false);
		}
		return null;
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
				mapText(word, item.text());
			}
		}
	}
	
	private static void mapText (String parentWord, String text) {
		String cleanedText = text
				.replaceAll("[ \u00a0]-|-[ \u00a0]", ". ") //Removes useless hyphens.
				.replaceAll("[\\p{Punct}&&[^'-]]", ""); // Apostrophe is kept because Larousse.fr understands it.
		String[] splitText = cleanedText.split("[ \u00a0]+");
		
		for (String word : splitText) {
			if (word.length() > 1) {
				System.out.println("Mapping: " + parentWord + ", " + word);
				String realWord = mapWord("/dictionnaires/francais/" + word, false);
				if (realWord != null) {
					matrix.add(parentWord,realWord);
				}
			}
		}
	}
	
	public static TMatrix calculateMatrix () {
		System.setProperty("http.proxyHost", Proxy.host);
		System.setProperty("http.proxyPort", Proxy.port);
		
		try {
			String wordURL = getNextWord();
			
			while(wordURL != null) {
				mapWord(wordURL, true);
				wordURL = getNextWord();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Calculated transition matrix.");
		return matrix;
	}
	
	public static void main (String[] args) {
		calculateMatrix();
	}
}
