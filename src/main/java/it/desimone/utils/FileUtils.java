package it.desimone.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.itextpdf.text.log.SysoLogger;

public class FileUtils {

    public static boolean zippaFile(File[] files, String targetName){
	   	    
    	boolean zipped = true;
	    // Create a buffer for reading the files
	    byte[] buf = new byte[1024];
	    
	    try {
	        // Create the ZIP file
	           	        
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetName));
	    
	        // Compress the files
	        for (int i=0; i<files.length; i++) {
	            FileInputStream in = new FileInputStream(files[i]);
	    
	            // Add ZIP entry to output stream.
	            String path = files[i].getAbsolutePath();
	            out.putNextEntry(new ZipEntry(path.substring(path.lastIndexOf(File.separator)+1)));
	    
	            // Transfer bytes from the file to the ZIP file
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	    
	            // Complete the entry
	            out.closeEntry();
	            in.close();
	        }
	    
	        // Complete the ZIP file
	        out.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	zipped = false;
	    }
	    return zipped;
    }	
    
    public static File estraiDaZip(File file, String targetName){

    	File result = null;

    	try {
    		FileInputStream fis = new FileInputStream(file);
    		ZipFile zipFile = new ZipFile(file);
    		ZipInputStream zis = new ZipInputStream(fis);
    		ZipEntry ze;
    		while((ze=zis.getNextEntry())!=null){
    			if (ze.getName().equals(targetName)){
    				String fileName = targetName;
    				if (fileName.contains("/")){
    					fileName = targetName.substring(targetName.indexOf("/")+1);
    				}
    				result = new File(fileName);
    				InputStream is = zipFile.getInputStream(ze);
    				int BUFFER = 2048;
    				int count;
    				byte data[] = new byte[BUFFER];
    				FileOutputStream fos = new FileOutputStream(result);
    				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
    				while ((count = is.read(data, 0, BUFFER)) != -1) {
    					dest.write(data, 0, count);
    				}
    				dest.flush();
    				dest.close();
    				is.close();
    				
    				break;
    			}
    			zis.closeEntry();
    		}
    		zis.close();

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return result;
    }
    
    
    public static InputStream estraiInputStreamDaZip(File file, String targetName){

    	InputStream result = null;

    	try {
    		FileInputStream fis = new FileInputStream(file);
    		ZipFile zipFile = new ZipFile(file);
    		ZipInputStream zis = new ZipInputStream(fis);
    		ZipEntry ze;
    		while((ze=zis.getNextEntry())!=null){
    			if (ze.getName().equals(targetName)){
    				result = zipFile.getInputStream(ze);   				
    				break;
    			}
    			zis.closeEntry();
    		}
    		zis.close();

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return result;
    }
    
    
    public static void main (String[] args){
    	File file = new File("C:\\GIT Repositories\\FirstRepo\\GestioneTorneo\\resources\\resources.zip");
    	try {
    		FileInputStream fis = new FileInputStream(file);
    		ZipFile zipFile = new ZipFile(file);
    		ZipInputStream zis = new ZipInputStream(fis);
    		ZipEntry ze;
    		while((ze=zis.getNextEntry())!=null){
    			System.out.println(ze.getName());
    			zis.closeEntry();
    		}
    		zis.close();

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
	
}
