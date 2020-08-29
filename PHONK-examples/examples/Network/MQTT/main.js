/*
 *  Phonk Example: MQTT Client
 */

ui.addTitle(app.name)
ui.addSubtitle('MQTT client example. Edit the values to connect to your server.')

var connectionData = {
  clientId: 'phonk',
  broker: 'tcp://192.168.10.100:1883',
  autoReconnect: true
  // user: 'myuser',
  // password: 'mypassword'
}

var c = network.createMQTTClient()

c.onConnect(function (e) {
  console.log(e)
  c.subscribe('debug')

  btn.props.borderWidth = 10
  btn.props.borderColor = ui.theme.primary
})

c.onDisconnect(function (e) {
  console.log(e)
})

c.onNewData(function (e) {
  console.log(e)
})

var btn = ui.addButton('connect and subscribe', 0.1, 0.45, 0.35, 0.1).onClick(function (e) {
  c.connect(connectionData)
})

ui.addButton('publish', 0.55, 0.45, 0.35, 0.1).onClick(function (e) {
  c.publish('debug', 'hello world', 2, false) // params: topic, data, qos, retain
})
