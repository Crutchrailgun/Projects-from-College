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
        found_indices = []

        for i in range(n - m + 1):
            for j in range(m):
                runs += 1
                if content[i + j] != pattern[j]:
                    break
            else:
                count += 1
                found_indices.append(i)

        print(f"Number of runs: {runs}")      
            
        
    return len(found_indices)




if __name__ == "__main__":
    print(native_string_search(None, None))
    