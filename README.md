# 🚛 FreteFlow

Sistema de gestão e automação de fretes desenvolvido para facilitar o controle de uma pequena frota de veículos, centralizando informações de fretes, motoristas, veículos, despesas e resultados financeiros.

O projeto também é desenvolvido como um **projeto prático de aprendizado e portfólio**, com foco em desenvolvimento backend, integração de sistemas, segurança, cloud, automação e inteligência artificial.

> 🚧 **Status:** Em desenvolvimento

---

## 📌 Sobre o projeto

O FreteFlow tem como objetivo transformar o controle manual de fretes em um sistema centralizado e automatizado.

A ideia é permitir que os registros possam ser realizados de maneira simples, inicialmente através de uma API e futuramente também pelo **WhatsApp**, enquanto os dados ficam armazenados em um banco de dados e podem ser consultados através de um aplicativo/web app.

### Fluxo planejado

```text
📱 WhatsApp
     │
     ▼
🌐 WhatsApp Business Platform
     │
     ▼
☕ Spring Boot API
     │
     ▼
🗄️ PostgreSQL
     │
     ├──────────────► 📊 Dashboard
     │
     └──────────────► 📗 Excel
```

Futuramente, uma camada de inteligência artificial poderá interpretar mensagens em linguagem natural e transformá-las em dados estruturados.

---

# 🎯 Objetivos

* Centralizar o gerenciamento de fretes.
* Cadastrar e gerenciar veículos.
* Cadastrar e gerenciar motoristas.
* Registrar despesas relacionadas aos fretes.
* Calcular valores e resultados.
* Disponibilizar relatórios.
* Permitir consulta através de uma aplicação web/PWA.
* Integrar o sistema com o WhatsApp.
* Automatizar a geração de relatórios.
* Futuramente utilizar IA para interpretar mensagens em linguagem natural.
* Aplicar boas práticas de desenvolvimento e segurança.

---

# 🛠️ Tecnologias

## Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Bean Validation
* Spring Security
* JWT
* Maven

## Banco de dados

* PostgreSQL
* Flyway

## Testes

* JUnit
* Mockito
* Spring Boot Test

## Frontend

* React
* TypeScript
* PWA

## Infraestrutura

* Docker
* Docker Compose

## Documentação

* Swagger / OpenAPI

## Versionamento

* Git
* GitHub

## Integrações futuras

* WhatsApp Business Platform / Cloud API
* Microsoft Graph API
* Excel

## Performance e mensageria — futuro

* Redis
* RabbitMQ

## Cloud e DevOps — futuro

* GitHub Actions
* Azure

## Inteligência Artificial — futuro

* APIs de LLM
* Structured Outputs
* Function/Tool Calling

---

# 📚 Aprendizados

Este projeto está sendo desenvolvido de forma incremental, com o objetivo de transformar conhecimentos teóricos em experiência prática.

Durante o desenvolvimento serão explorados conceitos como:

### Backend

* Desenvolvimento de APIs REST
* Arquitetura em camadas
* Controllers
* Services
* Repositories
* DTOs
* Injeção de dependências
* Tratamento de exceções
* Validação de dados
* Princípios SOLID
* Clean Code

### Banco de dados

* Modelagem relacional
* PostgreSQL
* SQL
* Relacionamentos
* Índices
* Transações
* Migrações com Flyway
* JPA / Hibernate

### Segurança

* Autenticação
* Autorização
* JWT
* Hash de senhas
* Controle de acesso
* Validação de entradas
* OWASP Top 10
* Proteção de APIs
* Gerenciamento de secrets

### Testes

* Testes unitários
* Testes de integração
* JUnit
* Mockito
* Testes de APIs

### DevOps

* Git
* GitHub
* Docker
* Docker Compose
* CI/CD
* GitHub Actions

### Integrações

* Webhooks
* APIs externas
* WhatsApp Business Platform
* Microsoft Graph API

### Frontend

* React
* TypeScript
* Comunicação com APIs REST
* PWA
* Desenvolvimento responsivo

### Inteligência Artificial

* Integração com APIs de LLM
* Engenharia de prompts
* Structured Outputs
* Function/Tool Calling
* Validação de respostas geradas por IA

---

# 🏗️ Arquitetura

A arquitetura será construída de forma incremental.

### Arquitetura inicial

```text
Client
  │
  ▼
REST API
  │
  ▼
Controller
  │
  ▼
Service
  │
  ▼
Repository
  │
  ▼
PostgreSQL
```

### Arquitetura planejada

```text
                    ┌──────────────┐
                    │   WhatsApp   │
                    └──────┬───────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │ WhatsApp Cloud  │
                  │      API        │
                  └────────┬────────┘
                           │
                           ▼
┌──────────────┐    ┌──────────────┐
│ React / PWA  │───►│  Spring Boot │
└──────────────┘    │      API     │
                    └───────┬──────┘
                            │
                 ┌──────────┼──────────┐
                 ▼          ▼          ▼
             PostgreSQL   Redis    RabbitMQ
                 │
                 ▼
             Relatórios
                 │
                 ▼
               Excel
```

A arquitetura poderá ser modificada conforme novos requisitos forem identificados.

---

# 🗃️ Entidades

As principais entidades planejadas são:

```text
User
 └── Usuários do sistema

Driver
 └── Motoristas

Vehicle
 └── Veículos da frota

Freight
 └── Registros de fretes

Expense
 └── Despesas relacionadas aos fretes
```

O modelo de dados será evoluído conforme o desenvolvimento.

---

# 🚧 Status atual

Atualmente o projeto encontra-se na fase inicial de desenvolvimento.

### Implementado

* [x] Configuração inicial do projeto Spring Boot
* [x] Java 21
* [x] Configuração do PostgreSQL
* [x] Configuração do Flyway
* [x] Primeira entidade `User`
* [x] Primeira migration do banco
* [x] Estrutura inicial do projeto

### Em desenvolvimento

* [ ] `UserRepository`
* [ ] `UserService`
* [ ] `UserController`
* [ ] Validação de dados
* [ ] Tratamento global de exceções
* [ ] Testes automatizados

---

# 🗺️ Roadmap

## Fase 1 — Backend e banco

* [x] Configuração do Spring Boot
* [x] Configuração do PostgreSQL
* [x] Flyway
* [x] Entidade User
* [ ] Repositories
* [ ] Services
* [ ] Controllers
* [ ] DTOs
* [ ] Validações
* [ ] Tratamento de exceções
* [ ] Testes

## Fase 2 — Segurança

* [ ] Spring Security
* [ ] Autenticação
* [ ] JWT
* [ ] Autorização
* [ ] Controle de acesso
* [ ] Proteção de endpoints
* [ ] Gerenciamento seguro de secrets

## Fase 3 — Gestão de frota

* [ ] Cadastro de veículos
* [ ] Cadastro de motoristas
* [ ] Cadastro de fretes
* [ ] Cadastro de despesas
* [ ] Regras de negócio
* [ ] Cálculo de resultados
* [ ] Relatórios

## Fase 4 — Frontend

* [ ] React
* [ ] TypeScript
* [ ] Dashboard
* [ ] Tela de fretes
* [ ] Tela de veículos
* [ ] Tela de motoristas
* [ ] Tela de despesas
* [ ] PWA
* [ ] Responsividade

## Fase 5 — Excel

* [ ] Exportação `.xlsx`
* [ ] Relatórios
* [ ] Tabelas
* [ ] Integração com Microsoft Graph futuramente

## Fase 6 — WhatsApp

* [ ] Configuração da WhatsApp Business Platform
* [ ] Webhook
* [ ] Recebimento de mensagens
* [ ] Envio de mensagens
* [ ] Fluxo de cadastro de frete
* [ ] Validação de mensagens
* [ ] Consulta de informações pelo WhatsApp

## Fase 7 — Performance e mensageria

* [ ] Redis
* [ ] Cache
* [ ] RabbitMQ
* [ ] Processamento assíncrono

## Fase 8 — Cloud e DevOps

* [ ] Docker
* [ ] Docker Compose
* [ ] GitHub Actions
* [ ] CI/CD
* [ ] Deploy em cloud
* [ ] Monitoramento

## Fase 9 — Inteligência Artificial

* [ ] Integração com LLM
* [ ] Interpretação de mensagens
* [ ] Structured Outputs
* [ ] Function/Tool Calling
* [ ] Validação dos dados gerados pela IA
* [ ] Consultas através de linguagem natural

---

# 🔐 Segurança

Segurança é uma prioridade durante todo o desenvolvimento.

O projeto busca seguir boas práticas relacionadas a:

* OWASP Top 10
* Princípio do menor privilégio
* Zero Trust
* Validação de entradas
* Autenticação segura
* Autorização
* Proteção de APIs
* Hash seguro de senhas
* Gerenciamento de tokens
* Proteção de secrets
* HTTPS
* CORS
* Rate limiting
* Logs sem informações sensíveis
* Validação de webhooks
* Controle de acesso ao banco de dados

Nenhuma credencial, senha, token ou chave de API deverá ser armazenada diretamente no código ou versionada no Git.

---

# 🧪 Estratégia de testes

O projeto utilizará diferentes níveis de testes:

```text
Testes unitários
       ↓
Testes de integração
       ↓
Testes da API
       ↓
Testes de segurança
       ↓
CI/CD
```

As regras de negócio críticas deverão possuir testes automatizados.

---

# 🐳 Execução local

> Esta seção será atualizada conforme o ambiente de desenvolvimento for configurado.

Requisitos previstos:

* Java 21
* Maven
* Docker
* Docker Compose
* PostgreSQL
* Git

---

# 📂 Estrutura do projeto

A estrutura será evoluída conforme novas funcionalidades forem implementadas.

Estrutura esperada:

```text
freteflow/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       └── application.yml
│   │
│   └── test/
│       └── java/
│
├── docker/
├── docs/
├── .gitignore
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# 📈 Evolução do projeto

O FreteFlow será desenvolvido de maneira incremental.

A prioridade será:

```text
Funcionalidade
     ↓
Entendimento
     ↓
Implementação
     ↓
Testes
     ↓
Segurança
     ↓
Documentação
     ↓
Próxima funcionalidade
```

Novas tecnologias somente serão adicionadas quando resolverem um problema real do projeto ou contribuírem para um aprendizado relevante.

---

# 👨‍💻 Autor

**Yago Siqueira**

Projeto desenvolvido como parte da minha jornada de aprendizado em desenvolvimento de software, com foco em **Backend Java, APIs REST, bancos de dados, segurança, cloud, automação e inteligência artificial**.

---

## ⭐ Objetivo do projeto

Mais do que construir uma aplicação, o objetivo do FreteFlow é transformar um problema real em uma solução tecnológica, aplicando boas práticas de engenharia de software e documentando todo o processo de desenvolvimento.

