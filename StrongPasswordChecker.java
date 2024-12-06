import java.io.*;
import java.net.URL;
import java.util.*;

public class StrongPasswordChecker {

    private static final int CHAINING_TABLE_SIZE = 1000;
    private static final int PROBING_TABLE_SIZE = 20000;

    public static void main(String[] args) throws IOException {
        // Initialize hash tables with both hash functions
        HashTableChaining chainingTable1 = new HashTableChaining(CHAINING_TABLE_SIZE, true);
        HashTableChaining chainingTable2 = new HashTableChaining(CHAINING_TABLE_SIZE, false);
        HashTableProbing probingTable1 = new HashTableProbing(PROBING_TABLE_SIZE, true);
        HashTableProbing probingTable2 = new HashTableProbing(PROBING_TABLE_SIZE, false);

        // Load dictionary
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL("https://www.mit.edu/~ecprice/wordlist.10000").openStream()));
        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            chainingTable1.insert(line, lineNumber);
            chainingTable2.insert(line, lineNumber);
            probingTable1.insert(line, lineNumber);
            probingTable2.insert(line, lineNumber);
            lineNumber++;
        }

        // Test passwords
        String[] testPasswords = {
                "account8",
                "accountability",
                "9a$D#qW7!uX&Lv3zT",
                "B@k45*W!c$Y7#zR9P",
                "X$8vQ!mW#3Dz&Yr4K5"
        };

        for (String password : testPasswords) {
            System.out.println("\nTesting password: " + password);
            checkPasswordStrength(password, chainingTable1, chainingTable2, probingTable1, probingTable2);
        }
    }

    private static void checkPasswordStrength(String password, HashTableChaining chainingTable1,
                                              HashTableChaining chainingTable2,
                                              HashTableProbing probingTable1,
                                              HashTableProbing probingTable2) {
        boolean isStrong = true;

        // Check minimum length
        if (password.length() < 8) {
            System.out.println("Password is too short.");
            isStrong = false;
        }

        // Check if password is a dictionary word or dictionary word + digit
        String passwordBase = password.replaceAll("\\d", "");
        boolean inDictionary = chainingTable1.contains(password) || chainingTable1.contains(passwordBase) ||
                chainingTable2.contains(password) || chainingTable2.contains(passwordBase) ||
                probingTable1.contains(password) || probingTable1.contains(passwordBase) ||
                probingTable2.contains(password) || probingTable2.contains(passwordBase);

        if (inDictionary) {
            System.out.println("Password is a dictionary word or a dictionary word followed by digits.");
            isStrong = false;
        }

        if (isStrong) {
            System.out.println("Password is strong.");
        }

        // Display costs
        System.out.println("Separate Chaining with hash function (x37): " + chainingTable1.getComparisonCount());
        System.out.println("Separate Chaining with hash function (x31): " + chainingTable2.getComparisonCount());
        System.out.println("Linear Probing with hash function (x37): " + probingTable1.getComparisonCount());
        System.out.println("Linear Probing with hash function (x31): " + probingTable2.getComparisonCount());
    }

    // Hash Table with Separate Chaining
    static class HashTableChaining {
        private LinkedList<Entry>[] table;
        private int comparisons = 0;
        private boolean useOldHash;

        public HashTableChaining(int size, boolean useOldHash) {
            this.table = new LinkedList[size];
            for (int i = 0; i < size; i++) {
                table[i] = new LinkedList<>();
            }
            this.useOldHash = useOldHash;
        }

        public void insert(String key, int value) {
            int index = hash(key);
            table[index].add(new Entry(key, value));
        }

        public boolean contains(String key) {
            int index = hash(key);
            for (Entry entry : table[index]) {
                comparisons++;
                if (entry.key.equals(key)) {
                    return true;
                }
            }
            return false;
        }

        private int hash(String key) {
            int hash = 0;
            if (useOldHash) { // Old hashCode implementation
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip) {
                    hash = (hash * 37) + key.charAt(i);
                }
            } else { // Modern hashCode implementation
                for (int i = 0; i < key.length(); i++) {
                    hash = (hash * 31) + key.charAt(i);
                }
            }
            return Math.abs(hash) % table.length;
        }

        public int getComparisonCount() {
            return comparisons;
        }
    }

    // Hash Table with Linear Probing
    static class HashTableProbing {
        private Entry[] table;
        private int comparisons = 0;
        private boolean useOldHash;

        public HashTableProbing(int size, boolean useOldHash) {
            this.table = new Entry[size];
            this.useOldHash = useOldHash;
        }

        public void insert(String key, int value) {
            int index = hash(key);
            while (table[index] != null) {
                index = (index + 1) % table.length;
            }
            table[index] = new Entry(key, value);
        }

        public boolean contains(String key) {
            int index = hash(key);
            while (table[index] != null) {
                comparisons++;
                if (table[index].key.equals(key)) {
                    return true;
                }
                index = (index + 1) % table.length;
            }
            return false;
        }

        private int hash(String key) {
            int hash = 0;
            if (useOldHash) { // Old hashCode implementation
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip) {
                    hash = (hash * 37) + key.charAt(i);
                }
            } else { // Modern hashCode implementation
                for (int i = 0; i < key.length(); i++) {
                    hash = (hash * 31) + key.charAt(i);
                }
            }
            return Math.abs(hash) % table.length;
        }

        public int getComparisonCount() {
            return comparisons;
        }
    }

    // Entry class
    static class Entry {
        String key;
        int value;

        public Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
