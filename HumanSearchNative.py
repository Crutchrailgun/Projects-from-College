from matplotlib import text


def native_string_search(file, pattern):

    file = input("Enter the file name: ")
    pattern = input("Enter the pattern to search for: ")
    #number of instances of the pattern in the file
    count = 0
    #number of runs
    runs = 0

    with open(file, 'r') as f:
        content = f.read()

        n = len(content)
        m = len(pattern)

        for i in range(n - m + 1):
            if content[i:i + m] == pattern:
                count += 1
            runs += 1


        print(f"Number of runs: {runs}")      
            
        
    return count




if __name__ == "__main__":
    print(native_string_search(None, None))
    