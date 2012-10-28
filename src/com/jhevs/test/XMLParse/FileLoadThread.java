package com.jhevs.test.XMLParse;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class FileLoadThread extends Thread  {
	XMLParse parser;
	File XMLFile;
	JTable table;
	
	public FileLoadThread(XMLParse parser, File file, JTable table) {
		super();
		this.parser = parser;
		this.XMLFile = file;
		this.table = table;
	}
	
    public void run() {
    	System.out.println("thread");
    	try {
			this.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        f.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder;
		try {
			builder = f.newDocumentBuilder();
			Document doc = builder.parse(XMLFile);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("offer");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Map<String,String> row =new HashMap<String, String>();
					NodeList sList = nNode.getChildNodes();
					for (int stemp = 0; stemp < sList.getLength(); stemp++) {
						Node sNode = sList.item(stemp);
						if (sNode.getNodeType() == Node.ELEMENT_NODE) {
							//System.out.print(sNode.getNodeName() + " " );
							if (sNode.getFirstChild() != null) {
								row.put(sNode.getNodeName(), sNode.getFirstChild().getNodeValue());
							} else {
								row.put(sNode.getNodeName(), "");
							}
						}
					}
					DefaultTableModel  model = (DefaultTableModel) table.getModel();
					ListIterator<String> litr = parser.cols.listIterator();
					List<String> sortedRow = new ArrayList<String>();
				    while(litr.hasNext()) {
				         String col = litr.next();
				         sortedRow.add(row.get(col));
				         row.remove(col);
				    }
				    Iterator<Map.Entry<String, String>> it = row.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
				        parser.cols.add(pairs.getKey());
				        model.addColumn(pairs.getKey());
				        sortedRow.add(pairs.getValue());
				        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
				    }
				    if (sortedRow.size()>0) {
				    	model.addRow(sortedRow.toArray());
				    }
				}
			}
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		System.out.println(parser.cols.toString());
        

    	parser.setLoadVisibility(true);
    }
}