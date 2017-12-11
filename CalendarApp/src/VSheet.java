import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.sql.*;


public class VSheet extends JInternalFrame 
{
   public String tablename;
   public int NUMROWS ;
   public int numRows ;
   final private int extraline = Extractor.extraline;
   public int numCols ;
   public String[] fields ;
   public String[] datatypes ;
   public String[] defaultvalues;
   public boolean[] notnull;
   public String[] keys;
   private String keystr;
    
   public String wherekey[];
   public int numRecords = 0;
   public Object[][] data;
   TableModel dataModel;
   int rowinediting = 0;
   int colinediting = 0;
   JTable table = null;
   final String filename = "myclass";
   JButton stopbut, setsch;
   static JProgressBar barDo;
   public JLabel fpath; 
   JDBCAdapter adapter = Extractor.adapter;
   synchronized static public void setProgress(int i)
   {
      barDo.setValue(i);
   }
   String pad(String s, int mx)
   {
      for (int i=s.length(); i < mx; i++)
         s += ' ';
      return s;
   }
   class FrameListener extends WindowAdapter
   {
    public void windowClosing(WindowEvent e)
    {
       if (0!=save()) ;
       System.exit(0);
    }
   }
  
   int save()
   {
        
        return 0;

  }
  
   
 
  public void addRow(Object [] record)
  {
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      
      model.addRow(record);
  }   
  
  void sortby(int col)
  {
      int n = numRecords;
      if (NUMROWS > n) n = NUMROWS;
      Integer rows[] = new Integer[n];
      for (int i=0; i < n; i++)
         rows[i] = new Integer(i); 
      Arrays.sort(rows, new RowComparator(col, this));	
      int [] a = new int[n];
      for (int i=0; i < n; i++)
      {
          a[i] = rows[i].intValue();
          System.out.println(a[i]);
      }
      
      Object[][] newd = new Object[n][];
      for (int i=0; i < n; i++)
      {
          newd[i]  = new Object[numCols]; 
          for (int j=0; j < numCols; j++)
              newd[i][j] = data[i][j];
      }
      for (int i=0; i < n; i++)
      {
           for (int j=0; j < numCols; j++)
            table.setValueAt(newd[a[i]][j], i, j); 
      }
      
  }
  
  public VSheet( String createSQL)
  {
      super(" ", true, true, true, true );
      String [] y = createSQL.replaceFirst("[^\\(]+\\(","").replaceFirst("(?i),[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY.*","").split(",");
      int numCols1 =  y.length;
      String fields0[] = new String[y.length];
      String [] datatypes0 = new String[y.length];
      defaultvalues  = new String[y.length];
      notnull = new boolean[y.length+1]; 
      String z[] = createSQL.replaceAll("[ |\t|\n|\r]+"," ").trim().split(" ");
      String tablename0 = z[2].replaceFirst("\\(.*", "");
      for (int i=0; i < y.length; i++)
      {
          defaultvalues[i] = y[i].replaceFirst("^[^']+'", "").replaceFirst("'[^']*$", "").replaceAll("''", "'");
          if (defaultvalues[i].equals(y[i]))
          {
              defaultvalues[i] = "";
          }
          y[i] = y[i].replaceAll("'[^']*'","").replaceAll("[ |\t|\n|\t]+"," ").trim();
          String yi = y[i].replaceFirst("(?i)not null", "");
          notnull[i] =  ( !yi.equals(y[i]) );
          
          fields0[i] = y[i].replaceFirst(" .*$", "");
          int k = fields0[i].length();
          if (k < y[i].length())
          {  
              y[i] = y[i].substring(k).trim();
              datatypes0[i] = y[i].replaceFirst(" .*$", "");
              k = datatypes0[i].length();
              if (k < y[i].length())
              {
                  y[i] = y[i].substring(k).trim();
                  k = y[i].indexOf("(");
                  if (k>=0)
                  {
                     int j = y[i].indexOf(")",k);
                     datatypes0[i]  = "varchar" + y[i].substring(k, j+1);
                  }
              }
              
              
              
          }
      }
      String x = createSQL.replaceFirst("(?i).*,[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY","");
      int k = x.indexOf("(");
      int j = -1; if (k>=0) j = x.indexOf(")",k);
      String key0[] ;
      if (j>-1) key0  = x.substring(k+1, j).split("[ |\n|\r|\t]*,[ |\n|\r|\t]*");
      else key0 = new String[]{"key"};
      maketable(  tablename0, fields0  ,  datatypes0 ,    key0   );
  }
   
  public VSheet( String tablename, String fields[], String datatypes[],  String keys[] )
  {      
       super(tablename, true, true, true, true );
       maketable(tablename,  fields ,   datatypes ,    keys  );
  }
  String pt(String s[])
  {
      String str = "";
      for (int i=0; i < s.length; i++)
        str += "  " + s[i];  
      return str;
  }
  private void  maketable( String tablename, String fields[], String datatypes[],  String keys[] )
  {
       System.out.println(pt(fields));
       System.out.println(pt(datatypes));
       System.out.println(pt(keys));
       if (fields.length!=datatypes.length)
       {
           System.out.println("fields.length!=datatypes.length");
       }
       this.numCols = fields.length  + 1;
       this.tablename= tablename;
       this.datatypes = new String[numCols];
       this.fields = new String[numCols];
       this.keys = new String[keys.length]; 
       for (int i=0; i < keys.length; i++)
       {
           this.keys[i] = keys[i];
       }
       final String colnames [] = new String[numCols];
       final String datp[] = new String[numCols];
       for (int i=0; i < numCols-1; i++)
       {
           this.datatypes[i] = datatypes[i].toLowerCase();
           this.fields[i] = fields[i];
           colnames[i] = fields[i];
           datp[i] = this.datatypes[i];
       }
       datp[numCols-1] = this.datatypes[numCols-1] = "boolean";
       this.fields[numCols-1] = "Mark"; 
       
       final int widths[] = new int[numCols];
       colnames[numCols-1] = "Mark"; 
       for (int i=0; i < numCols   ; i++) 
       {
           widths[i] = 50; 
           try{if (this.datatypes[i].contains("varchar"))
              widths[i] = 3*Integer.parseInt(this.datatypes[i].replaceAll("[^0-9]",""));
           }catch(Exception e){}
           if (widths[i] > 200) widths[i]=200;
           if (widths[i] < 70) widths[i] = 70;
           System.out.println(widths[i]);
       }
       
       final long t = System.currentTimeMillis();
       final  String randt = ("" + (t%24) + ":" + (t%60));
       data = mkdata();  
         
        dataModel = new AbstractTableModel() 
       {
            public int getColumnCount() { return numCols ; }
            public int getRowCount() { if (data==null) return 0; return data.length;}
            public Object getValueAt(int row, int col) 
            {
                   return  data[row][col]; 
            }

            public String getColumnName(int column) 
            { 
                if (column < numCols) 
                    return colnames[column]; 
                else return "";
            }
            public Class getColumnClass(int col) {return getValueAt(0,col).getClass();}
            public boolean isCellEditable(int row, int col) {return true;}
            public void setValueAt(Object aValue, int row, int column) 
            {
                data[row][column] = aValue;
                fireTableCellUpdated(row, column);
                fireTableDataChanged();
            }
        };

        final String tips[] = fields;
         
        table  = new JTable(dataModel)
        {
            protected JTableHeader createDefaultTableHeader() 
            {
               return new JTableHeader(columnModel) 
               {
                 public String getToolTipText(MouseEvent e) 
                 {
                    String tip = null;
                    java.awt.Point p = e.getPoint();
                    int index = columnModel.getColumnIndexAtX(p.x);
                    int realIndex =   columnModel.getColumn(index).getModelIndex();
                    return tips[realIndex];
                 }
               };
               
            }
            
            public Class getColumnClass(int column) 
            {
                 if (datp[column].contains("varchar")  )
                     return String.class;
                 else  if (datp[column].equals("integer") )
                      return Integer.class;
                  else  if (datp[column].equals("long")  )
                      return Long.class;
                 else  if (datp[column].equals("double") )
                      return Double.class; 
                  else  if ( column == numCols-1 )
                       return Boolean.class;
                  else 
                      return String.class; 
            }
               
        };
       
        table.getTableHeader().addMouseListener(
        new MouseAdapter() 
        {
           boolean direct = false;
           public void mouseClicked(MouseEvent e) 
           {
            int col = table.columnAtPoint(e.getPoint());
            String name = table.getColumnName(col);
            System.out.println("sort by  " + col + " " + name );
            if (col==5)
            {
               direct = !direct; 
                int n = numRecords;
                if (NUMROWS > n) n = NUMROWS;
               for (int i=0; i < n ; i++)
                   table.setValueAt(new Boolean(direct),i,col);
            }
            else
                sortby(col);
           }
         });
        
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        TableColumnModel columnModel = table.getColumnModel();
        
        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        int wd = 0;
        for (int i=0; i < numCols; i++)
        {
           wd += widths[i];
           TableColumn columni = columnModel.getColumn(i);
           columni.setPreferredWidth(widths[i]);
        }
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.lightGray);
        header.setPreferredSize(new Dimension(wd,25));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(wd, 650));
      

        table.addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent e)
          {
            if (e.getClickCount() == 2)
            {
               Point p = e.getPoint();
               int row = table.rowAtPoint(p);
               int col  = table.columnAtPoint(p); // This is the view column!
               rowinediting = row  ;
               colinediting = col;
              
            }
          }
        }
        );
      
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      Container content = getContentPane();
      content.setLayout(gridbag);
      c.fill = GridBagConstraints.VERTICAL;

      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 0;
      gridbag.setConstraints(scrollpane, c);

      content.add(scrollpane);
      // Container content = frame.getContentPane();
      JPanel buttonson = new JPanel();
      
      
      
      fpath = new JLabel("", SwingConstants.LEFT);
      
      c.gridx = 0;
      c.gridy = 1;
      gridbag.setConstraints(fpath, c);
      content.add(fpath);
       
      JButton choser = new JButton("Save");
      choser.setSize(320, 25);
      buttonson.add(choser);
      
      JButton update = new JButton("Delete");
      update.setSize(320, 25);
      buttonson.add(update);
      
      
      JButton openb = new JButton("Open");
      openb.setSize(320, 25);
      buttonson.add(openb);
      
      JButton openq = new JButton("Query");
      openq.setSize(320, 25);
      buttonson.add(openq);
      c.gridx = 0;
      c.gridy = 2;
      gridbag.setConstraints(buttonson, c);
      content.add(buttonson);

      ButtonListener actionlistener = new ButtonListener(table);
      choser.addActionListener(actionlistener);
      openb.addActionListener(actionlistener);
      openq.addActionListener(actionlistener);
      update.addActionListener(actionlistener);
      setSize(wd +10, 450);
      setVisible(true);
      
      
  }

     
     public String readTextAt(int i, int j)
  {
      if (i >= data.length || j >= data[i].length)
          return "";
      if (datatypes[j].toLowerCase().indexOf("varchar") >= 0)
      {
          if (data[i][j]!=null) 
              return (String)data[i][j];
          return "";
      }
      else if (datatypes[j].toLowerCase().indexOf("integer") >= 0  )
      {
          try{
          Integer q = (Integer)(data[i][j]);
          if (q ==null) return "";
          return "" + q.intValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("long") >= 0  )
      {
          try{
          Long q = (Long)(data[i][j]);
          if (q ==null) return "";
          return "" + q.longValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("double") >= 0)
      {
          try{
          Double q = (Double)(data[i][j]);
          if (q ==null) return "";
          return "" + q.doubleValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("boolean") >= 0  )
      {
          try{
          Boolean q = (Boolean)(data[i][j]);
          if (q ==null) return "";
          return "" + q.booleanValue();
          }catch(Exception e){return "";}
      }
      return "";
  }
   
     
    
    class ButtonListener implements ActionListener 
    {
      
      JTable table ; 
      public ButtonListener( JTable tbl) { table = tbl;}
      
      public void actionPerformed(ActionEvent e) 
      {
         String cmd = e.getActionCommand();
         if (cmd.equals("Query"))
         { 
               try{
               String s = JOptionPane.showInputDialog(getDesktopPane().getTopLevelAncestor(),"Enter your skill set");
               //Extractor.vr =  new VReport(s ,new String[]{"State", "Number", "Avepay", "Population","HousePrice","NormNumber","NormPay" }, new String[]{"varchar(10)","int","int","int","int","double","double" } );
               //Extractor.vr.pack(); // set internal frame to size of contents
               //Extractor.theDesktop.add(Extractor.vr); // attach internal frame
               //Extractor.vr.setVisible( true ); // show internal frame                          
               }catch(Exception e1){}
         }
         else 
         if (cmd.equals("Open"))
         { 
              
               int row = table.getSelectedRow();
               String filepath =  (String)(table.getValueAt(row, 4));
               System.out.println(filepath);
               File htmlFile = new File(filepath);
               try{
                Desktop.getDesktop().browse(htmlFile.toURI()); 
               }catch(Exception e1){}
         }
         else if (cmd.equals("Delete"))
         {
             try{
             Class.forName(Extractor.driver);
             Connection conn = DriverManager.getConnection(Extractor.server,  Extractor.dbuser,  Extractor.dbpassword);
           
            Statement stmt = conn.createStatement();
            int n1=0;
            for (int i=0; i < numRows ; i++)
             { 
                 if ( ((Boolean)(data[i][numCols-1])).booleanValue() == false) continue;
                
                 String sql  = "DELETE FROM "+  tablename  +" " + wherekey[i];
                 try
                 { 
                     int  nn = stmt.executeUpdate(sql);
                     if (nn>0)
                     { 
                         n1+=nn;  
                         for (int j = 0; j < numCols ; j++) 
                         {
                             if(datatypes[j].indexOf("integer")>=0) 
                             {
                                 table.setValueAt(new Integer(-1),i,j);
                             }
                             else if(datatypes[j].indexOf("long")>=0) 
                             {
                                 table.setValueAt(new Long(-1),i,j);
                             }
                             else if(datatypes[j].indexOf("boolean")>=0) 
                             {
                                 table.setValueAt(new Boolean(false),i,j);
                             }
                             else if(datatypes[j].indexOf("varchar")>=0) 
                             {
                                 table.setValueAt("",i,j);
                             }
                         }
                     }
                 }catch(Exception e1){}
             }
           stmt.close();
           conn.close();
           fpath.setText("deelted:" + n1);
             }
        catch(Exception e2){} 
         }
         else if (cmd.equals("Save"))
         {
            int n1 = 0;
            for (int i = 0; i < NUMROWS ; i++) 
            {
                 if ( readTextAt(i, 0).equals("")) continue;   
                 String sql = "";
                 String vk = "";
                 for (int j = 0; j < numCols - 1; j++) 
                 {
                     String q = "'";
                     sql += ",";
                     boolean iskey = keystr.contains("," + fields[j].toLowerCase() +",");
                     if (iskey) vk+=  "AND " + fields[j]  + "=";
                     if (datatypes[j].indexOf("varchar") < 0) 
                     {
                         q = "";
                         if (iskey) 
                             vk +=   readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") ;
                     }
                     else
                     {
                         if (iskey) 
                             vk += "'" + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + "'";
                     }
                     
                     if (readTextAt(i, j).trim().equals("")) 
                     {
                         if (q.equals("")) 
                         {
                             sql += "NULL";
                         } 
                         else 
                         {
                             sql += "''";
                         }
                     } 
                     else 
                     {
                         sql += q + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + q;
                     }
                      
                 }
                 sql = "INSERT INTO " + tablename + " values (" + sql.substring(1) + ")";
                 
                 try 
                 {
                     
                     int nn = adapter.executeUpdate(sql);
                     System.out.println(sql + nn+ adapter.error());
                     if (nn > 0) 
                     {
                         n1 += nn;
                         wherekey[i] = vk.replaceFirst("AND", "WHERE");
                     }

                 } 
                 catch (Exception e1) 
                 {
                 }

                 sql = "";
                 for (int j = 0; j < numCols - 1; j++) {
                     String x = readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
                     if (!x.equals("")) 
                     {
                         String q = "'";
                         if (datatypes[j].indexOf("varchar") < 0) 
                         {
                             q = "";
                         }
                         if (q.equals("") && readTextAt(i, j).trim().equals("")) 
                         {
                             sql += fields[j] + "=NULL";
                         } 
                         else 
                         {
                             sql += fields[j] + "=" + q + x + q;
                         }
                         sql += ",";

                     }

                 }
                 if (!sql.equals("")) 
                 {
                     sql = "UPDATE " + tablename + " SET " + sql.replaceFirst(",$", "") + wherekey[i];
                 }
                 if (!sql.equals("")) 
                 {
                     try 
                     {
                         System.out.println(sql);
                         int nn = adapter.executeUpdate(sql);
                         System.out.println(sql + nn+ adapter.error());
                     } catch (Exception e1) 
                     {
                     }
                 }
             }
             
            fpath.setText("saved:" + n1);  
         } 
          
      }
          
    }

    public static void main2 (String[] args)
    {
     // VSheet vf =  new VSheet("Job" ,new String[]{"Location", "Skills", "Position", "Salary"}, new String[]{"varchar(30)","varchar(30)","varchar(30)","integer" } );
      //vf.table.setValueAt("AAAA", 1, 1) ;      
    }
    
   
   
  
  public Object[][] mkdata()
  { 
            keystr = "";
            for (int i = 0; i < keys.length; i++) 
            {
                keystr += "," + keys[i] ;
            }
            String sql0 = "CREATE TABLE " + tablename + "(";
            for (int i = 0; i < numCols-1; i++) 
            {
                sql0 += fields[i] + " " + datatypes[i] + ",";
            }
            sql0 += "PRIMARY KEY(" + keystr.substring(1) + "))";
            adapter.executeUpdate(sql0);
            System.out.println(sql0);
            String sql2 = "SELECT ";
            for (int i=0; i < numCols-1; i++)
                sql2 +=   fields[i] + ",";
            sql2 = sql2.replaceFirst(",$","") +  " FROM " + tablename;
            
            NUMROWS = adapter.executeQuery(sql2);
            if (NUMROWS==-1) NUMROWS =  extraline;
            else NUMROWS +=  extraline;
            data = new Object[NUMROWS][];
            wherekey = new String[NUMROWS];
            keystr = (keystr + ",").toLowerCase();
             
            for (int i=0 ; i < NUMROWS-extraline ; i++)
            {
                    String kv = "";
                    data[i] = new Object[numCols];
                    for (int j = 0; j < numCols-1; j++)
                    {
                        boolean iskey = keystr.contains("," + fields[j].toLowerCase() +",");
                        String str = adapter.getValueAt(i,j);
                        if (iskey)
                            kv += "AND " + fields[j] + "=";
                        if (datatypes[j].indexOf("varchar")>=0)
                        {
                            if (iskey)
                            {
                                kv += "'" + str + "'";
                            }
                            data[i][j] = str;
                        }
                        else if(datatypes[j].indexOf("double")>=0) 
                        {
                            if (iskey)
                            {
                                kv +=   str  ;
                            }
                            double kk = -1;
                            try
                            {
                                kk = Double.parseDouble(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Double(kk);
                        }
                        else if(datatypes[j].indexOf("integer")>=0) 
                        {
                            if (iskey)
                            {
                                kv += str  ;
                            }
                            int kk = -1;
                            try
                            {
                                kk = Integer.parseInt(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Integer(kk);
                        }    
                        else if(datatypes[j].indexOf("long")>=0) 
                        {
                            if (iskey)
                            {
                                kv +=   str  ;
                            }
                            long kk = -1;
                            try
                            {
                                kk = Long.parseLong(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Long(kk);
                        } 
                        
                        
                    }
                    data[i][numCols-1] = new Boolean(true); 
                    wherekey[i] = kv.replaceFirst("AND", " WHERE");
            }
            for (int i=NUMROWS- extraline ; i < NUMROWS  ; i++)
            {
                    data[i] = new Object[numCols];
                    for (int j = 0; j < numCols-1; j++)
                    {
                        
                        if (datatypes[j].indexOf("varchar")>=0)
                            data[i][j] = "";
                        else if(datatypes[j].indexOf("double")>=0) 
                        {
                            
                            data[i][j] = new Double(0.0);
                        }
                        else if(datatypes[j].indexOf("integer")>=0) 
                        {
                             
                            data[i][j] = new Integer( -1 );
                        }    
                        else if(datatypes[j].indexOf("long")>=0) 
                        {
                             
                            data[i][j] = new Long(-1);
                        }     
                    }
                    data[i][numCols-1] = new Boolean(false);
            }
     
     
    return data;
  }
}

class RowComparator implements Comparator<Integer>
{
    int col = 0;
    VSheet vs;
    public RowComparator(int c, VSheet x){col=c; vs = x;}
    public int compare(Integer r1, Integer r2) 
    {
        if (vs.datatypes[col].indexOf("varchar")<0)
        {
           double i1 = ((Double)(vs.data[r1.intValue()][col])).doubleValue(); 
           double i2=  ((Double)(vs.data[r2.intValue()][col])).doubleValue(); 
           if (i1== i2) return  r1.intValue()-  r2.intValue();
           return (int)Math.round(i1 - i2);    
        }
	else
        {
           String s1 = (String)(vs.data[r1.intValue()][col]); 
           String s2 = (String)(vs.data[r2.intValue()][col]); 
           return s1.compareToIgnoreCase(s2);
        }
    }
}
