package com.ai.tom.tls12test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.conscrypt.Conscrypt;

import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TLS12HttpsUrlConnectionTest.TLS12TestCallback, TLS12MqttTest.TLS12MqttTestCallback
{

	private Button          _urlConnectionHttpTestBtn;
	private ExecutorService _executor;
	private SSLContext      _conscrypt;
	private TextView        _responseTextView;
	private Button            _mqttTestBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_urlConnectionHttpTestBtn = findViewById(R.id.btnSend);
		_mqttTestBtn=findViewById(R.id.btnSendMqtt);
		_executor = Executors.newSingleThreadExecutor();
		_conscrypt = setupConscrypt();
		_responseTextView = findViewById(R.id.textViewResponse);
		_urlConnectionHttpTestBtn.setOnClickListener(this);
		_mqttTestBtn.setOnClickListener(this);
	}

	private SSLContext setupConscrypt()
	{
		SSLContext conscryptContext = null;
		try
		{
			Provider conscryptProvider = Conscrypt.newProvider();
			Security.insertProviderAt(conscryptProvider, 1);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			conscryptContext = SSLContext.getInstance("TLSv1.2", conscryptProvider.getName());
			conscryptContext.init(null, null, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.i("TLS12TEST", "Failed to setup conscrypt");
		}
		return conscryptContext;
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.btnSendMqtt:
				_responseTextView.setText(R.string.nothing_to_show_yet);
				_mqttTestBtn.setEnabled(false);
				_mqttTestBtn.setText(R.string.in_progress);
				new TLS12MqttTest("ssl://iot.eclipse.org:8883","Mn Test Client").test("TLS12 Test passed",this);
				break;
			case R.id.btnSend:
				_responseTextView.setText(R.string.nothing_to_show_yet);
				_urlConnectionHttpTestBtn.setEnabled(false);
				_urlConnectionHttpTestBtn.setText(R.string.in_progress);
				_executor.submit(new TLS12HttpsUrlConnectionTest(_conscrypt, this));
				break;
		}
	}

	@Override
	protected void onDestroy()
	{
		_executor.shutdownNow();
		super.onDestroy();
	}

	@Override
	public void onGotResult(final TLS12HttpsUrlConnectionTest.Result result)
	{
		Log.i("TLS12TEST", result.toString());
		_responseTextView.post(new Runnable()
		{
			@Override
			public void run()
			{
				_urlConnectionHttpTestBtn.setEnabled(true);
				_urlConnectionHttpTestBtn.setText(R.string.send_https_request);
				_responseTextView.setText(result.toString());
			}
		});
	}

	@Override
	public void onGotMessage(final String message)
	{
		_responseTextView.post(new Runnable()
		{
			@Override
			public void run()
			{
				_mqttTestBtn.setEnabled(true);
				_mqttTestBtn.setText(R.string.test_mqtt);
				_responseTextView.setText(message);
			}
		});
	}
}
