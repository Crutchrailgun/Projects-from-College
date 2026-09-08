from matplotlib import text
import time
from collections import defaultdict, Counter

def get_file_content(file):
    with open(file, 'r') as f:
        content = f.read()
    return content

def get_suffix_array(str):
    return sorted(range(len(str)), key=lambda i: str[i:])

def suffix_array_Manber(str):
    result = []
    def sort_bucket(str, bucket, order):
        d = defaultdict(list)
        for i in bucket:
            key = str[i:i + order]
            d[key].append(i)
        result = []
        for k in sorted(d):
            v = d[k]
            if len(v) > 1:
                result += sort_bucket(str, v, order * 2)
            else:
                result.append(v[0])
        return result

    return sort_bucket(str, range(len(str)), 1)
if __name__ == "__main__":
    file = input("Enter the file name: ")
    pattern = input("Enter the pattern to search for: ")
    content = get_file_content(file)
    start_time = time.time()
    suffix_array = suffix_array_Manber(content)
    end_time = time.time()
    print(f"Suffix array: {suffix_array}")
    print(f"Time taken to build suffix array: {end_time - start_time} seconds")
     
