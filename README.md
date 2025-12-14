# Personal Finance Manager

CLI application for managing personal finances. Track income and expenses, manage budgets by categories, and get financial statistics.

## Features

- User registration and authentication
- Income and expense tracking with categories
- Budget management for expense categories
- Budget remaining balance tracking
- Financial statistics and summaries
- Automatic budget exceeded warnings
- Data persistence in JSON files

## Requirements

- Java 17 or higher
- Maven 3.6+

## Dependencies

All dependencies are managed by Maven and will be automatically downloaded during the build process:
- Jackson (JSON serialization) - version 2.15.2
- JUnit 5 (testing) - version 5.10.0
- Checkstyle (code style validation) - version 10.12.5

No manual installation required - Maven handles everything automatically.

## Installation and Running

1. Clone the repository or download the project
2. Open terminal in the project directory
3. Build the project:
   ```bash
   mvn clean compile
   ```
4. Run the application:
   ```bash
   mvn exec:java -Dexec.mainClass="vp.financemanager.cli.FinanceCliApp"
   ```

Or compile and run directly:
```bash
mvn package
java -cp target/classes vp.financemanager.cli.FinanceCliApp
```

## Usage

After starting, you'll see a command prompt. Type `help` to see available commands.

### Available Commands

- `register` - register a new user
- `login` - login as existing user
- `logout` - logout current user
- `add_income` - add income transaction
- `add_expense` - add expense transaction
- `set_budget` - set budget limit for a category
- `show_budgets` - show all budgets with remaining limits
- `show_categories` - list all categories with budgets
- `show_summary` - show income/expenses summary
- `show_transactions` - show transactions with filters (category, date range, type)
- `export_transactions` - export transactions to CSV file
- `import_transactions` - import transactions from CSV file
- `rename_category` - rename a category (updates all transactions and budgets)
- `help` - show help message
- `exit` - exit the application

## Data Storage

Data is automatically saved in the `data/` directory:
- `data/users.json` - user list (logins and password hashes)
- `data/wallet_{login}.json` - each user's wallet

Data is saved on every change and on application exit. All data is automatically loaded on startup.

## Project Structure

The project follows Clean Architecture principles with layer separation:

```
src/main/java/vp/financemanager/
├── cli/           # CLI interface
├── core/          # Business logic
│   ├── models/    # Domain models
│   ├── service/   # Business services
│   └── repository/ # Repository interfaces
└── infra/         # Infrastructure implementations
    └── repository/ # File-based repositories
```

## Testing

To run tests:
```bash
mvn test
```

## Code Quality

The project uses Checkstyle for code style validation:

```bash
mvn checkstyle:check
```

## Continuous Integration

The project includes GitHub Actions CI workflow that automatically:
- Builds the project
- Runs all tests
- Checks code style with Checkstyle

The CI runs on every push and pull request to main/master/develop branches.

## Technologies

- Java 17
- Maven
- Jackson (JSON serialization)
- SHA-256 (password hashing)
- JUnit 5 (testing)
- Checkstyle (code style validation)

