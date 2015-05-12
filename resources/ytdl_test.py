'''
Created on May 10, 2015

@author: jkarnuta
'''
import sys
import time
try:
    print "first arg: "+sys.argv[1]
    print "second arg: "+sys.argv[2]
    print "third arg: "+sys.argv[3]
except:
    print "no args passed"

for i in range(10):
    time.sleep(0.2)
    print i