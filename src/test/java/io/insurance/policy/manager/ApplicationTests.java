package io.insurance.policy.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = { "topico-pagamentos", "topico-subscricao", "topico-eventos" }, brokerProperties = {
		"listeners=PLAINTEXT://localhost:9092", "port=9092"
})
class ApplicationTests {

	@Autowired
	private MockMvc restClient;


	@Test
	void contextLoads() throws Exception {
		this.restClient.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("{\"status\":\"UP\"}"));
	}

}
