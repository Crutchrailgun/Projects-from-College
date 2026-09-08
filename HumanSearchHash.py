from matplotlib import text
import time
from collections import defaultdict, Counter

def get_file_content(file):
    with open(file, 'r') as f:
        content = f.read()
    return content
