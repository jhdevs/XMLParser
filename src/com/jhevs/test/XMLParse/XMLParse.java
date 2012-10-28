package com.jhevs.test.XMLParse;

import javax.swing.*;
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
	private final static String btnLoad = "Load XML";
	private final static String btnStop = "Stop loading";
	private final static String wndTitle = "XML Parse";
	private final static String filterText = "XML file (*.xml)";
	private final static String filterExt = "xml";

	private JFrame frame;
	private JFileChooser fileDialog;
	private JButton buttonLoad;
	private JButton buttonCancel;
	JTable table;							// таблица с данными из файла
	private JScrollPane pane;
	private FileLoadThread flThread;		// ссылка на поток разбора xml файла
	List <String> cols;						// список столбцов в таблице
	


	public static void main(String[] args) {
		new XMLParse();
	}
	
	public XMLParse(){
		  frame = new JFrame(wndTitle);
	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      fileDialog = new JFileChooser();
	      fileDialog.setFileFilter(new FileNameExtensionFilter(filterText, filterExt));
		  
		  table = new JTable();
		  table.setEnabled(false);
		  //JTableHeader header = table.getTableHeader();
		  //header.setBackground(Color.yellow);
		  pane = new JScrollPane(table);
		  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		  pane.setVisible(false);
		  
		  buttonLoad = new JButton(btnLoad);
		  buttonLoad.addActionListener(new ActionListener()
	      {
	            public void actionPerformed(ActionEvent event)
	            {
	                openDialog();
	            }
	      });
		  buttonCancel = new JButton(btnStop);
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

	private void stopLoading() 
	{
		//flThread.stop(); 				возможно некорректное завершение... в данной программе будет работать нормально
		//setLoadVisibility(true);
		flThread.stop = true;
	}
	
	private void openDialog()
    {
        int openChoice = fileDialog.showOpenDialog(frame);
        
        if (openChoice == JFileChooser.APPROVE_OPTION)
        {
            File openFile = fileDialog.getSelectedFile();
            pane.setVisible(true);
            TableColumn aColumn = new TableColumn();
            table.addColumn(aColumn);
            DefaultTableModel model = new DefaultTableModel();
            table.setModel(model);
            cols = new ArrayList<String>();
            
            flThread = new FileLoadThread(this, openFile);
            flThread.start();
            setLoadVisibility(false);
        }
    }
    
	void setLoadVisibility(boolean visible) {
		buttonLoad.setVisible(visible);
        buttonCancel.setVisible(!visible);
	}
}
