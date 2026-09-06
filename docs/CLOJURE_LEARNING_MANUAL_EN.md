# Clojure Learning Manual — Animal Management API

## 1. Purpose

This manual documents the Clojure learning journey built through the `animal-management-api` project.

The goal was not to learn Clojure through artificial beginner exercises. The goal was to build a real backend while translating existing senior Ruby/Rails engineering experience into Clojure's functional and JVM-oriented model.

The implemented evolution is:

```text
Domain
  ↓
HTTP / Reitit
  ↓
PostgreSQL
  ↓
Repository
  ↓
GraphQL
  ↓
Application
  ↓
Business Use Cases
```

The next evolution is:

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

---

# 2. The Main Mental Model Shift

Coming from Ruby/Rails, it is natural to think in terms of:

```text
Object
  ↓
Method
  ↓
Mutable State
```

Clojure encourages a model closer to:

```text
Data
  ↓
Function
  ↓
New Data
```

Example:

```clojure
(def animal
  {:name "Luna"
   :species :dog
   :status :available})

(def renamed
  (assoc animal :name "Bella"))
```

`animal` is not mutated. `assoc` returns another map.

Conceptual Ruby equivalent:

```ruby
animal = {
  name: "Luna",
  species: :dog,
  status: :available
}

renamed = animal.merge(name: "Bella")
```

The similarity is useful for learning, but immutability is a much more fundamental part of the Clojure data model.

---

# 3. Environment

## Java

This project uses Java 21.

```bash
java -version
```

The repository contains:

```text
.java-version
```

so the local Java version can be managed with `jenv`.

## Clojure CLI

Check:

```bash
clojure --version
```

The project uses `deps.edn` for paths, dependencies, and aliases.

---

# 4. `deps.edn`

A modern Clojure project can define configuration in:

```clojure
{:paths ["src" "resources"]

 :deps
 {ring/ring-core {:mvn/version "1.15.3"}
  ...}

 :aliases
 {:test
  {:extra-paths ["test"]}}}
```

## `:paths`

Defines directories added to the classpath:

```clojure
:paths ["src" "resources"]
```

This is why:

```clojure
animal-management.core
```

lives at:

```text
src/animal_management/core.clj
```

## `:deps`

External libraries are declared under `:deps`.

Example:

```clojure
org.postgresql/postgresql
```

## `:aliases`

Aliases provide reusable configurations.

This project defines:

```clojure
:test
```

which adds:

```text
test
```

to the classpath.

Therefore:

```bash
clojure -M:test
```

runs with the test alias.

---

# 5. Namespaces

Each source file generally represents one namespace.

Example:

```clojure
(ns animal-management.core)
```

The corresponding file is:

```text
src/animal_management/core.clj
```

Notice:

```text
animal-management.core
        ↓
animal_management/core.clj
```

Hyphens in namespace names become underscores in the file path.

## `require`

Example:

```clojure
(ns animal-management.application
  (:require [animal-management.repository :as repository]))
```

We can then write:

```clojure
(repository/find-animal id)
```

instead of:

```clojure
(animal-management.repository/find-animal id)
```

Conceptual Ruby:

```ruby
require "animal_management/repository"

Repository.find_animal(id)
```

---

# 6. `defn`

`defn` defines a function.

```clojure
(defn create-animal
  [name species]
  {:name name
   :species species
   :status :available})
```

Ruby:

```ruby
def create_animal(name, species)
  {
    name: name,
    species: species,
    status: :available
  }
end
```

Clojure call:

```clojure
(create-animal "Luna" :dog)
```

Ruby:

```ruby
create_animal("Luna", :dog)
```

---

# 7. Maps

Maps represent key/value data.

```clojure
{:name "Luna"
 :species :dog
 :status :available}
```

Ruby:

```ruby
{
  name: "Luna",
  species: :dog,
  status: :available
}
```

Lookup:

```clojure
(:name animal)
```

Ruby:

```ruby
animal[:name]
```

---

# 8. Keywords

Keywords are used extensively:

```clojure
:name
:species
:dog
:available
```

They are especially useful as map keys.

A keyword can also act as a function:

```clojure
(:name animal)
```

Conceptually:

```ruby
animal[:name]
```

You can also write:

```clojure
(get animal :name)
```

---

# 9. Immutability

Consider:

```clojure
(def luna
  {:name "Luna"
   :species :dog})
```

Create a new version:

```clojure
(def bella
  (assoc luna :name "Bella"))
```

Now:

```clojure
(:name luna)
```

is still:

```text
"Luna"
```

while:

```clojure
(:name bella)
```

is:

```text
"Bella"
```

This is fundamental to the project's Domain design.

---

# 10. `assoc`

`assoc` returns a new map with a changed or added key.

```clojure
(assoc animal :name "Bella")
```

Ruby:

```ruby
animal.merge(name: "Bella")
```

---

# 11. `update`

`update` applies a function to a map value.

```clojure
(update animal :name clojure.string/upper-case)
```

Conceptual Ruby:

```ruby
animal.merge(name: animal[:name].upcase)
```

In GraphQL we used:

```clojure
(update :species #(keyword (string/upper-case (name %))))
```

to transform:

```text
:dog
```

into:

```text
:DOG
```

---

# 12. `let`

`let` creates local bindings.

```clojure
(let [animal (repository/find-animal id)]
  ...)
```

Conceptual Ruby:

```ruby
animal = repository.find_animal(id)
```

Multiple bindings:

```clojure
(let [animal ...
      adopted ...
      result ...]
  result)
```

---

# 13. `if`

```clojure
(if (= :available (:status animal))
  ...
  ...)
```

Ruby:

```ruby
if animal[:status] == :available
  ...
else
  ...
end
```

Clojure does not need `end`.

---

# 14. `when`

`when` is useful when a block should execute only when a condition is truthy.

In the adoption use case:

```clojure
(when animal
  ...)
```

If `animal` is `nil`, the body is skipped.

Ruby equivalent:

```ruby
if animal
  ...
end
```

---

# 15. `nil` and Truthiness

Clojure uses:

```clojure
nil
```

to represent the absence of a value.

Important:

```text
false → false
nil   → false
everything else → true
```

Therefore:

```clojure
(when animal ...)
```

works naturally.

---

# 16. `map`

`map` transforms collection elements.

```clojure
(map :name animals)
```

Ruby:

```ruby
animals.map { |animal| animal[:name] }
```

This was used for database result transformation and GraphQL output.

---

# 17. `filter`

```clojure
(filter #(= :dog (:species %)) animals)
```

Ruby:

```ruby
animals.select { |animal| animal[:species] == :dog }
```

---

# 18. Anonymous Functions

Example:

```clojure
#(keyword (string/upper-case (name %)))
```

`%` represents the first argument.

Equivalent explicit function:

```clojure
(fn [value]
  (keyword (string/upper-case (name value))))
```

Ruby:

```ruby
->(value) { value.to_s.upcase.to_sym }
```

---

# 19. Threading with `->`

Clojure:

```clojure
(-> animal
    (update :species ...)
    (update :status ...))
```

Think of it as a sequence of transformations:

```text
animal
  ↓
update species
  ↓
update status
```

This is useful for readable data pipelines.

---

# 20. `some->`

We used:

```clojure
(some-> result
        database-animal->domain)
```

The idea is:

```text
result exists?
   ↓
yes → pass it to the next function
no  → remain nil
```

Conceptual Ruby:

```ruby
result && database_animal_to_domain(result)
```

---

# 21. Domain Layer

The file:

```text
src/animal_management/core.clj
```

contains pure domain functions.

Example:

```clojure
(defn adopt-animal
  [animal]
  (if (= :available (:status animal))
    (assoc animal :status :adopted)
    animal))
```

Notice what this function does not know about:

- HTTP
- PostgreSQL
- GraphQL
- Jetty
- LLMs

It receives data and returns data.

This makes business rules easy to test.

---

# 22. HTTP with Ring

Ring represents an HTTP request as a map.

Example:

```clojure
{:request-method :get
 :uri "/health"}
```

A handler returns a map:

```clojure
{:status 200
 :headers {"Content-Type" "text/plain"}
 :body "OK"}
```

This differs from Rails controllers, but the conceptual role is similar.

---

# 23. Reitit

Reitit connects routes to handlers:

```clojure
["/health" {:get health-handler}]
```

Meaning:

```text
GET /health
    ↓
health-handler
```

GraphQL:

```clojure
["/graphql" {:post graphql/graphql-handler}]
```

---

# 24. Jetty

Jetty provides the HTTP server.

The application is a Ring function:

```clojure
http/app
```

Jetty receives that application:

```clojure
(jetty/run-jetty http/app
                  {:port 3000
                   :join? false})
```

An important lesson:

```text
app = function
```

We do not need an object-oriented `Application` object to represent the server application.

---

# 25. PostgreSQL

The database is:

```text
animal_management
```

Table:

```sql
CREATE TABLE animals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL
);
```

---

# 26. next.jdbc

Clojure does not require an ActiveRecord-style ORM.

This project uses `next.jdbc`.

Example:

```clojure
(jdbc/execute-one!
  db/db-spec
  ["SELECT id, name, species, status
    FROM animals
    WHERE id = ?"
   id]
  {:builder-fn rs/as-unqualified-maps})
```

SQL remains explicit.

This is closer conceptually to using a database connection directly than to a full Rails ORM.

---

# 27. Repository Layer

The Repository owns database access.

Example:

```clojure
(defn find-animal
  [id]
  (some-> (jdbc/execute-one!
            db/db-spec
            ["SELECT id, name, species, status
              FROM animals
              WHERE id = ?"
             id]
            {:builder-fn rs/as-unqualified-maps})
          database-animal->domain))
```

The Application Layer does not need to know SQL.

---

# 28. Database → Domain Conversion

The database stores:

```text
species = "dog"
status  = "available"
```

The domain uses:

```clojure
:dog
:available
```

The conversion is:

```clojure
(defn- database-animal->domain
  [animal]
  (when animal
    (-> animal
        (update :species keyword)
        (update :status keyword))))
```

This is an important boundary.

---

# 29. GraphQL with Lacinia

GraphQL exposes:

```text
Animal
├── id
├── name
├── species
└── status
```

Enums:

```text
DOG
CAT

AVAILABLE
ADOPTED
```

The domain uses:

```clojure
:dog
:cat
:available
:adopted
```

GraphQL uses:

```text
DOG
CAT
AVAILABLE
ADOPTED
```

The GraphQL boundary performs the necessary transformation.

---

# 30. Application Layer

Initially the Application Layer was intentionally simple:

```clojure
(defn find-animal
  [id]
  (repository/find-animal id))
```

This may look trivial, but it establishes an explicit use-case boundary.

Later, we introduced real behavior.

---

# 31. First Real Use Case: Adoption

The use case is:

```clojure
(defn adopt-animal
  [id]
  (let [animal (repository/find-animal id)]
    (when animal
      (let [adopted-animal (core/adopt-animal animal)]
        (repository/update-animal! id adopted-animal)))))
```

Now the flow is:

```text
Application
    ↓
Repository → find
    ↓
Domain → business rule
    ↓
Repository → update
```

This is one of the most important architectural lessons from the project.

---

# 32. TDD

Development followed:

```text
Write test
   ↓
Run test
   ↓
Fail
   ↓
Implement
   ↓
Run test
   ↓
Pass
```

First, we wrote a test for `application/adopt-animal`.

It failed because the function did not exist:

```text
No such var: application/adopt-animal
```

Then we implemented it.

The focused result became:

```text
8 tests
24 assertions
0 failures
0 errors
```

The complete suite became:

```text
26 tests
74 assertions
0 failures
0 errors
```

---

# 33. Integration Tests

Not every test is a pure unit test.

Application tests use:

```text
Application
 ↓
Repository
 ↓
PostgreSQL
```

This was intentional: the use case should be validated against real persistence behavior.

---

# 34. Side Effects

An important distinction is:

```text
Pure:
core/adopt-animal
```

versus:

```text
Side effect:
repository/update-animal!
```

The Application Layer combines them:

```text
Application
 ├── effect: read database
 ├── pure: domain transformation
 └── effect: write database
```

This separation becomes especially important when an LLM enters the system.

---

# 35. Current Architecture

```text
HTTP
 ↓
Reitit
 ↓
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

GraphQL should not access the database directly.

The Domain should not know PostgreSQL exists.

The Repository should not own business rules.

The Application Layer coordinates use cases.

---

# 36. Next Stage: AI Agent

The next stage is:

```text
User
 ↓
Agent
 ↓
LLM
 ↓
Tool
 ↓
Application
 ↓
Domain
 ↓
Repository
 ↓
PostgreSQL
```

Future example:

```text
User:
"Where is Luna?"
       ↓
Agent
       ↓
LLM decides:
call find-animal
       ↓
Tool
       ↓
Application/find-animal
       ↓
Repository
       ↓
PostgreSQL
       ↓
Tool result
       ↓
LLM
       ↓
"Luna is a dog and is available."
```

The goal is not simply:

```text
prompt → LLM → text
```

The goal is to understand:

```text
reason
  ↓
choose tool
  ↓
execute tool
  ↓
observe result
  ↓
continue
  ↓
answer
```

---

# 37. Why the Agent Should Not Access PostgreSQL

The LLM should never receive:

```text
database credentials
SQL access
```

Instead, it receives controlled capabilities:

```text
find-animal
list-animals
adopt-animal
```

Tools call the Application Layer.

Therefore:

```text
LLM
 ↓
Tool
 ↓
Application
```

instead of:

```text
LLM
 ↓
Database
```

---

# 38. Agent Learning Topics

First phase:

- Gemini API
- LLM client
- prompts
- tool definitions
- tool calls
- tool results
- agent loop

Later:

- structured output
- conversation state
- memory
- retries
- timeouts
- observability
- evaluation

Future advanced project:

- RAG
- embeddings
- vector databases
- MCP
- multi-agent systems
- asynchronous agents
- human-in-the-loop
- guardrails
- evaluation pipelines

---

# 39. Git Flow

Every evolution follows:

```text
master
  ↓
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
merge
```

Conventional Commit examples:

```text
feat: add animal application layer
feat: add animal adoption use case
chore: add PostgreSQL persistence dependencies
```

---

# 40. Learning Checklist

## Clojure

- [x] Namespace
- [x] Require
- [x] Alias
- [x] Functions
- [x] Maps
- [x] Keywords
- [x] Vectors
- [x] `let`
- [x] `if`
- [x] `when`
- [x] `assoc`
- [x] `update`
- [x] `map`
- [x] `filter`
- [x] `some->`
- [x] Threading
- [x] Immutable data
- [x] Pure functions
- [x] Side effects

## Backend

- [x] Ring
- [x] Reitit
- [x] Jetty
- [x] JSON
- [x] PostgreSQL
- [x] JDBC
- [x] Migratus
- [x] Repository pattern
- [x] Application layer
- [x] GraphQL
- [x] Lacinia

## Engineering

- [x] TDD
- [x] Integration tests
- [x] Layered architecture
- [x] Git Flow
- [x] Conventional Commits
- [x] Pull Requests

## AI

- [ ] Gemini client
- [ ] Tool definition
- [ ] Tool calling
- [ ] Agent loop
- [ ] Structured output
- [ ] Memory
- [ ] Evaluation
- [ ] RAG
- [ ] MCP
- [ ] Multi-agent

---

# 41. How to Study This Project

Do not try to memorize every piece of syntax.

For each piece of code, ask:

1. What is the data?
2. What function transforms the data?
3. Is the function pure?
4. Where does a side effect happen?
5. Which layer owns that responsibility?
6. How would I write this in Ruby?
7. What changes because the data is immutable?
8. How would I test it?

This is more important than memorizing parentheses.

---

# 42. Personal Rule for This Project

Every new feature should follow:

```text
1. Define the use case
2. Write the test
3. Run the test
4. Implement
5. Run focused tests
6. Run full suite
7. Review diff
8. Commit
9. Push
10. Pull Request
11. Merge
```

For every new Clojure concept:

```text
Clojure
  ↓
Explain
  ↓
Ruby equivalent
  ↓
Run command
  ↓
Test
```

This repository is a learning laboratory, not an agent framework. Complexity should only be added when there is a real reason.
