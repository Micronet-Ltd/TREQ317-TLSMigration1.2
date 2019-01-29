package com.ai.tom.tls12test;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Properties;

public class TLS12MqttTest implements MqttCallback, IMqttActionListener
{
	private       TLS12MqttTestCallback _cb;
	private       MqttAsyncClient       _client;
	private final String                _mqttServerAddress;
	private final String                _clientId;
	private String _message;

	public TLS12MqttTest(String mqttServerAddress, String clientId)
	{
		_mqttServerAddress = mqttServerAddress;
		_clientId = clientId;

	}

	public void test(String message, TLS12MqttTestCallback cb)
	{
		try
		{
			_cb = cb;
			_message=message;
			_client = new MqttAsyncClient(_mqttServerAddress, _clientId, new MemoryPersistence());
			_client.setCallback(this);
			MqttConnectOptions connectOptions = new MqttConnectOptions();

			connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			connectOptions.setUserName("unused");
			connectOptions.setAutomaticReconnect(true);
			Properties sslProps = new Properties();
			sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
			connectOptions.setSSLProperties(sslProps);
			connectOptions.setPassword("NOPASSWORD".toCharArray());
			connectOptions.setKeepAliveInterval(60 * 15);
			_client.setCallback(this);
			_client.connect(connectOptions, null, this);
		}
		catch (MqttException e)

		{
			e.printStackTrace();
		}


	}


	@Override
	public void connectionLost(Throwable cause)
	{

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{
		Log.i(TLS12MqttTest.class.getSimpleName(), "messageArrived topic: " + topic + " message: " + new String(message.getPayload()));
		_cb.onGotMessage(new String(message.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		Log.i(TLS12MqttTest.class.getSimpleName(), "deliveryComplete");

	}

	@Override
	public void onSuccess(IMqttToken asyncActionToken)
	{
		Log.i(TLS12MqttTest.class.getSimpleName(), "Connected");
		String configTopic = "MQTT Examples";
		try
		{
			//_client.subscribe(new String[]{configTopic, deviceUpdatesTopic}, new int[]{1, 1}, new IMqttMessageListener[]{Mqtt.this, Mqtt.this});
			_client.subscribe(configTopic, 1);
			_client.publish(configTopic, new MqttMessage(_message.getBytes()));
		}
		catch (MqttException e)

		{
			e.printStackTrace();
			Log.w(TLS12MqttTest.class.getSimpleName(), Log.getStackTraceString(e));
		}
	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, Throwable exception)
	{
		Log.i(TLS12MqttTest.class.getSimpleName(), "Failed to test");
	}


	public interface TLS12MqttTestCallback
	{
		void onGotMessage(String message);
	}
}
