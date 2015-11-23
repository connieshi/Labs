var http = require('http'), fs = require('fs');
var port = 3000;
http.createServer(handleRequest).listen(port);
console.log('Started server on port', port);

function handleRequest(request, response) {
	var responseCode = 0;
	var url = request.url.toLowerCase();
	if (url.charAt(url.length - 1) == '/') {
		url = url.substr(0, url.length - 1);
	}

	if (url === '/' || url === '/home') {
		responseCode = 200;
		serveStatic(response, './public/index.html', 'text.html', responseCode);
	} else if (url === '/about') {
		responseCode = 200;
		serveStatic(response, './public/about.html', 'text/html', responseCode);
	} else if (url === '/me') {
		responseCode = 301;
		response.writeHead(301, {'Location': '/about'});
		response.end();
	} else if (url === '/img/image1.png') {
		responseCode = 200;
		serveStatic(response, './public/img/image1.png', 'image/png', responseCode);
	} else if (url === '/img/image2.png') {
		responseCode = 200;
		serveStatic(response, './public/img/image2.png', 'image/png', responseCode);
	} else if (url === '/css/base.css') {
		responseCode = 200;
		serveStatic(response, './public/css/base.css', 'text/css', responseCode);
	} else {
		responseCode = 404;
		serveStatic(response, './public/404.html', 'text/html', responseCode);
	}

	printToScreen(url, request.method, responseCode, http.STATUS_CODES[responseCode]);
}

function printToScreen(url, method, responseCode, message) {
  var date = new Date();
	console.log(date.toLocaleString(), method, url, responseCode, message);
}

function serveStatic(response, path, contentType, responseCode) {
	fs.readFile(path, function(err, data) {
		if (err) {
			response.writeHead(500, {'Content-Type': 'text/plain'}); 
			response.end('500 - Internal Error');
		} else {
			response.writeHead(responseCode, {'Content-Type': contentType}); 
			response.end(data);
		}
	});
}

