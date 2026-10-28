package in.demo.banking;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
public class Main extends JFrame {
 private final BankingRepository repo=new BankingRepository();
 private final DefaultTableModel accounts=new DefaultTableModel(new Object[]{"ID","Account No.","Holder Name","Email","Phone","Balance (₹)"},0){public boolean isCellEditable(int r,int c){return false;}};
 private final DefaultTableModel history=new DefaultTableModel(new Object[]{"Date/Time","Type","Amount (₹)","Balance After (₹)","Note"},0){public boolean isCellEditable(int r,int c){return false;}};
 private final JTable table=new JTable(accounts), txTable=new JTable(history); private final JTextField search=new JTextField(17);
 private final JLabel status=new JLabel("Ready"); private final DateTimeFormatter fmt=DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
 public Main(){super("NovaBank — Java + MySQL JDBC");setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1100,700);setMinimumSize(new Dimension(900,580));setLocationRelativeTo(null);setContentPane(ui());table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadHistory();});refresh();}
 private JPanel ui(){
  JPanel root=new JPanel(new BorderLayout());JPanel head=new JPanel(new BorderLayout());head.setBackground(new Color(18,28,48));head.setBorder(new EmptyBorder(18,22,18,22));
  JLabel logo=new JLabel("✦ NovaBank");logo.setForeground(Color.WHITE);logo.setFont(new Font("SansSerif",Font.BOLD,23));JLabel tag=new JLabel("JAVA DATABASE & JDBC  •  MYSQL");tag.setForeground(new Color(190,205,225));head.add(logo,BorderLayout.WEST);head.add(tag,BorderLayout.EAST);root.add(head,BorderLayout.NORTH);
  JPanel body=new JPanel(new BorderLayout(0,12));body.setBorder(new EmptyBorder(18,20,12,20));JPanel top=new JPanel(new BorderLayout());JLabel title=new JLabel("Account Management");title.setFont(new Font("SansSerif",Font.BOLD,24));
  JButton find=new JButton("Search");find.addActionListener(e->refresh());JPanel sr=new JPanel(new FlowLayout(FlowLayout.RIGHT));sr.add(new JLabel("Search:"));sr.add(search);sr.add(find);top.add(title,BorderLayout.WEST);top.add(sr,BorderLayout.EAST);body.add(top,BorderLayout.NORTH);
  JPanel ac=new JPanel(new BorderLayout(0,8));ac.setBackground(Color.WHITE);ac.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,228,238)),new EmptyBorder(12,12,12,12)));ac.add(new JLabel("Customer Accounts"),BorderLayout.NORTH);table.setRowHeight(26);ac.add(new JScrollPane(table),BorderLayout.CENTER);
  JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton add=new JButton("Add Account"),edit=new JButton("Edit Selected"),del=new JButton("Delete Selected"),dep=new JButton("Deposit"),wit=new JButton("Withdraw"),ref=new JButton("Refresh");
  add.addActionListener(e->addAccount());edit.addActionListener(e->editAccount());del.addActionListener(e->deleteAccount());dep.addActionListener(e->transaction("DEPOSIT"));wit.addActionListener(e->transaction("WITHDRAWAL"));ref.addActionListener(e->refresh());
  for(JButton b:new JButton[]{add,edit,del,dep,wit,ref})buttons.add(b);ac.add(buttons,BorderLayout.SOUTH);
  JPanel hist=new JPanel(new BorderLayout(0,7));hist.setBackground(Color.WHITE);hist.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,228,238)),new EmptyBorder(12,12,12,12)));hist.add(new JLabel("Transaction History — selected account"),BorderLayout.NORTH);txTable.setRowHeight(24);hist.add(new JScrollPane(txTable),BorderLayout.CENTER);
  JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,ac,hist);split.setResizeWeight(.57);split.setDividerLocation(350);body.add(split,BorderLayout.CENTER);status.setBorder(new EmptyBorder(8,0,0,0));body.add(status,BorderLayout.SOUTH);root.add(body,BorderLayout.CENTER);return root;
 }
 private Long selected(){int r=table.getSelectedRow();return r<0?null:((Number)accounts.getValueAt(table.convertRowIndexToModel(r),0)).longValue();}
 private void refresh(){try{accounts.setRowCount(0);for(var a:repo.findAccounts(search.getText()))accounts.addRow(new Object[]{a.id(),a.accountNo(),a.name(),a.email()==null?"":a.email(),a.phone()==null?"":a.phone(),a.balance().toPlainString()});status.setText("MySQL connected • "+accounts.getRowCount()+" account(s)");if(accounts.getRowCount()>0&&table.getSelectedRow()<0)table.setRowSelectionInterval(0,0);else loadHistory();}catch(SQLException e){error("Database connect nahi hua. README.md mein setup dekhein.\n"+e.getMessage());}}
 private void loadHistory(){Long id=selected();history.setRowCount(0);if(id==null)return;try{for(var t:repo.transactions(id))history.addRow(new Object[]{t.createdAt()==null?"":t.createdAt().toLocalDateTime().format(fmt),t.type(),t.amount().toPlainString(),t.balanceAfter().toPlainString(),t.note()==null?"":t.note()});}catch(SQLException e){error(e.getMessage());}}
 private void addAccount(){JTextField no=new JTextField(),name=new JTextField(),email=new JTextField(),phone=new JTextField(),bal=new JTextField("0.00");Object[] f={"Account number *",no,"Customer name *",name,"Email",email,"Phone",phone,"Opening balance (₹)",bal};if(JOptionPane.showConfirmDialog(this,f,"Add Account",JOptionPane.OK_CANCEL_OPTION)!=0)return;try{repo.create(no.getText(),name.getText(),email.getText(),phone.getText(),amount(bal.getText()));refresh();}catch(Exception e){error(e.getMessage());}}
 private void editAccount(){Long id=selected();if(id==null){error("Pehle account select karein.");return;}int r=table.convertRowIndexToModel(table.getSelectedRow());JTextField n=new JTextField(""+accounts.getValueAt(r,2)),em=new JTextField(""+accounts.getValueAt(r,3)),ph=new JTextField(""+accounts.getValueAt(r,4));Object[] f={"Name",n,"Email",em,"Phone",ph};if(JOptionPane.showConfirmDialog(this,f,"Edit Account",JOptionPane.OK_CANCEL_OPTION)!=0)return;try{repo.update(id,n.getText(),em.getText(),ph.getText());refresh();}catch(Exception e){error(e.getMessage());}}
 private void deleteAccount(){Long id=selected();if(id==null){error("Pehle account select karein.");return;}if(JOptionPane.showConfirmDialog(this,"Account aur history delete karni hai?","Confirm",JOptionPane.YES_NO_OPTION)!=0)return;try{repo.delete(id);refresh();}catch(Exception e){error(e.getMessage());}}
 private void transaction(String type){Long id=selected();if(id==null){error("Pehle account select karein.");return;}JTextField amount=new JTextField(),note=new JTextField(type.equals("DEPOSIT")?"Cash deposit":"Cash withdrawal");Object[] f={"Amount (₹)",amount,"Note",note};if(JOptionPane.showConfirmDialog(this,f,type.equals("DEPOSIT")?"Deposit":"Withdraw",JOptionPane.OK_CANCEL_OPTION)!=0)return;try{repo.transact(id,type,amount(amount.getText()),note.getText());refresh();status.setText("Transaction saved in MySQL.");}catch(Exception e){error(e.getMessage());}}
 private BigDecimal amount(String s){BigDecimal b=new BigDecimal(s.trim());if(b.scale()>2)throw new IllegalArgumentException("Maximum 2 decimal places allowed.");return b;}
 private void error(String s){JOptionPane.showMessageDialog(this,s==null?"Unknown error":s,"Error",JOptionPane.ERROR_MESSAGE);}
 public static void main(String[] args){SwingUtilities.invokeLater(()->{try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}new Main().setVisible(true);});}
}
