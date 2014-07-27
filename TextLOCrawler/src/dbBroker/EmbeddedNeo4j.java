package dbBroker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.log4j.Level;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.kernel.impl.util.FileUtils;

import textLOCrawler.CrawlUtilities;


@SuppressWarnings("deprecation")
public class EmbeddedNeo4j {
	
	private static volatile EmbeddedNeo4j _instance = null;
	
	private static String DB_PATH = null;
	public static UniqueFactory<Node> factory; 
	    
	public static GraphDatabaseService graphDb;
	
	public static org.apache.log4j.Logger logger;
	
	public static Index<Node> allDbPagesIndex;//index for speed the search and traverse over all nodes created 
    private static final String NAME_PAGES_INDEX = "pages";
    
    public static Index<Node> pdfIndex;//index for speed the search and traverse over all pdf nodes created 
    private static final String NAME_PDF_INDEX = "pdf";
    
    public static final String FIELDNAME_KEY = "Key";
    
	public static final String FILIATION_NAME = "Filiation";
	public static final String AUTHOR_NAME = "Author";

	public static EmbeddedNeo4j getInstance() {
		  if(_instance == null)
			  return getInstance(System.getProperty("user.dir"));
		  else 
			  return _instance;
	}
	
    public static EmbeddedNeo4j getInstance(String rootDBPath) { 
          if(_instance == null){
                  _instance = new EmbeddedNeo4j();
                  DB_PATH = rootDBPath + "/Neo4J";
                  File file = new File(DB_PATH);
                  file.mkdirs();
                  clearDb();
                  createDb();
                  registerShutdownHook( graphDb );
           } 
           return _instance;
    }
	
    public static enum RelTypes implements RelationshipType {Links,HasFiliation,HasAuthor,HasConnection}    //,Linked} deprecated

    @Deprecated
    public static void Save(DbPage nodeToSave){//deprecated
    	
    	Transaction tx = graphDb.beginTx();
        try
        {
        	
        	Node nodo = factory.getOrCreate( FIELDNAME_KEY, nodeToSave.getKey() );
	    	nodo.setProperty("Url", nodeToSave.getUrl());
	    	nodo.setProperty("Html", nodeToSave.getHtml());
	    	nodo.setProperty("Domain", nodeToSave.getDomain());
	    	nodo.setProperty("SubDomain", nodeToSave.getSubDomain());
	    	nodo.setProperty("ParentUrl", nodeToSave.getParentUrl());
	    	nodo.setProperty("Title", nodeToSave.getTitle());
	    	
	    	for ( DbPage innerPage : nodeToSave.getLinkedPages()){
	    		
	    		//no linkear a la misma pagina
	    		if (innerPage.getKey()== nodeToSave.getKey()) continue;
	    		
	    		Node nodoLinked = factory.getOrCreate( FIELDNAME_KEY, innerPage.getKey() );
	    		nodoLinked.setProperty("Url", innerPage.getUrl());
	    		nodoLinked.setProperty("Domain", innerPage.getDomain());
	    		nodoLinked.setProperty("SubDomain", innerPage.getSubDomain());
	    		nodoLinked.setProperty("ParentUrl", innerPage.getParentUrl());
	    		
	        	nodo.createRelationshipTo(nodoLinked, RelTypes.Links);
	    	}

	    	tx.success();
        }
        catch(Exception ex){
        	throw ex;
        }
        finally
        {
            tx.finish();
        }
    }
 
    public void saveObject(IDbNeo4jObject nodeToSave) throws Exception{
    	  	
    	
    	Transaction tx = graphDb.beginTx();
        try
        {
        	//get or create the unique node for the key (implemented by the interface)
        	Node nodo = factory.getOrCreate( FIELDNAME_KEY, nodeToSave.getKey() );
        	
        	logger.log(Level.INFO, "Saving nodo, Key: " + nodeToSave.getKey() + " nodo ID: " + nodo.getId());
        	
        	//recover all the public fields from the pojo 
    	    Field[] fields = nodeToSave.getClass().getDeclaredFields();
	        for(Field f : fields){
	        	if (f.getName().equalsIgnoreCase(FIELDNAME_KEY)) continue; //skip implementation
	        	
        		//Current Property Value
        		Object propertyValue = f.get(nodeToSave);
        		if (propertyValue == null) continue;
        		
        		//if the field is not a collection set it in the node
        		if (!java.lang.Iterable.class.isAssignableFrom(propertyValue.getClass())) {
        			
        			//if it is a dbNeoObject, save it (like a page or file)
        			if (IDbNeo4jObject.class.isAssignableFrom(propertyValue.getClass())) {
        				Node propertyNode = factory.getOrCreate( FIELDNAME_KEY, ((IDbNeo4jObject)propertyValue).getKey() );
        				logger.log(Level.INFO, "Saving Node Key: " + ((IDbNeo4jObject)propertyValue).getKey() + " nodo ID: " + propertyNode.getId());
        				for(Field innerPropF : propertyValue.getClass().getDeclaredFields()){
        					if (innerPropF.getName().equalsIgnoreCase(FIELDNAME_KEY)) continue;
        		        	Object innerPropertyValue = innerPropF.get(propertyValue);
         		        	//Skip if the inner object implements iterators
         		        	if (innerPropertyValue == null) continue;
         		        	if (java.lang.Iterable.class.isAssignableFrom(innerPropertyValue.getClass())) continue;
         		        	logger.log(Level.INFO, "Traverse intern property of node " + nodo.getId() + " => Field: " + innerPropF.getName() + " Value: " + innerPropertyValue);
         		        	propertyNode.setProperty(innerPropF.getName(), innerPropertyValue);
         		        }
        				/* In the model, only the DbPage has a ParentDbPage:IDbNeo4jObject property, so setting this relationship is redundant cause the RelType.Links
        				 * */
        				//nodo.createRelationshipTo(propertyNode, RelTypes.Linked);
        			}
        			else
        			{
        				//just a property
        				nodo.setProperty(f.getName(), propertyValue);
        			}
        			continue; //move next for the next field
        		}
        	
				//if the object implements an iterator loop it (i.e. outgoin links)
				@SuppressWarnings("rawtypes")
				java.lang.Iterable coleccion =  (Iterable) f.get(nodeToSave);
    			
    			//Loop the colection
    			@SuppressWarnings("rawtypes")
				java.util.Iterator iter = coleccion.iterator();
    			while (iter.hasNext()) {
    				Object innerObject = iter.next();

    				//see if implements the interfaz for save in the DB
    				if (!IDbNeo4jObject.class.isAssignableFrom(innerObject.getClass())) continue;
    				
    				Node innerNode = factory.getOrCreate( FIELDNAME_KEY, ((IDbNeo4jObject)innerObject).getKey() );
    				
    				logger.log(Level.INFO, "Saving nodo, Key: " + ((IDbNeo4jObject)innerObject).getKey() + " nodo ID: " + innerNode.getId());
    				
    		        for(Field innerF : innerObject.getClass().getDeclaredFields()){

    		        	if (innerF.getName().equalsIgnoreCase(FIELDNAME_KEY)) continue;
    		        	
    		        	Object innerPropertyValue = innerF.get(innerObject);
    		        	
    		        	//Skip if the inner object implements iterators
    		        	if (innerPropertyValue == null) continue;
    		        	if (java.lang.Iterable.class.isAssignableFrom(innerPropertyValue.getClass())) continue;

    		        	logger.log(Level.INFO, "Traver intern properties intern node: " + nodo.getId() + " => Field: " + innerF.getName() + " Value: " + innerPropertyValue);
    		        	
    					innerNode.setProperty(innerF.getName(), innerPropertyValue);
    		        }
    		        
    		        //finaly, create the relationship
    		        
    		        logger.log(Level.INFO, "Node "  + nodo.getId() + " links " + innerNode.getId() );
    		        nodo.createRelationshipTo(innerNode, RelTypes.Links);
    			}
	        }
	        
	        //If it is a pdf file, we add it to the pdfIndex 
	        if (nodeToSave.getKey().endsWith(".pdf")) //TODO: improve and abstract this logic 
	        	if ( pdfIndex.get(FIELDNAME_KEY, nodeToSave.getKey()) != null )
	        		pdfIndex.add( nodo, FIELDNAME_KEY, nodeToSave.getKey() );
	            
	        tx.success();
        
        } catch (Exception e) {
	        logger.log(Level.INFO, "Neo4J unsuccessful transaction.\nError: " );
	        logger.log(Level.INFO, e);
			e.printStackTrace();
			throw e;
		}
        finally
        {
            tx.finish();
        }
        
    }    
    

    private static void createDb()
    {
    	
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        registerShutdownHook( graphDb );
        
        try ( Transaction tx = graphDb.beginTx() )
        {
        	 pdfIndex=graphDb.index().forNodes(NAME_PDF_INDEX); 
             allDbPagesIndex=graphDb.index().forNodes(NAME_PAGES_INDEX);
             //http://docs.neo4j.org/chunked/stable/tutorials-java-embedded-unique-nodes.html#tutorials-java-embedded-unique-get-or-create
             //https://github.com/neo4j/neo4j/issues/73
             factory = new UniqueFactory.UniqueNodeFactory( allDbPagesIndex )
     	    {
         		 	@Override
         	        protected void initialize( Node created, Map<String, Object> properties )
         	        {
         		 		/*
         		 		 * Implement this method to initialize the Node or Relationship created for being stored in the index.
         		 		 * This method will be invoked exactly once per created unique entity.
         		 		 * The created entity might be discarded if another thread creates an entity concurrently.
         		 		 * This method will however only be invoked in the transaction that succeeds in creating the node.
         		 		 * */
         		 		String key = (String) properties.get( FIELDNAME_KEY);
         		 		if (key!=null) {
         		 			created.setProperty( FIELDNAME_KEY, properties.get( FIELDNAME_KEY ) );
             	            String extension = CrawlUtilities.getExtensionFromPathName((String)properties.get( FIELDNAME_KEY ));
             	            if (extension.endsWith(".pdf")) //TODO: improve and abstract this logic 
             	            	created.addLabel( DynamicLabel.label( "Pdf" ) );
             	            else {
             	            	created.addLabel( DynamicLabel.label( "Html" ) );
         		 			}
         		 		}
         	            
         		 		String author  = (String) properties.get( AUTHOR_NAME);
         		 		if (author != null) {
         		 			created.setProperty( AUTHOR_NAME, properties.get( AUTHOR_NAME ) );         		 			
         	            	created.addLabel( DynamicLabel.label( "Author" ) );
         		 		}
         		 		
         		 		String filiation  = (String) properties.get( FILIATION_NAME);
         		 		if (filiation != null) {
         		 			created.setProperty( FILIATION_NAME, properties.get( FILIATION_NAME ) );         		 			
         	            	created.addLabel( DynamicLabel.label( "Filiation" ) );
         		 		}
         	        }
     	    };
     	    
            tx.success();
        }
        
       
	    
        
    }

    private static void clearDb()
    {
        try
        {
            FileUtils.deleteRecursively( new File( DB_PATH ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

	void removeData()
    {
        Transaction tx = graphDb.beginTx();
        try
        {
            // START SNIPPET: removingData
            // let's remove the data
            //firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING ).delete();
            //firstNode.delete();
            //secondNode.delete();
            // END SNIPPET: removingData

            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    public void shutDown()
    {
        System.out.println();
        System.out.println( "Shutting down database ..." );
        graphDb.shutdown();
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
	    {
	        // Registers a shutdown hook for the Neo4j instance so that it
	        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	        // running application).
	        Runtime.getRuntime().addShutdownHook( new Thread()
	        {
	            @Override
	            public void run()
	            {
	                graphDb.shutdown();
	            }
	        } );
	    }

	public void setLogger(org.apache.log4j.Logger oLogger) {
		logger = oLogger;
	}

	}