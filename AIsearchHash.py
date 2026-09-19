"""
Naive Pattern Matching with Timing Benchmark
---------------------------------------------
Reads a text file and a pattern from the user, searches for every
occurrence of the pattern using the naive (brute-force) O(n*m)
algorithm, and measures its efficiency. Instead of asking for a
number of runs, it repeats the search on its own until a fixed
time budget (TIME_BUDGET seconds) elapses, then reports how many
runs that produced and the resulting timing statistics.
"""

import time
import sys


def naive_pattern_search(text, pattern):
    """
    Naive (brute-force) pattern matching algorithm.

    Slides the pattern over the text one character at a time and
    checks for a match at each position. No preprocessing of the
    pattern (unlike KMP, Boyer-Moore, etc.).

    Returns a list of starting indices where `pattern` occurs in `text`.
    """
    n = len(text)
    m = len(pattern)
    occurrences = []

    if m == 0 or m > n:
        return occurrences

    for i in range(n - m + 1):
        j = 0
        while j < m and text[i + j] == pattern[j]:
            j += 1
        if j == m:
            occurrences.append(i)

    return occurrences


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


TIME_BUDGET = 1.0  # seconds of wall-clock time to spend benchmarking


def autorun(text, pattern, time_budget=TIME_BUDGET):
    """
    Keep calling naive_pattern_search repeatedly until `time_budget`
    seconds of total wall-clock time have elapsed. This lets the
    algorithm decide its own number of runs: fast searches get run
    many times (for a stable average), slow searches get run just a
    few times, and the caller never has to guess a run count.

    Returns (occurrences, run_times).
    """
    occurrences = []
    run_times = []
    total_time = 0.0

    while total_time < time_budget:
        start = time.perf_counter()
        occurrences = naive_pattern_search(text, pattern)
        elapsed = time.perf_counter() - start
        run_times.append(elapsed)
        total_time += elapsed

    return occurrences, run_times


def main():
    filepath = input("Enter path to the text file: ").strip()
    pattern = input("Enter the pattern to search for: ")

    if not pattern:
        print("Pattern is empty, nothing to search.")
        return

    text = load_text_file(filepath)

    print(f"Benchmarking for ~{TIME_BUDGET:.1f} second(s)...")
    occurrences, run_times = autorun(text, pattern)

    runs = len(run_times)
    total_time = sum(run_times)
    avg_time = total_time / runs

    print("\n--- Results ---")
    print(f"Text length:          {len(text)} characters")
    print(f"Pattern:              '{pattern}' (length {len(pattern)})")
    print(f"Occurrences found:    {len(occurrences)}")
    if len(occurrences) <= 20:
        print(f"Positions:            {occurrences}")
    else:
        print(f"Positions (first 20): {occurrences[:20]} ...")
    print(f"Number of runs:       {runs}")
    print(f"Total time:           {total_time:.6f} seconds")
    print(f"Average time/run:     {avg_time:.6f} seconds")
    print(f"Min / Max run time:   {min(run_times):.6f} / {max(run_times):.6f} seconds")


if __name__ == "__main__":
    main()