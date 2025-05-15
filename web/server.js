/**
 * @author oNyell
 * GitHub: https://github.com/oNyell
 * Discord: onyell
 */

require('dotenv').config();

const CLIENT_ID = process.env.CLIENT_ID;
const CLIENT_SECRET = process.env.CLIENT_SECRET;
const REDIRECT_URI = process.env.REDIRECT_URI || "http://localhost:8080/callback";
const DISCORD_BOT_TOKEN = process.env.DISCORD_BOT_TOKEN;
const GUILD_ID = process.env.GUILD_ID;

const express = require('express');
const { Client, GatewayIntentBits } = require('discord.js');
const mysql = require('mysql2/promise');
const axios = require('axios');

const PORT = process.env.PORT || 8080;

const ROLE_MAPPING = {
    '1012388561598812240': 'Master',
    '1012388611305508925': 'Gerente',
    '1012388759007920258': 'Admin',
    '977427661020360794': 'Moderador',
    '1012388905435279443': 'Ajudante',
    '1012389714843672676': 'Construtor',
    '977446766137995344': 'YouTuber',
    '1012389467052593293': 'Streamer',
    '977555013750640670': 'MVPPlus',
    '1024464726547247104': 'MVP',
    '977554945354121248': 'VIP',
    '977389401531367516': 'Membro'
};

const dbConfig = {
    host: 'localhost',
    port: '3306',
    user: 'root',
    password: '',
    database: 'server'
};

async function connectToDatabase() {
    return await mysql.createPool(dbConfig);
}

async function checkRolesAndNicknames(client, db) {
    const guild = await client.guilds.fetch(GUILD_ID);
    const [rows] = await db.query('SELECT p.name, p.discord, k.role FROM profiles p INNER JOIN kCoreProfile k ON p.name = k.name WHERE p.discord IS NOT NULL');

    for (const row of rows) {
        const memberId = row.discord;
        const playerName = row.name;
        const minecraftRoleName = row.role;
        const correctRoleId = Object.keys(ROLE_MAPPING).find(key => ROLE_MAPPING[key] === minecraftRoleName);

        try {
            const member = await guild.members.fetch(memberId);
            const discordRoleIds = member.roles.cache.map(role => role.id);

            if (!discordRoleIds.includes(correctRoleId)) {
                await member.roles.set([correctRoleId]);
                console.log(`Atualizado cargo de ${member.user.tag} para ${minecraftRoleName}`);
            }

            const desiredNickname = `${playerName} ⚔`;
            if (member.nickname !== desiredNickname) {
                await member.setNickname(desiredNickname);
                console.log(`Atualizado apelido de ${member.user.tag} para ${desiredNickname}`);
            }
        } catch (err) {
            console.error(`Erro ao buscar membro ${memberId}: ${err.message}`);
        }
    }
}

async function unlinkPlayer(client, discordId) {
    const guild = await client.guilds.fetch(GUILD_ID);

    try {
        const member = await guild.members.fetch(discordId);

        await member.roles.set([]);
        console.log(`Removidos todos os cargos de ${member.user.tag}`);

        await member.setNickname(null);
        console.log(`Resetado o apelido de ${member.user.tag}`);

    } catch (err) {
        console.error(`Erro ao desvincular membro ${discordId}: ${err.message}`);
    }
}

const app = express();
app.get('/callback', async (req, res) => {
    const code = req.query.code;
    const state = req.query.state; // Nome do jogador no Minecraft (É o mesmo código que o vincular command manda)

    if (!code || !state) {
        return res.status(400).send('Código ou estado ausentes');
    }

    try {
        // Trocar o código pelo token de acesso
        const tokenResponse = await axios.post('https://discord.com/api/oauth2/token', new URLSearchParams({
            client_id: CLIENT_ID,
            client_secret: CLIENT_SECRET,
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: REDIRECT_URI
        }).toString(), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });

        const accessToken = tokenResponse.data.access_token;

        // Obter as informações do usuário do Discord
        const userResponse = await axios.get('https://discord.com/api/v10/users/@me', {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const discordId = userResponse.data.id;

        // Atualizar a tabela "profiles" com o ID do Discord
        const db = await connectToDatabase();
        await db.query('UPDATE profiles SET discord = ? WHERE name = ?', [discordId, state]);

        res.send('Conta do Discord vinculada com sucesso!');
    } catch (error) {
        console.error('Erro durante o processo de OAuth2:', error);
        res.send('Ocorreu um erro ao vincular sua conta. Tente novamente.');
    }
});

// Rota para desvinculação no site
app.get('/unlink', async (req, res) => {
    const discordId = req.query.discordId;

    if (!discordId) {
        return res.status(400).send('ID do Discord ausente');
    }

    try {
        const db = await connectToDatabase();
        await db.query('UPDATE profiles SET discord = NULL WHERE discord = ?', [discordId]);

        // Remove o cargo e reseta o apelido
        await unlinkPlayer(client, discordId);

        res.send('Conta do Discord desvinculada com sucesso!');
    } catch (error) {
        console.error('Erro durante o processo de desvinculação:', error);
        res.send('Ocorreu um erro ao desvincular sua conta. Tente novamente.');
    }
});

async function main() {
    const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMembers] });
    const db = await connectToDatabase();

    client.once('ready', () => {
        console.log(`Bot está online como ${client.user.tag}`);

        // Verificação a cada 5 segundos
        setInterval(() => checkRolesAndNicknames(client, db), 5000);
    });

    client.login(DISCORD_BOT_TOKEN);
    app.listen(PORT, () => console.log(`Servidor de callback OAuth2 rodando na porta ${PORT}`));
}

main().catch(console.error);

module.exports = {
    dbConfig,
    connectToDatabase,
    checkRolesAndNicknames,
    unlinkPlayer,
    app
};
