package com.assignment.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BackendApplicationUnitTest {

	@Test
	void mainMethod_shouldRunWithoutExceptions() {
		assertDoesNotThrow(() -> BackendApplication.main(new String[]{}));
	}
}