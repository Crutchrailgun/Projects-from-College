from matplotlib import text


def hash_string_search(file, pattern):

    file = input("Enter the file name: ")
    pattern = input("Enter the pattern to search for: ")
    #number of instances of the pattern in the file
    count = 0
    #number of runs
    runs = 0

    with open(file, 'r') as f:
        content = f.read()

      