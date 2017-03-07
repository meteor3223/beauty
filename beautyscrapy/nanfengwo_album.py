# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         xym
# Version:        1.0
# Program:        自动下载妹子图的图片并保存到本地
# History:        2015.5.31

import urllib2, os, os.path, urllib, cStringIO , Image, time
import re
import json
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

def urlOpenLoop(req):
    fails = 0
    while True:
        try:
            if fails>=20:
                break
            html=urllib2.urlopen(req, timeout=3).read()
        except:
            fails+=1
            print '网络连接出现问题, 正在尝试再次请求: ', fails
        else:
            return html

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

black_url_array=[]

def get_pages(url):
    """
    获取妹子图网站的页数
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    pageRoot=soup.find_all('div',class_='listpages')
    if pageRoot[0].find_all('a'):
        numsHtml=pageRoot[0].find_all('a')[-1]['href']
        numsStr=re.findall(r"\d+",numsHtml)
        pages=int(numsStr[0])
    else:
        pages=1
    return pages

def get_menu(url,tag):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    menu_list=soup.find_all('div',class_='minlistpic')
    menu=[]

    for i in menu_list:
        result=i.find_all('a', target='_blank')
        if result:
            address=result[0]['href']
            detail=result[0].find_all('img')
            name=detail[0]['alt']
            image=detail[0]['src']
            width_temp=detail[0]['width']
            height_temp=detail[0]['height']
            menu.append([name,address,image,width_temp,height_temp])
    return menu

def get_links(url):
    """
    获取单个妹子图主题一共具有多少张图片
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    linkRoot=soup.find_all('div', class_='wlinkpages')
    numsHtml=linkRoot[0].find_all('a')[-1]['href']
    numsStr=re.findall(r"\d+",numsHtml)
    pages=int(numsStr[-1])
    return pages

def get_image_num(index):
    pic_file_name = os.path.join('Beauty','nanfengwo','data',index)
    file_content = open(pic_file_name, "a+")
    image_num=0
    try:
        all_the_text = file_content.read( )
        s = json.loads(all_the_text)
        image_num=len(s['pic_total'])
    finally:
       file_content.close( )
    return str(image_num)

def main(url,page,tag):
    """
    下载第page页的妹子图
    """
    print u'正在下载第 %d 页' % page
    if page==1:
        page_url=url
    else:
        page_url=url+'index_'+str(page)+'.html'
    print page_url
    menu=get_menu(page_url,tag)
    print u'@@@@@@@@@@@@@@@@第 %d 页共有 %d 个主题@@@@@@@@@@@@@@@@' %(page,len(menu))

    album_name=[]
    album_address=[]
    album_thumb=[]
    album_width=[]
    album_height=[]
    album_pics=[]

    for i in menu:
        index_array=re.findall(r"\d+",i[1])
        album_index=str(index_array[-1])
#        if album_index in black_url_array:
#            continue
        album_name.append(i[0])
        address_temp='nanfengwo/'+album_index
        album_address.append(address_temp)
        album_thumb.append(i[2])
        album_width.append(i[3])
        album_height.append(i[4])
        pic_file_name = os.path.join('Beauty','nanfengwo','data',album_index)
        if os.path.exists(pic_file_name):
            pic_nums_str=get_image_num(album_index)+'P'
        else:
            print '############not exist:##########'+i[1]
            pic_nums_str=str(get_links(i[1]))+'P'
        album_pics.append(pic_nums_str)
        cache_album.append([int(index_array[-1]),i[0],address_temp,i[2],pic_nums_str,i[3],i[4]])

    pic_file_name=tag+'_page_'+str(page)
    album_list_name_1 = os.path.join('Beauty','nanfengwo_album','album_data',pic_file_name)
    album_list_1 = open(album_list_name_1, "w+")

    album_list_1.write('[')
    album_list_1.write('\r\n')
    for album in range(0, len(album_name)):
        album_list_1.write('{')
        album_list_1.write('\r\n')
        album_name_json=jsonCheckReplace(album_name[album])
        album_list_1.write('\"album_name\":\"'+album_name_json+'\",')
        album_list_1.write('\"album_address\":\"'+album_address[album]+'\",')
        album_list_1.write('\"album_thumb\":\"'+album_thumb[album]+'\",')
        album_list_1.write('\"album_pics\":\"'+album_pics[album]+'\",')
        album_list_1.write('\"album_width\":\"'+album_width[album]+'\",')
        album_list_1.write('\"album_height\":\"'+album_height[album]+'\"')
        album_list_1.write('}')
        if album<(len(album_name)-1):
            album_list_1.write(',')

    album_list_1.write(']')
    album_list_1.close

def url_parse_tag(url,tag,name,des,classify):
    pages=get_pages(url)
    str_log=u'***************妹子图(%s)一共有 %d 页******************' %(tag,pages)
    print str_log
    page_start=1
    page_end=pages+1

    for page in range(page_start,page_end):
        main(url,page,tag)
    return pages

if __name__=='__main__':
    main_url_array=[
          'http://www.nanfengwo.com/meinv/qingchun/']

    main_tag_array=[
          'qingchun']

    main_name_array=[
          '清纯美女']

    cache_album=[]

    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    print current_time

    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/nanfengwo_album'):
        os.mkdir('Beauty/nanfengwo_album')
    if not os.path.exists('Beauty/nanfengwo_album/album_data'):
        os.mkdir('Beauty/nanfengwo_album/album_data')
    file_list_name_1=os.path.join('Beauty','nanfengwo_album','mzitu_album_list')
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.write('[')
    file_list_1.close

    for i, url in enumerate(main_url_array):
        url=main_url_array[i]
        tag=main_tag_array[i]
        name=main_name_array[i]
        name=jsonCheckReplace(name)
        des=''
        classify='null'
        pages=url_parse_tag(url,tag,name,des,classify)
        file_list_1 = open(file_list_name_1, "a+")

        file_list_1.write('{\"tag_name\":\"'+name+'\",')
        file_list_1.write('\"tag_address_root\":\"album_data/'+tag+'_page_",')
        file_list_1.write('\"tag_page_num\":\"'+str(pages)+'\",')
        file_list_1.write('\"classify\":\"'+classify+'\",')

        file_list_1.write('\"tag_des\":\"'+des+'\"')
        file_list_1.write('}')
        file_list_1.write(',')
        file_list_1.close

    cache_album.sort(key=lambda x:x[0],reverse=1)

    nums_one_page=17
    total = len(cache_album)
    total_pages=total/nums_one_page
    for page in range(0, total_pages):
        pic_file_name='total_page_'+str(page+1)
        album_list_name_1 = os.path.join('Beauty','nanfengwo_album','album_data',pic_file_name)
        album_list_1 = open(album_list_name_1, "w+")

        album_list_1.write('[')
        album_list_1.write('\r\n')
        album=0
        for album in range(0, nums_one_page):
            album_list_1.write('{')
            album_list_1.write('\r\n')
            album_name_json=jsonCheckReplace(cache_album[page*nums_one_page+album][1])
            album_list_1.write('\"album_name\":\"'+album_name_json+'\",')
            album_list_1.write('\"album_address\":\"'+cache_album[page*nums_one_page+album][2]+'\",')
            album_list_1.write('\"album_thumb\":\"'+cache_album[page*nums_one_page+album][3]+'\",')
            album_list_1.write('\"album_pics\":\"'+cache_album[page*nums_one_page+album][4]+'\",')
            album_list_1.write('\"album_width\":\"'+cache_album[page*nums_one_page+album][5]+'\",')
            album_list_1.write('\"album_height\":\"'+cache_album[page*nums_one_page+album][6]+'\"')
            album_list_1.write('}')
            if album<(nums_one_page-1):
                album_list_1.write(',')

        album_list_1.write(']')
        album_list_1.close

    tag='total'
    name='所有'
    des=''
    classify='null'

    file_list_1 = open(file_list_name_1, "a+")
    file_list_1.write('{\"tag_name\":\"'+name+'\",')
    file_list_1.write('\"tag_address_root\":\"album_data/'+tag+'_page_",')
    file_list_1.write('\"tag_page_num\":\"'+str(total_pages)+'\",')
    file_list_1.write('\"classify\":\"'+classify+'\",')
    file_list_1.write('\"tag_des\":\"'+des+'\"')
    file_list_1.write('}')

    file_list_1.write(']')
    file_list_1.close

    str_log=u'***************结束******************'
    print str_log

