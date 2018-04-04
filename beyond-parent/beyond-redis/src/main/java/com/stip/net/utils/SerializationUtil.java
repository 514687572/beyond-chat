package com.stip.net.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 序列化和反序列化工具
 * @author cja
 *
 */
public class SerializationUtil {
	static final Class<?> CLAZZ = SerializationUtil.class;
	
	public static Log log = LogFactory.getLog(SerializationUtil.class);
		
	    public static byte[] serialize(Object value) {
	        if (value == null) { 
	            throw new NullPointerException("Can't serialize null");
	        }
	        byte[] rv = null;
	        ByteArrayOutputStream bos = null;
	        ObjectOutputStream os = null;
	        try {
	            bos = new ByteArrayOutputStream();
	            os = new ObjectOutputStream(bos);
	            os.writeObject(value);
	            os.close();
	            bos.close();
	            rv = bos.toByteArray();
	        } catch (Exception e) {
	        	log.error(e);
	        } finally {
	            close(os);
	            close(bos);
	        }
	        return rv;
	    }
	
	    
		public static Object deserialize(byte[] in) {
	        return deserialize(in, Object.class);
	    }
	
	    public static <T> T deserialize(byte[] in, Class...requiredType) {
	        T rv = null;
	        ByteArrayInputStream bis = null;
	        ObjectInputStream is = null;
	        try {
	            if (in != null) {
	                bis = new ByteArrayInputStream(in);
	                is = new ObjectInputStream(bis);
	                rv = (T) is.readObject();
	            }
	        } catch (Exception e) {
	        	log.error(e);
	        } finally {
	            close(is);
	            close(bis);
	        }
	        return rv;
	    }
	
	    private static void close(Closeable closeable) {
	        if (closeable != null)
	            try {
	                closeable.close();
	            } catch (IOException e) {
	            	log.error(e);
	            }
	    }
	
}
