# Manual de Aprendizado de Clojure — Animal Management API

## 1. Objetivo deste manual

Este manual documenta o aprendizado de Clojure realizado através do projeto `animal-management-api`.

A ideia não foi aprender Clojure através de exemplos artificiais. O objetivo foi construir uma API real, aproveitando conhecimento prévio de Ruby/Rails, e usar cada parte da aplicação para entender como Clojure pensa sobre dados, funções, efeitos e arquitetura.

A evolução construída foi:

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

A próxima evolução é:

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

## 2. A principal mudança de mentalidade

Como engenheiro Ruby/Rails, é natural pensar:

```text
Object
  ↓
Method
  ↓
Mutable State
```

Em Clojure, o modelo que estamos praticando é mais próximo de:

```text
Data
  ↓
Function
  ↓
New Data
```

Exemplo:

```clojure
(def animal
  {:name "Luna"
   :species :dog
   :status :available})

(def renamed
  (assoc animal :name "Bella"))
```

`animal` não é alterado. `assoc` retorna outro mapa.

Equivalente conceitual em Ruby:

```ruby
animal = {
  name: "Luna",
  species: :dog,
  status: :available
}

renamed = animal.merge(name: "Bella")
```

A ideia é parecida, mas a imutabilidade é central no modelo de dados do Clojure.

---

# 3. Ambiente

## Java

O projeto usa Java 21.

```bash
java -version
```

O projeto possui:

```text
.java-version
```

para definir a versão local via `jenv`.

## Clojure CLI

Verifique:

```bash
clojure --version
```

O projeto usa `deps.edn` para definir paths, dependências e aliases.

---

# 4. `deps.edn`

Um projeto Clojure moderno pode centralizar a configuração em:

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

Define diretórios que entram no classpath:

```clojure
:paths ["src" "resources"]
```

Por isso um namespace como:

```clojure
animal-management.core
```

fica em:

```text
src/animal_management/core.clj
```

## `:deps`

Declara bibliotecas externas.

Exemplo:

```clojure
org.postgresql/postgresql
```

## `:aliases`

Permitem criar configurações reutilizáveis.

Neste projeto:

```clojure
:test
```

adiciona:

```clojure
"test"
```

ao classpath.

Então:

```bash
clojure -M:test
```

executa usando o alias de testes.

---

# 5. Namespaces

Cada arquivo geralmente representa um namespace.

Exemplo:

```clojure
(ns animal-management.core)
```

O arquivo correspondente é:

```text
src/animal_management/core.clj
```

Observe:

```text
animal-management.core
        ↓
animal_management/core.clj
```

Hyphens no namespace viram underscores no caminho do arquivo.

## `require`

Exemplo:

```clojure
(ns animal-management.application
  (:require [animal-management.repository :as repository]))
```

Agora podemos escrever:

```clojure
(repository/find-animal id)
```

em vez de:

```clojure
(animal-management.repository/find-animal id)
```

Ruby equivalente conceitual:

```ruby
require "animal_management/repository"

Repository.find_animal(id)
```

---

# 6. `defn`

`defn` define uma função.

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

A chamada em Clojure é:

```clojure
(create-animal "Luna" :dog)
```

Ruby:

```ruby
create_animal("Luna", :dog)
```

---

# 7. Maps

Maps são estruturas chave/valor.

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

Para acessar:

```clojure
(:name animal)
```

Ruby:

```ruby
animal[:name]
```

---

# 8. Keywords

Usamos keywords constantemente:

```clojure
:name
:species
:dog
:available
```

Keywords são muito úteis como chaves de mapas.

Uma característica importante é que uma keyword pode ser usada como função:

```clojure
(:name animal)
```

Isso equivale conceitualmente a:

```ruby
animal[:name]
```

Também podemos usar:

```clojure
(get animal :name)
```

---

# 9. Imutabilidade

Considere:

```clojure
(def luna
  {:name "Luna"
   :species :dog})
```

Para produzir uma nova versão:

```clojure
(def bella
  (assoc luna :name "Bella"))
```

Agora:

```clojure
(:name luna)
```

continua sendo:

```text
"Luna"
```

E:

```clojure
(:name bella)
```

é:

```text
"Bella"
```

Isso é fundamental para o nosso Domain.

---

# 10. `assoc`

`assoc` cria uma nova versão do mapa com uma chave alterada/adicionada.

```clojure
(assoc animal :name "Bella")
```

Ruby:

```ruby
animal.merge(name: "Bella")
```

---

# 11. `update`

`update` aplica uma função ao valor de uma chave.

Exemplo:

```clojure
(update animal :name clojure.string/upper-case)
```

Conceitualmente:

```ruby
animal.merge(name: animal[:name].upcase)
```

No GraphQL usamos:

```clojure
(update :species #(keyword (string/upper-case (name %))))
```

para transformar:

```text
:dog
```

em:

```text
:DOG
```

---

# 12. `let`

`let` cria bindings locais.

```clojure
(let [animal (repository/find-animal id)]
  ...)
```

Ruby conceitualmente:

```ruby
animal = repository.find_animal(id)
```

Um `let` pode ter vários bindings:

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

Clojure não precisa de `end`.

---

# 14. `when`

`when` é útil quando queremos executar um bloco somente se uma condição for verdadeira.

No use case:

```clojure
(when animal
  ...)
```

Se `animal` for `nil`, o corpo não é executado.

Ruby equivalente:

```ruby
if animal
  ...
end
```

---

# 15. `nil` e truthiness

Clojure usa:

```clojure
nil
```

como ausência de valor.

Importante:

```text
false → falso
nil   → falso
qualquer outra coisa → verdadeiro
```

Por isso:

```clojure
(when animal ...)
```

funciona naturalmente.

---

# 16. `map`

`map` transforma elementos de uma coleção.

```clojure
(map :name animals)
```

Ruby:

```ruby
animals.map { |animal| animal[:name] }
```

Isso foi usado no projeto para transformar resultados de banco e GraphQL.

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

# 18. Funções anônimas

Exemplo:

```clojure
#(keyword (string/upper-case (name %)))
```

`%` representa o primeiro argumento.

Forma equivalente mais explícita:

```clojure
(fn [value]
  (keyword (string/upper-case (name value))))
```

Ruby:

```ruby
->(value) { value.to_s.upcase.to_sym }
```

---

# 19. Threading com `->`

Clojure:

```clojure
(-> animal
    (update :species ...)
    (update :status ...))
```

Isso permite ler uma sequência de transformações.

Conceitualmente:

```text
animal
  ↓
update species
  ↓
update status
```

É especialmente útil quando temos pipelines de transformação de dados.

---

# 20. `some->`

Usamos:

```clojure
(some-> result
        database-animal->domain)
```

A ideia é:

```text
result existe?
   ↓
sim → passa para a próxima função
não → continua nil
```

Isso é muito útil quando uma consulta ao banco pode retornar `nil`.

Ruby conceitualmente:

```ruby
result && database_animal_to_domain(result)
```

---

# 21. Domain Layer

O arquivo:

```text
src/animal_management/core.clj
```

contém regras puras.

Exemplo:

```clojure
(defn adopt-animal
  [animal]
  (if (= :available (:status animal))
    (assoc animal :status :adopted)
    animal))
```

Observe o que a função NÃO faz:

- não acessa PostgreSQL;
- não chama HTTP;
- não chama GraphQL;
- não conhece Jetty;
- não chama LLM.

Ela recebe dados e devolve dados.

Esse isolamento torna a regra fácil de testar.

---

# 22. HTTP com Ring

Ring representa uma requisição como mapa.

Exemplo:

```clojure
{:request-method :get
 :uri "/health"}
```

Um handler retorna um mapa:

```clojure
{:status 200
 :headers {"Content-Type" "text/plain"}
 :body "OK"}
```

Ruby/Rails possui uma abstração diferente, mas conceitualmente o handler está fazendo o papel de um endpoint/controller.

---

# 23. Reitit

Reitit conecta rotas a handlers:

```clojure
["/health" {:get health-handler}]
```

Isso significa:

```text
GET /health
    ↓
health-handler
```

Outra rota:

```clojure
["/graphql" {:post graphql/graphql-handler}]
```

---

# 24. Jetty

Jetty fornece o servidor HTTP.

A aplicação é uma função Ring:

```clojure
http/app
```

E o Jetty recebe essa aplicação:

```clojure
(jetty/run-jetty http/app
                  {:port 3000
                   :join? false})
```

Uma ideia importante aprendida aqui:

```text
app = function
```

Não é necessário um objeto `Application` como em alguns frameworks tradicionais.

---

# 25. PostgreSQL

O banco usado é:

```text
animal_management
```

Tabela:

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

Clojure não possui um equivalente obrigatório a ActiveRecord.

Neste projeto usamos `next.jdbc`.

Consulta:

```clojure
(jdbc/execute-one!
  db/db-spec
  ["SELECT id, name, species, status
    FROM animals
    WHERE id = ?"
   id]
  {:builder-fn rs/as-unqualified-maps})
```

O SQL continua explícito.

Isso é diferente de:

```ruby
Animal.find(id)
```

em Rails.

A filosofia aqui é mais próxima de:

```ruby
connection.exec_params(...)
```

do que de um ORM completo.

---

# 27. Repository Layer

O Repository concentra acesso ao banco.

Exemplo:

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

A Application Layer não precisa conhecer SQL.

---

# 28. Conversão Database → Domain

O banco armazena:

```text
species = "dog"
status  = "available"
```

Nosso domínio usa:

```clojure
:dog
:available
```

A conversão:

```clojure
(defn- database-animal->domain
  [animal]
  (when animal
    (-> animal
        (update :species keyword)
        (update :status keyword))))
```

Isso é um boundary importante.

---

# 29. GraphQL com Lacinia

O GraphQL possui:

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

O domínio usa:

```clojure
:dog
:cat
:available
:adopted
```

O GraphQL usa:

```text
DOG
CAT
AVAILABLE
ADOPTED
```

A camada GraphQL faz a transformação necessária.

---

# 30. Application Layer

A Application Layer inicialmente era simples:

```clojure
(defn find-animal
  [id]
  (repository/find-animal id))
```

Isso pode parecer trivial, mas cria um boundary explícito.

Depois começamos a colocar comportamento real nela.

---

# 31. Primeiro Use Case Real: Adoption

O use case:

```clojure
(defn adopt-animal
  [id]
  (let [animal (repository/find-animal id)]
    (when animal
      (let [adopted-animal (core/adopt-animal animal)]
        (repository/update-animal! id adopted-animal)))))
```

Agora temos:

```text
Application
    ↓
Repository → find
    ↓
Domain → business rule
    ↓
Repository → update
```

Essa é uma das lições arquiteturais mais importantes do projeto.

---

# 32. TDD

O desenvolvimento seguiu:

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

Primeiro criamos um teste para `application/adopt-animal`.

Ele falhou porque a função ainda não existia:

```text
No such var: application/adopt-animal
```

Depois implementamos.

O resultado:

```text
8 tests
24 assertions
0 failures
0 errors
```

Depois executamos a suíte completa:

```text
26 tests
74 assertions
0 failures
0 errors
```

---

# 33. Testes de integração

Nem todos os testes são puramente unitários.

Os testes de Application usam:

```text
Application
 ↓
Repository
 ↓
PostgreSQL
```

Isso foi proposital para verificar o comportamento real do use case.

---

# 34. O que aprendemos sobre efeitos colaterais

Uma distinção importante:

```text
Pure:
core/adopt-animal
```

vs.

```text
Side effect:
repository/update-animal!
```

A Application Layer combina os dois.

```text
Application
 ├── effect: read database
 ├── pure: domain transformation
 └── effect: write database
```

Essa separação será extremamente importante quando introduzirmos um LLM.

---

# 35. Arquitetura atual

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

O GraphQL não deve acessar o banco diretamente.

O Domain não deve saber que PostgreSQL existe.

O Repository não deve decidir regras de negócio.

A Application coordena o caso de uso.

---

# 36. Próxima etapa: AI Agent

O próximo estágio será:

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

Exemplo futuro:

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

O objetivo não é simplesmente fazer:

```text
prompt → LLM → text
```

O objetivo é entender:

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

# 37. Por que o Agent não acessará PostgreSQL

O LLM nunca deve receber diretamente:

```text
database credentials
SQL access
```

Ele recebe capacidades controladas:

```text
find-animal
list-animals
adopt-animal
```

As tools chamarão a Application Layer.

Isso mantém:

```text
LLM
 ↓
Tool
 ↓
Application
```

em vez de:

```text
LLM
 ↓
Database
```

---

# 38. O que queremos aprender no Agent

Primeira fase:

- Gemini API
- LLM client
- prompt
- tool definition
- tool call
- tool result
- agent loop

Depois:

- structured output
- conversation state
- memory
- retries
- timeouts
- observability
- evaluation

No projeto futuro:

- RAG
- embeddings
- vector database
- MCP
- multi-agent
- asynchronous agents
- human-in-the-loop
- guardrails
- evaluation pipelines

---

# 39. Git Flow

Cada evolução segue:

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
PR
  ↓
review
  ↓
merge
```

Exemplos de Conventional Commits:

```text
feat: add animal application layer
feat: add animal adoption use case
chore: add PostgreSQL persistence dependencies
```

---

# 40. Checklist de aprendizado

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

# 41. Como estudar este projeto

Não tente memorizar toda a sintaxe.

Para cada trecho, pergunte:

1. Qual é o dado?
2. Qual função transforma o dado?
3. A função é pura?
4. Onde acontece um efeito?
5. Qual camada deveria possuir essa responsabilidade?
6. Como eu escreveria isso em Ruby?
7. O que muda por causa da imutabilidade?
8. Como eu testaria isso?

Esse processo é mais importante do que decorar parênteses.

---

# 42. Regra pessoal para este projeto

Sempre que uma nova funcionalidade for implementada:

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

E para cada novo conceito Clojure:

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

Esse projeto é um laboratório de aprendizado, não um framework de produção. A complexidade só deve ser adicionada quando houver uma necessidade real.
