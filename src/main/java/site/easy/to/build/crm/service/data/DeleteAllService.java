package site.easy.to.build.crm.service.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAllService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void deleteAllTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.execute("TRUNCATE TABLE google_drive_file");
        jdbcTemplate.execute("TRUNCATE TABLE file");
        jdbcTemplate.execute("TRUNCATE TABLE ticket_settings");
        jdbcTemplate.execute("TRUNCATE TABLE lead_settings");
        jdbcTemplate.execute("TRUNCATE TABLE lead_action");
        jdbcTemplate.execute("TRUNCATE TABLE contract_settings");
        jdbcTemplate.execute("TRUNCATE TABLE trigger_contract");
        jdbcTemplate.execute("TRUNCATE TABLE trigger_ticket");
        jdbcTemplate.execute("TRUNCATE TABLE trigger_lead");
        jdbcTemplate.execute("TRUNCATE TABLE customer");
        jdbcTemplate.execute("TRUNCATE TABLE customer_login_info");
        jdbcTemplate.execute("TRUNCATE TABLE email_template");
        jdbcTemplate.execute("TRUNCATE TABLE employee");
        // jdbcTemplate.execute("TRUNCATE TABLE user_roles");
        // jdbcTemplate.execute("TRUNCATE TABLE roles");
        // jdbcTemplate.execute("TRUNCATE TABLE user_profile");
        // jdbcTemplate.execute("TRUNCATE TABLE oauth_users");
        // jdbcTemplate.execute("TRUNCATE TABLE users");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
