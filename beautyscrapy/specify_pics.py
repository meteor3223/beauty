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

pics_list_index1='local_20161014_1039_8'

pics_url_array1=['http://images.17173.com/2016/news/2016/07/06/mj0706sg01s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg03s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg04s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg06s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg07s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg08s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg09s.jpg',
                'http://images.17173.com/2016/news/2016/07/06/mj0706sg10s.jpg'
                ]

pics_list_index2='local_20161014_1056_9'

pics_url_array2=['http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/369/kuxQFAXk5RGAAQAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/785/1BnkAgXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/929/2szNCgXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/305/nptO6ATk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/625/_OFnBgXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/689/DMlRFwXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/609/lM8EgAXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/193/6nuCpQXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/529/OkPEFQXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/17/kn0GDwXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/97/oDepEgXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/625/onVljwTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/673/xnhqdwTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/929/TAM3WQTk5RGAAAAAAAAAAA.jpg_2.jpg'
                ]

pics_list_index3='local_20161014_1058_10'

pics_url_array3=['http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/865/DvIG0JEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/561/Wh3Wz5Es5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/561/fkAcz5Es5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/673/cJWozpEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/81/ZK8YzpEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/465/cA2qzZEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/337/XPyOzJEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/65/cvzRtJEs5hGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/113/aGz8xwTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/785/ojEiDAXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/865/no8J8wTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/817/lObJDQXk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/929/tHhpPwTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/689/xDySBgTk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/529/XP4ruQPk5RGAAAAAAAAAAA.jpg_2.jpg',
                'http://mp.weiweixiao.net/Uploads/bhl2PG7j5RGAAQAAAAAAAA/353/ZkY3zgTk5RGAAAAAAAAAAA.jpg_2.jpg'
                ]


def main(list_file,url_array):
    pics_url=[]
    pics_name=[]
    pics_width=[]
    pics_height=[]
    des=''
    summarize=''
    address=list_file


    for i in url_array:
        pics_name.append('')
        pics_url.append(i)


    print u'@@@@@@@@@@@@@@@@共有 %d 张图片@@@@@@@@@@@@@@@@' %len(pics_url)

    file_list_name=os.path.join('Beauty','specify','pic_list_specify_'+current_time)
    file_list = open(file_list_name, "a+")
    file_list.write('specify/'+list_file)
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

    pic_file_name = os.path.join('Beauty','specify','data',list_file)
    file = open(pic_file_name, "w+")
    file.write('{')
    file.write('\r\n')
    file.write('\"des\":\"'+des+'\",')
    file.write('\"address\":\"'+address+'\",')
    file.write('\"hide\":0,')
    file.write('\"summarize\":\"'+summarize+'\",')
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
    current_time =time.strftime('%Y%m%d%H%M%S',time.localtime())

    str_log=u'***************一共有 x 图集******************'
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
    if not os.path.exists('Beauty/specify'):
        os.mkdir('Beauty/specify')
    if not os.path.exists('Beauty/specify/data'):
        os.mkdir('Beauty/specify/data')

    file_list_name_1=os.path.join('Beauty','specify','pics_list_specify'+current_time)
    file_list_1 = open(file_list_name_1, "w+")
    file_list_1.close

    main(pics_list_index1,pics_url_array1)
    main(pics_list_index2,pics_url_array2)
    main(pics_list_index3,pics_url_array3)

    str_log=u'***************结束******************'
    file_log = open(file_log_name, "a+")
    file_log.write('\r\n');
    file_log.write(str_log);
    file_log.close