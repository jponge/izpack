package com.izforge.izpack.compiler.packager.impl;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.izforge.izpack.compiler.resource.ResourceFinder;

public class PackagerTest {

	@Test
	public void testWritePacks() throws IOException {
		final ResourceFinder resourceFinder = new ResourceFinder(null, null,
				null, null);
		final Packager packager = new Packager(null, null, null, null, null,
				null, null, null, null, null, null, resourceFinder);
		packager.writeManifest();
		fail("Not yet implemented");
	}

}
