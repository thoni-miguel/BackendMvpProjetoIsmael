# Backend Architecture Plan

Este guia descreve o mínimo necessário para que outro agente implemente o backend que dará suporte ao aplicativo **MvpProjetoIsmael**. O objetivo é permitir sincronização offline-first confiável, armazenamento seguro das evidências de entrega e capacidade de auditoria.

## 1. Stack Recomendada
- **Linguagem/Framework**: **Kotlin + Ktor** (principal). Agora que vamos manter todo o ecossistema em Kotlin, use Ktor com kotlinx.serialization e ktor-server-netty. FastAPI fica apenas como opção experimental se algum colaborador precisar validar ideias rapidamente.
- **Banco de dados**: PostgreSQL (preferido) ou SQLite para protótipo. Estrutura espelha o Room DB do app.
- **Gerenciamento de dependências**: Poetry (FastAPI) ou Gradle (Ktor).
- **Containerização**: Docker para subir localmente e facilitar deploy futuro.

## 2. Modelos e Tabelas
Reproduzir as entidades do app com campos adicionais para auditoria:
- `employees`: `id (UUID PK)`, `name`, `sector`, `role`, `shoe_size`, `uniform_size`, `active`, `updated_at`, `synced_at`.
- `epi_types`: `id`, `name`, `months_validity`, `updated_at`, `synced_at`.
- `deliveries`: `id`, `employee_id (FK)`, `epi_type_id (FK)`, `delivered_at`, `due_at`, `confirm_method`, `photo_url`, `signature_url`, `manager_biometric_hash`, `updated_at`, `synced_at`.
- `audit_log`: `id`, `action`, `entity`, `entity_id`, `payload (JSON)`, `timestamp`, `synced_at`.
- `media_files`: `id`, `delivery_id`, `type (photo|signature|pdf)`, `storage_path`, `checksum`, `uploaded_at`.

> `synced_at` permite reconciliar registros e detectar divergências. `checksum` ajuda a garantir integridade das evidências.

## 3. Endpoints Principais
Todos retornam/aceitam JSON e usam UUIDs gerados pelo app:

### 3.1 Funcionários
- `GET /employees?updated_after=<timestamp>`: retorna lista paginada para eventuais pull-syncs.
- `POST /employees/batch`: recebe lista para upsert. Deve ser idempotente usando `updated_at`.

### 3.2 Tipos de EPI
- `GET /epi-types?updated_after=<timestamp>`
- `POST /epi-types/batch`

### 3.3 Entregas
- `POST /deliveries`: upsert individual (aceita multipart para incluir arquivos). Responde com status e URLs finais.
- `POST /deliveries/batch`: alternativa quando enviar só metadados primeiro e subir arquivos depois.
- `GET /deliveries?employee_id=<id>`: útil para relatórios/auditorias.

### 3.4 Auditoria
- `POST /audit-log/batch`: grava múltiplos eventos.
- `GET /audit-log?entity=<name>&entity_id=<id>`: consulta para revisões.

### 3.5 Arquivos
- `POST /media/upload`: multipart com `delivery_id`, `type`, arquivo binário. Retorna URL/ID para referenciar no registro.
- `GET /media/{id}`: download autenticado (necessário para reprocessamento interno, não público).

## 4. Sincronização
1. App envia lote de registros pendentes (`synced=false`) agrupados por entidade.
2. API valida cada item:
   - Rejeita se versão recebida é mais antiga que `updated_at` armazenado.
   - Em caso de conflito, retorna `409` com payload descrevendo o estado atual.
3. Se sucesso, servidor atualiza `synced_at` e responde lista de IDs confirmados.
4. App recebe resposta, marca `synced=true` e salva `syncedAt`.
5. Para arquivos, upload ocorre antes de marcar entrega como sincronizada. `media_files` guarda `checksum` para validação.

## 5. Autenticação e Segurança
- Para MVP, usar API key estática enviada em header `X-Api-Key`. Guardar no app usando EncryptedSharedPreferences.
- Forçar HTTPS (mesmo em staging via ngrok ou Caddy).
- Validar payloads com esquemas (Pydantic em FastAPI ou kotlinx.serialization/Hibernate Validator em Ktor).
- Rate limiting simples (ex: 60 req/min por dispositivo) para evitar abuse cases.

## 6. Auditoria e Logs
- Toda mutação cria uma entrada em `audit_log`.
- Incluir `actor` (ex: `device:<android_id>` ou `manager:<id>`) no payload.
- Registrar upload de arquivos com tamanho, hash e origem (camera/signature).

## 7. Deploy e Ambiente
- **Local**: docker-compose com `api` + `postgres`.
- **Staging**: VM pequena (Railway/Fly.io/Render) suficiente para testes reais.
- Scripts `make migrate`, `make seed` (usando Liquibase) para subir esquemas e dados fake.

## 8. Próximos Passos
1. Escolher stack (FastAPI/Ktor) e gerar projeto base.
2. Definir modelos/ORM e migrations.
3. Implementar endpoints de upsert batch (`employees`, `epi_types`, `deliveries`, `audit_log`).
4. Adicionar upload de arquivos com armazenamento local (`/var/media`) + rota para download.
5. Escrever testes unitários/integrados para conflitos e upload.
6. Documentar API com OpenAPI/Swagger.

Seguindo este roteiro, outro LLM consegue iniciar o backend garantindo alinhamento com os requisitos do app offline-first e as necessidades de evidência segura.
