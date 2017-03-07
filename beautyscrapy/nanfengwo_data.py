# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         wucl
# Version:        1.0
# Program:        自动下载妹子图的图片并保存到本地
# History:        2015.5.31

import urllib2, os, os.path, urllib, cStringIO , Image, time
import re
import gzip
import StringIO
import chardet
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


black_url_array=[
                ]

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


def get_menu(url):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    menu_list=soup.find_all('p',class_='minlisth')
    menu=[]
    for i in menu_list:
        result=i.find_all('a', target='_blank')
        if result:
            name=result[0].text
            address=result[0]['href']
            menu.append([name,address])
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

def get_image(url,filename):
    """
    从单独的页面中提取出图片保存为filename
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    image=soup.find_all('p')[0].find_all('img')[0]['src']
    urllib.urlretrieve(image,filename)

def main(url,page):
    """
    下载第page页的妹子图
    """
    print u'正在下载第 %d 页' % page
    if page==1:
        page_url=url
    else:
        page_url=url+'index_'+str(page)+'.html'
    print page_url
    menu=get_menu(page_url)
    print u'@@@@@@@@@@@@@@@@第 %d 页共有 %d 个主题@@@@@@@@@@@@@@@@' %(page,len(menu))

    for i in menu:
        index_array=re.findall(r"\d+",i[1])
        index_array[-1]
        pic_file_index = str(index_array[-1])
        pic_file_name = os.path.join('Beauty','nanfengwo','data',pic_file_index)
        if os.path.exists(pic_file_name):
            print u'@@@@@@@@@@@@@@@@g该主题已经获取@@@@@@@@@@@@@@@@'
        else:
            pic_nums=int(get_links(i[1]))
            print u'\n\n\n*******主题 %s 一共有 %d 张图片******\n' %(i[0],pic_nums)
            des = i[0]
            des = jsonCheckReplace(des)
            address = "local_nanfengwo_"+str(index_array[-1])

            pics_url=[]
            pics_name=[]
            pics_width=[]
            pics_height=[]

            if pic_file_index in black_url_array:
                continue

            file_list_name=os.path.join('Beauty','nanfengwo','pic_list_nanfengwo_'+current_time)
            file_list = open(file_list_name, "a+")
            file_list.write('nanfengwo/'+pic_file_index)
            file_list.write('\r\n')
            file_list.close()

            for pic in range(1,pic_nums+1):
                try:
                    if pic==1:
                        pic_url=url+pic_file_index+'.html'
                    else:
                        pic_url=url+pic_file_index+'_'+str(pic)+'.html'
                    req = urllib2.Request(pic_url, headers=hdr)
                    html=urlOpenLoop(req)
                    soup=BeautifulSoup(html)
                    image=soup.find_all('div', class_='contentpic')[0].find_all('img')[0]['src']
                    #image='http://img.nanfengwo.com/pic/'+pic_file_index+'/'+str(pic)+'.jpg'
                    print image
                    req = urllib2.Request(image, headers=hdr)
                    fileIm=urlOpenLoop(req)
                    tmpIm = cStringIO.StringIO(fileIm)
                    im = Image.open(tmpIm)
                    if im.size[0]>0 and im.size[1]>0:
                        pics_url.append(image)
                        pics_name.append('');
                        pics_width.append(im.size[0])
                        pics_height.append(im.size[1])
                except Exception,ex:
                    print pic_url
                    print Exception,":",ex
                    file_log = open(file_log_name, "a+")
                    file_log.write('\r\n');
                    str_log=u'***************出错url：%s******************' %pic_url
                    file_log.write(str_log);
                    file_log.close

            file = open(pic_file_name, "w+")
            file.write('{')
            file.write('\r\n')
            file.write('\"des\":\"'+des+'\",')
            file.write('\"address\":\"'+address+'\",')
            file.write('\"hide\":0,')
            file.write('\"summarize\":\"'+des+'\",')
            file.write('\"pic_total\":[')
            for pic2 in range(0, len(pics_url)):
                file.write('{')
                file.write('\r\n')
                file.write('\"pic_url\":\"'+pics_url[pic2]+'\",')
                file.write('\"pic_name\":\"'+pics_name[pic2]+'\",')
                file.write('\"width\":'+str(pics_width[pic2]))
                file.write(',')
                file.write('\"height\":'+str(pics_height[pic2]))
                file.write('}')
                if pic2<(len(pics_url)-1):
                    file.write(',')
            file.write(']}')
            file.close()

def main_root(url):
    pages=get_pages(url)
    str_log=u'***************妹子图一共有 %d 页******************' %pages
    print str_log
    print current_time

    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/nanfengwo'):
        os.mkdir('Beauty/nanfengwo')
    if not os.path.exists('Beauty/nanfengwo/data'):
        os.mkdir('Beauty/nanfengwo/data')

    file_log_name=os.path.join('Beauty','beauty_scrapy_log')
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write('\r\n');
    file_log.write(current_time);
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close

    file_list_name_1=os.path.join('Beauty','nanfengwo','pic_list_nanfengwo_'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close

    page_start=1
    page_end=page_start+pages

    for page in range(page_start,page_end):
        main(url,page)



if __name__=='__main__':
    main_url_array=[
          'http://www.nanfengwo.com/meinv/qingchun/',
          'http://www.nanfengwo.com/meinv/zipai/']
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    print current_time

    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/nanfengwo'):
        os.mkdir('Beauty/nanfengwo')
    if not os.path.exists('Beauty/nanfengwo/data'):
        os.mkdir('Beauty/nanfengwo/data')

    file_log_name=os.path.join('Beauty','beauty_scrapy_log')
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write('\r\n');
    file_log.write(current_time);
    file_log.close


    for url in main_url_array:
        main_root(url)

    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close
