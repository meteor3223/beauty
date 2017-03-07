import urllib2, os, os.path, urllib, cStringIO , Image, time
from bs4 import BeautifulSoup

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )
import json
import re

def jsonCheckReplace(string):
    newstr=""
    for i, ch in enumerate(string):
        if ch==('\"'):
            newstr+='\\\"'
            continue
        if ch==('\\'):
            newstr+='\\\\'
            continue
        if ch==('\r'):
            newstr+='\\r'
            continue
        if ch==('\n'):
            newstr+='\\n'
            continue
        if ch==('\t'):
            newstr+='\\t'
            continue
        newstr+=ch
    return newstr

if __name__=='__main__':
    test="cdfdfdfd\"\'\r\n"
    print test
    test=jsonCheckReplace(test)
    print test
    sep=os.sep
    rootdir=os.getcwd()+sep+"data"+sep
    for parent,dirnames,filenames in os.walk(rootdir):
        for filename in filenames:
            try:
                file_absolute_path=os.path.join(parent,filename)
                f = file(file_absolute_path);
                s = json.load(f)
                f.close
            except Exception,ex:
                print filename