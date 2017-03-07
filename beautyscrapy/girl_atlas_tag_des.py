# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         wucl
# Version:        1.0
# Program:        自动下载girl-atlas的图片并保存到本地
# History:        2015.5.31

import urllib2, os, os.path, urllib, cStringIO , Image, time
from bs4 import BeautifulSoup

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )

hdr = {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
        'Accept-Encoding': 'none',
        'Accept-Language': 'en-US,en;q=0.8',
        'Connection': 'keep-alive'}
hdr2 = {'User-Agent':'Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6'}

def jsonCheckReplace(string):
    newstr=""
    for i, ch in enumerate(string):
        if ch==('\"'):
            newstr+='\\\\\"'
            continue
        if ch==('\\'):
            newstr+='\\\\\\\\'
            continue
        if ch==('\r'):
            newstr+='\\\\r'
            continue
        if ch==('\n'):
            newstr+='\\\\n'
            continue
        if ch==('\t'):
            newstr+='\\\\t'
            continue
        newstr+=ch
    return newstr


def get_des(url):
    """
    获取页面的所有girl-atlas主题的链接名称和地址，记入列表
    """
    fails = 0
    while True:
        try:
            if fails>=20:
                break
            req = urllib2.Request(url, headers=hdr)
            html=urllib2.urlopen(req, timeout=3).read()
            soup=BeautifulSoup(html)
        except:
            fails+=1
            print '网络连接出现问题, 正在尝试再次请求: ', fails
        else:
            break

    deslist=soup.find_all('meta')

    des_json=""
    default_des="「美女图片集」是一个干净利落的美女图片分享社区。你会以绝佳的横向浏览方式欣赏媛馆、尤果网、推女郎等国模写真；杉原杏璃、筱崎爱、原干惠等日本女优的经典写真集怎能错过；还有欧美美女出境的黑白人体艺术摄影，脱而不露的致命诱惑。"
    des=deslist[1]['content']
    if des!=default_des:
        des_json=jsonCheckReplace(des)
    return des_json

if __name__=='__main__':
    url='http://www.girl-atlas.com/'
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')

    tag_name_path=os.path.join('Beauty','girl_atlas_tag_name')
    file_tag_name = open(tag_name_path, "w+")

    tag_des_path=os.path.join('Beauty','girl_atlas_tag_des')
    file_tag_des = open(tag_des_path, "w+")
    file_tag_des.close

    fails = 0
    while True:
        try:
            if fails>=20:
                break
            req = urllib2.Request(url, headers=hdr2)
            html=urllib2.urlopen(req,timeout=3).read()
            soup=BeautifulSoup(html)
        except:
            fails+=1
            print '网络连接出现问题, 正在尝试再次请求: ', fails
        else:
            break
    tag_list=soup.find_all('div',class_='tag')

    file_tag_name.write('[')
    for i in tag_list:
        file_tag_name.write('\"')
        name=i.find_all('a')[0].text
        file_tag_name.write(name)
        file_tag_name.write('\"')
        file_tag_name.write(',')
    file_tag_name.write(']')
    file_tag_name.close

    file_tag_des = open(tag_des_path, "a+")
    file_tag_des.write('[')
    for i in tag_list:
        file_tag_des.write('\"')
        finalurl='http://www.girl-atlas.com'+i.find_all('a')[0]['href']
        print finalurl
        tag_des=get_des(finalurl)
        print tag_des
        file_tag_des.write(tag_des)
        file_tag_des.write('\"')
        file_tag_des.write(',')
        time.sleep(1)

    file_tag_des.write(']')
    file_tag_des.close