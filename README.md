Splitease

Splitease is a Splitwise-style expense sharing application builtwith Java and Spring Boot. It helps users create groups, add sharedexpenses, track individual shares, and settle balances. The project alsoincludes asynchronous email notifications for expense and settlementevents.

Features

👤 User management

👥 Group-based expense sharing

💰 Add and split expenses among group members

🧮 Track how much each member owes

🤝 Settle up between users

📧 Asynchronous email notifications

Notify members when a new expense is added

Notify users when a settlement is completed

⚡ Background email processing using Spring @Async

🧵 Dedicated notification thread pool using ThreadPoolTaskExecutor

🛡️ Per-recipient email error handling so one failed email does notstop other notifications

Tech Stack

Java 21

Spring Boot

Spring Data JPA / Hibernate

PostgreSQL / relational database

Maven

Lombok

Spring Mail

Spring Async

Git & GitHub

Maven Dependencies

spring-boot-starter-parent

splitease

jjwt-api

jjwt-impl

jjwt-jackson

spring-boot-starter-data-jpa

spring-boot-starter-mail

spring-boot-starter-security

spring-boot-starter-validation

spring-boot-starter-webmvc

springdoc-openapi-starter-webmvc-ui

postgresql

lombok

spring-boot-starter-data-jpa-test

spring-boot-starter-security-test

spring-boot-starter-validation-test

spring-boot-starter-webmvc-test

spring-boot-maven-plugin

maven-compiler-plugin

Project Structure

src/
├── main/
│   ├── java/
│   │   └── com/splitease/splitease/
│   │       ├── config/
│   │       │   └── AsyncConfig.java
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       │   └── NotificationService.java
│   │       └── SpliteaseApplication.java
│   └── resources/
│       └── application.yaml
└── test/

The exact package structure may evolve as new modules are added.

How the Notification System Works

Email notifications are intentionally processed asynchronously so thatsending an email does not block the API request.

Expense API
    │
    ▼
Expense Service
    │
    ├── Save expense
    │
    └── Notify members
             │
             ▼
      @Async("notificationExecutor")
             │
             ▼
       Notification Thread Pool
             │
             ▼
        JavaMailSender
             │
             ▼
          Email

The application uses a dedicated executor:

@Bean(name = "notificationExecutor")
public Executor notificationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("notif-");
    executor.initialize();
    return executor;
}

The notification service uses:

@Async("notificationExecutor")

This allows the caller to continue without waiting for the emailoperation to finish.

Email Configuration

Configure your SMTP sender in application.yaml orapplication.properties.

Example for Gmail:

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-sender-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

Important

Do not commit real passwords, API keys, or app passwords to GitHub.

For local development, prefer environment variables or a local, ignoredconfiguration file.

For Gmail, use an App Password rather than your normal accountpassword when applicable.

Getting Started

Prerequisites

Make sure you have:

Java 21+

Maven

PostgreSQL (if your configured environment uses PostgreSQL)

A configured SMTP account if you want to test email notifications

Clone

git clone <your-repository-url>
cd Splitease

Configure the Database

Update the database configuration in:

src/main/resources/application.yaml

Set your database URL, username, and password according to your localenvironment.

Configure Email

Add your SMTP configuration and credentials through environmentvariables or local configuration.

Run the Application

Using Maven:

./mvnw spring-boot:run

On Windows PowerShell:

.\mvnw.cmd spring-boot:run

Or build and run:

./mvnw clean package
java -jar target/*.jar

Testing Email Notifications

Configure a dedicated test sender email.

Create users in Splitease with real recipient email addresses.

Add users to a group.

Create an expense involving those members.

The notification service sends the emails in the background.

Complete a settlement to test settlement notifications.

The sender account configured for SMTP and the recipient's email areseparate:

SMTP username
      │
      ▼
Sender account
      │
      ▼
JavaMailSender
      │
      ▼
User email stored in database

Git Workflow

For feature development:

git switch -c feature/<feature-name>

git add .
git commit -m "Describe the change"

git push -u origin feature/<feature-name>

Then open a Pull Request:

feature/<feature-name>
          │
          ▼
     Pull Request
          │
          ▼
         main

After the PR is merged:

git switch main
git pull origin main

Error Handling

Notification failures are handled per recipient. If sending an email toone user fails, the service logs the failure and continues processingthe remaining recipients.

This prevents one email failure from stopping all notifications.

Future Improvements

HTML email templates

Email retry mechanism

Notification preferences per user

In-app notifications

Kafka-based event-driven notifications

Dead-letter handling for failed notifications

Centralized notification events

Production-grade secrets management

Integration and unit test coverage

License

This project is currently intended for learning and developmentpurposes.
