# Contributing to GatotAuth

Thank you for your interest in contributing to GatotAuth! This document provides guidelines to ensure a smooth contribution process.

## Code of Conduct

By participating in this project, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

### Reporting Bugs

- Search the [GitHub Issues](https://github.com/gatotauth/gatotauth/issues) to see if the bug has already been reported.
- If not, create a new issue using the Bug Report template. Include details, reproduction steps, and platform logs (e.g., Velocity, paper-loader logs).

### Proposing Features

- Submit a feature request using the Feature Request template.
- Explain the problem, why the feature is needed, and any potential alternatives.

### Submitting Pull Requests

1. Fork the repository.
2. Create a feature branch matching Conventional Commits names (`feat/my-feature`, `fix/bug-fix`).
3. Implement your changes. Preserve binary compatibility and follow existing design patterns (Hexagonal Architecture / Polymorphic IAM).
4. Run formatting checks:
   ```bash
   ./gradlew spotlessApply
   ./gradlew checkstyleMain
   ```
5. Ensure all unit and integration tests compile and pass:
   ```bash
   ./gradlew test
   ```
6. Submit the PR targeting the `main` branch.

## Commit Message Guidelines

We enforce Conventional Commits formatting:

```text
<type>(<scope>): <subject>

[optional body]
```

Types:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation-only changes
- `style`: Formatting, missing semicolons, etc. (no production code changes)
- `refactor`: Refactoring code without behavior modifications
- `test`: Adding missing tests or correcting existing ones
- `chore`: Build system configurations or dependencies updates
