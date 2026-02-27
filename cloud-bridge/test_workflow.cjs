const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

// Utils
const login = async (username, password) => {
    try {
        const res = await axios.post(`${BASE_URL}/auth/login`, { username, password });
        // Return full response data which includes token, id, username, role
        return res.data;
    } catch (e) {
        console.error(`Login failed for ${username}:`, e.response?.data || e.message);
        throw e;
    }
};

const sendMessage = async (token, message) => {
    try {
        const res = await axios.post(`${BASE_URL}/messages`, message, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return res.data;
    } catch (e) {
        console.error('Send message failed:', e.response?.data || e.message);
        throw e;
    }
};

const getReceivedMessages = async (token, userId) => {
    try {
        const res = await axios.get(`${BASE_URL}/messages/received/${userId}`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return res.data;
    } catch (e) {
        console.error('Get messages failed:', e.response?.data || e.message);
        throw e;
    }
};

// Main Test Flow
(async () => {
    try {
        console.log('--- STARTING WORKFLOW TEST ---');

        const register = async (user) => {
            try {
                await axios.post(`${BASE_URL}/auth/register`, user);
                console.log(`Registered ${user.username}`);
            } catch (e) {
                if (e.response?.status !== 400 && e.response?.status !== 409) {
                     console.log(`Register ${user.username} failed (might exist):`, e.message);
                } else {
                     console.log(`User ${user.username} likely exists.`);
                }
            }
        };

        const userA = { username: 'workflow_user_a', password: 'password123', role: 'ENTERPRISE', email: 'a@test.com' };
        const userB = { username: 'workflow_user_b', password: 'password123', role: 'EXPERT', email: 'b@test.com' };

        await register(userA);
        await register(userB);

        // Login and get IDs directly from response
        const dataA = await login(userA.username, userA.password);
        const tokenA = dataA.token;
        const idA = dataA.id;
        console.log(`User A logged in. ID: ${idA}`);
        
        const dataB = await login(userB.username, userB.password);
        const tokenB = dataB.token;
        const idB = dataB.id;
        console.log(`User B logged in. ID: ${idB}`);

        // 2. User B contacts User A (about a dummy demand ID 999)
        console.log('User B sending message to User A...');
        const msg1 = {
            senderId: idB,
            receiverId: idA,
            content: 'I am interested in your demand.',
            relatedEntityId: 999,
            relatedEntityType: 'DEMAND',
            cooperationType: 'Joint R&D',
            budget: '50-100'
        };
        
        await sendMessage(tokenB, msg1);
        console.log('Message sent.');

        // 3. User A checks messages
        console.log('User A checking messages...');
        const messagesA = await getReceivedMessages(tokenA, idA);
        // messagesA might be Page object or List.
        const msgListA = messagesA.content || messagesA;
        const lastMsgA = msgListA.find(m => m.content === 'I am interested in your demand.');
        
        if (lastMsgA) {
            console.log('User A received message:', lastMsgA.content);
        } else {
            console.log('Messages received:', JSON.stringify(msgListA, null, 2));
            throw new Error('User A did not receive the message.');
        }

        // 4. User A replies to User B
        console.log('User A replying to User B...');
        const replyMsg = {
            senderId: idA,
            receiverId: idB,
            content: 'Great! Let us talk.',
            relatedEntityId: lastMsgA.relatedEntityId,
            relatedEntityType: lastMsgA.relatedEntityType
        };
        
        await sendMessage(tokenA, replyMsg);
        console.log('Reply sent.');

        // 5. User B checks messages
        console.log('User B checking messages...');
        const messagesB = await getReceivedMessages(tokenB, idB);
        const msgListB = messagesB.content || messagesB;
        const lastMsgB = msgListB.find(m => m.content === 'Great! Let us talk.');
        
        if (lastMsgB) {
            console.log('User B received reply:', lastMsgB.content);
            console.log('Related Entity Type:', lastMsgB.relatedEntityType); // Should be DEMAND
        } else {
            console.log('Messages received:', JSON.stringify(msgListB, null, 2));
            throw new Error('User B did not receive the reply.');
        }

        console.log('--- WORKFLOW TEST PASSED ---');

    } catch (e) {
        console.error('--- WORKFLOW TEST FAILED ---');
        console.error(e);
        process.exit(1);
    }
})();
