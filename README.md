# 🎟️ GoTicket — Sistema de Gestão e Venda Presencial de Ingressos

Aplicação desktop profissional para gestão completa de eventos, setores, lotes, participantes, vendas presenciais na bilheteria física, emissão de comprovantes/ingressos em PDF, cancelamentos, controle de portaria (check-in) e relatórios.

---

## 🚀 Tecnologias Utilizadas
- **Linguagem**: Java 21 (LTS)
- **Interface Gráfica**: JavaFX 21 + FXML + CSS GoTicket Design System (Fiel ao Figma)
- **Banco de Dados**: PostgreSQL 16+
- **Pool de Conexões**: HikariCP 5.1.0
- **Segurança**: BCrypt Hashing (jBCrypt)
- **Emissão de Ingressos/Comprovantes**: OpenPDF (LibrePDF 2.0.2)
- **Testes Automatizados**: JUnit 5 + Mockito

---

## 🛠️ Como Executar o Projeto

### 1. Iniciar o Banco de Dados (PostgreSQL via Docker)
No terminal, execute:
```bash
docker compose up -d
```
> O banco `eventgo_db` subirá automaticamente na porta `5433` com as credenciais `postgres/postgres`. Todas as tabelas, constraints, índices e o usuário inicial `admin` são criados automaticamente ao iniciar a aplicação.

### 2. Rodar a Aplicação Desktop
No Windows (PowerShell / CMD), execute:
```bash
.\mvnw.cmd javafx:run
```
*(ou se você tiver o `mvn` instalado globalmente: `mvn javafx:run`)*

### 3. Rodar Testes Unitários
```bash
.\mvnw.cmd test
```

---

## 👤 Credenciais de Acesso Inicial
- **Usuário**: `admin`
- **Senha**: `admin123`