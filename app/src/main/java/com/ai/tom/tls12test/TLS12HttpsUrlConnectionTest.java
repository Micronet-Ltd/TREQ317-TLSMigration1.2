package com.ai.tom.tls12test;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class TLS12HttpsUrlConnectionTest implements Runnable
{

	private final SSLContext        _sslContext;
	private final TLS12TestCallback _cb;

	public TLS12HttpsUrlConnectionTest(SSLContext sslContext, TLS12TestCallback cb)
	{
		_cb = cb;
		_sslContext = sslContext;
	}

	@Override
	public void run()
	{
		HttpsURLConnection conn;
		Result result;
		int responseCode = -1;
		try
		{
			conn = (HttpsURLConnection) new URL("https://fancyssl.hboeck.de").openConnection();
			if (_sslContext != null)
			{
				conn.setSSLSocketFactory(_sslContext.getSocketFactory());
			}
			conn.connect();
			responseCode = conn.getResponseCode();
			Log.i("TLS12TEST", "response code " + responseCode);
			if (responseCode < 300)
			{
				InputStream is = conn.getInputStream();
				byte[] resultArr = inputStreamToBytearr(is);
				is.close();
				result = new Result(responseCode, resultArr);
			}
			else
			{
				InputStream is = conn.getErrorStream();
				byte[] resultArr = inputStreamToBytearr(is);
				is.close();
				result = new Result(responseCode, resultArr);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = new Result(responseCode, e);
		}
		_cb.onGotResult(result);
	}

	private byte[] inputStreamToBytearr(InputStream is)
	{
		try
		{
			byte[] buf = new byte[5120];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int red;
			do
			{
				red = is.read(buf);
				if (red > 0)
				{
					bos.write(buf, 0, red);
				}
			}
			while (red > 0);
			return bos.toByteArray();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static class Result
	{
		private int       _status;
		private byte[]    _response;
		private Exception _exception;

		public Result(int status, Exception e)
		{
			_status = status;
			_exception = e;
		}

		public Result(int status, byte[] response)
		{
			_status = status;
			_response = response;
		}

		public byte[] getResponse()
		{
			return _response;
		}

		public int getStatus()
		{
			return _status;
		}

		public Exception getException()
		{
			return _exception;
		}

		@Override
		public String toString()
		{
			return "Status code: " + _status + "  response: " + (_response != null ? new String(_response) : (_exception != null ? Log.getStackTraceString(_exception) : ""));
		}
	}

	public interface TLS12TestCallback
	{
		void onGotResult(Result result);
	}

}
