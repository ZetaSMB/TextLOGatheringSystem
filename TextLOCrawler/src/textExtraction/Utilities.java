package textExtraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Utilities {
	public static String removeExension(String filePathStr) {
		
		String result = filePathStr;
		int idx = filePathStr.lastIndexOf('.');
		if (idx > 0) {//remove extension
			result = filePathStr.substring(0, idx);
		}
		return result;
	}
	
	public static String getPathRemovingParentsFolder(File file) {
		
		String result = null;
		int idx = file.getParent().length();
		if (idx > 0) {//remove extension
			result = file.getPath().substring(++idx);
		}
		return result;
	}
	
	public static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
			}
	
	public static String readTextFromFile(File fileEntry, Charset encoding) 
			  throws IOException {
			byte[] encoded = Files.readAllBytes(fileEntry.toPath());
		  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
		
	}
	
	public static void saveStringToFile(String textToSave, String filePath) {
		
		Writer writer = null;

		try {
			
			File file  = new File(filePath);
			file.getParentFile().mkdirs();//create parents file if not exists
			final File txtFile = new File(file.getParentFile(), Utilities.getPathRemovingParentsFolder(file));
			file.createNewFile(); 
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(txtFile), "utf-8"));
		    writer.write(textToSave);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} 
		   catch (Exception ex) {}
		}
	}
	
	// utility function
	public static String getFileContents(String filename)
        throws IOException, FileNotFoundException
    {
        File file = new File(filename);
        StringBuilder contents = new StringBuilder();

        BufferedReader input = new BufferedReader(new FileReader(file));

        try {
            String line = null;

            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }

        return contents.toString();
    }

	public static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
	
	public static String defaultNeo4jConfig = 
		"org.neo4j.server.database.location=<DBPATHTOREPLACE>\n"
		+"org.neo4j.server.thirdparty_jaxrs_classes=org.neo4j.server.extension.test.delete=/db/data/cleandb\n"
        +"org.neo4j.server.thirdparty.delete.key=secret-key\n"
        +"org.neo4j.server.webserver.port=7474\n"
        +"org.neo4j.server.webserver.https.enabled=true\n"
        +"org.neo4j.server.webserver.https.port=7473\n"
        +"org.neo4j.server.webserver.https.cert.location=conf/ssl/snakeoil.cert\n"
        +"org.neo4j.server.webserver.https.key.location=conf/ssl/snakeoil.key\n"
        +"org.neo4j.server.webserver.https.keystore.location=data/keystore\n"
        +"org.neo4j.server.webadmin.rrdb.location=data/rrd\n"
        +"org.neo4j.server.webadmin.data.uri=/db/data/\n"
        +"org.neo4j.server.webadmin.management.uri=/db/manage/\n"
        +"org.neo4j.server.db.tuning.properties=conf/neo4j.properties\n"
        +"org.neo4j.server.manage.console_engines=shell\n"
        +"org.neo4j.server.http.log.enabled=false\n"
        +"org.neo4j.server.http.log.config=conf/neo4j-http-logging.xml\n";

}
