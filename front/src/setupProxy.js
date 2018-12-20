const proxy = require('http-proxy-middleware');
module.exports = function (app) {
    app.use(proxy('/projects', {target: 'http://localhost:8080'})),
        app.use(proxy('/check', {target: 'http://localhost:8080'}));
};
