"""
Naive Pattern Matching with Algorithmic Cost Counter
-----------------------------------------------------
Reads a text file and a pattern from the user, searches for every
occurrence of the pattern using the naive (brute-force) O(n*m)
algorithm, and measures its efficiency as *algorithmic cost*: the
total number of character comparisons performed. This is a
hardware-independent stand-in for "how much work did the algorithm
do", unlike wall-clock timing, which varies run to run and machine
to machine.
"""

import sys


def naive_pattern_search(text, pattern):
    """
    Naive (brute-force) pattern matching algorithm.

    Slides the pattern over the text one character at a time and
    checks for a match at each position. No preprocessing of the
    pattern (unlike KMP, Boyer-Moore, etc.).

    Returns (occurrences, comparisons):
      occurrences  -- list of starting indices where `pattern` occurs
      comparisons  -- total number of character comparisons performed
                       (the algorithmic cost)
    """
    n = len(text)
    m = len(pattern)
    occurrences = []
    comparisons = 0

    if m == 0 or m > n:
        return occurrences, comparisons

    for i in range(n - m + 1):
        j = 0
        while j < m:
            comparisons += 1
            if text[i + j] != pattern[j]:
                break
            j += 1
        else:
            occurrences.append(i)

    return occurrences, comparisons


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

    occurrences, comparisons = naive_pattern_search(text, pattern)

    n = len(text)
    m = len(pattern)
    worst_case = max(0, (n - m + 1) * m)  # comparisons if every check ran the full pattern length

    print("\n--- Results ---")
    print(f"Text length:            {n} characters")
    print(f"Pattern:                '{pattern}' (length {m})")
    print(f"Occurrences found:      {len(occurrences)}")
    if len(occurrences) <= 20:
        print(f"Positions:              {occurrences}")
    else:
        print(f"Positions (first 20):  {occurrences[:20]} ...")
    print(f"Comparisons performed:  {comparisons}")
    print(f"Worst-case comparisons: {worst_case}  (n-m+1)*m")
    if worst_case:
        print(f"Efficiency vs worst case: {comparisons / worst_case:.2%}")


if __name__ == "__main__":
    main()