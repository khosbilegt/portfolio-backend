package mn.khosbilegt.config;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class Configurator {
    @Inject
    AgroalDataSource dataSource;

    @ApplicationScoped
    public DSLContext getContext() {
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }
}
