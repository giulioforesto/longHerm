import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Scores {
	
	private class BMatrix {
		public boolean matrix[][];
		
		private int size;
		
		public BMatrix(int s) {
			matrix = new boolean[s][s];
			size = s;
		}
		
		public BMatrix(BMatrix m) {
			matrix = m.matrix.clone();
			size = m.size;
		}
		
		public BMatrix mult(BMatrix B) {
			BMatrix result = new BMatrix(size);
			
			for (int j = 0; j < size; j++) {
				for (int i = 0; i < size; i++) {
					for (int k = 0; k < size; j++) {
						if (matrix[i][k] && B.matrix[k][j]) {
							result.matrix[i][j] = true;
							break;
						}
					}
				}
			}
			
			return result;
		}
		
		public float getMeanLineSize() {
			float result = 0;
			
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if (matrix[i][j]) {
						result++;
					}
				}
			}
			
			return result/size;
		}
	}
	
	private class Index {
		private String[] sIndex;
		private TreeMap<String,Integer> tIndex;
		
		public Index(Set<String> set) {
			sIndex = (String[]) set.toArray();			
			tIndex = new TreeMap<String,Integer>();
			for (int i = 0; i < sIndex.length; i++) {
				tIndex.put(sIndex[i], i);
			}
		}
		
		public int indexOf (String word) {
			return tIndex.get(word);
		}
		
		public String wordAt (int i) {
			return sIndex[i];
		}
		
		public int length() {
			return sIndex.length;
		}
	}
	
	private Index index;
	
	private BMatrix Bmatrix;
	
	private float[] scores;
	
	private float score = 0;
	
	private int size;
	
	/**
	 * Class constructor.
	 * @param matrix	the matrix representing, for each word of the dictionary, the set of its definition's words. String elements in the TreeSet values of the parameter must be keys contained in the TreeMap.
	 * @throws Exception	if a String element of the matrix parameter is not a key of matrix.
	 */
	public Scores(TreeMap<String,TreeSet<String>> matrix) throws Exception {
		index = new Index(matrix.keySet());
		scores = new float[matrix.size()];
		size = matrix.size();
		
		for (TreeSet<String> set : matrix.values()) {
			for (String word : set) {
				if (!matrix.containsKey(word)) {
					throw new Exception("Word " + word + "of some definition is not mapped.");
				}
			}
		}
		
		// Builds Bmatrix
		Bmatrix = new BMatrix(matrix.size());
		for (int i = 0; i < index.length(); i++) {
			String parent = index.wordAt(i);
			
			Set<String> set = matrix.get(parent);
			for (String child : set) {
				Bmatrix.matrix[i][index.indexOf(child)] = true;
			}
		}
		System.out.println("Built binary transition matrix.");	
	}
	
	private float calculateScore(int k) {
		float result = 0;
		
		BMatrix mult = new BMatrix(Bmatrix);
		
		while (!mult.matrix[k][k]) {
			mult = mult.mult(Bmatrix);
			result++;
		}
		
		result /= size;
		scores[k] = result;
		return result;
	}
	
	/**
	 * Returns the given word's score.
	 * The result is stored for the next call for the same word.
	 * A word's score is the shortest loop divided by the dictionary size.
	 * @param word	word to get the score for.
	 * @return the score of the word
	 */
	public float getScore(String word) {
		float result = scores[index.indexOf(word)];
		if (result != 0) return result;
		else return calculateScore(index.indexOf(word));
	}
	
	/**
	 * Returns the dictionary's score.
	 * The result is stored for the next call.
	 * @return the score of the dictionary.
	 */
	public float getScore() {
		if (score == 0) {
			for (String word : index.sIndex) {
				score += getScore(word); 
			}
		}
		return score;
	}
	
	/**
	 * Returns the size of the dictionary.
	 * @return the size of the dictionary.
	 */
	public int getSize() {
		return size;
	}
}
