import subprocess



    
with open("vlms\GitV.java", mode="w") as f :

    open = 'package vlms;\n\npublic class GitV {\n'
    close = '}'
    prefix = '\tstatic public String '

    part1 = str(subprocess.check_output("git describe"))
    part1 = prefix + 'gitRevNum = "' + part1[2:-3] + '";\n'

    part2 = str(subprocess.check_output("git log -1 --pretty=%B"))
    part2 = prefix + 'gitMsg = "' + part2[2:-5] + '";\n'

    #print(open + part1 + part2 + close)
    f.write(open + part1 + part2 + close)

    
    
    
    
    
    