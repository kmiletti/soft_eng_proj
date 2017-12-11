import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
 

import java.awt.*;

public class Extractor  extends JFrame
 {
    public static void main( String args[] )
    {
           Extractor desktopFrame = new Extractor();
             desktopFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
           desktopFrame.setSize( 1000, 720 ); // set frame size
           desktopFrame.setVisible( true ); // display frame
    } // end mai
    
   
    final public static String driver = "org.h2.Driver";
    final public static String server = "jdbc:h2:~/events"; 
    final public static String dbuser = "sa";
    final public static String dbpassword = "";
    //final public static String driver = "com.mysql.jdbc.Driver";
    //final public static String server = "jdbc:mysql://localhost:3306/test"; 
    //final public static String dbuser = "root";
    //final public static String dbpassword = "tomcat";
    
    final static JDBCAdapter adapter =  new JDBCAdapter( server,  driver,  dbuser,  dbpassword);
    
    class FrameListener extends WindowAdapter
   {
    public void windowClosing(WindowEvent e)
    {
        adapter.close();
        System.exit(0);
    }
   }
    
    public static  JDesktopPane theDesktop;
    public static int extraline = 6;
    public VSheet vs;
    //static public VReport vr;
    JMenuBar bar = new JMenuBar(); // create menu bar
    JMenu fileMenu, mens[] = new JMenu[26];
    JMenuItem newMenuitem, searchMenuitem, sqlMenuitem;
    String tmp ;
    String allsearchs = ",";
    public Extractor()
    {
       super( "Extractor --- a generic search tool for data analytics" );
       adapter.executeUpdate("Create table Target( searchname varchar(20), target  varchar(20),  regex  text, finiteset text, required varchar(1),datatyp  varchar(20), num int, primary key(searchname, target))");
       adapter.executeUpdate("INSERT INTO Target values('job','location', 'abc', 'cde','1','varchar', 1)");
      
       fileMenu = new JMenu( "File" ); // create Add menu
       newMenuitem = new JMenuItem("New");
       newMenuitem.addActionListener(new ActionListener()
       {
            public void actionPerformed( ActionEvent event )
            { 
            String searchname ;
            String error = "";
            while (true)
            {
                searchname =  JOptionPane.showInputDialog(theDesktop, error + "Enter the name of your search", "Search Name",JOptionPane.QUESTION_MESSAGE );
                if (searchname !=null)
                {
                    if (allsearchs.toLowerCase().contains("," + searchname.toLowerCase().trim() + ","))
                    {
                       error = searchname + " exists already. ";
                    }
                    else 
                        break;
                }
                else return;
            }
                      
            JMenuItem anitem = new JMenuItem( searchname);
            int i = (searchname.toUpperCase().charAt(0) - 'A');
            anitem.addActionListener
            (
             new ActionListener()  
             {
                public void actionPerformed( ActionEvent event )
                {
                  String s = event.getActionCommand();
                  //VTarget frame = new VTarget(s);
                  //frame.pack(); // set internal frame to size of contents
                  //theDesktop.add(  frame ); // attach internal frame
                  //frame.setVisible( true ); // show internal frame           
                } // end method actionPerformed
             } // end anonymous inner class
            ); // end call to addActionListener
            mens[i].add(anitem);
            //VTarget frame = new VTarget(searchname);
            //frame.pack(); // set internal frame to size of contents
            //theDesktop.add(  frame ); // attach internal frame
            //frame.setVisible( true ); 
            }
       
       }  
       );
       fileMenu.add(newMenuitem);
       
       searchMenuitem = new JMenuItem("Download");
       searchMenuitem.addActionListener(new ActionListener()
       {
            public void actionPerformed( ActionEvent event )
            { 
                Sitesearch  vs = new Sitesearch();
                vs.pack();
                theDesktop.add(vs);
                vs.setVisible( true );
            }
       });
       fileMenu.add(searchMenuitem);
       
       sqlMenuitem = new JMenuItem("SQL Sheet");
       sqlMenuitem.addActionListener(new ActionListener()
       {
            public void actionPerformed( ActionEvent event )
            { 
                SQLenter  vs = new SQLenter();
                vs.pack();
                theDesktop.add(vs);
                vs.setVisible( true );
            }
       });
       fileMenu.add(sqlMenuitem);
       
       bar.add( fileMenu ); // add Add menu to menu bar
       
       for (int i=0; i < 26; i++)
       {
           mens[i] = new JMenu(new String("" + (char)('A' + i )));
       } 
       
       String searchname;
       String sql = "select distinct searchname FROM Target";
       int n=0, N = adapter.executeQuery(sql);
       for (;n < N; n++)
       {  
            searchname = adapter.getValueAt(n,0); 
            allsearchs += searchname.toLowerCase().trim() + ",";
            JMenuItem anitem = new JMenuItem( searchname);
            int i = (searchname.toUpperCase().charAt(0) - 'A');
            anitem.addActionListener
            (
             new ActionListener()  
             {
               public void actionPerformed( ActionEvent event )
              {
                  String s = event.getActionCommand();
                  VTarget frame = new VTarget(s);
                  frame.pack(); // set internal frame to size of contents
                  theDesktop.add(  frame ); // attach internal frame
                  frame.setVisible( true ); // show internal frame           
              } // end method actionPerformed
             } // end anonymous inner class
            ); // end call to addActionListener
            mens[i].add(anitem);
        }
       for (int i=0; i < 26; i++)
       {
           bar.add( mens[i]);
       } 
       /*
       JMenu sqlMenu = new JMenu( "SQLSheet" );  
       sqlMenu.addMouseListener(new MouseListener()
       {
            public void mouseReleased(MouseEvent e) {}  
            public void mousePressed(MouseEvent e) {} 
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) 
            {
                
             SQLenter  vs = new SQLenter();
             vs.pack();
             theDesktop.add(vs);
             vs.setVisible( true );
            }
       });
       bar.add( sqlMenu );
       */
       
       setJMenuBar( bar ); // set menu bar for this application
       theDesktop = new JDesktopPane(); // create desktop pane
       add( theDesktop ); // add desktop pane to frame
       addWindowListener(new FrameListener());
       
    } 
    
 } // end class Extractor
  
 
