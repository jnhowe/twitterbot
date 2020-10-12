import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class App {

    /*----------------------------------------------------------------------------*/
    private static final int ASCII = 128; // the number of characters

    private static Random random; // pseudo-random number generator

    // creates a Markov model of order k for the specified text
    private static class Markov {

        private final int k; // order of Markov model
        // tells you how many times kgram appears in the text.
        private final HashMap<String, Integer> kgramFreq = new HashMap<>();

        // tells youhow many times each character succeeds the k-gram in the text
        private final HashMap<String, int[]> charFreq = new HashMap<>();

        public Markov(String text, int k) {
            random = new Random();
            this.k = k;
            StringBuilder textString = new StringBuilder(text);
            textString.append(text, 0, k);

            for (int i = 0; i < text.length(); i++) {
                String temp = textString.substring(i, i + k);
                if (!kgramFreq.containsKey(temp)) {
                    kgramFreq.put(temp, 1);
                }
                else {
                    int count = kgramFreq.get(temp);
                    kgramFreq.put(temp, count + 1);
                }

                char character = textString.charAt(i + k);
                if (!charFreq.containsKey(temp)) {
                    charFreq.put(temp, new int[ASCII + 1]); // extra is for emojis
                }
                int[] characterArray = charFreq.get(temp);
                // logic deals with emojis
                if (character > 127) {
                    characterArray[128]++;
                }
                else {
                    characterArray[character]++;
                }
            }
        }


        // returns the order k of this Markov model
        public int order() {
            return k;
        }

        // returns a string representation of the Markov model (as described below)
        public String toString() {
            // st2 is the second symbol table
            // (corresponding to the two-argument freq() method)
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, int[]> table : charFreq.entrySet()) {
                String key = table.getKey();
                result.append(key);
                result.append(":");

                // get the character frequency array
                int[] frequency = charFreq.get(key);

                // for each non-zero entry, append the character and the frequency
                for (int i = 0; i < ASCII; i++) {
                    if (!(frequency[i] == 0)) {
                        result.append(" " + (char) i + " " + frequency[i]);
                    }
                }

                // append a newline character
                result.append("\n");
            }
            return result.toString();
        }

        // returns the number of times the specified kgram appears in the text
        public int freq(String kgram) {
            if (!(kgram.length() == k)) {
                throw new IllegalArgumentException("kgram is not of length k");
            }
            if (!(kgramFreq.containsKey(kgram))) {
                return 0;
            }

            return kgramFreq.get(kgram);
        }

        // returns the number of times the character c follows the specified
        // kgram in the text
        public int freq(String kgram, char c) {
            if (!(kgram.length() == k)) {
                throw new IllegalArgumentException("kgram is not of length k");
            }

            if (!(kgramFreq.containsKey(kgram))) {
                return 0;
            }

            int[] letterProbability = charFreq.get(kgram);
            return letterProbability[c];
        }

        // returns a random character that follows the specified kgram in the text,
        // chosen with weight proportional to the number of times that character
        // follows the specified kgram in the text
        public char random(String kgram) {

            if (!(kgram.length() == k)) {
                throw new IllegalArgumentException("kgram is not of length k");
            }

            // exception does not work because of emojis
            //todo
            // if (!(kgramFreq.containsKey(kgram))) {
            //     throw new IllegalArgumentException("kgram does not appear in the "
            //                                                + "text");
            // }
            int[] frequencies = charFreq.get(kgram);
            int sum = 0;
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] < 0)
                    throw new IllegalArgumentException(
                            "array entry " + i + " must be nonnegative: " + frequencies[i]);
                sum += frequencies[i];
            }
            int r = random.nextInt(sum);
            sum = 0;
            for (int i = 0; i < frequencies.length; i++) {
                sum += frequencies[i];
                if (sum > r) {
                    return (char) i;
                }
            }
            return '\0';
        }
    }


    // tests this class by directly calling all instance methods
    public static void main(String[] args) {
        Random rando = new Random();
        Twitter twitter = TwitterFactory.getSingleton();
        int k = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        String user = args[2];
        try {
            // Status status = twitter.updateStatus("TestingSystem_again.java");
            List<Status> statuses = new ArrayList<Status>();
            Paging page = new Paging(1, 2);
            int p = 1;
            while (p <= 10) {
                page.setPage(p);
                statuses.addAll(twitter.getUserTimeline(user, page));
                p++;
            }
            int length = statuses.size();
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < length; i++) {
                text.append(statuses.get(length - i - 1).getText());
            }
            Markov markov = new Markov(text.toString(), k);
            // String kgram = text.substring(0, k); // creates new kgram
            String kgram = "";
            for (int i = 0; i < k; i++) {
                kgram += (char) rando.nextInt(ASCII);
            }
            System.out.print(kgram); // prints first k characters
            StringBuilder sbKgram = new StringBuilder(kgram);
            for (int i = 1; i < t - k + 1; i++) {

                char next = markov.random(kgram); // computes random character

                System.out.print(next); // prints new character to output text

                sbKgram.append(next);

                kgram = sbKgram.substring(i, i + k); // creates new kgram

            }

        }
        catch (TwitterException name) {
            System.out.println("You don't have internet connection.");
        }

        // System.out.println("Status was sent");
    }
}

