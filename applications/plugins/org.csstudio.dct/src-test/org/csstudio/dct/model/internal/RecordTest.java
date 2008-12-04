/**
 * 
 */
package org.csstudio.dct.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.csstudio.dct.model.IPrototype;
import org.csstudio.dct.model.IRecord;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link Record}.
 * 
 * @author Sven Wende
 * 
 */
public class RecordTest {
	private Record parentRecord;
	private Record record;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parentRecord = new Record("record_$a$", "ai");
		parentRecord.addField("field1", "value1");
		parentRecord.addField("field2", "value2");
		parentRecord.addField("field3", "value3");
		parentRecord.addProperty("property1", "propertyvalue1");
		parentRecord.addProperty("property2", "propertyvalue2");
		parentRecord.addProperty("property3", "propertyvalue3");

		record = new Record(parentRecord);
		record.addField("field4", "value4");
		record.addProperty("property4", "propertyvalue4");
	}

	/**
	 * Test method for {@link org.csstudio.dct.model.internal.Record#equals()}.
	 */
	@Test
	public final void testEquals() {
		Record r1 = new Record(record);
		Record r2 = new Record(record);

		// .. parent
		assertEquals(r1, r2);

		// .. name
		r1.setName("xx");
		assertNotSame(r1, r2);
		r2.setName("xx");
		assertEquals(r1, r2);

		// .. fields
		r1.addField("a", "a");
		assertNotSame(r1, r2);
		r2.addField("a", "a");
		assertEquals(r1, r2);

		// .. properties
		r1.addProperty("a", "a");
		assertNotSame(r1, r2);
		r2.addProperty("a", "a");
		assertEquals(r1, r2);

		// .. container
		Prototype prototype = new Prototype("a");
		r1.setContainer(prototype);
		assertNotSame(r1, r2);
		r2.setContainer(prototype);
		assertEquals(r1, r2);
	}

	/**
	 * Test method for {@link org.csstudio.dct.model.internal.Record#getType()}.
	 */
	@Test
	public final void testGetType() {
		assertEquals("ai", parentRecord.getType());
		assertEquals("ai", record.getType());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getFinalProperties()}.
	 */
	@Test
	public final void testGetFinalProperties() {
		Map<String, Object> parentProperties = parentRecord.getProperties();
		Map<String, Object> properties = record.getProperties();
		Map<String, Object> finalProperties = record.getFinalProperties();
		assertNotNull(parentProperties);
		assertNotNull(properties);
		assertNotNull(finalProperties);
		assertEquals(3, parentProperties.size());
		assertEquals(1, properties.size());
		assertEquals(4, finalProperties.size());

		assertEquals(finalProperties.get("property1"), "propertyvalue1");
		assertEquals(finalProperties.get("property2"), "propertyvalue2");
		assertEquals(finalProperties.get("property3"), "propertyvalue3");
		assertEquals(finalProperties.get("property4"), "propertyvalue4");

		// test override
		parentRecord.addProperty("property2", "newpropertyvalue2");
		record.addProperty("property1", "newpropertyvalue1");
		finalProperties = record.getFinalProperties();
		assertEquals(finalProperties.get("property1"), "newpropertyvalue1");
		assertEquals(finalProperties.get("property2"), "newpropertyvalue2");

	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#addField(java.lang.String, java.lang.Object)}
	 * .
	 */
	@Test
	public final void testAddField() {
		assertNull(record.getField("field5"));
		record.addField("field5", "value5");
		assertEquals("value5", record.getField("field5"));
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getField(java.lang.String)}
	 * .
	 */
	@Test
	public final void testGetField() {
		assertEquals("value1", parentRecord.getField("field1"));
		assertEquals("value2", parentRecord.getField("field2"));
		assertEquals("value3", parentRecord.getField("field3"));
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#removeField(java.lang.String)}
	 * .
	 */
	@Test
	public final void testRemoveField() {
		assertEquals("value1", parentRecord.getField("field1"));
		parentRecord.removeField("field1");
		assertNull(parentRecord.getField("field1"));
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getFields()}.
	 */
	@Test
	public final void testGetFields() {
		Map<String, Object> fields = record.getFields();
		assertNotNull(fields);
		assertFalse(fields.isEmpty());
		assertTrue(fields.containsKey("field4"));
		assertEquals(1, fields.size());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getFinalFields()}.
	 */
	@Test
	public final void testGetFinalFields() {
		Map<String, Object> parentFields = parentRecord.getFields();
		Map<String, Object> fields = record.getFields();
		Map<String, Object> finalFields = record.getFinalFields();
		assertNotNull(parentFields);
		assertNotNull(fields);
		assertNotNull(finalFields);
		assertEquals(3, parentFields.size());
		assertEquals(1, fields.size());
		assertEquals(4, finalFields.size());

		assertEquals(finalFields.get("field1"), "value1");
		assertEquals(finalFields.get("field2"), "value2");
		assertEquals(finalFields.get("field3"), "value3");
		assertEquals(finalFields.get("field4"), "value4");

		// test override
		parentRecord.addField("field2", "newvalue2");
		record.addField("field1", "newvalue1");
		finalFields = record.getFinalFields();
		assertEquals(finalFields.get("field1"), "newvalue1");
		assertEquals(finalFields.get("field2"), "newvalue2");

	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getNameFromHierarchy(boolean)}
	 * .
	 */
	@Test
	public final void testGetNameFromHierarchy() {
		assertEquals("record_$a$", record.getNameFromHierarchy());
		record.setName("myname");
		assertEquals("myname", record.getNameFromHierarchy());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getParentRecord()}.
	 */
	@Test
	public final void testGetParentRecord() {
		assertEquals(parentRecord, record.getParentRecord());
		assertNull(parentRecord.getParentRecord());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#getContainer()}.
	 */
	@Test
	public final void testGetContainer() {
		assertNull(parentRecord.getContainer());
		assertNull(record.getContainer());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#setContainer(org.csstudio.dct.model.IRecordContainer)}
	 * .
	 */
	@Test
	public final void testSetContainer() {
		Prototype prototype = new Prototype("p");
		record.setContainer(prototype);
		assertEquals(prototype, record.getContainer());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#isInheritedFromPrototype()}
	 * .
	 */
	@Test
	public final void testIsInheritedFromPrototype() {
		Prototype prototype = new Prototype("p");
		parentRecord.setContainer(prototype);
		assertEquals(prototype, parentRecord.getContainer());

		assertFalse(parentRecord.isInheritedFromPrototype());
		assertTrue(record.isInheritedFromPrototype());

	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#addDependentRecord(org.csstudio.dct.model.IRecord)}
	 * .
	 */
	@Test
	public final void testGetAddRemoveDependentRecord() {
		assertTrue(parentRecord.getDependentRecords().isEmpty());
		// add
		parentRecord.addDependentRecord(record);
		assertEquals(1, parentRecord.getDependentRecords().size());
		assertEquals(record, parentRecord.getDependentRecords().get(0));
		// remove
		parentRecord.removeDependentRecord(record);
		assertTrue(parentRecord.getDependentRecords().isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.internal.Record#equals(Object)} .
	 */
	@Test
	public final void testEqualsHashCode() {
		UUID id = UUID.randomUUID();

		// .. ids must equal
		IRecord r1 = new Record("name", "ai", id);
		IRecord r2 = new Record("name", "ai", id);
		IRecord r3 = new Record("name", "ai", UUID.randomUUID());
		
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());
		assertNotSame(r1, r3);
		assertNotSame(r1.hashCode(), r3.hashCode());
		assertNotSame(r2, r3);
		assertNotSame(r2.hashCode(), r3.hashCode());

		// .. type must equal
		IRecord r4 = new Record("name", "ao", id);
		assertNotSame(r1, r4);
		assertNotSame(r1.hashCode(), r4.hashCode());
		
		// .. container must equals
		IPrototype prototype = new Prototype("test");
		r1.setContainer(prototype);
		assertNotSame(r1, r2);
		assertNotSame(r1.hashCode(), r2.hashCode());
		r2.setContainer(prototype);
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());
		
		// .. parent must equal
		IRecord r5 = new Record(record, id);
		assertNotSame(r1, r5);
		assertNotSame(r1.hashCode(), r5.hashCode());
		
		// .. properties must equal
		r1.addProperty("a", "a");
		assertNotSame(r1, r2);
		assertNotSame(r1.hashCode(), r2.hashCode());
		r2.addProperty("a", "a");
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

		// .. fields must equal
		r1.addField("f1", "v1");
		assertNotSame(r1, r2);
		assertNotSame(r1.hashCode(), r2.hashCode());
		r2.addField("f1", "v1");
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

		// .. names must equal
		r1.setName("x");
		assertNotSame(r1, r2);
		assertNotSame(r1.hashCode(), r2.hashCode());
		r2.setName("x");
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());
		

		
		
}

}
