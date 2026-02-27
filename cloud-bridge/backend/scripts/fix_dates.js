
const fs = require('fs');
const path = require('path');

const FILE = path.join(__dirname, '../data/raw_demands.json');
const TODAY = new Date('2026-02-21');

try {
    const data = fs.readFileSync(FILE, 'utf8');
    let demands = JSON.parse(data);
    let fixedCount = 0;

    demands = demands.map(d => {
        if (d.deadline) {
            let original = d.deadline;
            
            // 1. Fix invalid days (June 31 etc)
            if (d.deadline.match(/-(04|06|09|11)-31T/)) {
                d.deadline = d.deadline.replace(/-31T/, '-30T');
            }
            if (d.deadline.match(/-02-(29|30|31)T/)) {
                d.deadline = d.deadline.replace(/-02-\d\dT/, '-02-28T');
            }

            // 2. Fix past dates
            // Parse date manually to avoid timezone issues or just simple string comparison if ISO
            // "2026-02-21" is today.
            // If year < 2026, set to 2027.
            // If year == 2026 and month < 03, set to 2027.
            
            let year = parseInt(d.deadline.substring(0, 4));
            let month = parseInt(d.deadline.substring(5, 7));
            
            if (year < 2026 || (year === 2026 && month < 3)) {
                // Change year to 2027
                d.deadline = d.deadline.replace(/^\d{4}/, '2027');
            }

            if (d.deadline !== original) {
                fixedCount++;
            }
        }
        return d;
    });

    if (fixedCount > 0) {
        console.log(`Fixed ${fixedCount} dates.`);
        fs.writeFileSync(FILE, JSON.stringify(demands, null, 2), 'utf8');
    } else {
        console.log("No dates needed fixing.");
    }

} catch (e) {
    console.error("Error:", e.message);
}
