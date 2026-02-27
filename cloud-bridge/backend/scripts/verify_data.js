const mysql = require('mysql2/promise');
const neo4j = require('neo4j-driver');

const MYSQL_CONFIG = {
    host: 'localhost',
    user: 'user',
    password: 'password',
    database: 'cloud_bridge'
};

const NEO4J_CONFIG = {
    uri: 'bolt://localhost:7687',
    user: 'neo4j',
    password: 'password'
};

async function verify() {
    let connection;
    const driver = neo4j.driver(NEO4J_CONFIG.uri, neo4j.auth.basic(NEO4J_CONFIG.user, NEO4J_CONFIG.password));
    let session;

    try {
        // MySQL Verification
        connection = await mysql.createConnection(MYSQL_CONFIG);
        const [achievements] = await connection.execute('SELECT COUNT(*) as count FROM achievements');
        const [demands] = await connection.execute('SELECT COUNT(*) as count FROM demands');
        
        console.log(`MySQL Verification:`);
        console.log(`- Achievements: ${achievements[0].count}`);
        console.log(`- Demands: ${demands[0].count}`);

        // Neo4j Verification
        session = driver.session();
        const result = await session.run('MATCH (n) RETURN count(n) as count');
        const nodeCount = result.records[0].get('count').toInt();
        
        const relResult = await session.run('MATCH ()-[r]->() RETURN count(r) as count');
        const relCount = relResult.records[0].get('count').toInt();

        console.log(`Neo4j Verification:`);
        console.log(`- Nodes: ${nodeCount}`);
        console.log(`- Relationships: ${relCount}`);

        // Specific Check for New Data (e.g., "量子通信")
        const checkResult = await session.run('MATCH (n:Field {name: "量子通信"}) RETURN n');
        if (checkResult.records.length > 0) {
            console.log('✅ Found new field "量子通信" in Graph.');
        } else {
            console.warn('❌ "量子通信" not found in Graph.');
        }

    } catch (err) {
        console.error('Verification failed:', err);
    } finally {
        if (connection) await connection.end();
        if (session) await session.close();
        await driver.close();
    }
}

verify();
