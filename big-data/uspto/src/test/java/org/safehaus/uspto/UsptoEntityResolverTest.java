package org.safehaus.uspto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UsptoEntityResolverTest {

	
	private UsptoEntityResolver usptoEntityResolver;

	@Before
    public void setUp() {
		usptoEntityResolver = new UsptoEntityResolver();
    }
	
	@After
    public void tearDown() {
		usptoEntityResolver = null;
    }

	@Test
	public void testResetCurrentVersion() {
		testResolveUsptoV44();
		usptoEntityResolver.resetCurrentVersion();
		assertEquals("Reset Current Version check", null, usptoEntityResolver.getCurrentVersion());
	}

	
	@Test
	public void testResolveUsptoV24() {
		String entityName = "ST32-US-Grant-024.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V24, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV25() {
		String entityName = "ST32-US-Grant-025xml.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V25, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV30() {
		String entityName = "us-patent-grant-v30-2004-03-04.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V30, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV40_1() {
		String entityName = "us-patent-grant-v40-2004-04-15.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V40, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV40_2() {
		String entityName = "us-patent-grant-v40-2004-09-08.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V40, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV40_3() {
		String entityName = "us-patent-grant-v40-2004-09-27.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V40, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV40_4() {
		String entityName = "us-patent-grant-v40-2004-10-28.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V40, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testResolveUsptoV40_5() {
		String entityName = "us-patent-grant-v40-2004-12-02.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V40, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

	
	@Test
	public void testResolveUsptoV41() {
		String entityName = "us-patent-grant-v41-2005-08-25.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V41, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResolveUsptoV42() {
		String entityName = "us-patent-grant-v42-2006-08-23.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V42, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResolveUsptoV43() {
		String entityName = "us-patent-grant-v43-2012-12-04.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V43, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResolveUsptoV44() {
		String entityName = "us-patent-grant-v44-2013-05-16.dtd";
		try {
			InputSource inputSource = usptoEntityResolver.resolveEntity("name", "publicId", "baseURI", entityName);
			if (inputSource == null)
			{
				fail("Can't resolve ".concat(entityName));
			}
			assertEquals("Version check", UsptoEntityResolver.Version.US_PATENT_GRANT_V44, usptoEntityResolver.getCurrentVersion());
		} catch (SAXException | IOException e) {
			fail(e.getMessage());
		}
	}

}
