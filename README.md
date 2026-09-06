# Animal Management API

Animal management API built with Clojure and PostgreSQL, evolving toward GraphQL and AI-powered agents.

## Overview

This repository is a hands-on Clojure learning project built around a real backend application instead of a toy tutorial.

The project intentionally grows one architectural layer at a time:

```text
GraphQL
   ↓
Application
   ↓
Domain
   ↓
Repository
   ↓
next.jdbc
   ↓
PostgreSQL
```

The next evolution is an AI Agent that will use the Application Layer through explicit tools:

```text
User
 ↓
AI Agent
 ↓
LLM
 ↓
Tools
 ↓
Application
 ↓
Domain
 ↓
Repository
 ↓
PostgreSQL
```

The first AI-agent experiments will use Gemini Flash and will focus on understanding agent architecture rather than hiding the concepts behind a framework.

## Goals

- Learn Clojure by building a real backend.
- Translate senior Ruby/Rails engineering experience into a functional JVM language.
- Understand immutable data and function-oriented design.
- Build HTTP, persistence, repository, GraphQL, application, and domain layers.
- Practice TDD in Clojure.
- Understand the boundary between pure business logic and side effects.
- Prepare the architecture for AI agents and tool calling.

## Tech Stack

- Clojure 1.12.x
- Clojure CLI / `deps.edn`
- Java 21 for this project
- Ring
- Reitit
- Jetty
- Cheshire
- next.jdbc
- PostgreSQL
- Migratus
- Lacinia GraphQL
- clojure.test

## Project Structure

```text
animal-management-api/
├── README.md
├── docs/
│   ├── CLOJURE_LEARNING_MANUAL_PT-BR.md
│   └── CLOJURE_LEARNING_MANUAL_EN.md
├── deps.edn
├── .java-version
├── resources/
│   └── migrations/
│       ├── 001-create-animals-table.up.sql
│       └── 001-create-animals-table.down.sql
├── src/
│   └── animal_management/
│       ├── core.clj
│       ├── http.clj
│       ├── server.clj
│       ├── db.clj
│       ├── migration.clj
│       ├── repository.clj
│       ├── graphql.clj
│       └── application.clj
└── test/
    └── animal_management/
        ├── core_test.clj
        ├── http_test.clj
        ├── db_test.clj
        ├── repository_test.clj
        ├── graphql_test.clj
        └── application_test.clj
```

## Architecture

### Domain

`core.clj` contains pure domain functions.

```clojure
(defn create-animal
  [name species]
  {:name name
   :species species
   :status :available})

(defn rename-animal
  [animal new-name]
  (assoc animal :name new-name))

(defn adopt-animal
  [animal]
  (if (= :available (:status animal))
    (assoc animal :status :adopted)
    animal))
```

The domain does not know about HTTP, PostgreSQL, GraphQL, or the LLM.

### HTTP

Ring represents HTTP requests and responses as Clojure maps.

Reitit maps routes to handlers:

```text
HTTP
 ↓
Jetty
 ↓
Ring
 ↓
Reitit
 ↓
Handler
```

Current routes:

- `GET /health`
- `GET /animals`
- `POST /graphql`

### Persistence

PostgreSQL is used as the database.

`next.jdbc` provides direct JDBC access rather than an ActiveRecord-style ORM.

The schema is managed with Migratus.

### Repository

The Repository owns database access:

```text
Application
    ↓
Repository
    ↓
next.jdbc
    ↓
PostgreSQL
```

It exposes operations such as:

```clojure
(repository/create-animal! animal)
(repository/find-animal id)
(repository/list-animals)
(repository/update-animal! id animal)
(repository/delete-animal! id)
```

Database rows are converted back into domain data, including keyword conversion for `species` and `status`.

### GraphQL

Lacinia exposes the API through GraphQL.

Current schema concepts:

```text
Animal
├── id: Int
├── name: String
├── species: Species
└── status: AnimalStatus
```

Enums:

```text
Species
├── DOG
└── CAT

AnimalStatus
├── AVAILABLE
└── ADOPTED
```

The GraphQL layer uses the Application Layer rather than calling the Repository directly.

### Application

The Application Layer represents use cases:

```clojure
(application/create-animal animal)
(application/find-animal id)
(application/list-animals)
(application/update-animal id animal)
(application/delete-animal id)
(application/adopt-animal id)
```

The adoption use case demonstrates the intended architecture:

```text
Application
    ↓
Repository → find
    ↓
Domain → apply business rule
    ↓
Repository → persist
```

The Application Layer orchestrates effects; the Domain Layer owns the business rule.

## Animal Adoption Use Case

The current rule is:

```text
AVAILABLE → ADOPTED
ADOPTED   → ADOPTED
```

For an unknown ID, the use case returns `nil` without attempting an update.

Implementation:

```clojure
(defn adopt-animal
  [id]
  (let [animal (repository/find-animal id)]
    (when animal
      (let [adopted-animal (core/adopt-animal animal)]
        (repository/update-animal! id adopted-animal)))))
```

Ruby equivalent:

```ruby
def adopt_animal(id)
  animal = repository.find_animal(id)
  return unless animal

  adopted_animal = Domain.adopt_animal(animal)
  repository.update_animal(id, adopted_animal)
end
```

## Clojure Concepts Learned

The project has been used to learn:

- `ns` and namespaces
- `require` and aliases
- `defn`
- function arguments and arity
- maps
- vectors
- keywords
- keywords as functions
- `nil`
- truthiness
- `let`
- `if`
- `when`
- `assoc`
- `update`
- `map`
- `filter`
- `first`
- `some?`
- `some->`
- threading with `->`
- lazy sequences
- immutable data
- pure functions
- side effects
- data transformation
- namespace/file conventions
- Clojure CLI
- `deps.edn`
- dependency aliases
- Ring
- Reitit
- Jetty
- next.jdbc
- Migratus
- PostgreSQL
- Lacinia GraphQL
- GraphQL enums
- repository pattern
- application/use-case layer
- domain/application/repository separation
- TDD with `clojure.test`
- Conventional Commits
- Git Flow with feature branches and pull requests

See the complete learning manuals:

- [Clojure Learning Manual — PT-BR](docs/CLOJURE_LEARNING_MANUAL_PT-BR.md)
- [Clojure Learning Manual — English](docs/CLOJURE_LEARNING_MANUAL_EN.md)

## Clojure vs Ruby

A recurring goal of this project is to map new Clojure concepts to concepts already familiar from Ruby.

### Map vs Hash

```clojure
{:name "Luna"
 :species :dog}
```

Conceptually:

```ruby
{
  name: "Luna",
  species: :dog
}
```

### Keyword lookup

```clojure
(:name animal)
```

Conceptually:

```ruby
animal[:name]
```

### Immutable update

```clojure
(assoc animal :name "Bella")
```

Conceptually:

```ruby
animal.merge(name: "Bella")
```

The important difference is that the Clojure map is immutable; `assoc` returns a new value.

### Function vs method

```clojure
(adopt-animal animal)
```

Conceptually:

```ruby
adopt_animal(animal)
```

Clojure favors functions operating on data rather than objects carrying mutable state.

### Mapping

```clojure
(map :name animals)
```

Conceptually:

```ruby
animals.map { |animal| animal[:name] }
```

Keywords can act as functions for map lookup.

## Testing

Application Layer:

```bash
clojure -M:test -e "(require '[clojure.test :as t] '[animal-management.application-test]) (t/run-tests 'animal-management.application-test)"
```

Repository:

```bash
clojure -M:test -e "(require '[clojure.test :as t] '[animal-management.repository-test]) (t/run-tests 'animal-management.repository-test)"
```

GraphQL:

```bash
clojure -M:test -e "(require '[clojure.test :as t] '[animal-management.graphql-test]) (t/run-tests 'animal-management.graphql-test)"
```

HTTP:

```bash
clojure -M:test -e "(require '[clojure.test :as t] '[animal-management.http-test]) (t/run-tests 'animal-management.http-test)"
```

Full suite:

```bash
clojure -M:test -e '(require (quote animal-management.core-test) (quote animal-management.http-test) (quote animal-management.db-test) (quote animal-management.repository-test) (quote animal-management.graphql-test) (quote animal-management.application-test)) (clojure.test/run-tests (quote animal-management.core-test) (quote animal-management.http-test) (quote animal-management.db-test) (quote animal-management.repository-test) (quote animal-management.graphql-test) (quote animal-management.application-test))'
```

Latest validated result before the AI-agent phase:

```text
Ran 26 tests containing 74 assertions.
0 failures, 0 errors.
```

## Database

PostgreSQL database:

```text
animal_management
```

Animals table:

```text
animals
├── id BIGSERIAL PRIMARY KEY
├── name VARCHAR(255) NOT NULL
├── species VARCHAR(50) NOT NULL
└── status VARCHAR(50) NOT NULL
```

Cleanup during development:

```bash
psql -d animal_management -c "TRUNCATE TABLE animals RESTART IDENTITY;"
```

## deps.edn

The project uses `deps.edn` for classpaths, dependencies, and the test alias.

Important concepts:

```clojure
{:paths ["src" "resources"]

 :deps
 {...}

 :aliases
 {:test
  {:extra-paths ["test"]}}}
```

`src` and `resources` are project paths, dependencies are declared under `:deps`, and the test alias adds `test` to the classpath.

## Git Workflow

Every feature follows:

```text
feature branch
    ↓
implementation
    ↓
tests
    ↓
commit
    ↓
push
    ↓
Pull Request
    ↓
review
    ↓
merge → master
```

Conventional Commits are used.

Examples:

```text
feat: add animal application layer
feat: add animal adoption use case
chore: add PostgreSQL persistence dependencies
```

## Lessons From Real Errors

### Qualified JDBC result keys

`next.jdbc` can return qualified column keys depending on the result-set builder. The project uses:

```clojure
{:builder-fn rs/as-unqualified-maps}
```

so database results become easy-to-use domain maps such as:

```clojure
{:id 1
 :name "Luna"
 :species :dog
 :status :available}
```

### Ring JSON body

`wrap-json-body` parses the JSON request body into the request's `:body`.

Therefore the GraphQL handler reads:

```clojure
(get-in request [:body :query])
```

rather than `:body-params`.

### Pure domain vs effects

A major lesson is keeping business rules independent from infrastructure.

Good:

```text
Domain → pure transformation
Repository → database side effect
Application → orchestration
```

## Current Roadmap

### Completed

1. Domain
2. HTTP API
3. PostgreSQL persistence
4. Repository CRUD
5. GraphQL API
6. Application Layer
7. Animal adoption use case

### Current

8. AI Agent

### Planned AI Agent Evolution

```text
User
 ↓
Agent
 ↓
LLM
 ↓
Tool selection
 ↓
Application
 ↓
Domain
 ↓
Repository
 ↓
PostgreSQL
```

Planned learning topics:

- Gemini API
- LLM client abstraction
- prompts
- structured output
- tool definitions
- tool calling
- agent loop
- tool execution
- conversation state
- error handling
- retries and timeouts
- observability
- evaluation

The advanced future project may later explore:

- memory
- RAG
- embeddings
- vector databases
- MCP
- multi-agent systems
- asynchronous/event-driven agents
- guardrails
- human-in-the-loop
- evaluation frameworks

This repository intentionally remains a small learning laboratory. A separate future repository can explore production-grade agent architecture.

## Development Environment

The project is developed on macOS Apple Silicon.

This project uses Java 21 locally through `.java-version`.

Example:

```bash
jenv local 21.0.12.1
```

The repository intentionally does not require changing the system-wide Terminal architecture.

## License

Learning project.
