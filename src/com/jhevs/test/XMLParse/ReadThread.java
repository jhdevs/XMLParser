package com.jhevs.test.XMLParse;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

public class ReadThread extends Thread  {
	FileLoadThread flThread;
	long subXMLLength;
	int threadNum;
	
	public ReadThread(FileLoadThread flThread, int threadNum, long subXMLLength) {
		super();
		this.flThread = flThread;
		this.subXMLLength = subXMLLength;
		this.threadNum = threadNum;
	}
	
	public void run() {
		FileReader file;
		try {
			file = new FileReader(flThread.XMLFile);
			if (threadNum>0) file.skip(threadNum*subXMLLength);
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
		    long cnt = 0;
		    long cnt2 = 0;
		    do {
		    	ch = file.read();
		    	if (ch != -1) {
			    	cnt2++;
		    		c = (char) ch;
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
												//System.out.println(row.toString());
												cnt++;
										      	flThread.addRow(row);
											    if ((cnt/1000)*1000 == cnt) System.out.println(threadNum + ": " + cnt);
											    row = new HashMap<String, String>();
											    if ((cnt/10000)*10000 == cnt) System.gc();
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
											String val = row.get(tagName);
											if (val==null || val.equals("")) {
												row.put(tagName, tagText);
											} else {
												if (!tagText.equals("")) {
													row.put(tagName, val + "; " + tagText);
												}
											}
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
												if (row.get(tagName)==null) row.put(tagName, "");
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
		    } while (ch != -1 && !(cnt2>subXMLLength && !offerStarted) && !flThread.stopped);
		    file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flThread.threadEnd();
	}
	
	
	
}
