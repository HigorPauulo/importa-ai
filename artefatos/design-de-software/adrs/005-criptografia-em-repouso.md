# ADR-005: Criptografia de PII em repouso

**Status:** Aceito
**Data:** 2026-05-17
**Autor:** Equipe Importa Aí

## Contexto

RNF09 exige que dados pessoais (nome e e-mail do usuário) sejam criptografados em repouso (AES-256) para conformidade com a LGPD. Dois requisitos conflitantes precisam ser atendidos simultaneamente:

1. **Confidencialidade:** um atacante com acesso ao banco de dados (dump SQL, acesso direto ao MySQL) não deve conseguir ler e-mails ou nomes em texto claro.
2. **Buscabilidade:** o sistema precisa localizar um usuário pelo e-mail (login, verificação de unicidade) sem descriptografar todos os registros.

Criptografia simétrica pura não resolve o segundo requisito: para buscar `WHERE email = ?` seria necessário descriptografar linha a linha. Um hash puro (SHA-256) não resolve o primeiro: e-mails têm entropia baixa e são reversíveis por dicionário.

## Decisão

Adotar **criptografia de coluna na camada de aplicação** com dois campos complementares na tabela `usuario`:

| Campo | Tipo | Conteúdo |
|-------|------|----------|
| `email` | `VARBINARY(255)` | E-mail cifrado com AES-256-GCM + IV aleatório por linha |
| `email_hash` | `CHAR(64)` | HMAC-SHA256 do e-mail com chave secreta do servidor |

**AES-256-GCM para `email` e `nome_completo`:**
- Modo autenticado: detecta adulteração sem descriptografar (integridade garantida).
- IV (nonce) aleatório de 12 bytes por linha, concatenado ao cipher-text antes de armazenar — impede reutilização de IV e ataques de replay.
- Chave lida de `IMPORTAAI_ENCRYPTION_KEY` (variável de ambiente; nunca em `application.properties` nem no repositório).
- O tipo `VARBINARY` acomoda os bytes brutos do cipher-text + IV concatenados.

**HMAC-SHA256 para `email_hash`:**
- Determinístico: o mesmo e-mail sempre produz o mesmo hash com a mesma chave.
- Chave secreta (`IMPORTAAI_HMAC_KEY`) separada da chave de criptografia — comprometimento de uma não expõe a outra.
- Resiste a dicionários: sem a chave, o hash não pode ser revertido nem rainbow-tabled.
- Permite `WHERE email_hash = HMAC(input)` — busca exata em O(log n) via índice UNIQUE.
- `nome_completo` não precisa de hash separado: nunca é chave de busca.

Chaves gerenciadas exclusivamente via variáveis de ambiente. Rotação de chaves é responsabilidade da operação (requer script offline para re-cifrar as linhas existentes).

## Alternativas consideradas

- **MySQL TDE (tablespace encryption)** — rejeitada como solução principal. TDE cifra os arquivos físicos em disco mas mantém os dados em texto claro na memória e acessíveis via SQL por qualquer DBA com permissão. Não protege contra acesso SQL ao banco. Pode ser ativada como camada adicional de defesa em profundidade, mas não substitui a criptografia de coluna.
- **Hash SHA-256 simples no e-mail** — rejeitada. E-mails têm entropia baixa e padrões previsíveis; um dicionário de e-mails comuns reverte o hash trivialmente. HMAC com chave secreta elimina essa vulnerabilidade.
- **`MySQL AES_ENCRYPT()` / funções de criptografia do SGBD** — rejeitada. Delega a criptografia ao banco, expondo a chave nos logs de query e ao processo do SGBD. Criptografia na aplicação mantém a chave completamente fora do banco.
- **HashiCorp Vault** — rejeitada nesta versão. Adiciona dependência operacional significativa. Variáveis de ambiente com rotação manual é proporcional ao porte atual do projeto.

## Consequências

- **(+)** Dados pessoais protegidos mesmo em dump SQL completo ou acesso direto ao MySQL por terceiros.
- **(+)** Busca por e-mail mantida em O(log n) via índice `UNIQUE` em `email_hash`.
- **(+)** IV aleatório por linha: dois usuários com o mesmo e-mail geram cipher-texts distintos — não há correlação visível no banco.
- **(−)** Busca por prefixo ou `LIKE` no e-mail não é possível — busca é sempre exata via hash. Aceitável: nenhum caso de uso exige busca parcial por e-mail.
- **(−)** Rotação de chave exige re-cifrar todas as linhas (operação de manutenção offline).
- **(~)** `email_hash` é o único campo comparável para verificação de unicidade — o índice `UNIQUE` está sobre ele, não sobre `email`.

## Referências

- ERS — RNF09 (LGPD, criptografia de PII em repouso)
- [Modelo de Dados](../../modelagem-de-dados/modelo-de-dados.md) — §4.1 (`usuario`), campos `email`, `email_hash` e `nome_completo`
- OWASP Cryptographic Storage Cheat Sheet — https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html