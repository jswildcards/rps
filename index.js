const express = require('express');
const app = express();
const port = process.env.PORT || 3000;

const data = [
    {
        name: "Alex",
        age: 23,
        hand: 0,
    },
    {
        name: "Andy",
        age: 43,
        hand: 1,
    },
    {
        name: "Billy",
        age: 31,
        hand: 2,
    },
];

app.get('*', (req, res) => res.json(data[~~(Math.random() * 3)]));

app.listen(port, () => console.log(`Example app listening on port ${port}!`));
