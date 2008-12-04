/**
 * 
 */
package org.csstudio.dct.model.commands;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.csstudio.dct.model.IInstance;
import org.csstudio.dct.model.IPrototype;
import org.csstudio.dct.model.IRecord;
import org.csstudio.dct.model.commands.AddInstanceCommand;
import org.csstudio.dct.model.internal.Instance;
import org.csstudio.dct.model.internal.Prototype;
import org.csstudio.dct.model.internal.RecordFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link AddInstanceCommand}.
 * 
 * @author Sven Wende
 * 
 */
public class AddInstanceCommandTest {
	private IPrototype prototypeA;
	private IInstance instanceA;
	private IPrototype prototypeB;
	private IInstance instanceB;
	private AddInstanceCommand command;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		prototypeA = new Prototype("A");
		instanceA = new Instance(prototypeA);
		prototypeA.addDependentContainer(instanceA);

		prototypeB = new Prototype("B");
		instanceB = new Instance(prototypeB);

		command = new AddInstanceCommand(prototypeA, instanceB);
	}

	/**
	 * Test method for
	 * {@link org.csstudio.dct.model.commands.AddRecordCommand#execute()}
	 * .
	 */
	@Test
	public final void testExecute() {
		verifyAlways();
		verifyBeforeCommandExecution();
		command.execute();
		verifyAlways();
		verifyAfterCommandExecution();
		command.undo();
		verifyAlways();
		verifyBeforeCommandExecution();
	}

	private void verifyAfterCommandExecution() {
		assertEquals(prototypeA, instanceB.getContainer());
		assertEquals(1, instanceA.getInstances().size());
		assertTrue(prototypeB.getDependentContainers().contains(instanceB));
		IInstance pushedInstance = instanceA.getInstances().get(0);
		assertEquals(instanceB, pushedInstance.getParent());
		assertTrue(instanceB.getDependentContainers().contains(pushedInstance));
	}

	private void verifyBeforeCommandExecution() {
		assertNull(instanceB.getContainer());
		assertTrue(instanceA.getInstances().isEmpty());
		assertTrue(instanceB.getDependentContainers().isEmpty());
		assertTrue(prototypeB.getDependentContainers().isEmpty());
	}
	
	private void verifyAlways() {
		assertEquals(instanceA.getParent(), prototypeA);
		assertEquals(instanceB.getParent(), prototypeB);
		assertNotSame(prototypeA, prototypeB);
		assertNotSame(instanceA, instanceB);
		assertTrue(prototypeA.getDependentContainers().contains(instanceA));
		assertNull(instanceA.getContainer());
		
	}
	
}
