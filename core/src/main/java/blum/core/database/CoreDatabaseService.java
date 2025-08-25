package blum.core.database;

import blum.api.database.DatabaseService;
import blum.api.exception.ServiceStartException;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;

@ServiceDescriptor(
        name = "database",
        displayName = "Blum Database Service",
        description = "Global database access for Blum Core, Plugins and others apps",
        version = "1.0.0"
)
public class CoreDatabaseService implements Service, DatabaseService {

    @Override
    public void start() throws ServiceStartException {

    }

    @Override
    public void stop() throws ServiceStartException {

    }
}
