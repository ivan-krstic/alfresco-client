package me.krstic.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64Converter {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Base64Converter.class);

	public static byte[] encodeBase64(ContentStream stream) {
		try {
			if (stream != null) {
				InputStream is = stream.getStream();
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				int nRead;
				byte[] data = new byte[16384];
	
				while ((nRead = is.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}
	
				buffer.flush();
				
				return Base64.getEncoder().encode(buffer.toByteArray());
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public static String encodeBase64(String text) {
		return Base64.getEncoder().encodeToString(text.getBytes());
	}
	
	public static byte[] decodeBase64(byte[] encoded) {
		return Base64.getDecoder().decode(encoded);
	}
	
	public static String[] decodeBase64Authorization(String authorization) {
		if (authorization != null && !authorization.isEmpty()) {
			String usernameAndPassword = authorization.split(" ")[1];
			
			String user = new String(Base64.getDecoder().decode(usernameAndPassword), StandardCharsets.UTF_8);
			
			return user.split(":");
		} else {
			return null;
		}
	}
}
