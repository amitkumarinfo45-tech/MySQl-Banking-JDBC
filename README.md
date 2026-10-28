# Module 04 — Java Database & JDBC (NovaBank)

Java Swing GUI + JDBC + MySQL project for learning SQL, CRUD, PreparedStatement, and database transactions.

## Requirements
- JDK 17+
- MySQL Server 8+
- VS Code with Extension Pack for Java and Maven support
- Maven

## 1. Create the database
Open `database/setup.sql` in MySQL Workbench and execute the whole script. It creates `java_banking`, `accounts`, `transactions`, and a demo account:
- Account number: `1001001`
- Name: `Demo Customer`
- Opening balance: ₹5000.00

## 2. Configure MySQL password
Default settings are username `root` and blank password. If your MySQL root user has a password, open PowerShell terminal in VS Code and run:
```powershell
$env:BANK_DB_USER="root"
$env:BANK_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```
Replace the example with your local password. Do not upload real passwords to GitHub.

## 3. Run in VS Code
Open the `JavaMySQLBankingJDBC` folder. In Terminal > New Terminal, run:
```bash
mvn clean package
mvn exec:java -Dexec.mainClass="in.demo.banking.Main"
```
If Maven says the `exec` goal is unavailable, open `Main.java` and click **Run** above `main`; make sure VS Code has imported the Maven project and downloaded dependencies.

## Features
- Add account, search, edit and delete account
- Deposit / withdraw with insufficient-balance validation
- Transaction history from MySQL
- SQL parameters use `PreparedStatement`
- Money changes use JDBC transaction commit/rollback and `SELECT ... FOR UPDATE`

## Common errors
- `Communications link failure`: start MySQL Server and check port 3306.
- `Access denied`: set the right `BANK_DB_USER` and `BANK_DB_PASSWORD`.
- `Unknown database java_banking`: run `database/setup.sql`.
- `No suitable driver`: reload Maven project and allow it to download Connector/J.

Educational demo only, not for real banking. No login/authentication or production security is included.
