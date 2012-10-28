package com.jhevs.test.XMLParse;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
	final static int threadCnt = 8;
	boolean stopped = false;
	long commonCnt = 0;
	int tcnt = 1;
	long dt;
	
	public FileLoadThread(XMLParse parser, File file, JTable table) {
		super();
		this.parser = parser;
		this.XMLFile = file;
		this.table = table;
	}
	
    public void run() { 
    	dt = (new Date()).getTime();
		Long subXMLLength = XMLFile.length();
		//System.out.println(SubXMLLength);
		tcnt = 1;
		commonCnt = 0;
		if (subXMLLength>1024*1024*3) {
			tcnt = threadCnt;
    		subXMLLength = subXMLLength / threadCnt;
		} 
		
		for (int i=0;i<tcnt;i++)
		{
			ReadThread rt = new ReadThread(this, i, subXMLLength);
			rt.start();
		}
    	//parser.setLoadVisibility(true);
    }
    
    synchronized void addRow(Map<String,String> row) {
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
	    commonCnt++;
	    System.out.println("cnt: " + commonCnt);
	    if (commonCnt>90000) stopped=true;
	    if ((commonCnt/10000)*10000 == commonCnt) System.out.println("cnt: " + commonCnt);
    }
    
    synchronized void threadEnd() {
    	tcnt--;
    	if (tcnt<=0) {
    		parser.setLoadVisibility(true);
    		long dtEnd = (new Date()).getTime();
    		System.out.println((dtEnd-dt)+"millis");
    	}
    }
}