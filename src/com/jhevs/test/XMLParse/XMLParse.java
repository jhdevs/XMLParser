package com.jhevs.test.XMLParse;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLParse {

	private JFrame frame;
	private JFileChooser fileDialog;
	private JButton buttonLoad;
	private JButton buttonCancel;
	private JTable table;
	private JScrollPane pane;
	private FileLoadThread flThread;
	public List <String> cols;
	
	public JTable getTable() {
		return table;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XMLParse a = new XMLParse();
	}
	
	public XMLParse(){
		  frame = new JFrame("XML Parse");
	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      fileDialog = new JFileChooser();
	      fileDialog.setFileFilter(new FileNameExtensionFilter("XML file (*.xml", "xml"));
		  String data[][] = {{"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"}, /*
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"},
		   {"001","vinod","Bihar","India","Biology","65","First"},
		   {"002","Raju","ABC","Kanada","Geography","58","second"},
		   {"003","Aman","Delhi","India","computer","98","Dictontion"},*/
		   {"004","Ranjan","Bangloor","India","chemestry","90","Dictontion"}};
		  String col[] = {"Roll","Name","State","country","Math","Marks","Grade"};
		  table = new JTable(data,col);
		  table.setEnabled(false);
		  //JTableHeader header = table.getTableHeader();
		  //header.setBackground(Color.yellow);
		  pane = new JScrollPane(table);
		  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		  pane.setVisible(false);
		  
		  buttonLoad = new JButton("Load XML");
		  buttonLoad.addActionListener(new ActionListener()
	      {
	            public void actionPerformed(ActionEvent event)
	            {
	                openDialog();
	            }
	      });
		  buttonCancel = new JButton("Stop loading");
		  buttonCancel.addActionListener(new ActionListener()
	      {
	            public void actionPerformed(ActionEvent event)
	            {
	                stopLoading();
	            }
	      });
		  buttonCancel.setVisible(false);
		  
		  Container contentPane = frame.getContentPane();
		  contentPane.add(pane);
		  contentPane.add(buttonLoad, BorderLayout.NORTH);
		  contentPane.add(buttonCancel, BorderLayout.SOUTH);
		  frame.setSize(600,300);
		  frame.setUndecorated(true);
		  frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.setVisible(true);
		  }

	@SuppressWarnings("deprecation")
	private void stopLoading() 
	{
		//flThread.stop();
		flThread.stopped=true;
		//setLoadVisibility(true);
	}
	
	private void openDialog()
    {
        int openChoice = fileDialog.showOpenDialog(frame);
        
        //display choice using tracker 
        //logChoice(openChoice, "Open Dialog");
        
        if (openChoice == JFileChooser.APPROVE_OPTION)
        {
            //Put open file code in here
            File openFile = fileDialog.getSelectedFile();
            //tracker.append("\nThe file selected is " + openFile.getName());
            //tracker.append("\nThe file's path is " + openFile.getPath());
            pane.setVisible(true);
            TableColumn aColumn = new TableColumn();
            table.addColumn(aColumn);
            DefaultTableModel model = new DefaultTableModel();
            table.setModel(model);
            cols = new ArrayList<String>();
            
            flThread = new FileLoadThread(this, openFile, table);
            flThread.start();
            setLoadVisibility(false);
        }
    }
    
	public void setLoadVisibility(boolean visible) {
		buttonLoad.setVisible(visible);
        buttonCancel.setVisible(!visible);
	}
}
