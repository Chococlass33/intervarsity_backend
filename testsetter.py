import requests

url = 'http://localhost:5002/setDancers/Salsa'

myobj = {"judgeID":"webmaster@monashdancesociety.com","vote":["1","3","2"]}

requests.post(url, data = myobj)
