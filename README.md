# DiscordVincular

Plugin para servidores Minecraft (BungeeCord) que permite vincular contas do Minecraft com Discord, com atribuição automática de cargos baseado no kCore.

## Funcionalidades

- Vinculação de contas Minecraft com Discord via OAuth2
- Atualização automática de cargos no Discord (a cada 5 segundos por padrão)
- Integração com kCore para obtenção de informações de cargos
- Compatível com BungeeCord
- Sistema de banco de dados MySQL

## Comandos

- `/discord vincular` - Gera um link para vincular a conta do Discord
- `/discord desvincular` - Remove a vinculação atual da conta

## Configuração

O arquivo de configuração (`config.yml`) permite personalizar:
- Conexão com banco de dados MySQL
- Intervalo de atualização de cargos
- URL de redirecionamento para OAuth2

## Instalação

1. Coloque o arquivo JAR na pasta de plugins do seu servidor BungeeCord
2. Reinicie o servidor
3. Configure o arquivo `config.yml` em `plugins/DiscordVincular/`
4. Reinicie novamente para aplicar as alterações

## Requisitos

- Servidor BungeeCord
- MySQL
- kCore (para informações de cargos)

## Créditos

Desenvolvido por:

- [oNyell](https://github.com/oNyell) <- GitHub
- onyell <- Discord

Favor manter os créditos ao utilizar este plugin.
