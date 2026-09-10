from matplotlib import text
import time
from collections import defaultdict, Counter
# grabbing the content of the file
def get_file_content(file):
    with open(file, 'r') as f:
        content = f.read()
    return content
# Rabin-Karp algorithm for pattern searching
def rabin_karp_search(file, pattern, q):
    file = input("Enter the file name: ")
    pattern = input("Enter the pattern to search for: ")
    # number of instances of the pattern in the file
    count = 0
    # number of runs
    runs = 0
    d = 256  # number of characters in the input alphabet
    m = len(pattern)
    n = len(get_file_content(file))
    p = 0  # hash value for pattern
    t = 0  # hash value for text
    i = 0
    j = 0
    h = 1
    # The value of h would be "pow(d, m-1)%q"
    for i in range(m-1):
        h = (h * d) % q
    # Calculate the hash value of pattern and first window of text
    for i in range(m):
        p = (d * p + ord(pattern[i])) % q
        t = (d * t + ord(get_file_content(file)[i])) % q

    # Slide the pattern over text one by one
    for i in range(n - m + 1):
        runs += 1
        if p == t:
            for j in range(m):
                if get_file_content(file)[i + j] != pattern[j]:
                    break
            j += 1
            if j == m:
                count += 1
    # Update hash value for next window of text: Remove leading digit, add trailing digit
        if i < n - m:
            t = (d * (t - ord(get_file_content(file)[i]) * h) + ord(get_file_content(file)[i + m])) % q
            if t < 0:
                t = t + q

    return count, runs
   

if __name__ == "__main__":
    print(rabin_karp_search(None, None,101))
    