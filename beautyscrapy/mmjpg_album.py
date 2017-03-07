# -*- coding:utf8 -*-
# Python:         2.7.8
# Platform:       Windows
# Author:         xym
# Version:        1.0
# Program:        自动下载妹子图的图片并保存到本地
# History:        2015.5.31

import urllib2, os, os.path, urllib, cStringIO , time
import re
import json
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
    print url
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    numsHtml=soup.find_all('em',class_='info')
    if numsHtml:
        numUnit=numsHtml[0].text[-1:]
        if numUnit=='页':
            numsStr=re.findall(r"\d+",numsHtml[0].text)
            pages=int(numsStr[0])
            return pages
        else:
            return 1
    else:
        return 1

tag_good_web_list=["ROSI","推女郎","秀人网","尤果网","第四印象","美媛馆","如壹写真","推女神","Beautyleg","美腿骇客","菠萝社","优星馆","魅妍社","爱蜜社","嗲囡囡","模范学院","蜜桃社","尤物馆","头条女神","尤蜜荟","花の颜","糖果画报","",""]

tag_black_list=["DISI"]

tag_sense_list=["性感","小清新","美胸","大胸女神","美臀","美腿","内衣","童颜巨乳","妩媚","爆乳","私房写真","制服诱惑","诱惑",">假面女皇","丰满","萌妹","可爱","嫩模","甜美","E杯奶茶",""]

def get_zhuanti(url):
    """
    获取妹子图网站的专题
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    menu=[]

    url='http://www.mmjpg.com/hot'
    tag='hot'
    name='最热'
    classify='null'
    menu.append([url,tag,name,classify])
    url='http://www.mmjpg.com/top'
    tag='best'
    name='推荐美图'
    classify='null'
    menu.append([url,tag,name,classify])

    menu_list=soup.find_all('div',class_='tag')[0].find_all('a',target='_blank')
    menu_nums=soup.find_all('div',class_='tag')[0].find_all('i')

    for i in range(len(menu_list)):
        if menu_nums[i].text=='共1套图':
            continue
        elif menu_nums[i].text=='共2套图':
            continue
        elif menu_nums[i].text=='共3套图':
            continue
        else:
            url=menu_list[i]['href']
            tag_url_index = len(url) - 25
            tag=url[-tag_url_index:]
            name=menu_list[i].text
            if name in tag_black_list:
                continue
            elif name in tag_good_web_list:
                classify='名站写真'
            elif name in tag_sense_list:
                classify='视觉'
            else:
                classify='妹子'
            menu.append([url,tag,name,classify])
    return menu

def get_tag_des(url):
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    summary=soup.find_all('div',class_='summary')
    summary_text=''
    if summary:
        summary_text=summary[0].find_all('span')[0].text
    return summary_text

def get_menu(url,tag):
    """
    获取页面的所有妹子图主题的链接名称和地址，记入列表
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    main=soup.find_all('div',class_='pic')
    menu=[]

    menu_list=main[0].find_all('a', target='_blank')
    for i in menu_list:
        result=i.find_all('img')
        if result:
            if result[0].has_attr('alt'):
                name=result[0]['alt']
                address=i['href']
                image=result[0]['src']
                width_temp=result[0]['width']
                height_temp=result[0]['height']
                menu.append([name,address,image,width_temp,height_temp])
    return menu

def get_links(url):
    """
    获取单个妹子图主题一共具有多少张图片
    """
    req = urllib2.Request(url, headers=hdr)
    html=urlOpenLoop(req)
    soup=BeautifulSoup(html)
    all_=soup.find_all('div', class_='page')
    nums=[]
    for i in all_:
        span=i.find_all('a')
        if span:
            nums.append(span[-2].text)
    return nums[0]

def get_image_num(index):
    pic_file_name = os.path.join('Beauty','mmjpg','data',index)
    file_content = open(pic_file_name, "a+")
    image_num=0
    try:
        all_the_text = file_content.read()
        print all_the_text
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
    if tag=='total':
        page_url=url+'/home/'+str(page)
    elif page==1:
        page_url=url
    else:
        page_url=url+'/'+str(page)
    menu=get_menu(page_url,tag)
    print u'@@@@@@@@@@@@@@@@第 %d 页共有 %d 个主题@@@@@@@@@@@@@@@@' %(page,len(menu))

    album_name=[]
    album_address=[]
    album_thumb=[]
    album_width=[]
    album_height=[]
    album_pics=[]

    for i in menu:
        tmep_len = len(i[1]) - 24
        album_index=i[1][-tmep_len:]
#        if album_index in black_url_array:
#            continue
        album_name.append(i[0])
        address_temp='mmjpg/'+album_index
        album_address.append(address_temp)
        album_thumb.append(i[2])
        album_width.append(i[3])
        album_height.append(i[4])
        pic_file_name = os.path.join('Beauty','mmjpg','data',album_index)
        print pic_file_name
        if os.path.exists(pic_file_name):
            pic_nums_str=get_image_num(album_index)+'P'
        else:
            print '############not exist:##########'+i[1]
            pic_nums_str=get_links(i[1])+'P'
        album_pics.append(pic_nums_str)

    pic_file_name=tag+'_page_'+str(page)
    album_list_name_1 = os.path.join('Beauty','mmjpg_album','album_data',pic_file_name)
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
    base_url='http://www.mmjpg.com/'
    zhuanti_url='http://www.mmjpg.com/more/'
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())
    print current_time

    if not os.path.exists('Beauty'):
        os.mkdir('Beauty')
    if not os.path.exists('Beauty/mmjpg_album'):
        os.mkdir('Beauty/mmjpg_album')
    if not os.path.exists('Beauty/mmjpg_album/album_data'):
        os.mkdir('Beauty/mmjpg_album/album_data')
    file_list_name_1=os.path.join('Beauty','mmjpg_album','mzitu_album_list')
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.write('[')
    file_list_1.close

    url=base_url
    tag='total'
    name='所有'
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
    file_list_1.close

    menu=get_zhuanti(zhuanti_url)
    for i in menu:
        url=i[0]
        tag=i[1]
        name=i[2]
        name=jsonCheckReplace(name)
        des=''
        classify=i[3]
        pages=url_parse_tag(url,tag,name,des,classify)
        file_list_1 = open(file_list_name_1, "a+")
        file_list_1.write(',')
        file_list_1.write('{\"tag_name\":\"'+name+'\",')
        file_list_1.write('\"tag_address_root\":\"album_data/'+tag+'_page_",')
        file_list_1.write('\"tag_page_num\":\"'+str(pages)+'\",')
        file_list_1.write('\"classify\":\"'+classify+'\",')


        des=get_tag_des(url)
        print des
        des=jsonCheckReplace(des)



        file_list_1.write('\"tag_des\":\"'+des+'\"')
        file_list_1.write('}')
        file_list_1.close

    file_list_1 = open(file_list_name_1, "a+")
    file_list_1.write(']')
    file_list_1.close
    str_log=u'***************结束******************'
    print str_log