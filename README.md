# 🚚 FreteFlow

Sistema de gestão de fretes desenvolvido para a operação real de uma pequena frota de caminhões em São Paulo. Nasceu para resolver um problema concreto: o controle manual de fretes e despesas via WhatsApp e papel, e evoluiu para uma API REST completa, segura e testada, com relatórios financeiros e exportação em Excel.

> Projeto construído em produção contínua, incorporando mudanças de requisito reais conforme o entendimento do negócio evoluiu, do mesmo jeito que aconteceria num projeto freelancer com um cliente de verdade.

---

## 📋 Sobre o projeto

O dono de uma pequena transportadora precisava trocar o controle manual de fretes (motorista, veículo, rota, valor) e despesas (diesel, pedágio, manutenção) por algo confiável, com histórico e cálculo automático de lucro por caminhão. O FreteFlow resolve isso com:

- Cadastro de motoristas, veículos, rotas fixas (lojas) e fretes
- Registro de despesas operacionais por veículo
- Relatório quinzenal por motorista (fechamento nos períodos de 11 a 25 e 26 a 10, replicando a rotina real do cliente)
- Relatório de lucro líquido por caminhão (fretes menos despesas), com exportação em Excel estilo dashboard (cards, gráficos de pizza, formatação condicional)
- Autenticação JWT com controle de acesso por papel (Admin / Operador)

---

## 🛠️ Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 16 |
| Versionamento de schema | Flyway |
| Segurança | Spring Security (stateless) + JWT (Auth0/java-jwt) |
| Documentação de API | springdoc-openapi (Swagger UI) |
| Exportação | Apache POI (Excel com gráficos nativos) |
| Testes | JUnit 5, Mockito, Testcontainers (PostgreSQL real) |
| Build | Maven |
| Infraestrutura local | Docker / Docker Compose |

---

## 🏗️ Decisões de arquitetura

- **UUID como chave primária** em todas as entidades. Isso evita enumeração sequencial de IDs em uma API pública.
- **Flyway com `ddl-auto: validate`**. O schema do banco é 100% controlado por migrations versionadas; o Hibernate nunca altera a estrutura sozinho, só valida.
- **Padrão em camadas** (`Controller → Service → Repository`), com DTOs em `record` isolando completamente as entidades JPA da API pública.
- **Testes de integração com Testcontainers, não H2**. Os testes rodam contra um PostgreSQL real, garantindo que recursos específicos do banco (tipos nativos, geração de UUID) se comportem exatamente como em produção.
- **Soft delete** em Veículo, Motorista e Loja. Nada é apagado fisicamente, preservando histórico de operação.
- **Máquina de estados no ciclo de vida do frete** (`PENDING → IN_PROGRESS → DELIVERED`, com `CANCELED` sempre disponível a partir de qualquer estado não final), impedindo transições inválidas via regra de negócio explícita.

---

## 🔐 Segurança

- Autenticação stateless via JWT, com filtro próprio validando o token em cada requisição.
- Controle de acesso por papel (`@PreAuthorize`). Operadores têm acesso somente leitura a dados mestres (veículos, motoristas, lojas), enquanto Fretes e Despesas são de uso operacional diário liberado a ambos os papéis.
- Registro público de usuário sempre cria um usuário com papel mínimo (`OPERATOR`), ignorando qualquer tentativa de elevar privilégio no payload de entrada. Essa proteção corrige uma vulnerabilidade real de auto-promoção a administrador identificada durante o desenvolvimento.
- Promoção a administrador exposta apenas por endpoint protegido, exclusivo para quem já é admin.
- CPF e telefone de motoristas mascarados nas respostas da API.
- Tratamento global de exceções (`@RestControllerAdvice`) garante que nenhum erro interno (stacktrace, estrutura de pacotes, versão de biblioteca) vaze para o cliente. Toda exceção não mapeada retorna uma mensagem genérica, com o detalhe completo registrado apenas no log do servidor.
- Validação real de CPF (algoritmo de dígitos verificadores), não apenas checagem de formato.

---

## 🧪 Testes

Cobertura via testes de integração ponta a ponta (Controller → Service → Repository → banco PostgreSQL real via Testcontainers), incluindo:

- Fluxo completo de autenticação e autorização por papel
- Regras de negócio (validação de CPF, placa, disponibilidade de recurso, transições de status)
- Casos de erro (recurso inexistente, duplicidade, dado inválido)
- Cálculo de relatórios financeiros com múltiplos registros e filtro de período

```bash
./mvnw test
```

---

## 🚀 Rodando localmente

### Pré-requisitos
- JDK 21
- Docker + Docker Compose
- Maven (ou use o wrapper `./mvnw` incluso)

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/yagosiqueira-dev/freteflow.git
cd freteflow

# 2. Configure as variáveis de ambiente
cp .env.example .env
# edite o .env com suas credenciais locais

# 3. Suba o banco de dados
docker compose up -d

# 4. Rode a aplicação (o Flyway aplica as migrations automaticamente)
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### Documentação interativa

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui/index.html
```

Faça login via `POST /auth/login`, copie o token retornado, clique em **Authorize** e cole o token para testar os endpoints protegidos diretamente pela interface.

---

## 📊 Exemplo de relatório exportado

O endpoint de relatório de lucro por caminhão gera uma planilha Excel com aba de resumo executivo (cards de indicadores, gráficos de pizza de composição do frete e despesas por categoria) e abas detalhadas de fretes e despesas, com filtro e totalizador dinâmico.

```
GET /api/reports/vehicle/{vehicleId}/profit/export?startDate=2026-08-11&endDate=2026-08-25
```

---

## 🗺️ Roadmap

- [x] CRUD completo de motoristas, veículos, lojas/rotas e fretes
- [x] Autenticação JWT e controle de acesso por papel
- [x] Módulo de despesas operacionais
- [x] Relatório quinzenal por motorista
- [x] Relatório de lucro líquido por caminhão, com exportação em Excel
- [x] Documentação interativa via Swagger
- [ ] Integração com WhatsApp Business Platform para registro de fretes por mensagem de texto
- [ ] Interpretação de linguagem natural via LLM (a IA interpreta a mensagem, o backend valida e decide, nunca o inverso)
- [ ] Deploy em produção

---

## 👤 Autor

**Yago Siqueira**
Estudante de Análise e Desenvolvimento de Sistemas, em transição para backend Java.

[LinkedIn](https://www.linkedin.com/in/yago-machado-siqueira/) · [GitHub](https://github.com/yagosiqueira-dev)