package com.jhevs.test.XMLParse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

class FileLoadThread extends Thread  {
	private final static String tblTag = "offer";
	XMLParse parser;	// ссылка на родительский объект
	File XMLFile;		
	boolean stop;		// флаг для остановки потока
	
	public FileLoadThread(XMLParse parser, File file) {
		super();
		this.parser = parser;
		this.XMLFile = file;
	}
	
    public void run() {
		long dt = (new Date()).getTime();
		stop = false;
    	try {
    		XMLFile.length();
			FileReader file = new FileReader(XMLFile);
			char c;
			boolean offerStarted = false;	// обраружен элемент строки (offer)
			boolean tagNameStarted = false;	// начало заполнения названия тега
			boolean tagStarted = false;		// начало тега <
			boolean subTagStarted = false;	// начало вложенного тега (не выводится)
			boolean textStarted = false;	// начало определения содержимого тега
			char prevc = ' ';				// предыдущий считанный символ
			int tagDepth = 0;				// вложенность тегов при отсечении ненужных 
			String tagName = "";
			String tagText = "";
			String subTagName = "";
			Map<String,String> row = new HashMap<String, String>();	//заполненных хэш с парами тег-содержимое
		    int ch;
		    long cnt2 = 0;					// количество считанных символов
		    long cnt = 0;					// количество считанных строк
		    do {
		      ch = file.read();
		      if (ch != -1) {
		    	  cnt2++;
		    	  c = (char) ch;
					//System.out.print(c);
					if (!offerStarted) { 						// ищем тег offer
						if (tagNameStarted) {					// заполняем название тега
							if (c==' ' || c== '/' || c=='>') {	
								tagNameStarted = false;
								if (tagName.equals(tblTag) ) {	// найден тег offer
									//System.out.println("offerStarted=true"); 
									prevc = c;
									offerStarted=true;
									tagStarted=(c=='>');
								} 
							} else {
								tagName += c;
							}
						} else {								// ищем начало тега
							if (c=='<') {
								tagNameStarted = true;
								tagName = "";
							}
						}
					} else {									// тег offer обнаружен, разбираем содержимое
						if (tagStarted) {						// заполняем название тега
							if (tagNameStarted) {
								if (c==' ' || (c=='/' && !tagName.equals("")) || c=='>') {	// конец названия
									tagNameStarted = false;
									textStarted = false;
									if (tagName.charAt(0)=='/') {
										if (tagName.equals("/"+tblTag)) {	// обнаружен закрывающий тег offer
											offerStarted = false;			// добавляем строку в интерфейс...
											//System.out.println(row.toString());
									      	DefaultTableModel  model = (DefaultTableModel) parser.table.getModel();
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
										    cnt++;
										    if ((cnt/1000)*1000==cnt) System.out.println(cnt);
										}
									} else {						 
										if (c=='>') {				// тег закрыт, переходим к определению содержимого 
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
								if (textStarted) {				// заполнение содержимого тега
									if (tagDepth>0) { 			// переход по вложенным тегам
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
										if (c=='<') {			//содержимое тега заеончилась, надо искать закрывающий
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
										if (prevc=='/') {					// тег с закрытием
											if (tagName.equals(tblTag)) {
												offerStarted = false;
											} else {
												//текст пустой
												row.put(tagName, "");
												tagStarted = false;
											}
										} else {
											if (!tagName.equals(tblTag)) {
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
						} else {		// ищем вложенные теги		
							if (c=='<') {
								tagNameStarted = true;
								tagName = "";
								tagStarted = true;
							}
						}
					}
				}
		    } while (ch != -1 & !stop);// && cnt < 90000);
		    file.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	/* Вариант со стандартным DOM XML Parser. Закомментированно, т.к. вылетает на больших файлах
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
        */
    	
    	parser.setLoadVisibility(true);
    	System.out.println(((new Date()).getTime()-dt)+"millis");
    }
}