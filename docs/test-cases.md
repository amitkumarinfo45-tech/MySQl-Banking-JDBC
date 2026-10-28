# Test cases

| Test | Expected result |
|---|---|
| Run setup.sql | Database and tables created |
| Start app with valid credentials | Accounts load in GUI |
| Add unique account number | Account appears in table |
| Add duplicate account number | Error; no duplicate |
| Search name/account number | Matching accounts shown |
| Edit selected account | Name/email/phone update |
| Deposit ₹250 | Balance and history update |
| Withdraw ₹100 | Balance and history update |
| Withdraw more than balance | Error and rollback |
| Close and reopen app | Records remain in MySQL |
| Delete account | Account and its history deleted |
