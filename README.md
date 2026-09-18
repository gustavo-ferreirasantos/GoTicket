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

---

## 📋 Guia Rápido: Copiar e Colar para Testes

### 🎟️ Passo 1: Criar um Evento
Acesse o menu **Eventos** > clique em **+ Novo Evento** e cole:

- **Nome do evento**:
```text
Festival GoMusic 2026
```
- **Descrição**:
```text
Festival de música ao vivo com múltiplos palcos e praça de alimentação.
```
- **Data**: Selecione uma data futura no calendário (ex: `20/12/2026`)
- **Horário**:
```text
20:00
```
- **Local**:
```text
Arena Central - São Paulo/SP
```
- **Capacidade**:
```text
5000
```
- **Situação**: Selecione `Aberto para Vendas` no seletor.

---

### 💺 Passo 2: Adicionar Setores e Lotes
Na tabela de Eventos, clique no botão **Setores** do evento criado:

#### ➕ Setor 1 (Pista):
- **Nome do setor**: `Pista`
- **Capacidade do setor**: `3500`
- **Tipo de ingresso**: `Inteira` (ou selecione no combo)
- **Preço do lote**: `100,00`
- **Quantidade de ingressos**: `2000`
*(Clique em **Adicionar Setor**)*

#### ➕ Setor 2 (Camarote VIP):
- **Nome do setor**: `Camarote VIP`
- **Capacidade do setor**: `1500`
- **Tipo de ingresso**: `Inteira` (ou selecione no combo)
- **Preço do lote**: `250,00`
- **Quantidade de ingressos**: `1500`
*(Clique em **Adicionar Setor** e depois em **Fechar**)*

---

### 💳 Passo 3: Realizar uma Venda (Bilheteria)
Acesse o menu **Bilheteria**, selecione o evento **Festival GoMusic 2026** e o setor **Pista**. Em seguida, cole os dados do participante:

#### Participante 1:
- **CPF**:
```text
123.456.789-09
```
- **Nome Completo**:
```text
Carlos Eduardo da Silva
```
- **E-mail**:
```text
carlos.silva@exemplo.com
```
- **Telefone**:
```text
(11) 98765-4321
```

#### Participante 2 (Alternativo):
- **CPF**:
```text
111.444.777-35
```
- **Nome Completo**:
```text
Mariana Souza Santos
```
- **E-mail**:
```text
mariana.souza@exemplo.com
```
- **Telefone**:
```text
(21) 99876-5432
```

> 💡 Escolha a forma de pagamento (ex: **PIX** ou **Cartão de Crédito**) e clique em **Finalizar Venda** para gerar o comprovante/ingresso em PDF com QR Code.