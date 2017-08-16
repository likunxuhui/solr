package com.likun.app;

/**
 * Created by likun(li.k@ikcrm.com) on 2017/8/16.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.likun.entity.Book;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class SolrMain {
    //指定solr服务器的地址
    private final static String SOLR_URL = "http://localhost:88/solr/";

    /**
     * 创建SolrServer对象
     *
     * 该对象有两个可以使用，都是线程安全的
     * 1、CommonsHttpSolrServer：启动web服务器使用的，通过http请求的
     * 2、 EmbeddedSolrServer：内嵌式的，导入solr的jar包就可以使用了
     * 3、solr 4.0之后好像添加了不少东西，其中CommonsHttpSolrServer这个类改名为HttpSolrClient
     *
     * @return
     */
    public HttpSolrClient createSolrServer(){
        HttpSolrClient solr = null;
        solr = new HttpSolrClient(SOLR_URL);
        return solr;
    }


    /**
     * 往索引库添加文档
     * @throws IOException
     * @throws SolrServerException
     */
    public void addDoc() throws SolrServerException, IOException{
        System.out.println("***********创建索引开始******************");
        //构造一篇文档
        List<SolrInputDocument> list = new ArrayList<SolrInputDocument>();
        for(int i=0;i<10;i++){
            SolrInputDocument document = new SolrInputDocument();
            //往doc中添加字段,在客户端这边添加的字段必须在服务端中有过定义
            document.addField("id", i);
            document.addField("name", "周新星"+i);
            document.addField("price", 12.7);
            list.add(document);
        }

        //获得一个solr服务端的请求，去提交 ,选择具体的某一个solr core
        HttpSolrClient solr = new HttpSolrClient(SOLR_URL + "book");
        solr.add(list);
        solr.commit();
        solr.close();
        System.out.println("***********创建索引结束******************");
    }


    /**
     * 根据id从索引库删除文档
     */
    public void deleteDocumentById() throws Exception {
        System.out.println("***********根据id删除索引开始******************");
        //选择具体的某一个solr core
        HttpSolrClient server = new HttpSolrClient(SOLR_URL+"book");
        //删除文档
        server.deleteById("8");
        //删除所有的索引
        //solr.deleteByQuery("*:*");
        //提交修改
        server.commit();
        server.close();
        System.out.println("***********根据id删除索引结束******************");
    }

    /**
     * 查询
     * @throws Exception
     */
    public void querySolr() throws Exception{
        System.out.println("***********查询索引开始******************");
        HttpSolrClient solrServer = new HttpSolrClient(SOLR_URL+"book/");
        SolrQuery query = new SolrQuery();
        //下面设置solr查询参数
        //query.set("q", "*:*");// 参数q 查询所有
        query.set("q","周星驰");//相关查询，比如某条数据某个字段含有周、星、驰三个字 将会查询出来 ，这个作用适用于联想查询

        //参数fq, 给query增加过滤查询条件
        query.addFilterQuery("id:[0 TO 9]");//id为0-4

        //给query增加布尔过滤条件
        //query.addFilterQuery("description:演员"); //description字段中含有“演员”两字的数据

        //参数df,给query设置默认搜索域
        query.set("df", "name");

        //参数sort,设置返回结果的排序规则
        query.setSort("id",SolrQuery.ORDER.desc);

        //设置分页参数
        query.setStart(0);
        query.setRows(10);//每一页多少值

        //参数hl,设置高亮
        query.setHighlight(true);
        //设置高亮的字段
        query.addHighlightField("name");
        //设置高亮的样式
        query.setHighlightSimplePre("<font color='red'>");
        query.setHighlightSimplePost("</font>");

        //获取查询结果
        QueryResponse response = solrServer.query(query);
        //两种结果获取：得到文档集合或者实体对象

        //查询得到文档的集合
        SolrDocumentList solrDocumentList = response.getResults();
        System.out.println("通过文档集合获取查询的结果");
        System.out.println("查询结果的总数量：" + solrDocumentList.getNumFound());
        //遍历列表
        for (SolrDocument doc : solrDocumentList) {
            System.out.println("id:"+doc.get("id")+"  name:"+doc.get("name")+"  price:"+doc.get("price"));
        }

        //得到实体对象
        List<Book> tmpLists = response.getBeans(Book.class);
        System.out.println("***********list******************"+tmpLists);
        if(tmpLists!=null && tmpLists.size()>0){
            System.out.println("通过文档集合获取查询的结果");
            for(Book book:tmpLists){
                System.out.println("id:"+book.getId()+"  name:"+book.getName()+"  price:"+book.getPrice());
            }
        }
        System.out.println("***********查询索引结束******************");
    }

    public static void main(String[] args) throws Exception {
        SolrMain solr = new SolrMain();
        //solr.createSolrServer();
        solr.addDoc();
        solr.deleteDocumentById();
        solr.querySolr();
    }
}