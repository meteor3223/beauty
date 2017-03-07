# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         wucl
# Version:        1.0
# Program:        自动下载妹子图的图片并保存到本地
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


black_url_array=['77722'
                ]


def get_menu(url):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urllib2.urlopen(req).read()
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
    html=urllib2.urlopen(req).read()
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
    html=urllib2.urlopen(req).read()
    soup=BeautifulSoup(html)
    image=soup.find_all('p')[0].find_all('img')[0]['src']
    urllib.urlretrieve(image,filename)

def main(input_url):
    menu=get_menu(input_url)
    print input_url
    print u'@@@@@@@@@@@@@@@@共有 %d 个主题@@@@@@@@@@@@@@@@' %len(menu)

    for i in menu:
        pic_nums=int(get_links(i[1]))
        print u'\n\n\n*******主题 %s 一共有 %d 张图片******\n' %(i[0],pic_nums)
        des = i[0]
        tmep_len = len(i[1]) - 21
        address = "local_mzitu_best_"+i[1][-tmep_len:]
        pic_file_index = i[1][-tmep_len:]
        pics_url=[]
        pics_name=[]
        pics_width=[]
        pics_height=[]

        if pic_file_index in black_url_array:
            continue

        file_list_name=os.path.join('Beauty','mzitu_best','pic_list_mzitu_best_'+current_time)
        file_list = open(file_list_name, "a+")
        file_list.write('mzitu/'+pic_file_index)
        file_list.write('\r\n')
        file_list.close()

        pic_file_name = os.path.join('Beauty','mzitu_best','data',pic_file_index)
        if os.path.exists(pic_file_name):
            print u'@@@@@@@@@@@@@@@@g该主题已经获取@@@@@@@@@@@@@@@@'
        else:
            for pic in range(1,pic_nums+1):
                try:
                    pic_url=i[1]+'/'+str(pic)
                    req = urllib2.Request(pic_url, headers=hdr)
                    html=urllib2.urlopen(req).read()
                    soup=BeautifulSoup(html)
                    image=soup.find_all('p')[0].find_all('img')[0]['src']
                    req = urllib2.Request(image, headers=hdr)
                    fileIm=urllib2.urlopen(req).read()
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
    url_best='http://www.mzitu.com/best'
    url_hot='http://www.mzitu.com/hot'
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())

    str_log=u'***************妹子图best一共有 2 页******************'
    print str_log
    print current_time
    file_log_name=os.path.join('Beauty','beauty_scrapy_log')
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write('\r\n');
    file_log.write(current_time);
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close
    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/mzitu_best'):
        os.mkdir('Beauty/mzitu_best')
    if not os.path.exists('Beauty/mzitu_best/data'):
        os.mkdir('Beauty/mzitu_best/data')
    file_list_name_1=os.path.join('Beauty','mzitu_best','pic_list_mzitu_best_'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close

    main(url_hot)

    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close

