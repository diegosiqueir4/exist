package org.exist.xquery.test;

import junit.framework.TestCase;

import org.exist.storage.DBBroker;
import org.exist.xmldb.DatabaseInstanceManager;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XQueryService;

public class DocumentUpdateTest extends TestCase {

	private static final String TEST_COLLECTION_NAME = "testup";
    
    private Database database;
    private Collection testCollection;
    
    /**
     * Test if the doc, collection and document functions are correctly
     * notified upon document updates. Call a function once on the empty collection, 
     * then call it again after a document was added, and compare the results.
     */
    public void testUpdate() {
    	String imports = 
    		"import module namespace xdb='http://exist-db.org/xquery/xmldb';\n" + 
    		"import module namespace util='http://exist-db.org/xquery/util';\n";
    	
		try {
			System.out.println("-- TEST 1: doc() function --");
			String query = imports +
	    		"declare function local:get-doc($path as xs:string) {\n" + 
	    		"    doc($path)\n" + 
	    		"};\n" +
	    		"let $col := xdb:create-collection('/db', 'testup')\n" + 
	    		"let $path := '/db/testup/test1.xml'\n" +
	    		"let $d1 := local:get-doc($path)\n" +
	    		"let $doc := xdb:store($col, 'test1.xml', <test><n>1</n></test>)\n" + 
	    		"return string-join((count(local:get-doc($path)), doc-available($path)), ' ')";
			String result = execQuery(query);
			assertEquals(result, "1 true");
			
			System.out.println("-- TEST 2: document() function --");
			query = imports +
	    		"declare function local:get-doc($path as xs:string) {\n" + 
	    		"    document($path)\n" + 
	    		"};\n" +
	    		"let $col := xdb:create-collection('/db', 'testup')\n" + 
	    		"let $path := '/db/testup/test1.xml'\n" +
	    		"let $d1 := local:get-doc($path)\n" +
	    		"let $doc := xdb:store($col, 'test1.xml', <test><n>1</n></test>)\n" + 
	    		"return string-join((count(local:get-doc($path)), doc-available($path)), ' ')";
			result = execQuery(query);
			assertEquals(result, "1 true");
			
			System.out.println("-- TEST 3: collection() function --");
			query = imports +
				"declare function local:xpath($collection as xs:string) {\n" + 
	    		"    for $c in collection($collection) return $c//n\n" + 
	    		"};\n" +
	    		"let $col := xdb:create-collection('/db', 'testup')\n" + 
	    		"let $path := '/db/testup'\n" +
	    		"let $d1 := local:xpath($path)//n/text()\n" +
	    		"let $doc := xdb:store($col, 'test1.xml', <test><n>1</n></test>)\n" + 
	    		"return local:xpath($path)//n/text()";
			result = execQuery(query);
			assertEquals(result, "1");
		
			System.out.println("-- TEST 4: 'update insert' statement --");
			query = imports +
				"declare function local:xpath($collection as xs:string) {\n" + 
	    		"    collection($collection)\n" + 
	    		"};\n" +
	    		"let $col := xdb:create-collection('/db', 'testup')\n" + 
	    		"let $path := '/db/testup'\n" +
	    		"let $d1 := local:xpath($path)//n\n" +
	    		"let $doc := xdb:store($col, 'test1.xml', <test><n>1</n></test>)\n" +
	    		"return (\n" +
	    		"	update insert <n>2</n> into collection($path)/test,\n" +
	    		"	count(local:xpath($path)//n)\n" +
	    		")";
			result = execQuery(query);
			assertEquals(result, "2");
		} catch (XMLDBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    private String execQuery(String query) throws XMLDBException {
    	XQueryService service = (XQueryService) testCollection.getService("XQueryService", "1.0");
    	ResourceSet result = service.query(query);
    	assertEquals(result.getSize(), 1);
    	return result.getResource(0).getContent().toString();
    }
    
    protected void setUp() {
        try {
            // initialize driver
            Class cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);
            
            Collection root =
                DatabaseManager.getCollection("xmldb:exist://" + DBBroker.ROOT_COLLECTION, "admin", null);
            CollectionManagementService service =
                (CollectionManagementService) root.getService(
                    "CollectionManagementService",
                    "1.0");
            testCollection = service.createCollection(TEST_COLLECTION_NAME);
            assertNotNull(testCollection);
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() {
    	try {
	        Collection root =
	            DatabaseManager.getCollection("xmldb:exist://" + DBBroker.ROOT_COLLECTION, "admin", null);
	        CollectionManagementService service =
	            (CollectionManagementService) root.getService(
	                "CollectionManagementService",
	                "1.0");
	        service.removeCollection(TEST_COLLECTION_NAME);
	        
	        DatabaseManager.deregisterDatabase(database);
	        DatabaseInstanceManager dim =
	            (DatabaseInstanceManager) testCollection.getService(
	                "DatabaseInstanceManager", "1.0");
	        dim.shutdown();
            database = null;
            testCollection = null;
	        System.out.println("tearDown PASSED");
		} catch (XMLDBException e) {
			fail(e.getMessage());
		}
    }
}
