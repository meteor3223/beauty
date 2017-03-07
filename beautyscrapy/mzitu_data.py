# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         wucl
# Version:        1.0
# Program:        自动下载妹子图的图片并保存到本地
# History:        2015.5.31

import urllib2, os, os.path, urllib, cStringIO , time
from bs4 import BeautifulSoup
from PIL import Image

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )

hdr = {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
        'Accept-Encoding': 'none',
        'Accept-Language': 'en-US,en;q=0.8',
        'Connection': 'keep-alive'}


black_url_array=['77722',
                '76128',
                '77443',
                '74100',
                '76974',
                '75263',
                '74394',
                '75769',
                '74438',
                '74205',
                '75636',
                '73288',
                '73995',
                '76307',
                '74315',
                '75909',
                '77323',
                '74731',
                '73654',
                '77136',
                '73404',
                '72816',
                '72684',
                '75167',
                '77671',
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
    nums=soup.find_all('a',class_='page-numbers')
    pages=int(nums[-2].text)
    return pages


def get_menu(url):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    menu=[]
    menu_list=soup.find_all('a',target='_blank')
    for i in menu_list:
        result=i.find_all('img',class_='lazy')
        if result:
            name=result[0]['alt']
            address=i['href']
            menu.append([name,address])
    return menu

def get_links(url):
    """
    获取单个妹子图主题一共具有多少张图片
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    all_=soup.find_all('a')
    nums=[]
    for i in all_:
        span=i.find_all('span')
        if span:
            nums.append(span[0].text)
    return nums[-2]

def get_image(url,filename):
    """
    从单独的页面中提取出图片保存为filename
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    image=soup.find_all('p')[0].find_all('img')[0]['src']
    urllib.urlretrieve(image,filename)

def main(page):
    """
    下载第page页的妹子图
    """
    print u'正在下载第 %d 页' % page
    page_url=url+'/page/'+str(page)
    menu=get_menu(page_url)
    print u'@@@@@@@@@@@@@@@@第 %d 页共有 %d 个主题@@@@@@@@@@@@@@@@' %(page,len(menu))

    for i in menu:
        tmep_len = len(i[1]) - 21
        address = "local_mzitu_"+i[1][-tmep_len:]
        pic_file_index = i[1][-tmep_len:]

        pic_file_name = os.path.join('Beauty','mzitu','data',pic_file_index)
        if os.path.exists(pic_file_name):
            print u'@@@@@@@@@@@@@@@@g该主题已经获取@@@@@@@@@@@@@@@@'
        else:
            pic_nums=int(get_links(i[1]))
            print u'\n\n\n*******主题 %s 一共有 %d 张图片******\n' %(i[0],pic_nums)
            des = i[0]
            des = jsonCheckReplace(des)
            pics_url=[]
            pics_name=[]
            pics_width=[]
            pics_height=[]

            if pic_file_index in black_url_array:
                continue

            file_list_name=os.path.join('Beauty','mzitu','pic_list_mzitu_'+current_time)
            file_list = open(file_list_name, "a+")
            file_list.write('mzitu/'+pic_file_index)
            file_list.write('\r\n')
            file_list.close()

            for pic in range(1,pic_nums+1):
                try:
                    pic_url=i[1]+'/'+str(pic)
                    req = urllib2.Request(pic_url, headers=hdr)
                    html=urlOpenLoop(req)
                    soup=BeautifulSoup(html)
                    image=soup.find_all('p')[0].find_all('img')[0]['src']
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

if __name__=='__main__':
    url='http://www.mzitu.com/'
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    pages=get_pages(url)
    str_log=u'***************妹子图一共有 %d 页******************' %pages
    print str_log
    print current_time

    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/mzitu'):
        os.mkdir('Beauty/mzitu')
    if not os.path.exists('Beauty/mzitu/data'):
        os.mkdir('Beauty/mzitu/data')
        
    file_log_name=os.path.join('Beauty','beauty_scrapy_log')
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write('\r\n');
    file_log.write(current_time);
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close

    file_list_name_1=os.path.join('Beauty','mzitu','pic_list_mzitu_'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close

    page_start=input(u'Input the first page number:\n')
    page_end=input(u'Input the last page number:\n')
    str_log=u'***************输入--开始页%d，结束页%d******************' %(page_start,page_end)
    str_log2=u'**************实际--开始页%d，结束页%d******************' %(page_start,page_end-1)
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.write('\r\n');
    file_log.write(str_log2);
    file_log.close
    if page_end>page_start:
        for page in range(page_start,page_end):
            main(page)
    elif page_end==page_start:
        main(page_end)
    else:
        print u"输入错误，起始页必须小于等于结束页\n"
    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close

