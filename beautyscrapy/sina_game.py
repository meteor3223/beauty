# -*- coding:utf8 -*-
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

sina_game_url_root='http://slide.games.sina.com.cn/'

sina_game_url_array=['slide_21_41065_434504','slide_21_41065_435352','slide_21_41065_426877','slide_21_41065_436564',
'slide_21_41065_436853','slide_21_41065_407119','slide_21_41065_389686','slide_21_41065_426559']



url_suffix='.html'


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

def main(url):
    """
    下载图集url的妹子图
    """
    print u'*******************正在下载图集%s****************' %url
    page_url=sina_game_url_root+url+url_suffix

    pics_url=[]
    pics_name=[]
    pics_width=[]
    pics_height=[]
    des=''
    summarize=''
    address='local_sina_game_'+url

    req = urllib2.Request(page_url, headers=hdr)
    html=urllib2.urlopen(req).read()
    soup=BeautifulSoup(html)
    des=soup.find_all('div',class_='nInfo')[0].text
    menu_list=soup.find_all('div',id='eData')

    for i in menu_list:
        result=i.find_all('dl')
        if result:
            for j in result:
                pics_name.append(j.find_all('dt')[0].text)
                pics_url.append(j.find_all('dd')[0].text)
                summarize=j.find_all('dd')[4].text

    print u'@@@@@@@@@@@@@@@@共有 %d 张图片@@@@@@@@@@@@@@@@' %len(pics_url)

    file_list_name=os.path.join('Beauty','sina_game','pic_list_sina_game_'+current_time)
    file_list = open(file_list_name, "a+")
    file_list.write('sina_game/'+url)
    file_list.write('\r\n')
    file_list.close()

    for i in range(0,len(pics_url)):
        try:
            image=pics_url[i]
            req = urllib2.Request(image, headers=hdr)
            fileIm=urllib2.urlopen(req).read()
            tmpIm = cStringIO.StringIO(fileIm)
            im = Image.open(tmpIm)
            if im.size[0]>0 and im.size[1]>0:
                pics_width.append(im.size[0])
                pics_height.append(im.size[1])
            else:
                pics_width.append(0)
                pics_height.append(0)
        except Exception,ex:
            print page_url
            print Exception,":",ex
            file_log = open(file_log_name, "a+")
            file_log.write('\r\n');
            str_log=u'***************出错url：%s******************' %page_url
            file_log.write(str_log);
            file_log.close

    pic_file_name = os.path.join('Beauty','sina_game','data',url)
    file = open(pic_file_name, "w+")
    file.write('{')
    file.write('\r\n')
    des_json=jsonCheckReplace(des)
    summarize_json=jsonCheckReplace(summarize)
    file.write('\"des\":\"'+summarize_json+'\",')
    file.write('\"address\":\"'+address+'\",')
    file.write('\"hide\":0,')
    file.write('\"summarize\":\"'+'\",')
    file.write('\"pic_total\":[')

    for pic2 in range(0, len(pics_url)):
        file.write('{')
        file.write('\r\n')
        file.write('\"pic_url\":\"'+pics_url[pic2]+'\",')
        pics_name_json=jsonCheckReplace(pics_name[pic2])
        file.write('\"pic_name\":\"'+pics_name_json+'\",')
        file.write('\"width\":'+str(pics_width[pic2]))
        file.write(',')
        file.write('\"height\":'+str(pics_height[pic2]))
        file.write('}')
        if pic2<(len(pics_url)-1):
            file.write(',')
    file.write(']}')
    file.close()


if __name__=='__main__':
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    pages=len(sina_game_url_array)
    str_log=u'***************sina游戏美女图一共有 %d 图集******************' %pages
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
    if not os.path.exists('Beauty/sina_game'):
        os.mkdir('Beauty/sina_game')
    if not os.path.exists('Beauty/sina_game/data'):
        os.mkdir('Beauty/sina_game/data')

    file_list_name_1=os.path.join('Beauty','sina_game','pic_list_sina_game_'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close


    for url in sina_game_url_array:
        main(url)

    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close