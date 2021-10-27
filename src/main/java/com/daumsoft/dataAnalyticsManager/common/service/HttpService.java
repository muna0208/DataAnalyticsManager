package com.daumsoft.dataAnalyticsManager.common.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daumsoft.dataAnalyticsManager.common.utils.FileUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class HttpService {

	private Logger logger = LoggerFactory.getLogger(HttpService.class);

	private OkHttpClient client;

	public HttpService() {
		try {
			client = new OkHttpClient();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.connectTimeout(30, TimeUnit.SECONDS);
			builder.readTimeout(30, TimeUnit.SECONDS);
			builder.writeTimeout(30, TimeUnit.SECONDS);
			client = builder.build();

			logger.info("--- HttpService : Set client ");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("--- HttpService : " + e.toString());
		}
	}

	/**
	 * HttpService GET
	 * 
	 * @param connUrl
	 * @return
	 * @throws IOException
	 */
	public JSONObject httpServiceGET(String connUrl) throws Exception {
		logger.info("--- httpServiceGET connUrl: " + connUrl + " ---");
		JSONObject result = new JSONObject();
		Request request = null;

		request = new Request.Builder().url(connUrl).build();
		String resMessage = "";

		Response response = client.newCall(request).execute();
		resMessage = response.body().string();

		result.put("type", "" + response.code());
		result.put("title", response.message());
		result.put("data", resMessage);

		response.body().close();
		logger.info("--- httpServiceGET result : " + result.toString());
		return MakeUtil.nvlJson(result);
	}

	/**
	 * httpService POST
	 * 
	 * @param connUrl
	 * @param jsonMessage
	 * @return
	 * @throws IOException
	 */
	public JSONObject httpServicePOST(String connUrl, String jsonMessage) throws Exception {
		logger.info("--- httpServicePOST connUrl: " + connUrl + ", jsonMessage: " + jsonMessage + " ---");
		JSONObject result = new JSONObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";

		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
				jsonMessage.toString());
		okRequest = new Request.Builder().url(connUrl).post(requestBody).build();

		response = client.newCall(okRequest).execute();
		resMessage = response.body().string();

		result.put("type", "" + response.code());
		result.put("title", response.message());
		result.put("data", resMessage);

		response.body().close();
		logger.info("--- httpServicePOST result : " + result.toString());
		return MakeUtil.nvlJson(result);

	}

	/**
	 * httpService PATCH
	 * 
	 * @param connUrl
	 * @param jsonMessage
	 * @param option
	 * @return
	 * @throws Exception
	 */
	public JSONObject httpServicePATCH(String connUrl, String jsonMessage) throws Exception {
		logger.info("--- httpServicePATCH connUrl: " + connUrl + ", jsonMessage: " + jsonMessage + " ---");
		JSONObject result = new JSONObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";

		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
				jsonMessage.toString());
		okRequest = new Request.Builder().url(connUrl).patch(requestBody).build();

		response = client.newCall(okRequest).execute();
		resMessage = response.body().string();

		result.put("type", "" + response.code());
		result.put("title", response.message());
		result.put("data", resMessage);

		response.body().close();
		logger.info("--- httpServicePATCH result : " + result.toString());
		return MakeUtil.nvlJson(result);

	}

	/**
	 * httpService DELETE
	 * 
	 * @param connUrl
	 * @return
	 * @throws Exception
	 */
	public JSONObject httpServiceDELETE(String connUrl) throws Exception {
		logger.info("--- httpServiceDELETE connUrl: " + connUrl + " ---");
		JSONObject result = new JSONObject();
		Request request = null;

		request = new Request.Builder().url(connUrl).delete().build();

		String resMessage = "";

		Response response = client.newCall(request).execute();
		resMessage = response.body().string();

		result.put("type", "" + response.code());
		result.put("title", response.message());
		result.put("data", resMessage);

		response.body().close();
		logger.info("--- httpServiceDELETE result : " + result.toString());
		return MakeUtil.nvlJson(result);
	}

	/**
	 * 파일 다운로드
	 * 
	 * @param connUrl
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void httpServiceDownloader(String connUrl, String filePath, String fileName) throws Exception {
		logger.info("--- httpServiceDownloader connUrl: " + connUrl + ", filePath: " + filePath + ", fileName: "
				+ fileName + " ---");
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			FileUtil.mkdir(filePath);
			fos = new FileOutputStream(filePath + "\\" + fileName);

			URL url = new URL(connUrl);
			URLConnection urlConnection = url.openConnection();
			is = urlConnection.getInputStream();
			byte[] buffer = new byte[1024];
			int readBytes;
			while ((readBytes = is.read(buffer)) != -1) {
				fos.write(buffer, 0, readBytes);
			}
		} finally {
			if (fos != null)
				fos.close();
			if (is != null)
				is.close();
		}
		logger.info("--- httpServiceDownloader compleate download!! ---");
	}

}
