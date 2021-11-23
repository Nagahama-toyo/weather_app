package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class RegularXMLThread extends Thread{
    File in;
    String search;
    String result;
    private File out;
    public RegularXMLThread (File in, String search) {
        this.in = in;
        this.search = search;
    }
    public void run() {
        

        try {
            FileInputStream is = new FileInputStream(in);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            

            // 2.XPathの処理を実行するXPathのインスタンスを取得する
            XPath xpath = XPathFactory.newInstance().newXPath();
            // 3.XPathでの検索条件を作る
            XPathExpression expr = xpath.compile(search);
            // 4.DocumentをXPathで検索して、結果をDOMのNodeListで受け取る
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            // 5.XPathでの検索結果を持っているNodeListの内容でループ
            result = nodeList.item(0).getTextContent();
            System.out.println(result);
            /*
            for (int i = 0; i < nodeList.getLength(); i++) {
                result = nodeList.item(i).getTextContent();
                System.out.println(result);

            }
            */
            System.out.println("analyze finished");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
            String fileName = now.format(dateTimeFormatter);

            out = new File("C:/Users/toitt/Documents/B3_SecondSemester/DesignProject_Java/weather_app/tmp_xml/" + fileName + ".xml");
            Thread download = new Thread(new application.XMLDownloader(result, out));
            download.start();
            try {
                download.join();
              } catch (Exception e) {
                System.out.println("tmpXML download join failed");
            }
            System.out.println("tmpXML donwload finished");
            /*
            kuiki_search = "//Title";
            chiten_search = "//Title";
            nichibetu_search = "//Title";
            sevendays_search = "//Title";

            Thread kuiki = new PreXMLAnalyzer(out, kuiki_search);
            Thread chiten = new PreXMLAnalyzer(out, chiten_search);
            Thread nichibetu = new PreXMLAnalyzer(out, nichibetu_search);
            Thread sevendays = new PreXMLAnalyzer(out, sevendays_search);

            while (true){
                try {
                    Thread.sleep(1000L);

                } catch (InterruptedException e) {
                }

                if (kuiki.finished()){
                    break;
                }
            }
            System.out.println(kuiki.getResult());
            */
        }catch (Exception e) {
            System.out.println("analyze failed");
        }

    }

    File getResult(){
        return out;
    }
}