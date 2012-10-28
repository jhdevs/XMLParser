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
    	
    	try {
    		XMLFile.length();
			FileReader file = new FileReader(XMLFile);
			//file.skip(10);
			char c;
			boolean offerStarted = false;
			boolean tagNameStarted = false;
			boolean tagStarted = false;
			boolean subTagStarted = false;
			boolean textStarted = false;
			char prevc = ' ';
			int tagDepth = 0;
			String tagName = "";
			String tagText = "";
			String subTagName = "";
			Map<String,String> row = new HashMap<String, String>();
		    int ch;
		    do {
		      ch = file.read();
		      if (ch != -1) {
		        c = (char) ch;
			    //if (true) return;
				//while ((c = (char) file.read()) != -1) {
					System.out.print(c);
					if (!offerStarted) {
						if (tagNameStarted) {
							if (c==' ' || c== '/' || c=='>') {
								tagNameStarted = false;
								if (tagName.equals("offer") ) {
									//System.out.println("offerStarted=true"); 
									prevc = c;
									offerStarted=true;
									tagStarted=(c=='>');
								} 
							} else {
								tagName += c;
							}
						} else {
							if (c=='<') {
								tagNameStarted = true;
								tagName = "";
							}
						}
					} else {
						if (tagStarted) {
							if (tagNameStarted) {
								if (c==' ' || (c=='/' && !tagName.equals("")) || c=='>') {
									tagNameStarted = false;
									textStarted = false;
									if (tagName.charAt(0)=='/') {
										if (tagName.equals("/offer")) {
												offerStarted = false;
												System.out.println(row.toString());
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
											    row = new HashMap<String, String>();
											}
									} else {
										if (c=='>') {
											textStarted = true;
											tagDepth = 0;
											tagText = "";
										}
										//название есть 
									}
								} else {
									tagName += c;
								}
							} else {
								if (textStarted) {
									if (tagDepth>0) {
										if (subTagStarted) {
											if (c==' ' || (c=='/' && !subTagName.equals("")) || c=='>') {
												subTagStarted = false;
												if (subTagName.equals("/" + tagName)) {
													if (--tagDepth==0) {
														tagStarted = false;
														textStarted = false;
													}
												} else if (subTagName.equals(tagName)) {
													tagDepth++;
													prevc = c;
												}
											} else {
												subTagName += c;
											}
										} else {
											if (c=='<') {
												subTagStarted = true;
												subTagName = "";
											} else if (c=='>' && prevc=='/') {
												if (subTagName.equals(tagName)) {
													if (--tagDepth==0) {
														tagStarted = false;
														textStarted = false;
													}
												}
											}
										}
									} else {
										if (c=='<') {
											subTagStarted = true;
											subTagName = "";
											tagDepth = 1;
											//есть текст
											row.put(tagName, tagText);
										} else {
											tagText += c;
										}
									}
								} else {
									if (c=='>') {
										if (prevc=='/') {
											if (tagName.equals("offer")) {
												offerStarted = false;
											} else {
												//текст пустой
												row.put(tagName, "");
												tagStarted = false;
											}
										} else {
											if (!tagName.equals("offer")) {
												textStarted = true;
												tagDepth = 0;
												tagText = "";
											} else {
												tagStarted = false;
											}
										}
									}
									prevc = c;
								}
							}
						} else {
							if (c=='<') {
								tagNameStarted = true;
								tagName = "";
								tagStarted = true;
							}
						}
					}
				}
		    } while (ch != -1);
		    file.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	/*DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
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
        */

    	parser.setLoadVisibility(false);
    }
}