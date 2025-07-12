import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import javax.naming.spi.DirStateFactory.Result;

public class App {
    static Scanner sc= new Scanner(System.in);
    private static final String url = "jdbc:mysql://localhost:3306/project";
    private static final String user = "root";         
    private static final String password = "your_database_password";  
    private Connection connection;
    Statement statement;
    public App() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement=connection.createStatement();
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed! " + e.getMessage());
        }

    }
    public void createAccount() {
        System.out.println("Enter your name:");
        String name = sc.nextLine();

        System.out.println("Enter your birth year (YYYY):");
        int year_of_birth = sc.nextInt();
        System.out.println("Enter your birth month (MM):");
        int month_of_birth = sc.nextInt();
        System.out.println("Enter your birth date (DD):");
        int date_of_birth = sc.nextInt();
        sc.nextLine();

        LocalDate birthDate = LocalDate.of(year_of_birth, month_of_birth, date_of_birth);
        LocalDate currentDate = LocalDate.now();
        int actualAge = Period.between(birthDate, currentDate).getYears();

        int age;
        do {
            System.out.println("Enter your age:");
            age = sc.nextInt();
            if (age != actualAge) {
                System.out.println("Age does not match with your date of birth. Please try again.");
            }
        } while (age != actualAge);

        String dob = String.format("%04d-%02d-%02d", year_of_birth, month_of_birth, date_of_birth);

        System.out.println("Enter your phone number:");
        sc.nextLine();
        String phone_no = sc.nextLine();

        System.out.println("Enter your email:");
        String email = sc.nextLine();

        System.out.println("Enter your address:");
        String address = sc.nextLine();

        System.out.println("Enter your salary:");
        int salary = sc.nextInt();

        System.out.println("Enter your cibil score:");
        int cibil_score = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter your account type (Savings/Current):");
        String account_type = sc.nextLine();
        String account_category;
        if(age>=18){
            account_category = "Major";
        }
        else{
            account_category = "Minor";
        }
        System.out.println("Enter your initial deposit amount (amount should be greater than 1000):");
        float balance;
        do {
            balance = sc.nextFloat();
            if (balance < 1000) {
                System.out.println("Initial deposit amount should be greater than 1000. Please enter again:");
            }
        } while (balance < 1000);

        int pin, confirmpin;
        do {
            System.out.println("Enter your 4-digit pin:");
            pin = sc.nextInt();
            System.out.println("Confirm your pin:");
            confirmpin = sc.nextInt();
            if (pin != confirmpin || pin < 1000 || pin > 9999) {
                System.out.println("Pins do not match or are not 4-digit. Please enter again:");
            }
        } while (pin != confirmpin || pin < 1000 || pin > 9999);

        String[] bcodes = {"KT001", "PT001", "KT002", "MT001", "NT001"};
        String branch_code;
        do {
            System.out.println("Enter your branch code:");
            System.out.println("1) Kavesar (KT001)");
            System.out.println("2) Patlipada (PT001)");
            System.out.println("3) Kasarvadavli (KT002)");
            System.out.println("4) Majiwada (MT001)");
            System.out.println("5) Navpada (NT001)");
            branch_code = sc.next();
            if (!Arrays.asList(bcodes).contains(branch_code)) {
                System.out.println("Invalid branch code. Please try again.");
            }
        } while (!Arrays.asList(bcodes).contains(branch_code));

        String accountNumber = String.format("%08d", new java.util.Random().nextInt(100000000));
        String queryUser = "INSERT INTO user_details (name, age, date_of_birth, phone_no, email, address, salary, cibil_score, account_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String queryAccount = "INSERT INTO account_details (account_type, account_category, balance, branch_code, account_status, created_at, updated_at, pin, account_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try (
                PreparedStatement pstmtUser = conn.prepareStatement(queryUser);
                PreparedStatement pstmtAccount = conn.prepareStatement(queryAccount)
            ) {
                pstmtUser.setString(1, name);
                pstmtUser.setInt(2, age);
                pstmtUser.setString(3, dob);
                pstmtUser.setString(4, phone_no);
                pstmtUser.setString(5, email);
                pstmtUser.setString(6, address);
                pstmtUser.setInt(7, salary);
                pstmtUser.setInt(8, cibil_score);
                pstmtUser.setString(9, accountNumber);
                pstmtUser.executeUpdate();

                pstmtAccount.setString(1, account_type);
                pstmtAccount.setString(2, account_category);
                pstmtAccount.setFloat(3, balance);
                pstmtAccount.setString(4, branch_code);
                pstmtAccount.setString(5, "Active");
                pstmtAccount.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                pstmtAccount.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
                pstmtAccount.setInt(8, pin);
                pstmtAccount.setString(9, accountNumber);
                pstmtAccount.executeUpdate();

                conn.commit();
                System.out.println("Account created successfully. Your account number is: " + accountNumber);
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error occurred while creating the account. Transaction rolled back.");
            }
        } catch (SQLException e) {
            System.out.println("Database connection error.");
        }
    }   

    public void depositMoney() {
        sc.nextLine();
        System.out.println("Enter your account number:");
        String accountNumber = sc.next();
        System.out.println("Enter the amount to deposit:");
        float amount;
        do {
            amount = sc.nextFloat();
            if (amount <= 0) {
                System.out.println("Invalid amount! Please enter a valid amount to deposit.");
            }
        } while (amount <= 0);

        int attempts = 3;
        boolean success = false;

        while (attempts > 0) {
            System.out.println("Enter your pin (" + attempts + " attempt left):");
            int pin = sc.nextInt();

            String updateQuery = "UPDATE account_details SET balance = balance + ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND pin = ?";
            String selectQuery = "SELECT balance FROM account_details WHERE account_number = ? AND pin = ?";
            String insertPassbookQuery = "INSERT INTO passbook (account_number, transaction_type, amount, balance, transaction_date, description) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

            try {
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setFloat(1, amount);
                updateStatement.setString(2, accountNumber);
                updateStatement.setInt(3, pin);
                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                    selectStatement.setString(1, accountNumber);
                    selectStatement.setInt(2, pin);
                    ResultSet resultSet = selectStatement.executeQuery();
                    if (resultSet.next()) {
                        float newBalance = resultSet.getFloat("balance");

                        PreparedStatement insertPassbook = connection.prepareStatement(insertPassbookQuery);
                        insertPassbook.setString(1, accountNumber);
                        insertPassbook.setString(2, "deposit");
                        insertPassbook.setFloat(3, amount);
                        insertPassbook.setFloat(4, newBalance);
                        insertPassbook.setString(5, "Cash deposit");
                        insertPassbook.executeUpdate();
                    }

                    System.out.println("Deposit successful! Amount deposited: $" + amount);
                    System.out.println("Money deposited successfully!");
                    success = true;
                    break;
                } else {
                    attempts--;
                    if (attempts > 0) {
                        System.out.println("Incorrect pin. Try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
                return;
            }
        }

        if (!success) {
            System.out.println("Deposit failed! Maximum attempts reached.");
        }
    }


    public void withdrawMoney() {
        sc.nextLine();
        System.out.println("Enter your account number:");
        String accountNumber = sc.nextLine();
        System.out.println("Enter the amount to withdraw:");
        float amount;
        do {
            amount = sc.nextFloat();
            if (amount <= 0) {
                System.out.println("Invalid amount! Please enter a valid amount to withdraw.");
            }
        } while (amount <= 0);

        int attempts = 3;
        boolean pinMatched = false;
        int pin = 0;

        String selectQuery = "SELECT pin, balance FROM account_details WHERE account_number = ?";
        try {
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, accountNumber);
            ResultSet resultSet = selectStatement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("Account does not exist.");
                return;
            }

            int storedPin = resultSet.getInt("pin");
            float currentBalance = resultSet.getFloat("balance");

            while (attempts > 0) {
                System.out.println("Enter your pin (" + attempts + " attempt left):");
                pin = sc.nextInt();
                if (pin == storedPin) {
                    pinMatched = true;
                    break;
                } else {
                    attempts--;
                    if (attempts == 0) {
                        System.out.println("You have exhausted all attempts.");
                        return;
                    }
                    System.out.println("Incorrect pin.");
                }
            }

            if (currentBalance < amount) {
                System.out.println("Insufficient balance!");
                return;
            }

            String updateQuery = "UPDATE account_details SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND pin = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setFloat(1, amount);
            updateStatement.setString(2, accountNumber);
            updateStatement.setInt(3, pin);
            updateStatement.executeUpdate();

            String insertPassbookQuery = "INSERT INTO passbook (account_number, transaction_type, amount, balance, transaction_date, description) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
            PreparedStatement passbookStmt = connection.prepareStatement(insertPassbookQuery);
            passbookStmt.setString(1, accountNumber);
            passbookStmt.setString(2, "withdraw");
            passbookStmt.setFloat(3, amount);
            passbookStmt.setFloat(4, currentBalance - amount);
            passbookStmt.setString(5, "Cash withdrawal");
            passbookStmt.executeUpdate();

            System.out.println("Money withdrawn successfully!");
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }


    public void checkBalance() {
        sc.nextLine();
        int choice = 3;
        System.out.println("Enter your account number:");
        String accountNumber = sc.nextLine();
        System.out.println("Enter your pin:");
        int pin = sc.nextInt();
        
        String query = "SELECT pin, balance FROM account_details WHERE account_number = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int storedPin = resultSet.getInt("pin");
                if (storedPin != pin) {
                    while (choice > 0) {
                        choice--;
                        System.out.println("Invalid pin! Please try again.");
                        pin = sc.nextInt();
                        if (storedPin == pin) {
                            float balance = resultSet.getFloat("balance");
                            System.out.println("Your current balance is: $" + balance);
                            return;
                        }
                    }
                    System.out.println("You have entered wrong pin 3 times! Please try again later.");
                    return;
                } else {
                    float balance = resultSet.getFloat("balance");
                    System.out.println("Your current balance is: $" + balance);
                }
            } else {
                System.out.println("Account not found or invalid credentials.");
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    public void transferMoney() {
        sc.nextLine(); 
        System.out.println("Enter your account number:");
        String fromAccount = sc.nextLine();
        System.out.println("Enter the receiver's account number:");
        String toAccount = sc.nextLine();
        System.out.println("Enter the amount to transfer:");
        float amount;
        do {
            amount = sc.nextFloat();
            if (amount <= 0) {
                System.out.println("Invalid amount! Please enter a valid amount to transfer.");
            } 
        } while (amount <= 0);

        System.out.println("Enter your pin:");
        int pin = sc.nextInt();

        String query = "SELECT account_number, pin, balance FROM account_details WHERE account_number = ? AND pin = ?";
        String query2 = "SELECT account_number FROM account_details WHERE account_number = ?";
        String updateQuery1 = "UPDATE account_details SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND pin = ?";
        String updateQuery2 = "UPDATE account_details SET balance = balance + ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ?";
        String passbookInsert = "INSERT INTO passbook (account_number, to_account, transaction_type, amount, balance, transaction_date, description) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, fromAccount);
            preparedStatement.setInt(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();

            PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
            preparedStatement2.setString(1, toAccount);
            ResultSet resultSet2 = preparedStatement2.executeQuery();

            if (!resultSet.next() || !resultSet2.next()) {
                System.out.println("Invalid account number(s)! Transfer failed.");
                return;
            }

            float balance = resultSet.getFloat("balance");

            if (fromAccount.equals(toAccount)) {
                System.out.println("Cannot transfer money to the same account! Transfer failed.");
                return;
            }

            if (balance < amount) {
                System.out.println("Insufficient balance! Transfer failed.");
                return;
            }

            PreparedStatement deductStatement = connection.prepareStatement(updateQuery1);
            deductStatement.setFloat(1, amount);
            deductStatement.setString(2, fromAccount);
            deductStatement.setInt(3, pin);
            deductStatement.executeUpdate();

            PreparedStatement addStatement = connection.prepareStatement(updateQuery2);
            addStatement.setFloat(1, amount);
            addStatement.setString(2, toAccount);
            addStatement.executeUpdate();

            float updatedSenderBalance = balance - amount;

            PreparedStatement senderPassbook = connection.prepareStatement(passbookInsert);
            senderPassbook.setString(1, fromAccount);
            senderPassbook.setString(2, toAccount);
            senderPassbook.setString(3, "transfer");
            senderPassbook.setFloat(4, amount);
            senderPassbook.setFloat(5, updatedSenderBalance);
            senderPassbook.setString(6, "Amount transferred to " + toAccount);
            senderPassbook.executeUpdate();

            PreparedStatement getReceiverBalance = connection.prepareStatement("SELECT balance FROM account_details WHERE account_number = ?");
            getReceiverBalance.setString(1, toAccount);
            ResultSet receiverResult = getReceiverBalance.executeQuery();
            float receiverBalance = 0;
            if (receiverResult.next()) {
                receiverBalance = receiverResult.getFloat("balance");
            }

            PreparedStatement receiverPassbook = connection.prepareStatement(passbookInsert);
            receiverPassbook.setString(1, toAccount);
            receiverPassbook.setString(2, fromAccount);
            receiverPassbook.setString(3, "receive");
            receiverPassbook.setFloat(4, amount);
            receiverPassbook.setFloat(5, receiverBalance);
            receiverPassbook.setString(6, "Amount received from " + fromAccount);
            receiverPassbook.executeUpdate();

            System.out.println("Money transferred successfully!");

        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }   


    public void printPassbook() {
        int choice = 3;
        System.out.println("Enter Your Account Number");
        String accountNumber = sc.next();

        String query = "SELECT p.* FROM passbook p JOIN account_details a ON p.account_number = a.account_number WHERE a.account_number = ? AND a.pin = ?";

        try {
            while (choice > 0) {
                System.out.println("Enter Your Pin");
                int pin = sc.nextInt();

                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, accountNumber);
                pstmt.setInt(2, pin);
                ResultSet result = pstmt.executeQuery();
                int id=1;
                if (result.next()) {
                    System.out.println("========================================= Passbook History================================================");
                    do {
                        String to_account = result.getString("to_account");
                        String transaction_type = result.getString("transaction_type");
                        float amount = result.getFloat("amount");
                        float balance = result.getFloat("balance");
                        String date = result.getString("transaction_date");
                        String description = result.getString("description");

                        System.out.println("ID: " + id++);
                        if(to_account==null){
                            System.out.println("To Account: N/A");
                        }
                        else{
                            System.out.println("To Account: "+to_account);
                        }
                        System.out.println("Transaction Type: " + transaction_type);
                        System.out.println("Amount: " + amount);
                        System.out.println("Balance: " + balance);
                        System.out.println("Date: " + date);
                        System.out.println("Description: " + description);
                        System.out.println("---------------------------------------------------");
                    } while (result.next());

                    System.out.println("Transaction history displayed successfully!");
                    break;
                } else {
                    choice--;
                    if (choice > 0) {
                        System.out.println("Incorrect pin. You have " + choice + " attempt(s) left.");
                    } else {
                        System.out.println("You have exhausted all your attempts.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }


    public void changeAccountDetails() {
        System.out.println("1) Change Name");
        System.out.println("2) Change Age");
        System.out.println("3) Change Date of Birth");
        System.out.println("4) Change Phone Number");
        System.out.println("5) Change Address");
        System.out.println("6) Change Email Id");
        System.out.println("7) Change Salary");
        System.out.println("8) Change Branch Code");
        System.out.println("Enter your choice:");
        int choice = sc.nextInt();
        System.out.println("Enter your Account Number:");
        String accountNumber = sc.next();
        int pin = 0;
        boolean verified = false;
        int attempts = 3;
        while (attempts > 0) {
            System.out.println("Enter your pin (" + attempts + " attempt(s) left):");
            pin = sc.nextInt();
            String verifyQuery = "SELECT * FROM user_details u JOIN account_details a ON u.account_number = a.account_number WHERE u.account_number = ? AND a.pin = ?";
            try {
                PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
                verifyStmt.setString(1, accountNumber);
                verifyStmt.setInt(2, pin);
                ResultSet result = verifyStmt.executeQuery();
                if (result.next()) {
                    verified = true;
                    break;
                } else {
                    attempts--;
                    if (attempts == 0) {
                        System.out.println("Maximum attempts reached. Access denied.");
                        return;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
                return;
            }
        }
        String updateQuery = "";
        String columnValue = "";
        try {
            switch (choice) {
                case 1:
                    System.out.println("Enter new name:");
                    sc.nextLine();
                    columnValue = sc.nextLine();
                    updateQuery = "UPDATE user_details SET name = ? WHERE account_number = ?";
                    break;

                case 2:
                case 3:
                    LocalDate dob;
                    int age;
                    while (true) {
                        System.out.println("Enter new date of birth (YYYY-MM-DD):");
                        sc.nextLine();
                        String dobStr = sc.nextLine();
                        System.out.println("Enter age:");
                        age = sc.nextInt();

                        dob = LocalDate.parse(dobStr);
                        int calculatedAge = Period.between(dob, LocalDate.now()).getYears();

                        if (calculatedAge != age) {
                            System.out.println("Age does not match the date of birth. Please re-enter.");
                        } else {
                            PreparedStatement pstmt1 = connection.prepareStatement("UPDATE user_details SET date_of_birth = ?, age = ? WHERE account_number = ?");
                            pstmt1.setString(1, dob.toString());
                            pstmt1.setInt(2, age);
                            pstmt1.setString(3, accountNumber);
                            int updated = pstmt1.executeUpdate();
                            if (updated > 0) {
                                System.out.println("Date of birth and age updated successfully!");
                            }
                            return;
                        }
                    }

                case 4:
                    System.out.println("Enter new phone number:");
                    sc.nextLine();
                    columnValue = sc.nextLine();
                    updateQuery = "UPDATE user_details SET phone_no = ? WHERE account_number = ?";
                    break;

                case 5:
                    System.out.println("Enter new address:");
                    sc.nextLine();
                    columnValue = sc.nextLine();
                    updateQuery = "UPDATE user_details SET address = ? WHERE account_number = ?";
                    break;

                case 6:
                    System.out.println("Enter new email ID:");
                    sc.nextLine();
                    columnValue = sc.nextLine();
                    updateQuery = "UPDATE user_details SET email = ? WHERE account_number = ?";
                    break;

                case 7:
                    System.out.println("Enter new salary:");
                    columnValue = String.valueOf(sc.nextInt());
                    updateQuery = "UPDATE user_details SET salary = ? WHERE account_number = ?";
                    break;

                case 8:
                    System.out.println("Enter new branch code:");
                    sc.nextLine();
                    String[] bcodes = {"KT001", "PT001", "KT002", "MT001", "NT001"};
                    do {
                        System.out.println("Enter your branch code:");
                        System.out.println("1) Kavesar (KT001)");
                        System.out.println("2) Patlipada (PT001)");
                        System.out.println("3) Kasarvadavli (KT002)");
                        System.out.println("4) Majiwada (MT001)");
                        System.out.println("5) Navpada (NT001)");
                        columnValue = sc.next();
                        if (!Arrays.asList(bcodes).contains(columnValue)) {
                            System.out.println("Invalid branch code. Please try again.");
                        }
                    } while (!Arrays.asList(bcodes).contains(columnValue));
                    updateQuery = "UPDATE account_details SET branch_code = ? WHERE account_number = ?";
                    break;

                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            if (!updateQuery.isEmpty()) {
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, columnValue);
                updateStmt.setString(2, accountNumber);
                int updated = updateStmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("Account details updated successfully!");
                } else {
                    System.out.println("Update failed.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    public void viewAccountDetails() {
        int choice = 3;
        System.out.println("Enter account number:");
        String accountNumber = sc.nextLine();
        System.out.println("Enter pin:");
        String pin = sc.nextLine();
        String query = "SELECT * FROM account_details LEFT JOIN user_details ON account_details.account_number = user_details.account_number WHERE account_details.account_number = ? AND account_details.pin = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setString(2, pin);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("Invalid account number or pin");
                return;
            }
            String storedPin = resultSet.getString("pin");
            while (!pin.equals(storedPin) && choice > 0) {
                choice--;
                System.out.println("Invalid pin, you have " + choice + " attempts left.");
                if (choice > 0) {
                    System.out.println("Enter pin:");
                    pin = sc.nextLine();
                }
            }
            if (choice == 0) {
                System.out.println("You have exhausted all your choices.");
                return;
            }
            do {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String dob = resultSet.getString("date_of_birth");
                String phone_no = resultSet.getString("phone_no");
                String email = resultSet.getString("email");
                String address = resultSet.getString("address");
                int salary = resultSet.getInt("salary");
                int cibil_score = resultSet.getInt("cibil_score");
                String account_type = resultSet.getString("account_type");
                String account_category = resultSet.getString("account_category");
                float balance = resultSet.getFloat("balance");
                String branch_code = resultSet.getString("branch_code");
                String account_status = resultSet.getString("account_status");
                String created_at = resultSet.getString("created_at");
                String updated_at = resultSet.getString("updated_at");
                System.out.println();
                System.out.println("========================================Account Details of " + name + " :========================================");
                System.out.println("Name: " + name);
                System.out.println("Age: " + age);
                System.out.println("Date of Birth: " + dob);
                System.out.println("Phone Number: " + phone_no);
                System.out.println("Email: " + email);
                System.out.println("Address: " + address);
                System.out.println("Salary: " + salary);
                System.out.println("Civil Score: " + cibil_score);
                System.out.println("Account Type: " + account_type);
                System.out.println("Account Category: " + account_category);
                System.out.println("Balance: " + balance);
                System.out.println("Branch Code: " + branch_code);
                System.out.println("Account Status: " + account_status);
                System.out.println("Created At: " + created_at);
                System.out.println("Updated At: " + updated_at);
                System.out.println("Account details displayed successfully!");
                System.out.println("===============================================================================================================");
                System.out.println();
            }while (resultSet.next());

        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        } 
        finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
            }catch (SQLException closeEx) {
            System.out.println("Error closing resources: " + closeEx.getMessage());
            }
        }
    }

    public void makeFixedDeposit() {
        int attempts = 3;
        System.out.println("Enter your account number:");
        String accountNumber = sc.next();
        String checkAccountQuery = "SELECT * FROM account_details JOIN user_details ON account_details.account_number = user_details.account_number WHERE account_details.account_number = ?";
        try {
            PreparedStatement checkStmt = connection.prepareStatement(checkAccountQuery);
            checkStmt.setString(1, accountNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Invalid account number.");
                return;
            }

            float balance = rs.getFloat("balance");
            int age = rs.getInt("age");

            while (attempts > 0) {
                System.out.println("Enter your PIN (" + attempts + " attempt(s) left):");
                int pin = sc.nextInt();
                if (pin == rs.getInt("pin")) {
                    System.out.println("Enter FD amount:");
                    float amount = sc.nextFloat();
                    if (amount <= 0 || amount > balance) {
                        System.out.println("Invalid FD amount or insufficient balance.");
                        return;
                    }

                    System.out.println("Enter time period in years (min 1):");
                    int years;
                    do {
                        years = sc.nextInt();
                        if (years <= 0) {
                            System.out.println("Please enter valid time period:");
                        }
                    } while (years <= 0);

                    float rate_of_interest;
                    if (age < 60) {
                        rate_of_interest = 7.0f;
                    } else {
                        rate_of_interest = 7.5f;
                    }

                    float maturityAmount = amount + (amount * rate_of_interest * years) / 100;
                    int months = years * 12;

                    String insertFD = "INSERT INTO fixed_deposit (account_number, amount, amount_on_maturity, rate_of_interest, fd_time, fd_creation_date, fd_maturity_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_DATE(), INTERVAL ? MONTH))";
                    PreparedStatement insertStmt = connection.prepareStatement(insertFD);
                    insertStmt.setString(1, accountNumber);
                    insertStmt.setFloat(2, amount);
                    insertStmt.setFloat(3, maturityAmount);
                    insertStmt.setFloat(4, rate_of_interest);
                    insertStmt.setInt(5, months);
                    insertStmt.setInt(6, months);
                    insertStmt.executeUpdate();

                    String updateBal = "UPDATE account_details SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND pin = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateBal);
                    updateStmt.setFloat(1, amount);
                    updateStmt.setString(2, accountNumber);
                    updateStmt.setInt(3, pin);
                    updateStmt.executeUpdate();

                    String fetchBal = "SELECT balance FROM account_details WHERE account_number = ?";
                    PreparedStatement balStmt = connection.prepareStatement(fetchBal);
                    balStmt.setString(1, accountNumber);
                    ResultSet balRes = balStmt.executeQuery();
                    float updatedBalance = 0;
                    if (balRes.next()) {
                        updatedBalance = balRes.getFloat("balance");
                    }

                    String insertPassbook = "INSERT INTO passbook (account_number, to_account, transaction_type, amount, balance, transaction_date, description) VALUES (?, NULL, 'withdraw', ?, ?, CURRENT_TIMESTAMP, 'Cash withdrawal for FD')";
                    PreparedStatement pbStmt = connection.prepareStatement(insertPassbook);
                    pbStmt.setString(1, accountNumber);
                    pbStmt.setFloat(2, amount);
                    pbStmt.setFloat(3, updatedBalance);
                    pbStmt.executeUpdate();

                    System.out.println("Fixed Deposit created successfully!");
                    return;
                } else {
                    attempts--;
                    if (attempts == 0) {
                        System.out.println("Incorrect PIN. Maximum attempts reached.");
                        return;
                    } else {
                        System.out.println("Incorrect PIN. Try again.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }



    public void viewfd() {
        int choice = 3;
        System.out.println("Enter your Account Number");
        String accountNumber = sc.next();
        System.out.println("Enter your pin");
        int pin = sc.nextInt();

        String query = "SELECT * from account_details where account_number=? and pin=?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setInt(2, pin);
            ResultSet result = pstmt.executeQuery();

            if (!result.next()) {
                System.out.println("Invalid Account Number or PIN.");
                return;
            }

            int storedpin = result.getInt("pin");
            while (choice > 1 && pin != storedpin) {
                choice--;
                System.out.println("Invalid pin. You have " + choice + " attempt(s) left");
                System.out.println("Enter pin:");
                pin = sc.nextInt();
                if (pin == storedpin) break;
            }

            if (pin != storedpin) {
                System.out.println("You have exhausted all your attempts");
                return;
            }

            String query1 = "SELECT * from fixed_deposit where account_number=?";
            PreparedStatement pstmt1 = connection.prepareStatement(query1);
            pstmt1.setString(1, accountNumber);
            ResultSet result1 = pstmt1.executeQuery();
            System.out.println("========================================F/D Details of ==============================================");

            int id = 1;
            while (result1.next()) {
                float amount = result1.getFloat("amount");
                float amount_on_maturity = result1.getFloat("amount_on_maturity");
                float rateofinterest = result1.getFloat("rate_of_interest");
                String fdtime = result1.getString("fd_time");
                String fdcreationdate = result1.getString("fd_creation_date");
                String fdmaturitydate = result1.getString("fd_maturity_date");
                System.out.println("ID: " + id++);
                System.out.println("Amount: " + amount);
                System.out.println("Amount on F/D Maturity: " + amount_on_maturity);
                System.out.println("Time Period: " + fdtime + " months");
                System.out.println("Date of F/D Creation: " + fdcreationdate);
                System.out.println("Date of F/D Maturity: " + fdmaturitydate);
                System.out.println("---------------------------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }   

    public void takeLoan() {
    System.out.println("Enter the Type of Loan:");
    System.out.println("1) Personal Loan (Rate: 15% per annum)");
    System.out.println("2) Home Loan (Rate: 10% per annum)");
    System.out.println("3) Education Loan (Rate: 8.5% per annum)");
    System.out.println("4) Business Loan (Rate: 15% per annum)");
    System.out.println("5) Gold Loan (Rate: 9% per annum)");
    System.out.println("6) Agricultural Loan (Rate: 6% per annum)");
    System.out.println("7) Marriage Loan (Rate: 14% per annum)");
    System.out.println("8) Startup Loan (Rate: 15% per annum)");
    System.out.println("9) Vehicle Loan (Rate: 11% per annum)");
    
    float rate_of_interest;
    String loan_type;
    int choice;
    System.out.println("Enter your choice:");
    choice = sc.nextInt();
    
    switch(choice) {
        case 1:
            rate_of_interest = 15;
            loan_type = "Personal Loan";
            break;
        case 2:
            rate_of_interest = 10;
            loan_type = "Home Loan";
            break;
        case 3:
            rate_of_interest = 8.5f;
            loan_type = "Education Loan";
            break;
        case 4:
            rate_of_interest = 15;
            loan_type = "Business Loan";
            break;
        case 5:
            rate_of_interest = 9;
            loan_type = "Gold Loan";
            break;
        case 6:
            rate_of_interest = 6;
            loan_type = "Agricultural Loan";
            break;
        case 7:
            rate_of_interest = 14;
            loan_type = "Marriage Loan";
            break;
        case 8:
            rate_of_interest = 15;
            loan_type = "Startup Loan";
            break;
        case 9:
            rate_of_interest = 11;
            loan_type = "Vehicle Loan";
            break;
        default:
            System.out.println("Invalid Choice");
            return;
    }
    
    System.out.println("Enter your account number:");
    String accountNumber = sc.next();
    
    // PIN verification with 3 attempts
    int pin;
    int attempts = 0;
    boolean pinVerified = false;
    
    while (attempts < 3 && !pinVerified) {
        System.out.println("Enter your PIN:");
        pin = sc.nextInt();
        
        String query = "SELECT * FROM user_details u JOIN account_details a ON u.account_number = a.account_number WHERE u.account_number = ? AND a.pin = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setInt(2, pin);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                pinVerified = true;
                
                int cibil_score = rs.getInt("cibil_score");
                int salary = rs.getInt("salary");
                
                if (cibil_score < 750) {
                    System.out.println("Loan not approved. CIBIL score must be at least 750. Your CIBIL score: " + cibil_score);
                    return;
                }
                
                String checkExistingLoan = "SELECT * FROM loan WHERE account_number = ? AND loan_status = 'Active'";
                PreparedStatement checkStmt = connection.prepareStatement(checkExistingLoan);
                checkStmt.setString(1, accountNumber);
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (checkRs.next()) {
                    System.out.println("You already have an active loan. Please pay your existing loan before taking a new one.");
                    return;
                }
                
                System.out.println("Enter loan amount:");
                float loanAmount = sc.nextFloat();
                
                if (loanAmount <= 0) {
                    System.out.println("Invalid loan amount");
                    return;
                }
                
                // Loan duration validation with while loop
                int duration = 0;
                boolean validDuration = false;
                
                while (!validDuration) {
                    System.out.println("Enter loan duration (in months, maximum 60 months/5 years):");
                    duration = sc.nextInt();
                    
                    if (duration <= 0) {
                        System.out.println("Invalid loan duration. Duration must be greater than 0. Please try again.");
                    } else if (duration > 60) {
                        System.out.println("Loan duration cannot exceed 5 years (60 months). Please enter a duration of 60 months or less.");
                    } else {
                        validDuration = true;
                    }
                }
                
                float monthlyRate = rate_of_interest / (12 * 100);
                float monthlyEmi = (float) (loanAmount * monthlyRate * Math.pow(1 + monthlyRate, duration) / (Math.pow(1 + monthlyRate, duration) - 1));
                
                if (monthlyEmi > salary) {
                    System.out.println("Loan not approved. Monthly EMI (" + monthlyEmi + ") exceeds your salary (" + salary + ")");
                    return;
                }
                
                String insertLoan = "INSERT INTO loan (account_number, loan_type, loan_amount, rate_of_interest, loan_duration) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertLoan);
                insertStmt.setString(1, accountNumber);
                insertStmt.setString(2, loan_type);
                insertStmt.setFloat(3, loanAmount);
                insertStmt.setFloat(4, rate_of_interest);
                insertStmt.setInt(5, duration);
                insertStmt.executeUpdate();
                
                String updateBalance = "UPDATE account_details SET balance = balance + ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateBalance);
                updateStmt.setFloat(1, loanAmount);
                updateStmt.setString(2, accountNumber);
                updateStmt.executeUpdate();
                
                String getNewBalance = "SELECT balance FROM account_details WHERE account_number = ?";
                PreparedStatement balStmt = connection.prepareStatement(getNewBalance);
                balStmt.setString(1, accountNumber);
                ResultSet balRs = balStmt.executeQuery();
                float newBalance = 0;
                if (balRs.next()) {
                    newBalance = balRs.getFloat("balance");
                }
                
                String insertPassbook = "INSERT INTO passbook (account_number, transaction_type, amount, balance, transaction_date, description) VALUES (?, 'deposit', ?, ?, CURRENT_TIMESTAMP, 'Loan deposit')";
                PreparedStatement pbStmt = connection.prepareStatement(insertPassbook);
                pbStmt.setString(1, accountNumber);
                pbStmt.setFloat(2, loanAmount);
                pbStmt.setFloat(3, newBalance);
                pbStmt.executeUpdate();
                
                System.out.println("Loan approved successfully!");
                System.out.println("Loan Amount: " + loanAmount);
                System.out.println("Monthly EMI: " + monthlyEmi);
                System.out.println("Duration: " + duration + " months");
                
            } else {
                attempts++;
                if (attempts < 3) {
                    System.out.println("Invalid PIN. You have " + (3 - attempts) + " attempts remaining.");
                } else {
                    System.out.println("Maximum attempts reached. Access denied.");
                    return;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
            return;
        }
    }
}

public void viewloan() {
    int choice = 3;
    System.out.println("Enter your Account Number:");
    String accountNumber = sc.next();
    System.out.println("Enter your PIN:");
    int pin = sc.nextInt();
    
    String verifyQuery = "SELECT * FROM account_details WHERE account_number = ? AND pin = ?";
    try {
        PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
        verifyStmt.setString(1, accountNumber);
        verifyStmt.setInt(2, pin);
        ResultSet verifyRs = verifyStmt.executeQuery();
        
        if (!verifyRs.next()) {
            System.out.println("Invalid account number or PIN");
            return;
        }
        
        int storedPin = verifyRs.getInt("pin");
        while (choice > 1 && pin != storedPin) {
            choice--;
            System.out.println("Invalid PIN. You have " + choice + " attempt(s) left");
            System.out.println("Enter PIN:");
            pin = sc.nextInt();
            if (pin == storedPin) break;
        }
        
        if (pin != storedPin) {
            System.out.println("You have exhausted all your attempts");
            return;
        }
        
        String loanQuery = "SELECT * FROM loan WHERE account_number = ?";
        PreparedStatement loanStmt = connection.prepareStatement(loanQuery);
        loanStmt.setString(1, accountNumber);
        ResultSet loanRs = loanStmt.executeQuery();
        
        if (!loanRs.next()) {
            System.out.println("No loan found for this account");
            return;
        }
        
        System.out.println("=============================== Loan Details ===============================");
        do {
            int loanId = loanRs.getInt("loan_id");
            String loanType = loanRs.getString("loan_type");
            float loanAmount = loanRs.getFloat("loan_amount");
            float monthlyEmi = loanRs.getFloat("monthly_emi");
            float totalAmount = loanRs.getFloat("total_amount");
            float amountPaid = loanRs.getFloat("amount_paid");
            float remainingAmount = loanRs.getFloat("remaining_amount");
            String loanStatus = loanRs.getString("loan_status");
            String nextEmiDate = loanRs.getString("next_emi_date");
            
            System.out.println("Loan ID: " + loanId);
            System.out.println("Loan Type: " + loanType);
            System.out.println("Loan Amount: " + loanAmount);
            System.out.println("Monthly EMI: " + monthlyEmi);
            System.out.println("Total Amount: " + totalAmount);
            System.out.println("Amount Paid: " + amountPaid);
            System.out.println("Remaining Amount: " + remainingAmount);
            System.out.println("Loan Status: " + loanStatus);
            if (loanStatus.equals("Active")) {
                System.out.println("Next EMI Date: " + nextEmiDate);
            }
            System.out.println("---------------------------------------------------");
        } while (loanRs.next());
        
    } catch (SQLException e) {
        System.out.println("Error executing query: " + e.getMessage());
    }
}

public void payemi() {
    System.out.println("Enter your Account Number:");
    String accountNumber = sc.next();
    System.out.println("Enter your PIN:");
    int pin = sc.nextInt();
    
    String verifyQuery = "SELECT * FROM account_details WHERE account_number = ? AND pin = ?";
    try {
        PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
        verifyStmt.setString(1, accountNumber);
        verifyStmt.setInt(2, pin);
        ResultSet verifyRs = verifyStmt.executeQuery();
        
        if (!verifyRs.next()) {
            System.out.println("Invalid account number or PIN");
            return;
        }
        
        float currentBalance = verifyRs.getFloat("balance");
        
        String nextEmiQuery = "SELECT e.*, l.monthly_emi FROM emi e JOIN loan l ON e.loan_id = l.loan_id WHERE e.account_number = ? AND e.emi_status = 'Pending' ORDER BY e.emi_due_date LIMIT 1";
        PreparedStatement emiStmt = connection.prepareStatement(nextEmiQuery);
        emiStmt.setString(1, accountNumber);
        ResultSet emiRs = emiStmt.executeQuery();
        
        if (!emiRs.next()) {
            System.out.println("No pending EMI found for this account");
            return;
        }
        
        int emiId = emiRs.getInt("emi_id");
        int loanId = emiRs.getInt("loan_id");
        float emiAmount = emiRs.getFloat("emi_amount");
        String dueDate = emiRs.getString("emi_due_date");
        
        System.out.println("EMI Amount: " + emiAmount);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Your Current Balance: " + currentBalance);
        
        if (currentBalance < emiAmount) {
            System.out.println("Insufficient balance to pay EMI");
            return;
        }
        
        System.out.println("Do you want to pay this EMI? (yes/no):");
        String confirmation = sc.next();
        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("EMI payment cancelled");
            return;
        }
        
        String updateEmi = "UPDATE emi SET emi_status = 'Paid', emi_paid_date = CURRENT_DATE, total_paid = emi_amount WHERE emi_id = ?";
        PreparedStatement updateEmiStmt = connection.prepareStatement(updateEmi);
        updateEmiStmt.setInt(1, emiId);
        updateEmiStmt.executeUpdate();
        
        String updateLoan = "UPDATE loan SET amount_paid = amount_paid + ?, remaining_amount = remaining_amount - ?, last_payment_date = CURRENT_DATE WHERE loan_id = ?";
        PreparedStatement updateLoanStmt = connection.prepareStatement(updateLoan);
        updateLoanStmt.setFloat(1, emiAmount);
        updateLoanStmt.setFloat(2, emiAmount);
        updateLoanStmt.setInt(3, loanId);
        updateLoanStmt.executeUpdate();
        
        String updateNextEmiDate = "UPDATE loan SET next_emi_date = (SELECT MIN(emi_due_date) FROM emi WHERE loan_id = ? AND emi_status = 'Pending') WHERE loan_id = ?";
        PreparedStatement updateNextEmiStmt = connection.prepareStatement(updateNextEmiDate);
        updateNextEmiStmt.setInt(1, loanId);
        updateNextEmiStmt.setInt(2, loanId);
        updateNextEmiStmt.executeUpdate();
        
        String updateBalance = "UPDATE account_details SET balance = balance - ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ?";
        PreparedStatement updateBalStmt = connection.prepareStatement(updateBalance);
        updateBalStmt.setFloat(1, emiAmount);
        updateBalStmt.setString(2, accountNumber);
        updateBalStmt.executeUpdate();
        
        float newBalance = currentBalance - emiAmount;
        String insertPassbook = "INSERT INTO passbook (account_number, transaction_type, amount, balance, transaction_date, description) VALUES (?, 'withdraw', ?, ?, CURRENT_TIMESTAMP, 'Withdrawal for paying EMI')";
        PreparedStatement pbStmt = connection.prepareStatement(insertPassbook);
        pbStmt.setString(1, accountNumber);
        pbStmt.setFloat(2, emiAmount);
        pbStmt.setFloat(3, newBalance);
        pbStmt.executeUpdate();
        
        System.out.println("EMI paid successfully!");
        System.out.println("Amount paid: " + emiAmount);
        System.out.println("Remaining balance: " + newBalance);
        
    } catch (SQLException e) {
        System.out.println("Error executing query: " + e.getMessage());
    }
}

public void deleteAccount() {
    int attempts = 3;
    sc.nextLine();
    System.out.println("Enter account number to be deleted:");
    String accountNumber = sc.nextLine();
    
    int pin;
    while (attempts > 0) {
        System.out.println("Enter PIN (" + attempts + " attempt(s) left):");
        pin = sc.nextInt();
        sc.nextLine();
        
        String query = "SELECT * FROM account_details WHERE account_number = ? AND pin = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, accountNumber);
            pstmt.setInt(2, pin);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                float balance = rs.getFloat("balance");
                
                if (balance > 0) {
                    System.out.println("Cannot delete account. Please withdraw all money first. Current balance: " + balance);
                    return;
                }
                
                String checkFDQuery = "SELECT * FROM fixed_deposit WHERE account_number = ?";
                PreparedStatement checkFD = connection.prepareStatement(checkFDQuery);
                checkFD.setString(1, accountNumber);
                ResultSet fdResult = checkFD.executeQuery();
                if (fdResult.next()) {
                    System.out.println("Cannot delete account. Active fixed deposit exists.");
                    return;
                }
                
                String checkLoanQuery = "SELECT * FROM loan WHERE account_number = ? AND loan_status = 'Active'";
                PreparedStatement checkLoan = connection.prepareStatement(checkLoanQuery);
                checkLoan.setString(1, accountNumber);
                ResultSet loanResult = checkLoan.executeQuery();
                if (loanResult.next()) {
                    System.out.println("Cannot delete account. Active loan exists. Please pay all EMIs first.");
                    return;
                }
                
                System.out.println("Are you sure you want to delete the account? (yes/no):");
                String confirmation = sc.nextLine();
                if (confirmation.equalsIgnoreCase("no")) {
                    System.out.println("Account deletion cancelled.");
                    return;
                }
                
                String deleteQuery = "DELETE user_details, account_details FROM user_details JOIN account_details ON user_details.account_number = account_details.account_number WHERE user_details.account_number = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
                deleteStmt.setString(1, accountNumber);
                int rowsDeleted = deleteStmt.executeUpdate();
                
                if (rowsDeleted > 0) {
                    System.out.println("Account deleted successfully!");
                } else {
                    System.out.println("Account not found or deletion failed.");
                }
                return;
            } else {
                attempts--;
                if (attempts == 0) {
                    System.out.println("Invalid PIN. Maximum attempts reached. Action cancelled.");
                    return;
                } else {
                    System.out.println("Invalid PIN. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
            return;
        }
    }
}


    public static void main(String[] args) {
        int choice;
        App ob=new App();
        Scanner sc = new Scanner(System.in);
        do{
            System.out.println("Welcome To Virtual Banking System");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Transfer Money");
            System.out.println("6. View Transaction History");
            System.out.println("7. Change Account Details");
            System.out.println("8. View Account Details");
            System.out.println("9. Make Fixed Deposit(FD)");
            System.out.println("10. View Fixed Deposit(s)");
            System.out.println("11. Take Loan");
            System.out.println("12. View Loan History");
            System.out.println("13. Pay Loan EMI");
            System.out.println("14. Delete Account");
            System.out.println("15. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            switch(choice){
                case 1:
                    ob.createAccount();
                    break;
                case 2:
                    ob.depositMoney();
                    break;
                case 3:
                    ob.withdrawMoney();
                    break;
                case 4:
                    ob.checkBalance();
                    break;
                case 5:
                    ob.transferMoney();
                    break;
                case 6:
                    ob.printPassbook();
                    break;
                case 7:
                    ob.changeAccountDetails();
                    break;
                case 8:
                    ob.viewAccountDetails();
                    break;
                case 9:
                    System.out.println("Rate of Interest is 7% per annum but if you are a senior citizen Rate of Interest is 7.5% per annum");
                    ob.makeFixedDeposit();
                    break;
                case 10:
                    ob.viewfd();
                    break;
                case 11:
                    ob.takeLoan();
                    break;
                case 12:
                    ob.viewloan();
                    break;
                case 13:
                    ob.payemi();
                    break;
                case 14:
                    ob.deleteAccount();
                    break;
                case 15:
                    System.out.println("Thank you for using Virtual Banking System!");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }while(choice!=15);
    }
}






