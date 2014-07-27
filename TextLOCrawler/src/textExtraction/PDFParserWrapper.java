package textExtraction;
//package textExtraction;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collections;
//import java.util.Set;
//
//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.mime.MediaType;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.sax.XHTMLContentHandler;
//import org.xml.sax.ContentHandler;
//import org.apache.tika.sax.BodyContentHandler;
//import org.xml.sax.SAXException;
//
//import java.io.*;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
// 
//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.pdf.*;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.sax.BodyContentHandler;
//import org.xml.sax.ContentHandler;
//import org.xml.sax.SAXException;
//
//public class PDFParserWrapper {
//        
//        public String parsePdfFile (File file) throws FileNotFoundException {
//        	PDFParser parser = new PDFParser();
//            Metadata metadata = new Metadata();
//            ParseContext parseContext = new ParseContext();
//
//            BodyContentHandler handler =  new BodyContentHandler(-1);
//            FileInputStream is = new FileInputStream(file);
//            try {
//				parser.parse(is, handler, metadata);
//				
//			      
//                for (String name : metadata.names()) {
//                    String value = metadata.get(name);
//     
//                    if (value != null) {
//                        System.out.println("Metadata Name:  " + name);
//                        System.out.println("Metadata Value: " + value);
//                    }
//                }
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (TikaException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            return handler.toString();
//
//        }
//}