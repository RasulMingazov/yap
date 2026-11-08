# Spec Delta

## MODIFIED Requirements

### Requirement: The sidebar opens the characters

The sidebar of the admin app SHALL show Topics and Characters in the group Content, in this order,
and Server in the group Settings. The admin app SHALL open on Topics when it has saved server
settings, and on Server when it has none.

#### Scenario: The first start

- **WHEN** the owner starts the admin app for the first time
- **THEN** the admin app opens Server

#### Scenario: A later start

- **WHEN** the owner starts the admin app after a connection to a server
- **THEN** the admin app opens Topics
