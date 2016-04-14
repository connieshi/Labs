document.addEventListener('DOMContentLoaded', init);

function init() {
  var req = new XMLHttpRequest();
  var url = document.URL;
	console.log(url);

  var btn = document.querySelector('#find');
  btn.addEventListener('click', handleClick);
}

function handleClick() {
  var tutor = document.querySelector('#find').value;
  var req = new XMLHttpRequest();
  var url = document.URL;
  req.open('POST', url, true);
  req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
  req.send(tutor);
}
