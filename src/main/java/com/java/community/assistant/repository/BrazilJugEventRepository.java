package com.java.community.assistant.repository;

import org.hibernate.dialect.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BrazilJugEventRepository {

	private final JdbcTemplate jdbcTemplate;

	public BrazilJugEventRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void saveEvent(String name, String description, String date, String local, String link, float[] vector) {
		String sql = """
			INSERT INTO events_brazil_jug (
				event_name, full_description, event_date, event_location, registration_link, vetor_embedding
			)
			VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)
			""";

		jdbcTemplate.update(connection -> {
			var ps = connection.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, description);
			ps.setString(3, date);
			ps.setString(4, local);
			ps.setString(5, link);
			ps.setObject(6, vector, OracleTypes.VECTOR_FLOAT32);
			return ps;
		});
	}

	public List<String> findRelevantContext(float[] queryVector) {
		String sql = """
			SELECT 'Evento: ' || event_name ||
				' | Data: ' || TO_CHAR(event_date, 'DD/MM/YYYY') ||
				' | Local: ' || event_location ||
				' | Descrição: ' || full_description ||
				' | Link: ' || registration_link AS full_context
			FROM events_brazil_jug
			ORDER BY VECTOR_DISTANCE(vetor_embedding, ?, COSINE)
			FETCH FIRST 3 ROWS ONLY
		""";

		return jdbcTemplate.query(connection -> {
			var ps = connection.prepareStatement(sql);
			ps.setObject(1, queryVector, OracleTypes.VECTOR_FLOAT32);
			return ps;
		}, (rs, rowNum) -> rs.getString("full_context"));
	}

}
