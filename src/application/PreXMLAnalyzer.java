package application;


import java.io.File;
import java.io.FileInputStream;
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


class PreXMLAnalyzer extends Thread{
    private File in;
    private String search;
    private ArrayList<String> result = new ArrayList<>();
    private boolean finished;
    private String element_string;

    public PreXMLAnalyzer (File in, String search) {
        this.in = in;
        this.search = search;
    }
    
    public synchronized void run() {
        try {
            FileInputStream is = new FileInputStream(in);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(search);
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            //System.out.println(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                element_string = element.getTextContent();
                element_string = element_string.trim();
                result.add(element_string);
            }
            
            finished = true;
        } catch (Exception e) {
            System.out.println("tmp_xml search failed");
            
        }
        
    }
    
    ArrayList<String> getResult() {
        return result;
    }

    boolean finished() {
        return finished;
    }
}
