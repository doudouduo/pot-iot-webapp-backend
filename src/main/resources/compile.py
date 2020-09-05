import os
import sys
compile_path=sys.argv[1]
project_path=os.path.dirname(os.path.realpath(__file__))
command="sh "+project_path+"/make.sh "+compile_path;
os.system(command)
