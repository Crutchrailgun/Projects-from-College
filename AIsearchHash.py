"""
Rabin-Karp Pattern Matching with Algorithmic Cost Counter
------------------------------------------------------------
Reads a text file and a pattern from the user, searches for every
occurrence of the pattern using the Rabin-Karp algorithm (rolling
hash), and measures its efficiency as *algorithmic cost*: the total
number of comparisons performed, split into

  hash comparisons       -- one per window, O(n - m + 1), always
  character comparisons  -- only spent verifying a window whose hash
                             matched the pattern's hash (a "candidate"),
                             including any spurious hits (hash
                             collisions that aren't real matches)

This mirrors the comparison-counting approach used in the naive
version, so the two can be compared directly on the same input.
"""

import sys

BASE = 256   # size of the character alphabet (treats bytes 0-255)
PRIME = 1_000_000_007  # large prime modulus, keeps hash collisions rare


def rabin_karp_search(text, pattern, base=BASE, prime=PRIME):
    """
    Rabin-Karp pattern matching algorithm.

    Computes a rolling hash of every m-length window of `text` and
    only falls back to a full character-by-character comparison when
    a window's hash matches the pattern's hash (a hash collision is
    still possible, so the character check confirms a real match).

    Returns (occurrences, hash_comparisons, char_comparisons):
      occurrences       -- list of starting indices where `pattern` occurs
      hash_comparisons  -- number of hash-vs-hash comparisons performed
      char_comparisons  -- number of character-vs-character comparisons
                            performed during verification of hash hits
    """
    n = len(text)
    m = len(pattern)
    occurrences = []
    hash_comparisons = 0
    char_comparisons = 0

    if m == 0 or m > n:
        return occurrences, hash_comparisons, char_comparisons

    # highest place-value factor, used to remove the leading character
    # from the rolling hash: base^(m-1) mod prime
    high_order = pow(base, m - 1, prime)

    pattern_hash = 0
    window_hash = 0
    for i in range(m):
        pattern_hash = (base * pattern_hash + ord(pattern[i])) % prime
        window_hash = (base * window_hash + ord(text[i])) % prime

    for i in range(n - m + 1):
        hash_comparisons += 1

        if window_hash == pattern_hash:
            # Candidate match -- verify character by character, since
            # equal hashes don't guarantee equal strings (collision).
            match = True
            for j in range(m):
                char_comparisons += 1
                if text[i + j] != pattern[j]:
                    match = False
                    break
            if match:
                occurrences.append(i)

        # Roll the hash forward by one character for the next window.
        if i < n - m:
            window_hash = (base * (window_hash - ord(text[i]) * high_order) + ord(text[i + m])) % prime
            if window_hash < 0:
                window_hash += prime

    return occurrences, hash_comparisons, char_comparisons


def load_text_file(filepath):
    try:
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            return f.read()
    except FileNotFoundError:
        print(f"Error: file '{filepath}' not found.")
        sys.exit(1)
    except OSError as e:
        print(f"Error reading file: {e}")
        sys.exit(1)


def main():
    filepath = input("Enter path to the text file: ").strip()
    pattern = input("Enter the pattern to search for: ")

    if not pattern:
        print("Pattern is empty, nothing to search.")
        return

    text = load_text_file(filepath)

    occurrences, hash_comparisons, char_comparisons = rabin_karp_search(text, pattern)
    total_comparisons = hash_comparisons + char_comparisons

    n = len(text)
    m = len(pattern)
    best_case = max(0, n - m + 1)                # every hash comparison is a mismatch, no verification needed
    worst_case = best_case + max(0, best_case * m)  # every window hashes equal (collision), full verify each time

    print("\n--- Results ---")
    print(f"Text length:              {n} characters")
    print(f"Pattern:                  '{pattern}' (length {m})")
    print(f"Occurrences found:        {len(occurrences)}")
    if len(occurrences) <= 20:
        print(f"Positions:                {occurrences}")
    else:
        print(f"Positions (first 20):     {occurrences[:20]} ...")
    print(f"Hash comparisons:         {hash_comparisons}")
    print(f"Character comparisons:    {char_comparisons}")
    print(f"Total comparisons:        {total_comparisons}")
    print(f"Best-case comparisons:    {best_case}  (n-m+1, no hash hits)")
    print(f"Worst-case comparisons:   {worst_case}  (n-m+1) + (n-m+1)*m, all hash collisions")
    if worst_case:
        print(f"Efficiency vs worst case: {total_comparisons / worst_case:.2%}")


if __name__ == "__main__":
    main()