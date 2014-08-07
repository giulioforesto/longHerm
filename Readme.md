LongHerm
========
Hermeneutic length of a dictionary

##Context and purpose
Hermeneutics is the science of interpretation of the human speech.
Since this interpretation is founded on the adoption of a *language*, that dictionaries aim to define, it would be interesting to define the *quality* of a dictionary.

One of the measures of this quality could be what I call the **hermeneutic length of a dictionary**.

###Definition of the hermeneutic length
Let's assume that a dictionary is simply a set of words, each related by its definition to other words of the dictionary.
Then we see that by parsing recursively the definitions of all the words of a word's definition, with any start word, we can get soon or late back to the start word, no matter which path we followed through the definitions. This is what we call a **loop**.

We see that a word can have zero or several loops. We call **hermeneutic length of a word** the minimum loop length of this word.

###Score of a dictionary
Based on this hermeneutic length of a word for a given dictionary, the puropose is to define a **score** function that takes into account the dictionary's size and that associates to every word an intuitive quantity (e.g. a percentage). Based on the scores of the dictionary's word, one should then be able to define a **dictionary score** function whose result should be an intuitive quantity representing the quality of the dictionary (e.g. a percentage).

The **score function** that has been chosen for the moment is the simplest one:  
If `w` is a word, `l` the length of its shortest loop and `N` the size of the dictionary:

    S(w) = l_w/N

So the total **score of the dictionary** `S` would be the sum of the scores of the dictionary's words:

    S = Σ_w (S(w))

Another possibility would be the **geometric score**:

    S = N*(Π_w (S(w)))^1/N

##Project
###Modus operandi
As a first step, the code must build a square *boolean matrix* of the same size as the dictionary. Each word will be associated to an integer ranging from `0` to `N-1` via an index. Each line of the matrix will represend the corresponding word's definition, with `true` for each column corresponding to a word that appears in the line's word's definition, and `false` otherwise.

Once this matrix is built, the hermeneutic length of a given word will be easily obtained by multiplying the matrix with itself a certain number of times (with `&&` operator as multiplication and **`||` operator as addition** - even if it does not form a group, we don't care).

###Code plan

- **TransMatrix.java** builds a `TreeMap<String,TreeSet<String>>` representation of the transition matrix. It is not factored yet and abundantly depends on the structure of the dictionary and of its HTML pages. 
- **Scores.java** is a source-independent score calculator. It takes a `TreeMap<String,TreeSet<String>>` representation of the transition matrix and builds the associated boolean matrix, from which it can the calculate different values such as the mean number of relevant words in a definition, the score of a word or the score of the dictionary.

###Relevance of a word  