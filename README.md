# Virtual-Banking-System
Virtual Banking System using Java and MySQL

Overview
This is a comprehensive virtual banking system implemented in Java using JDBC for database connectivity. The application provides a complete banking solution with features like account management, transactions, fixed deposits, loans, and more.

Features-
1)Account Management
   Create new bank accounts
   View account details
   Modify account information
   Delete accounts
   
2)Transactions
   Deposit money
   Withdraw money
   Transfer funds between accounts
   View transaction history
   Check account balance

3)Fixed Deposits
   Create fixed deposits with automatic maturity calculation
   View FD details
   Automatic crediting of matured FDs

4)Loan Management
   Apply for various loan types (personal, home, education, etc.)
   View loan details
   Pay EMIs
   Loan eligibility based on CIBIL score

5)Security Features
   PIN-based authentication
   Multiple attempt limits
   Transaction logging
   Account validation


🗄️ Database Setup
Create Database
CREATE DATABASE project;
USE project;

Required Tables

1)User Details Table-

CREATE TABLE user_details (
    user_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(90) NOT NULL,
    age INT NOT NULL,
    date_of_birth DATE NOT NULL,
    phone_no CHAR(10) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    salary INT NOT NULL,
    cibil_score INT NOT NULL,
    account_number CHAR(8) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY (email),
    UNIQUE KEY (account_number),
    FOREIGN KEY (account_number) REFERENCES account_details(account_number)
);

2)Account Details Table-

CREATE TABLE account_details (
    account_id INT NOT NULL AUTO_INCREMENT,
    account_type VARCHAR(50) NOT NULL,
    account_category VARCHAR(10) NOT NULL,
    balance FLOAT NOT NULL DEFAULT 0,
    branch_code VARCHAR(10),
    account_status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pin CHAR(4) NOT NULL,
    account_number CHAR(8) NOT NULL,
    PRIMARY KEY (account_id),
    UNIQUE KEY (account_number)
);

3)Passbook Table-

CREATE TABLE passbook (
    passbook_id INT NOT NULL AUTO_INCREMENT,
    account_number CHAR(8),
    to_account CHAR(8),
    transaction_type VARCHAR(90),
    amount FLOAT NOT NULL,
    balance FLOAT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    PRIMARY KEY (passbook_id),
    FOREIGN KEY (account_number) REFERENCES account_details(account_number),
    FOREIGN KEY (to_account) REFERENCES account_details(account_number)
);

4)Fixed Deposit Table-

CREATE TABLE fixed_deposit (
    fd_id INT AUTO_INCREMENT NOT NULL,
    account_number CHAR(8) NOT NULL,
    amount FLOAT NOT NULL,
    amount_on_maturity FLOAT NOT NULL,
    rate_of_interest FLOAT NOT NULL,
    fd_time INT NOT NULL,
    fd_creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fd_maturity_date DATE NOT NULL,
    PRIMARY KEY (fd_id),
    FOREIGN KEY (account_number) REFERENCES account_details(account_number),
    CHECK (amount > 0),
    CHECK (rate_of_interest > 0),
    CHECK (fd_time > 0)
);

DELIMITER //
CREATE EVENT process_matured_fds
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_account_number CHAR(8);
    DECLARE v_amount FLOAT;
    DECLARE v_fd_id INT;
    
    DECLARE fd_cursor CURSOR FOR 
        SELECT fd_id, account_number, amount_on_maturity 
        FROM fixed_deposit 
        WHERE fd_maturity_date = CURDATE() 
        AND amount_on_maturity > 0;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN fd_cursor;
    
    fd_loop: LOOP
        FETCH fd_cursor INTO v_fd_id, v_account_number, v_amount;
        
        IF done THEN
            LEAVE fd_loop;
        END IF;
        
        START TRANSACTION;
        
        UPDATE user_details 
        SET balance = balance + v_amount 
        WHERE account_number = v_account_number;
        
        UPDATE fixed_deposit 
        SET amount_on_maturity = 0 
        WHERE fd_id = v_fd_id;
        
        COMMIT;
    END LOOP;
    
    CLOSE fd_cursor;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER before_fd_insert
BEFORE INSERT ON fixed_deposit
FOR EACH ROW
BEGIN
    SET NEW.fd_maturity_date = DATE_ADD(
        DATE(NEW.fd_creation_date), 
        INTERVAL NEW.fd_time MONTH
    );
    
    SET NEW.amount_on_maturity = NEW.amount * 
        (1 + (NEW.rate_of_interest * NEW.fd_time) / 1200);
END//
DELIMITER ;

SET GLOBAL event_scheduler = ON;

5)Loan and Emi Table-

CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT NOT NULL,
    account_number CHAR(8) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_amount FLOAT NOT NULL CHECK (loan_amount > 0),
    rate_of_interest FLOAT NOT NULL CHECK (rate_of_interest > 0),
    loan_duration INT NOT NULL CHECK (loan_duration > 0),
    monthly_emi FLOAT NOT NULL CHECK (monthly_emi > 0),
    total_amount FLOAT NOT NULL CHECK (total_amount > 0),
    amount_paid FLOAT DEFAULT 0 CHECK (amount_paid >= 0),
    remaining_amount FLOAT NOT NULL CHECK (remaining_amount >= 0),
    loan_status VARCHAR(20) DEFAULT 'Active',
    loan_creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_emi_date DATE NOT NULL,
    last_payment_date DATE NULL,
    PRIMARY KEY (loan_id),
    FOREIGN KEY (account_number) REFERENCES account_details(account_number) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE emi (
    emi_id INT AUTO_INCREMENT NOT NULL,
    loan_id INT NOT NULL,
    account_number CHAR(8) NOT NULL,
    emi_amount FLOAT NOT NULL CHECK (emi_amount > 0),
    emi_due_date DATE NOT NULL,
    emi_paid_date DATE NULL,
    emi_status VARCHAR(20) DEFAULT 'Pending',
    penalty_amount FLOAT DEFAULT 0 CHECK (penalty_amount >= 0),
    total_paid FLOAT DEFAULT 0 CHECK (total_paid >= 0),
    PRIMARY KEY (emi_id),
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (account_number) REFERENCES account_details(account_number) ON DELETE CASCADE ON UPDATE CASCADE
);

DELIMITER //
CREATE TRIGGER before_loan_insert
BEFORE INSERT ON loan
FOR EACH ROW
BEGIN
    DECLARE monthly_rate FLOAT;
    DECLARE emi_calc FLOAT;
    SET monthly_rate = NEW.rate_of_interest / (12 * 100);
    SET emi_calc = NEW.loan_amount * monthly_rate * POWER(1 + monthly_rate, NEW.loan_duration) / (POWER(1 + monthly_rate, NEW.loan_duration) - 1);
    SET NEW.monthly_emi = ROUND(emi_calc, 2);
    SET NEW.total_amount = NEW.monthly_emi * NEW.loan_duration;
    SET NEW.remaining_amount = NEW.total_amount;
    SET NEW.next_emi_date = DATE_ADD(DATE(NEW.loan_creation_date), INTERVAL 1 MONTH);
END;
//
DELIMITER ;

DELIMITER //
CREATE TRIGGER after_loan_insert
AFTER INSERT ON loan
FOR EACH ROW
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE emi_date DATE;
    WHILE i <= NEW.loan_duration DO
        SET emi_date = DATE_ADD(DATE(NEW.loan_creation_date), INTERVAL i MONTH);
        INSERT INTO emi (loan_id, account_number, emi_amount, emi_due_date)
        VALUES (NEW.loan_id, NEW.account_number, NEW.monthly_emi, emi_date);
        SET i = i + 1;
    END WHILE;
END;
//
DELIMITER ;

DELIMITER //
CREATE EVENT process_overdue_emis
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_account_number CHAR(8);
    DECLARE cur CURSOR FOR
        SELECT DISTINCT e.account_number
        FROM emi e
        JOIN loan l ON e.loan_id = l.loan_id
        WHERE e.emi_status = 'Pending' 
        AND DATEDIFF(CURDATE(), e.emi_due_date) = 5
        AND l.loan_status = 'Active';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    emi_loop: LOOP
        FETCH cur INTO v_account_number;
        IF done THEN
            LEAVE emi_loop;
        END IF;
        
        UPDATE user_details
        SET cibil_score = GREATEST(cibil_score - 5, 300)
        WHERE account_number = v_account_number;
        
    END LOOP;
    CLOSE cur;
END;
//
DELIMITER ;

DELIMITER //
CREATE TRIGGER after_emi_payment
AFTER UPDATE ON emi
FOR EACH ROW
BEGIN
    DECLARE pending_emis INT;
    IF NEW.emi_status = 'Paid' AND OLD.emi_status = 'Pending' THEN
        SELECT COUNT(*) INTO pending_emis
        FROM emi
        WHERE loan_id = NEW.loan_id AND emi_status = 'Pending';
        
        IF pending_emis = 0 THEN
            UPDATE loan
            SET loan_status = 'Completed',
                remaining_amount = 0
            WHERE loan_id = NEW.loan_id;
        END IF;
    END IF;
END;
//
DELIMITER ;

SET GLOBAL event_scheduler = ON;
