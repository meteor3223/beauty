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

def get_pages(url):
    """
    获取妹子图自拍网站的页数
    """
    req = urllib2.Request(url, headers=hdr)
    html=urllib2.urlopen(req).read()
    soup=BeautifulSoup(html)
    nums=soup.find_all('span',class_='page-numbers current')
    pages=int(nums[0].text)
    return pages


def get_menu(url):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urllib2.urlopen(req).read()
    soup=BeautifulSoup(html)
    menu=[]
    menu_list=soup.find_all('div',class_='comment-body')

    for i in menu_list:
        result=i.find_all('p')
        if result:
            url=result[0].find_all('img')[0]['src']
            menu.append(url)
    return menu


def main(page):
    """
    下载第page页的妹子图
    """
    print u'正在下载第 %d 页' % page
    page_url=url_zipai+str(page)
    menu=get_menu(page_url)
    pic_file_index=str(page)

    print u'@@@@@@@@@@@@@@@@第 %d 页共有 %d 张图片@@@@@@@@@@@@@@@@' %(page,len(menu))

    file_list_name=os.path.join('Beauty','mzitu_zipai','pic_list_mzitu_zipai_'+current_time)
    file_list = open(file_list_name, "a+")
    file_list.write('mzitu_zipai/'+pic_file_index)
    file_list.write('\r\n')
    file_list.close()

    pics_url=[]
    pics_name=[]
    pics_width=[]
    pics_height=[]
    des=''
    address='local_zipai_'+pic_file_index

    pic_nums = len(menu)

    for i in range(0,pic_nums):
        try:
            image=menu[i]
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
            print page_url
            print Exception,":",ex
            file_log = open(file_log_name, "a+")
            file_log.write('\r\n');
            str_log=u'***************出错url：%s******************' %page_url
            file_log.write(str_log);
            file_log.close

    pic_file_name = os.path.join('Beauty','mzitu_zipai','data',pic_file_index)
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
    url='http://www.mzitu.com/share'
    url_zipai='http://www.mzitu.com/share/comment-page-'
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    pages=get_pages(url)
    str_log=u'***************妹子图自拍一共有 %d 页******************' %pages
    print str_log
    str_log2=u'***************注意倒序排列:第一页是 %d 页*******************' %pages
    print str_log2
    print current_time
    file_log_name=os.path.join('Beauty','beauty_scrapy_log')
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write('\r\n');
    file_log.write(current_time);
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.write('\r\n');
    file_log.write(str_log2);
    file_log.close
    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/mzitu_zipai'):
        os.mkdir('Beauty/mzitu_zipai')
    if not os.path.exists('Beauty/mzitu_zipai/data'):
        os.mkdir('Beauty/mzitu_zipai/data')

    file_list_name_1=os.path.join('Beauty','mzitu_zipai','pic_list_mzitu_zipai_'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close

    page_start=input(u'Input the first page number:\n')
    page_end=input(u'Input the last page number:\n')
    str_log=u'***************输入--开始页%d，结束页%d******************' %(page_start,page_end)
    if page_start==page_end:
        str_log2=u'***************实际--开始页%d，结束页%d******************' %(page_start,page_end)
    else:
        str_log2=u'***************实际--开始页%d，结束页%d******************' %(page_start,page_end+1)
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.write('\r\n');
    file_log.write(str_log2);
    file_log.close
    if page_end<page_start:
        for page in range(page_start,page_end,-1):
            main(page)
    elif page_end==page_start:
        main(page_end)
    else:
        print u"输入错误，起始页必须大于等于结束页\n"
    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close
