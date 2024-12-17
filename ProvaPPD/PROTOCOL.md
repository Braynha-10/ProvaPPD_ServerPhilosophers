# Protocolo para o problema dos filósofos de jantar

## Visão geral

Este documento define o protocolo de comunicação entre os filósofos (clientes) e o servidor que gerencia(como um se fosse um mordomo) o problema dos filósofos do jantar.

Pensei em manter a estrutura do Protocolo SMTP por isso utilizo um escopo semelhante de mensageiro(filosofo, aonde também obtemos as respostas mesmo que superficiais e individuais daquela instancia, observamos ocorrer as ações possiveis para o filosofo, seu andamento e tempo de duração) e receptor("respondedor server, aonde tambem se encontra os logs da aplicação portanto conseguimos vizualizar o que está havendo")com a porta 5000, mas também utilizei conhecimentos adquiritos com o protocolo HTTP mas muito mais com SMTP e aparentemente com minhas pesquisas e estudos vi que por se tratar de um procedimento de comunicação entre processos este código tambem pode se encaixar com RCP-TCP

---
## Commands

### 1. **HELLO**
- **Purpose**: Registrar um novo filósofo no servidor.
- **Client Sends**: `HELLO`
- **Server Responds**: `HI <ID>` (where `<ID>` is the philosopher's unique identifier).

### 2. **LOGIN: <name>**
- **Purpose**: Associar um filosofo a um nome e ID.
- **Client Sends**: `LOGIN: <name>`
- **Server Responds**:
  - `Philosopher <name> joined the table as ID <ID>` if successful.
  - `ERROR: Philosopher <name> is already at the table.` if the name is already in use.

### 3. **THINK**
- **Purpose**: Notifica ao Servidor que Filosofo está pensando.
- **Client Sends**: `THINK`
- **Server Responds**: `ACK`

### 4. **REQUEST_FORKS**
- **Purpose**: Request forks to eat.
- **Client Sends**: `REQUEST_FORKS`
- **Server Responds**:
  - `FORKS_GRANTED` if forks are available.
  - `FORKS_DENIED` if forks are unavailable.

### 5. **EAT**
- **Purpose**: Notifica ao servidor que filosofo está comendo.
- **Client Sends**: `EAT`
- **Server Responds**: `ACK`

### 6. **RELEASE_FORKS**
- **Purpose**: Largando os garfos após comer.
- **Client Sends**: `RELEASE_FORKS`
- **Server Responds**: `ACK`

---

## Error Messages

- `ERROR: Philosopher <name> is already at the table.`: Nomes de filsofos duplicados.
- `MAXIMUM NUMBER OF PHILOSOPHERS REACHED`: Servidor nao comporta mais filsofos na mesa.
