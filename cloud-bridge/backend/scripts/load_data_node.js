const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');
const neo4j = require('neo4j-driver');

// Configuration
const MYSQL_CONFIG = {
    host: 'localhost',
    user: 'user',
    password: 'password',
    database: 'cloud_bridge',
    multipleStatements: true
};

const NEO4J_CONFIG = {
    uri: 'bolt://localhost:7687',
    user: 'neo4j',
    password: 'password'
};

const DATA_DIR = path.join(__dirname, '../data');
const CYPHER_FILE = path.join(__dirname, '../src/main/resources/cypher/import.cypher');

async function loadData() {
    console.log('Starting data loading process...');

    // 1. MySQL Connection
    let connection;
    try {
        connection = await mysql.createConnection(MYSQL_CONFIG);
        console.log('Connected to MySQL.');
    } catch (err) {
        console.error('Failed to connect to MySQL:', err);
        return;
    }

    // 2. Neo4j Driver
    const driver = neo4j.driver(NEO4J_CONFIG.uri, neo4j.auth.basic(NEO4J_CONFIG.user, NEO4J_CONFIG.password));
    let session;

    try {
        // --- MySQL Data Loading ---
        
        // Create Tables
        const createAchievementsTable = `
            CREATE TABLE IF NOT EXISTS achievements (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT NOT NULL,
                field VARCHAR(255) NOT NULL,
                maturity VARCHAR(255) NOT NULL,
                price DECIMAL(19, 2) NOT NULL,
                owner_id BIGINT NOT NULL,
                status VARCHAR(50) DEFAULT 'PENDING_REVIEW',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `;
        
        const createDemandsTable = `
            CREATE TABLE IF NOT EXISTS demands (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT NOT NULL,
                field VARCHAR(255) NOT NULL,
                budget DECIMAL(19, 2) NOT NULL,
                deadline DATETIME NOT NULL,
                type VARCHAR(50) NOT NULL,
                owner_id BIGINT NOT NULL,
                status VARCHAR(50) DEFAULT 'PENDING_REVIEW',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `;

        await connection.execute(createAchievementsTable);
        await connection.execute(createDemandsTable);
        console.log('MySQL tables created/verified.');

        // Load Achievements
        const achievementsPath = path.join(DATA_DIR, 'raw_achievements.json');
        if (fs.existsSync(achievementsPath)) {
            const achievements = JSON.parse(fs.readFileSync(achievementsPath, 'utf8'));
            // Clear existing
            await connection.execute('TRUNCATE TABLE achievements');
            
            for (const a of achievements) {
                const sql = `INSERT INTO achievements (title, description, field, maturity, price, owner_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)`;
                // Map JSON fields to columns. 
                // Note: JSON might use camelCase, DB uses snake_case if I defined it so, but JPA default is often snake_case. 
                // My create table uses snake_case for owner_id, others match.
                // Assuming JSON keys: title, description, field, maturity, price, ownerId, status
                await connection.execute(sql, [
                    a.title, 
                    a.description, 
                    a.field, 
                    a.maturity, 
                    a.price, 
                    a.ownerId || 1, // Default ownerId if missing
                    a.status || 'PUBLISHED'
                ]);
            }
            console.log(`Loaded ${achievements.length} achievements into MySQL.`);
        } else {
            console.warn('raw_achievements.json not found.');
        }

        // Load Demands
        const demandsPath = path.join(DATA_DIR, 'raw_demands.json');
        if (fs.existsSync(demandsPath)) {
            const demands = JSON.parse(fs.readFileSync(demandsPath, 'utf8'));
            // Clear existing
            await connection.execute('TRUNCATE TABLE demands');
            
            for (const d of demands) {
                const sql = `INSERT INTO demands (title, description, field, budget, deadline, type, owner_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`;
                // JSON keys: title, description, field, budget, deadline, type, ownerId, status
                // deadline might be ISO string
                let deadline = d.deadline;
                if (!deadline) {
                     // Set default future date
                     const date = new Date();
                     date.setDate(date.getDate() + 30);
                     deadline = date;
                }
                
                await connection.execute(sql, [
                    d.title, 
                    d.description, 
                    d.field, 
                    d.budget, 
                    deadline, 
                    d.type || 'NORMAL',
                    d.ownerId || 1,
                    d.status || 'PUBLISHED'
                ]);
            }
            console.log(`Loaded ${demands.length} demands into MySQL.`);
        } else {
            console.warn('raw_demands.json not found.');
        }


        // --- Neo4j Data Loading ---
        session = driver.session();
        console.log('Connected to Neo4j.');

        if (fs.existsSync(CYPHER_FILE)) {
            const cypherScript = fs.readFileSync(CYPHER_FILE, 'utf8');
            const statements = cypherScript.split(';').map(s => s.trim()).filter(s => s.length > 0);
            
            // Clear database first? Maybe dangerous if other data exists. 
            // DataInitializer.java doesn't clear unless I uncommented the check.
            // But for "load data" usually we want a clean state or idempotent.
            // Let's clear just in case to avoid duplicates if constraints aren't set.
            // Or just run the import. MERGE is better than CREATE.
            // import.cypher uses CREATE or MERGE? Usually CREATE in generated scripts.
            // If it uses CREATE, running twice duplicates data.
            // Let's detach delete all for a clean slate as per user request "load data".
            await session.run('MATCH (n) DETACH DELETE n');
            console.log('Cleared Neo4j database.');

            for (const stmt of statements) {
                await session.run(stmt);
            }
            console.log(`Executed ${statements.length} Cypher statements.`);
        } else {
            console.warn('import.cypher not found.');
        }

    } catch (err) {
        console.error('Error during data loading:', err);
    } finally {
        if (connection) await connection.end();
        if (session) await session.close();
        await driver.close();
        console.log('Data loading complete.');
    }
}

loadData();
