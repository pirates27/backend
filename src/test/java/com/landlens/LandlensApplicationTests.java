package com.landlens;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import com.landlens.property.service.PropertyService;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class LandlensApplicationTests {

	@Autowired
	private PropertyService propertyService;

	@Test
	void contextLoads() {
		assertNotNull(propertyService);
	}

}
