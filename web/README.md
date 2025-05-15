# Servidor Web DiscordVincular

Sistema para vinculação de contas Minecraft com Discord.

## Configuração

1. Crie um arquivo `.env` na pasta `web/` com as seguintes variáveis:

```env
# Configurações do Discord
CLIENT_ID=seu_client_id_aqui
CLIENT_SECRET=seu_client_secret_aqui
DISCORD_BOT_TOKEN=seu_token_do_bot_aqui
GUILD_ID=id_do_seu_servidor_discord

# Configurações de redirecionamento
REDIRECT_URI=http://localhost:8080/callback

# Porta do servidor
PORT=8080
```

2. Substitua os valores acima pelos dados do seu aplicativo Discord:
   - `CLIENT_ID`: ID do aplicativo Discord
   - `CLIENT_SECRET`: Segredo do aplicativo Discord
   - `DISCORD_BOT_TOKEN`: Token do bot Discord
   - `GUILD_ID`: ID do servidor Discord

## Instalação

```bash
# Instalar dependências
npm install

# Iniciar servidor em modo de desenvolvimento (com reload automático)
npm run dev

# Iniciar servidor em modo de produção
npm start
```

## Funcionalidades

- Autenticação OAuth2 com Discord
- Sincronização automática de cargos entre Minecraft e Discord
- Atualização de apelidos no Discord com nomes do Minecraft
- Desvinculação de contas

## Endpoints

- `/callback`: Endpoint de redirecionamento OAuth2
- `/unlink`: Endpoint para desvinculação de contas

## Créditos

Desenvolvido por:
- [oNyell](https://github.com/oNyell) - GitHub
- onyell - Discord 