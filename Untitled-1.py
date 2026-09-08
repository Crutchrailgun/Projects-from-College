from matplotlib import text

def native_string_search(file, pattern):

    file = input("Enter the file name: ")
    pattern = input("Enter the pattern to search for: ")
    #number of instances of the pattern in the file
    count = 0

    with open(file, 'r') as f:
        content = f.read()
        if pattern in content:
            count+= 1


    return count


if __name__ == "__main__":
    print(native_string_search(None, None))
