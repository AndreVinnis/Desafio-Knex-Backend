# TechMart API

API REST desenvolvida para a TechMart, uma loja virtual em fase inicial que precisa
substituir o controle de estoque e vendas feito em planilhas por um sistema interno
simples e confiável.

O sistema permite que **vendedores** gerenciem o catálogo de produtos e que
**clientes** visualizem e comprem os itens disponíveis, cobrindo autenticação,
CRUD de produtos, fluxo de compra e relatórios básicos de vendas para o vendedor.

*A documentação completa está em:* [Wiki](https://github.com/AndreVinnis/Desafio-Knex-Backend/wiki)

## Contexto geral

O projeto foi construído como a primeira versão da API interna da TechMart,
com foco em:

- **Autenticação e autorização** — cadastro, login e permissionamento das rotas
  de acordo com o papel do usuário (cliente ou vendedor).
- **CRUD de produtos** — cadastro, edição, exclusão e consulta de produtos,
  com regras específicas para o vendedor.
- **Fluxo de compra** — simulação de uma compra, com baixa de estoque e
  registro do pedido.
- **Relatórios de vendas** — consultas voltadas ao vendedor, com o histórico
  de vendas por produto.

## Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| **Java** | Linguagem principal do backend |
| **Spring Boot** | Framework para construção da API REST |
| **MySQL** | Banco de dados relacional utilizado em produção/desenvolvimento |
| **Docker / Docker Compose** | Containerização da aplicação e do banco de dados |
| **Swagger (OpenAPI)** | Documentação interativa das rotas da API |
| **JaCoCo** | Geração de relatórios de cobertura de testes |
| **GitHub Actions** | Pipeline de CI, executando build e testes a cada push/PR |

## Como rodar o projeto

O projeto foi configurado para subir com **Docker Compose**, então não é
necessário instalar Java ou MySQL localmente — apenas Docker.

### Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/) (já incluso no Docker Desktop)

### Passo a passo

1. Clone o repositório:

   ```bash
   git clone <url-do-repositorio>
   cd <nome-do-projeto>
   ```

2. Suba os containers da aplicação e do banco de dados:

   ```bash
   docker-compose up --build
   ```

   Esse comando irá:
   - Buildar a imagem da aplicação Spring Boot;
   - Subir o container do MySQL;
   - Executar as migrações/inicialização do banco;
   - Deixar a API disponível na porta configurada (por padrão `8080`).

3. Para rodar em segundo plano:

   ```bash
   docker-compose up -d --build
   ```

4. Para parar os containers:

   ```bash
   docker-compose down
   ```

   Caso queira remover também o volume do banco de dados:

   ```bash
   docker-compose down -v
   ```

### Acessando a aplicação

- **API:** `http://localhost:8080`
- **Documentação Swagger:** `http://localhost:8080/swagger-ui.html`

### Rodando os testes e cobertura (JaCoCo)

```bash
./mvnw test
```

O relatório de cobertura gerado pelo JaCoCo fica disponível em:

```
target/site/jacoco/index.html
```

### Integração Contínua (CI)

O projeto conta com um pipeline configurado em **GitHub Actions**
(`.github/workflows/`), que executa automaticamente o build e os testes a
cada push ou pull request, garantindo que novas alterações não quebrem a
aplicação.

## Estrutura principal das rotas

| Método | Rota | Acesso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |
| GET | `/products` | Cliente / Vendedor |
| GET | `/products/:id` | Cliente / Vendedor |
| POST | `/products` | Vendedor |
| PUT | `/products/:id` | Vendedor |
| DELETE | `/products/:id` | Vendedor |
| POST | `/orders` | Cliente |
| GET | `/orders` | Cliente |
| GET | `/seller/sales` | Vendedor |
| GET | `/seller/sales/:product_id` | Vendedor |

---
