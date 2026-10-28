package in.demo.banking;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
public class BankingRepository {
 public record Account(long id,String accountNo,String name,String email,String phone,BigDecimal balance,Timestamp createdAt){}
 public record Tx(String type,BigDecimal amount,BigDecimal balanceAfter,String note,Timestamp createdAt){}
 public List<Account> findAccounts(String search)throws SQLException{
  List<Account> out=new ArrayList<>(); String q="SELECT * FROM accounts WHERE account_no LIKE ? OR holder_name LIKE ? ORDER BY id DESC";
  try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement(q)){String s="%"+(search==null?"":search.trim())+"%";p.setString(1,s);p.setString(2,s);
   try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Account(r.getLong("id"),r.getString("account_no"),r.getString("holder_name"),r.getString("email"),r.getString("phone"),r.getBigDecimal("balance"),r.getTimestamp("created_at")));}}
  return out;
 }
 public void create(String no,String name,String email,String phone,BigDecimal opening)throws SQLException{
  if(no.isBlank()||name.isBlank())throw new IllegalArgumentException("Account number aur naam zaroori hain.");
  if(opening.signum()<0)throw new IllegalArgumentException("Opening balance negative nahi ho sakta.");
  try(Connection c=Db.getConnection()){c.setAutoCommit(false);try{
   long id; try(PreparedStatement p=c.prepareStatement("INSERT INTO accounts(account_no,holder_name,email,phone,balance) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
    p.setString(1,no.trim());p.setString(2,name.trim());p.setString(3,blankNull(email));p.setString(4,blankNull(phone));p.setBigDecimal(5,opening.setScale(2,java.math.RoundingMode.HALF_UP));p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();id=k.getLong(1);}}
   if(opening.signum()>0)insertTx(c,id,"DEPOSIT",opening,opening,"Opening balance");c.commit();
  }catch(Exception e){c.rollback();if(e instanceof SQLException s)throw s;if(e instanceof RuntimeException r)throw r;throw new SQLException(e);} }
 }
 public void update(long id,String name,String email,String phone)throws SQLException{
  if(name==null||name.isBlank())throw new IllegalArgumentException("Naam khaali nahi ho sakta.");
  try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement("UPDATE accounts SET holder_name=?,email=?,phone=? WHERE id=?")){
   p.setString(1,name.trim());p.setString(2,blankNull(email));p.setString(3,blankNull(phone));p.setLong(4,id);if(p.executeUpdate()==0)throw new SQLException("Account nahi mila.");}
 }
 public void delete(long id)throws SQLException{try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM accounts WHERE id=?")){p.setLong(1,id);if(p.executeUpdate()==0)throw new SQLException("Account nahi mila.");}}
 public void transact(long id,String type,BigDecimal amount,String note)throws SQLException{
  if(amount==null||amount.signum()<=0)throw new IllegalArgumentException("Amount zero se zyada hona chahiye.");
  if(amount.scale()>2)throw new IllegalArgumentException("Maximum 2 decimal places allowed hain.");
  if(!type.equals("DEPOSIT")&&!type.equals("WITHDRAWAL"))throw new IllegalArgumentException("Invalid transaction.");
  amount=amount.setScale(2,java.math.RoundingMode.HALF_UP);
  try(Connection c=Db.getConnection()){c.setAutoCommit(false);try{
   BigDecimal current;try(PreparedStatement p=c.prepareStatement("SELECT balance FROM accounts WHERE id=? FOR UPDATE")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Account nahi mila.");current=r.getBigDecimal(1);}}
   if(type.equals("WITHDRAWAL")&&current.compareTo(amount)<0)throw new IllegalArgumentException("Account mein paryapt balance nahi hai.");
   BigDecimal next=type.equals("DEPOSIT")?current.add(amount):current.subtract(amount);
   try(PreparedStatement p=c.prepareStatement("UPDATE accounts SET balance=? WHERE id=?")){p.setBigDecimal(1,next);p.setLong(2,id);p.executeUpdate();}
   insertTx(c,id,type,amount,next,blankNull(note));c.commit();
  }catch(Exception e){c.rollback();if(e instanceof SQLException s)throw s;if(e instanceof RuntimeException r)throw r;throw new SQLException(e);}finally{c.setAutoCommit(true);}}
 }
 private void insertTx(Connection c,long id,String type,BigDecimal amount,BigDecimal balance,String note)throws SQLException{
  try(PreparedStatement p=c.prepareStatement("INSERT INTO transactions(account_id,transaction_type,amount,balance_after,note) VALUES(?,?,?,?,?)")){
   p.setLong(1,id);p.setString(2,type);p.setBigDecimal(3,amount);p.setBigDecimal(4,balance);p.setString(5,note);p.executeUpdate();}
 }
 public List<Tx> transactions(long id)throws SQLException{
  List<Tx> out=new ArrayList<>();try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM transactions WHERE account_id=? ORDER BY id DESC")){
   p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Tx(r.getString("transaction_type"),r.getBigDecimal("amount"),r.getBigDecimal("balance_after"),r.getString("note"),r.getTimestamp("created_at")));}}return out;
 }
 private static String blankNull(String s){return s==null||s.isBlank()?null:s.trim();}
}
